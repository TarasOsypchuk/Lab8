#!/usr/bin/env python3
"""
Обчислення метрик CBO, LCOM4 та WMC за байткодом (target/classes).

Використання:
    mvn -q compile
    python tools/grasp_metrics.py target/classes > docs/metrics/metrics-generated.md

Потрібні утиліти JDK `jdeps` і `javap` у PATH (входять до будь-якого JDK 11+).

Методика (детальніше — docs/metrics/metrics-report.md):

CBO (Coupling Between Objects), варіант fan-out (Ce):
    кількість РІЗНИХ типів, від яких залежить клас (поля, параметри, типи повернення,
    локальні змінні, виклики, наслідування/реалізація), за даними `jdeps -verbose:class`.
    * CBO_all  — усі типи проєкту + JDK-типи вводу/виводу (java.io.*, java.util.Scanner,
                 java.nio.file.*), бо це інфраструктура, від якої клас стає залежним.
    * CBO_core — те саме, але БЕЗ незмінних value-типів проєкту (record, enum):
                 вони стабільні й не мають побічних ефектів, як String чи BigDecimal.
    Не рахуються: сам клас, java.lang.*, java.math.*, java.time.*, колекції та
    функціональні інтерфейси java.util.* (стабільні бібліотечні абстракції).

LCOM4 (Hitz & Montazeri):
    кількість зв'язних компонент графа, де вершини — методи екземпляра, а ребро
    з'єднує два методи, якщо вони звертаються до спільного поля або один викликає інший.
    Конструктори, static-методи та синтетичні методи не враховуються; тіла лямбд
    (lambda$m$N) зараховуються методу m.
    * LCOM4       — без тривіальних аксесорів (метод без параметрів, що лише повертає поле);
    * LCOM4_strict — з аксесорами (для прозорості).
    "—" означає, що в класі немає жодного методу для аналізу.

WMC (Weighted Methods per Class):
    сума цикломатичних складностей усіх методів (1 + кількість умовних переходів).
"""

import os
import re
import subprocess
import sys
from collections import defaultdict

PROJECT_PREFIXES = ("ua.chnu.kpz.", "variant3_fitnessmembership.")
IO_TYPES = re.compile(r"^(java\.io\.|java\.nio\.file\.|java\.util\.Scanner$)")
BRANCH_OPS = re.compile(r"^\s*\d+: (if\w*|if_\w+)\b")
SWITCH_CASE = re.compile(r"^\s+-?\d+: \d+$|^\s+default: \d+$")


def run(cmd):
    return subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", check=True).stdout


def class_files(root):
    for dirpath, _, files in os.walk(root):
        for f in files:
            if f.endswith(".class"):
                yield os.path.join(dirpath, f)


def fqcn(root, path):
    rel = os.path.relpath(path, root)[: -len(".class")]
    return rel.replace(os.sep, ".")


def is_project(name):
    return name.startswith(PROJECT_PREFIXES)


def outer(name):
    return name.split("$", 1)[0]


def kind_of(header):
    if " interface " in f" {header} ":
        return "interface"
    if "extends java.lang.Record" in header:
        return "record"
    if "extends java.lang.Enum" in header:
        return "enum"
    return "class"


def parse_dependencies(root):
    deps = defaultdict(set)
    out = run(["jdeps", "-verbose:class", "-filter:none", root])
    for line in out.splitlines():
        m = re.match(r"^\s+(\S+)\s+->\s+(\S+)", line)
        if not m:
            continue
        src, dst = outer(m.group(1)), outer(m.group(2))
        if src != dst:
            deps[src].add(dst)
    return deps


