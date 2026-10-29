# Звіт метричного аналізу: Low Coupling (CBO) і High Cohesion (LCOM4)

- **Завдання ЛР:** 5
- **Як відтворити:** `mvn -q compile && python tools/grasp_metrics.py target/classes > docs/metrics/metrics-generated.md`
- **Повна автоматично згенерована таблиця (усі 39 класів):** [metrics-generated.md](metrics-generated.md)

Метрики рахуються за **байткодом**, а не «на око». Залежності береться з `jdeps -verbose:class`, звернення до полів і виклики методів — з `javap -c`. Скрипт `tools/grasp_metrics.py` можна запустити на захисті.

## 1. Методика

### CBO (Coupling Between Objects)

Рахуємо **вихідне зчеплення (fan-out)**: кількість різних класів, від яких залежить клас. Сюди входять поля, параметри, типи повернення, локальні змінні, виклики, `extends/implements`. Вхідне зчеплення (Ca) подано довідково в повній таблиці.

| Рахується                                                                                   | Не рахується                                                                                                         |
| ------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| усі типи проєкту                                                                            | сам клас                                                                                                             |
| JDK-типи вводу/виводу: `java.io.*`, `java.util.Scanner`, `java.nio.file.*` (інфраструктура) | `java.lang.*` (`String`, `Object`), `java.math.*`, `java.time.*`, колекції та функціональні інтерфейси `java.util.*` |

Наводимо два значення:

- **CBO_all** — усі залежності за правилами вище;
- **CBO_core** — без незмінних value-типів проєкту (`record`, `enum`: `Money`, `PriceBreakdown`, `ContractTerms`, DTO-команди…). Вони стабільні, не мають побічних ефектів і не можуть «зламати» клієнта, так само як `String` чи `BigDecimal`. **Цільове значення CBO ≤ 4 застосовуємо до CBO_core.** CBO_all показано для прозорості.

### LCOM4 (Hitz & Montazeri)

LCOM4 — кількість зв'язних компонент графа: вершини — методи екземпляра; ребро між двома методами є, якщо вони використовують спільне поле або один викликає інший. **LCOM4 = 1** означає, що клас має один обов'язок. LCOM4 ≥ 2 означає, що клас можна розбити на кілька незалежних.

- Конструктори та static-методи не враховуються (класичне правило LCOM4).
- **LCOM4** — без тривіальних аксесорів (метод без параметрів, що лише повертає поле). Кожен такий метод читає одне поле й штучно утворює окрему компоненту.
- **LCOM4_strict** — з аксесорами, для прозорості.
- «—» означає, що в класі немає жодного методу для аналізу (лише дані).

### WMC (Weighted Methods per Class)

WMC — сума цикломатичних складностей методів (1 + кількість умовних переходів). Ця метрика доповнює LCOM4 там, де LCOM4 «сліпа» (див. п. 3).

## 2. Порівняльна таблиця «до / після»

### До рефакторингу (legacy)

| Клас                      |                        CBO_all | CBO_core | LCOM4 | LCOM4_strict | WMC |    Обов'язків |
| ------------------------- | -----------------------------: | -------: | ----: | -----------: | --: | ------------: |
| `LegacyFitnessService`    | **6** (+2 приховані: БД, push) |    **6** |   1\* |          1\* |   6 |         **7** |
| `Subscription` (legacy)   |                              2 |        2 |     — |            — |   1 | 0 (лише дані) |
| `Member` (legacy)         |                              0 |        0 |     — |            — |   1 | 0 (лише дані) |
| `MembershipPlan` (legacy) |                              0 |        0 |     — |            — |   1 | 0 (лише дані) |

\* Про те, чому LCOM4 = 1 тут нічого не доводить, див. п. 3.

### Після рефакторингу (ключові класи)

| Шар            | Клас                                                        | Роль GRASP           | CBO_all | **CBO_core** | **LCOM4** | LCOM4_strict | WMC |
| -------------- | ----------------------------------------------------------- | -------------------- | ------: | -----------: | --------: | -----------: | --: |
| domain         | `Subscription`                                              | Expert, Creator      |       8 |        **3** |     **1** |            1 |  14 |
| domain         | `SubscriptionOffer`                                         | Creator              |       5 |        **4** |     **1** |            1 |   3 |
| domain         | `MembershipPlan`                                            | Expert, Polymorphism |       2 |        **1** |     **1** |            3 |  13 |
| domain         | `Member`                                                    | Creator (factory)    |       0 |        **0** |         — |            4 |  11 |
| domain         | `DiscountCardPolicy`                                        | Polymorphism, PV     |       3 |        **2** |     **1** |            1 |   5 |
| domain         | `MultiplierPricing` / `SurchargePricing` / `RegularPricing` | Polymorphism         |       2 |        **1** |     **1** |            1 | 2–3 |
| application    | `RegisterSubscriptionUseCase`                               | Controller           |       7 |        **4** |     **1** |            1 |   2 |
| application    | `SubscriptionFactory`                                       | Pure Fabrication     |       5 |        **4** |     **1** |            1 |   2 |
| cli            | `ConsoleUi`                                                 | Thin UI              |       6 |        **4** |     **1** |            1 |   6 |
| cli            | `ConsoleCommandParser`                                      | Pure Fabrication     |       1 |        **0** |     **1** |            1 |  11 |
| cli            | `ContractFormatter`                                         | Pure Fabrication     |       1 |        **0** |     **1** |            1 |   3 |
| cli            | `SystemConsole`                                             | Indirection          |       3 |        **3** |     **1** |            1 |   4 |
| infrastructure | `InMemoryTariffCatalog`                                     | Indirection, PV      |       5 |        **4** |     **1** |            1 |  10 |
| infrastructure | `InMemorySubscriptionRepository`                            | Pure Fabrication     |       3 |        **3** |     **1** |            1 |   3 |
| infrastructure | `ConsolePushNotifier`                                       | PV (адаптер)         |       3 |        **2** |     **1** |            1 |   2 |
| infrastructure | `ConsoleAuditLogger`                                        | Pure Fabrication     |       2 |        **2** |     **1** |            1 |   2 |

