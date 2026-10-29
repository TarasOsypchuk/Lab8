# ADR-0002. Тонкий CLI, Use-Case Controller, Indirection, Pure Fabrication

- **Статус:** прийнято
- **Дата:** 2026-10-28
- **Завдання ЛР:** 3
- **Пов'язані:** ADR-0000 (п. 2.2, 2.5)

## Контекст

`registerSubscriptionConsole()` одночасно читає консоль, рахує, форматує, «пише в БД» і «надсилає push» (Bloated Controller / Monolithic Main). Потрібно розділити ці обов'язки так, щоб:
- консольний шар тільки читав, перетворював на DTO і виводив;
- перший об'єкт за межами UI (контролер) координував use case без вводу/виводу;
- домен не знав про інфраструктуру.

## QOC 1. Яким має бути GRASP Controller?

**Question:** хто приймає системну подію «оформити абонемент» від UI?

| Options \ Criteria | C1. Відсутність I/O в контролері | C2. Розмір/згуртованість | C3. Повторне використання (CLI → REST) | C4. Простота |
|---|---|---|---|---|
| O1. Facade Controller `FitnessClubController` на всі операції | + | − розростається з кожною операцією | ± | + спочатку |
| O2. **Use-Case Controller `RegisterSubscriptionUseCase`** (один клас на одну системну операцію) | + | + LCOM4 = 1, CBO_core = 4 | + той самий клас викличе REST-контролер | ± |
| O3. Логіка прямо в `ConsoleUi` | − | − | − | + |

**Рішення: O2.** Контролер `RegisterSubscriptionUseCase.register(command)`:
1. отримує агрегат від `SubscriptionFactory` (Pure Fabrication, що делегує Creator-ам домену);
2. делегує бізнес-операцію домену: `subscription.activate()`;
3. зберігає через порт `SubscriptionRepository`;
4. сповіщає через порт `SubscriptionNotifier`;
5. повертає DTO `SubscriptionReceipt`.

У ньому немає ні `Scanner`, ні `System.out`, ні формул.

## QOC 2. Як розірвати прямі зв'язки CLI ↔ контролер ↔ інфраструктура (Indirection)?

**Question:** як зробити так, щоб контролер і домен не залежали від конкретних БД, push-API та консолі?

| Options \ Criteria | C1. Заміна реалізації без змін ядра | C2. Тестованість | C3. Кількість класів |
|---|---|---|---|
| O1. Конкретні класи напряму (`new JdbcRepository()` у контролері) | − | − | + |
| O2. **Інтерфейси-порти в `application.port` + реалізації в `infrastructure`; збирання в Composition Root** | + | + фейки в тестах | − більше файлів |
| O3. Service Locator / глобальний реєстр | ± | − прихована залежність | ± |

**Рішення: O2 (Indirection + Dependency Inversion).**

| Посередник (Indirection) | Між ким | Реалізація |
|---|---|---|
| `TariffCatalog` | контролер/фабрика ↔ сховище тарифів | `InMemoryTariffCatalog` |
| `SubscriptionRepository` | контролер ↔ БД | `InMemorySubscriptionRepository` |
| `SubscriptionNotifier` | контролер ↔ push-API | `ConsolePushNotifier` |
| `Console` | CLI ↔ `Scanner`/`System.out` | `SystemConsole`, у тестах `ScriptedConsole` |
| DTO `RegisterSubscriptionCommand` / `SubscriptionReceipt` | CLI ↔ домен | CLI не бачить доменних сутностей |
| `RegisterSubscriptionUseCase` | CLI ↔ домен | сам контролер теж є Indirection |

Конкретні класи з'єднуються лише в `FitnessClubApp.main` (Composition Root).

## QOC 3. Де розмістити технічні обов'язки (Pure Fabrication)?

**Question:** куди винести парсинг, форматування договору, збереження, журналювання, щоб не перевантажити доменні сутності?

| Options \ Criteria | C1. High Cohesion сутностей | C2. Відповідність предметній області | C3. Повторне використання |
|---|---|---|---|
| O1. У сутності (`subscription.save()`, `subscription.toContractText()`) — Active Record | − сутність знає про БД і формат тексту | ± | − |
| O2. **Штучні класи, яких немає в домені** | + | − (свідомо: це не доменні поняття) | + |

**Рішення: O2.** Pure Fabrication:
- `ConsoleCommandParser` — синтаксичний розбір (y/n, дата, код) у `RegisterSubscriptionCommand`;
- `ContractFormatter` — текст договору;
- `SubscriptionFactory` — збирає агрегат з DTO, щоб контролер лишався тонким;
- `InMemorySubscriptionRepository` — «таблиця subscriptions»;
- `AuditLogger` / `ConsoleAuditLogger` — технічний журнал («[База даних] …»);
- `Console` / `SystemConsole` — абстракція терміналу.

**Компроміс:** класів більше (≈ 35 замість 4). Зате кожен має один обов'язок, а метрики CBO і LCOM4 у межах цілей.

## Наслідки

- `ConsoleUi` (Thin Console UI) має лише цикл меню, `readLine`, виклик парсера, виклик контролера й `println`. Обчислень у ньому немає.
- Контролер і домен тестуються без консолі (`RegisterSubscriptionUseCaseTest` з фейковими портами).
- Щоб додати REST-API, достатньо написати новий адаптер, який викликає той самий `RegisterSubscriptionUseCase`.