def parse_class(path, simple_name):
    """Повертає (kind, fields, methods) з javap -p -c."""
    text = run(["javap", "-p", "-c", path])
    lines = text.splitlines()
    header = next(l for l in lines if re.search(r"\b(class|interface|enum|record)\b", l) and l.rstrip().endswith("{"))
    kind = kind_of(header)
    fields = set()
    methods = {}
    current = None
    for line in lines:
        if line.strip() == "static {};":  # статичний ініціалізатор
            current = {"name": "<clinit>", "static": True, "params": "", "fields": set(), "calls": set(),
                       "instructions": 0, "branches": 0, "accessor_pattern": []}
            methods.setdefault("<clinit>", []).append(current)
            continue
        mh = re.match(r"^  (?!.*\bCode:)(.*?)([\w$<>]+)\((.*?)\)(?: throws [\w.,$ ]+)?;$", line)
        if mh:
            mods, name, params = mh.group(1), mh.group(2), mh.group(3)
            if name == simple_name:  # javap друкує конструктор як ім'я класу
                name = "<init>"
            static = " static " in f" {mods} "
            current = {"name": name, "static": static, "params": params.strip(),
                       "fields": set(), "calls": set(), "instructions": 0, "branches": 0,
                       "accessor_pattern": []}
            methods.setdefault(name, []).append(current)
            continue
        mf = re.match(r"^  (?:[\w.<>\[\], ?$]+ )+([\w$]+);$", line)
        if mf and "(" not in line:
            fields.add(mf.group(1))
            current = None
            continue
        if current is None:
            continue
        ins = re.match(r"^\s+\d+: (\w+)", line)
        if ins:
            current["instructions"] += 1
            current["accessor_pattern"].append(ins.group(1))
            if BRANCH_OPS.match(line):
                current["branches"] += 1
            fm = re.search(r"// Field ([\w$]+):", line)
            if fm and ins.group(1) in ("getfield", "putfield"):
                current["fields"].add(fm.group(1))
            cm = re.search(r"// (?:Method|InterfaceMethod) ([\w$<>]+):", line)
            if cm and ins.group(1) in ("invokevirtual", "invokespecial", "invokeinterface"):
                current["calls"].add(cm.group(1))
        elif SWITCH_CASE.match(line):
            current["branches"] += 1
    return kind, fields, methods


def is_accessor(m):
    p = m["accessor_pattern"]
    return (not m["static"] and m["params"] == "" and len(p) == 3
            and p[0] == "aload_0" and p[1] == "getfield" and p[2].endswith("return"))


def lcom4(methods, include_accessors):
    nodes = {}
    for name, overloads in methods.items():
        for m in overloads:
            if name in ("<init>", "<clinit>") or m["static"] and not name.startswith("lambda$"):
                continue
            owner = name
            lm = re.match(r"lambda\$(\w+)\$\d+", name)
            if lm:
                owner = lm.group(1)
                if owner not in methods:
                    continue
            if not include_accessors and is_accessor(m):
                continue
            node = nodes.setdefault(owner, {"fields": set(), "calls": set()})
            node["fields"] |= m["fields"]
            node["calls"] |= m["calls"]
    names = list(nodes)
    if not names:
        return None
    parent = {n: n for n in names}

    def find(x):
        while parent[x] != x:
            parent[x] = parent[parent[x]]
            x = parent[x]
        return x

    def union(a, b):
        parent[find(a)] = find(b)

    for i, a in enumerate(names):
        for b in names[i + 1:]:
            if nodes[a]["fields"] & nodes[b]["fields"] or b in nodes[a]["calls"] or a in nodes[b]["calls"]:
                union(a, b)
    return len({find(n) for n in names})


def main():
    root = sys.argv[1] if len(sys.argv) > 1 else "target/classes"
    deps = parse_dependencies(root)
    info = {}
    for path in class_files(root):
        name = fqcn(root, path)
        if "$" in name:
            continue
        info[name] = parse_class(path, name.rsplit(".", 1)[-1])

    value_types = {n for n, (k, _, _) in info.items() if k in ("record", "enum")}
    afferent = defaultdict(set)
    for src, targets in deps.items():
        for t in targets:
            if t in info:
                afferent[t].add(src)

    rows = []
    for name in sorted(info):
        kind, fields, methods = info[name]
        targets = deps.get(name, set())
        counted = sorted(t for t in targets if (is_project(t) and t in info) or IO_TYPES.match(t))
        core = [t for t in counted if t not in value_types]
        wmc = sum(1 + m["branches"] for ov in methods.values() for m in ov
                  if not m["name"].startswith("lambda$"))
        if kind in ("interface", "record", "enum"):
            l4 = l4s = None
        else:
            l4, l4s = lcom4(methods, False), lcom4(methods, True)
        rows.append((name, kind, len(counted), len(core), len(afferent[name]), l4, l4s, wmc, counted))

    def short(n):
        return n.rsplit(".", 1)[-1]

    def fmt(v):
        return "—" if v is None else str(v)

    print("| Клас | Вид | CBO_all | CBO_core | Ca | LCOM4 | LCOM4_strict | WMC | Залежності (CBO_all) |")
    print("|---|---|---:|---:|---:|---:|---:|---:|---|")
    for name, kind, cbo, core, ca, l4, l4s, wmc, counted in rows:
        pkg = name.rsplit(".", 1)[0].replace("ua.chnu.kpz.fitness", "fitness")
        dep_list = ", ".join(short(t) + "°" if t in value_types
                             else (t if not is_project(t) else short(t)) for t in counted)
        print(f"| `{pkg}.{short(name)}` | {kind} | {cbo} | {core} | {ca} | {fmt(l4)} | {fmt(l4s)} | {wmc} | {dep_list} |")
    print()
    print("° — незмінний value-тип (record/enum), не входить у CBO_core.")


if __name__ == "__main__":
    main()