Свідомі винятки (не нормуються):

| Клас             | CBO_all | Чому допустимо                                                                                                                                      |
| ---------------- | ------: | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `FitnessClubApp` |      20 | **Composition Root**: єдине призначення класу — знати всі конкретні реалізації і з'єднати їх. Завдяки цьому решта коду залежить лише від абстракцій |
| `DefaultTariffs` |      10 | Конфігурація тарифів, частина Composition Root. Тут зібрані всі «магічні» числа legacy (1200, 12, 0.8, 5000, 3000, 0.10). Логіки немає (WMC = 3)    |

## 3. Інтерпретація та інженерні висновки

1. **CBO знизився до цілі.** У legacy один клас мав CBO = 6 (фактично 8 з урахуванням вшитих БД і push) і замикав на собі консоль, домен та інфраструктуру. Після рефакторингу кожен клас поза Composition Root має **CBO_core ≤ 4**, а максимум (4) — у координаторів (`RegisterSubscriptionUseCase`, `SubscriptionFactory`, `ConsoleUi`, `SubscriptionOffer`, `InMemoryTariffCatalog`), яким за роллю потрібно знати кількох співрозмовників. Доменні класи залежать від 0–3 типів і **не залежать ні від application, ні від infrastructure** (перевіряється стовпцем «Залежності» у `metrics-generated.md`).
2. **Чому CBO_all у кількох класах > 4.** Різниця між CBO_all і CBO_core — це лише незмінні records/enums (`Money`, `PriceBreakdown`, `ContractTerms`, `SubscriptionStatus`, DTO). Наприклад, `Subscription` має CBO_all = 8: з них 5 — value-типи (`Money`, `PriceBreakdown`, `ContractTerms`, `AddOn`, `SubscriptionStatus`), а «справжніх» залежностей лише 3 (`Member`, `MembershipPlan`, `DiscountPolicy`). Заміна value-типів на `double`/`String`, щоб «покращити» CBO_all, повернула б Primitive Obsession з legacy-коду. Тобто якість погіршилась би, хоча метрика покращилась.
3. **LCOM4 = 1 для всіх ключових класів із поведінкою.** Кожен клас має один обов'язок: методи пов'язані спільними полями. Наприклад, у `Subscription` методи `activate()` і `isActive()` використовують `status`, а `contractTerms()` читає всі поля, зокрема `status`.
4. **Сліпа зона LCOM4 у legacy.** `LegacyFitnessService` формально має LCOM4 = 1, але лише тому, що в ньому **один метод і жодного поля**: граф з однієї вершини завжди зв'язний. Насправді цей метод виконує 7 обов'язків (ADR-0000, п. 2.2) і має цикломатичну складність 5. Тому низьку згуртованість legacy доводимо кількістю обов'язків і WMC, а LCOM4 використовуємо для класів «після», де методів кілька. Legacy-сутності мають LCOM4 «—»: методів немає взагалі, це прямий числовий прояв Anemic Domain Model.
5. **`Member` і `MembershipPlan` у LCOM4_strict.** Значення 4 і 3 означають, що аксесори (`id()`, `name()`, `code()`, `title()`…) читають по одному полю. Без аксесорів `MembershipPlan` має LCOM4 = 1 (`termPrice()` використовує ставку, місяці й стратегію). `Member` у цьому use case не має обчислювальних обов'язків, лише зберігає валідні дані клієнта, тому LCOM4 = «—». Розбивати його на кілька класів не потрібно.
6. **Складність розподілилась.** Сумарний WMC зріс, бо з'явилися валідації, яких у legacy не було взагалі. Найскладніший метод legacy (CC = 5, 55 рядків, 7 обов'язків) розпався на методи з одним обов'язком, кожен з яких протестовано окремо (29 тестів).

## 4. Ручна перевірка (для захисту)

**CBO_core(`RegisterSubscriptionUseCase`)** — імпорти й сигнатури: `SubscriptionFactory`, `SubscriptionRepository`, `SubscriptionNotifier`, `Subscription` = **4**. Value-типи `RegisterSubscriptionCommand`, `SubscriptionReceipt`, `ContractTerms` не враховуються. CBO_all = 7.

**LCOM4(`Subscription`)**, без конструктора, static `open()` і аксесорів `id()`, `price()`, `addOns()`:

```
activate()       → {status, id}
isActive()       → {status}
contractTerms()  → {id, member, plan, addOns, startDate, price, status}
Ребра: activate–isActive (status), activate–contractTerms (status, id), isActive–contractTerms (status)
Компонент: 1  ⇒ LCOM4 = 1
```

**CBO(`LegacyFitnessService`)**: `Scanner`, `InputStream` (`System.in`), `PrintStream` (`System.out`), `Member`, `MembershipPlan`, `Subscription` = **6**. Логічно до них додаються вшиті «БД» і «push» — **8**.
