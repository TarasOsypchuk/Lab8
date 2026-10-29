# КПЗ. Лабораторна робота №8 — GRASP

**Варіант 3: «Мережа фітнес-клубів та продаж клубних карт»** · Java 17+ · Maven · JUnit 5

Рефакторинг legacy-коду з анемічною моделлю і «Bloated Controller» у Rich Domain Model з тонким консольним UI. Застосовано всі 9 патернів GRASP, кожне рішення задокументовано в ADR/QOC, а покращення доведено метриками CBO і LCOM4.

## Швидкий старт

```bash
mvn test                       # усі тести, зокрема порівняння з оригінальним legacy-кодом
mvn -q compile exec:java       # запуск консольного меню
```

Без Maven-плагіна exec: `java -cp target/classes ua.chnu.kpz.fitness.FitnessClubApp`.

> **Windows / Git Bash:** щоб кирилиця відображалася коректно, перед запуском виконайте `chcp.com 65001`. Якщо не допоможе — запускайте з `-Dstdout.encoding=UTF-8`.

Метрики (потрібні `jdeps` і `javap` з JDK та Python 3):

```bash
mvn -q compile
python tools/grasp_metrics.py target/classes > docs/metrics/metrics-generated.md
```

## Відповідність завданням

| Завдання | Що зроблено | Де |
|---|---|---|
| 1. Аудит + ADR/QOC | 5 ADR з таблицями QOC: аудит, Expert+Creator, Controller+Indirection+PF, Polymorphism+PV, гроші і зміни поведінки | `docs/adr/` |
| 2. Information Expert + Creator | Розрахунок у `MembershipPlan`/`Subscription`; приватні конструктори + `register`/`of`/`open`; `SubscriptionOffer.subscribe`; `List.copyOf`; `private final` | `domain/` |
| 3. Controller + Indirection + Pure Fabrication | Thin `ConsoleUi` (меню на `Scanner`), `RegisterSubscriptionUseCase`, порти `TariffCatalog`/`SubscriptionRepository`/`SubscriptionNotifier`, `Console`, `ConsoleCommandParser`, `ContractFormatter`, `SubscriptionFactory`, `AuditLogger` | `cli/`, `application/`, `infrastructure/` |
| 4. Polymorphism + Protected Variations | `PlanPricing` (Regular / Multiplier / Surcharge) замість `if/else` за тарифом; `DiscountPolicy`; усі точки нестабільності за інтерфейсами | `domain/pricing`, `domain/discount` |
| 5. Low Coupling + High Cohesion | CBO_core ≤ 4 і LCOM4 = 1 для ключових класів; скрипт розрахунку за байткодом; порівняльна таблиця й висновки | `docs/metrics/`, `tools/` |
| 6. Крос-рев'ю + захист | Чек-лист рецензента, попередні знахідки, шаблон підсумку; шпаргалка з 9 патернів і типових запитань | `docs/review/`, `docs/defense/` |

## Структура

```
src/main/java/
├── variant3_fitnessmembership/      ← ОРИГІНАЛЬНИЙ legacy-код (еталон «до»)
└── ua/chnu/kpz/fitness/
    ├── FitnessClubApp.java          ← Composition Root (main)
    ├── cli/                         ← Thin Console UI
    │   ├── ConsoleUi                  меню, ввід → DTO → контролер → вивід
    │   ├── ConsoleCommandParser       [Pure Fabrication] текст → RegisterSubscriptionCommand
    │   ├── ContractFormatter          [Pure Fabrication] текст договору
    │   └── Console / SystemConsole    [Indirection] абстракція терміналу
    ├── application/                 ← сценарії використання
    │   ├── RegisterSubscriptionUseCase [Controller]
    │   ├── SubscriptionFactory        [Pure Fabrication] DTO → агрегат
    │   ├── RegisterSubscriptionCommand, SubscriptionReceipt  (DTO)
    │   └── port/                      [Indirection + PV] TariffCatalog, SubscriptionRepository, SubscriptionNotifier
    ├── domain/                      ← Rich Domain Model (не залежить від інших шарів)
    │   ├── Subscription               [Expert + Creator] корінь агрегату
    │   ├── SubscriptionOffer          [Creator] тариф + послуги + знижка → абонемент
    │   ├── MembershipPlan, Member, AddOn, Money, PriceBreakdown, ContractTerms, SubscriptionStatus
    │   ├── pricing/                   [Polymorphism + PV] PlanPricing: Regular / Multiplier / Surcharge
    │   └── discount/                  [Polymorphism + PV] DiscountPolicy: DiscountCard / NoDiscount
    └── infrastructure/              ← адаптери портів
        ├── InMemoryTariffCatalog, DefaultTariffs (ціни варіанта)
        ├── InMemorySubscriptionRepository, AuditLogger, ConsoleAuditLogger
        └── ConsolePushNotifier
src/test/java/…                      ← юніт-, інтеграційні та характеризаційні тести
docs/adr/  docs/metrics/  docs/review/  docs/defense/
tools/grasp_metrics.py               ← CBO / LCOM4 / WMC за байткодом
```

Напрям залежностей: `cli → application → domain ← infrastructure`. Домен не імпортує нічого з інших шарів.

## Робота в команді та Git

Пропонований розподіл (за умовою: напр. 3 + 2 завдання, 6 — спільно):

| Хто | Завдання | Гілка |
|---|---|---|
| Студент А (Решко) | 1, 2, 3 — аудит/ADR, домен, CLI + контролер + порти | `feature/Reshko` |
| Студент Б | 4, 5 — стратегії тарифів/знижок, метрики | `feature/<прізвище>` |
| Разом | 6 — крос-рев'ю PR одне одного, підготовка до захисту | — |

Пропонований порядок комітів, щоб історія відображала кроки рефакторингу:

1. `legacy: варіант 3 + характеризаційні тести`
2. `docs: ADR-0000 аудит legacy-коду`
3. `domain: Information Expert + Creator (Money, Member, Plan, Subscription)`
4. `app/cli: Controller, порти, Pure Fabrication, тонкий ConsoleUi`
5. `domain: Polymorphism + Protected Variations (PlanPricing, DiscountPolicy)`
6. `metrics: CBO/LCOM4 до/після`
7. `docs: крос-рев'ю та підготовка до захисту`

## Перевірка поведінки

`LegacyEquivalenceTest` запускає **оригінальний** `LegacyFitnessService` (через підміну `System.in`/`System.out`) і новий код на однакових даних:

| Тариф | Базова вартість | Знижка 10% | До сплати |
|---|---:|---:|---:|
| Day | 14 520.00 | 1 452.00 | **13 068.00** |
| Full | 17 400.00 | 1 740.00 | **15 660.00** |
| VIP | 22 400.00 | 2 240.00 | **20 160.00** |

(ставка 1200 грн × 12 міс., персональний тренер +3000, дисконтна карта є — як у legacy-коді)

Свідомі зміни поведінки описано в [ADR-0004](docs/adr/0004-money-and-behaviour-changes.md).
