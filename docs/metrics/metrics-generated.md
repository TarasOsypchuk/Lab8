| Клас | Вид | CBO_all | CBO_core | Ca | LCOM4 | LCOM4_strict | WMC | Залежності (CBO_all) |
|---|---|---:|---:|---:|---:|---:|---:|---|
| `fitness.FitnessClubApp` | class | 20 | 20 | 0 | — | — | 3 | java.io.Console, java.io.InputStream, java.io.PrintStream, java.util.Scanner, RegisterSubscriptionUseCase, SubscriptionFactory, SubscriptionNotifier, SubscriptionRepository, TariffCatalog, Console, ConsoleCommandParser, ConsoleUi, ContractFormatter, SystemConsole, AuditLogger, ConsoleAuditLogger, ConsolePushNotifier, DefaultTariffs, InMemorySubscriptionRepository, InMemoryTariffCatalog |
| `fitness.application.RegisterSubscriptionCommand` | record | 0 | 0 | 4 | — | — | 10 |  |
| `fitness.application.RegisterSubscriptionUseCase` | class | 7 | 4 | 2 | 1 | 1 | 2 | RegisterSubscriptionCommand°, SubscriptionFactory, SubscriptionReceipt°, SubscriptionNotifier, SubscriptionRepository, ContractTerms°, Subscription |
| `fitness.application.SubscriptionFactory` | class | 5 | 4 | 2 | 1 | 1 | 2 | RegisterSubscriptionCommand°, TariffCatalog, Member, Subscription, SubscriptionOffer |
| `fitness.application.SubscriptionReceipt` | record | 4 | 0 | 3 | — | — | 16 | ContractTerms°, Money°, PriceBreakdown°, SubscriptionStatus° |
| `fitness.application.port.SubscriptionNotifier` | interface | 1 | 0 | 3 | — | — | 1 | ContractTerms° |
| `fitness.application.port.SubscriptionRepository` | interface | 1 | 1 | 3 | — | — | 2 | Subscription |
| `fitness.application.port.TariffCatalog` | interface | 1 | 1 | 3 | — | — | 1 | SubscriptionOffer |
| `fitness.cli.Console` | interface | 0 | 0 | 3 | — | — | 2 |  |
| `fitness.cli.ConsoleCommandParser` | class | 1 | 0 | 2 | 1 | 1 | 11 | RegisterSubscriptionCommand° |
| `fitness.cli.ConsoleUi` | class | 6 | 4 | 1 | 1 | 1 | 6 | RegisterSubscriptionCommand°, RegisterSubscriptionUseCase, SubscriptionReceipt°, Console, ConsoleCommandParser, ContractFormatter |
| `fitness.cli.ContractFormatter` | class | 1 | 0 | 2 | 1 | 1 | 3 | SubscriptionReceipt° |
| `fitness.cli.SystemConsole` | class | 3 | 3 | 1 | 1 | 1 | 4 | java.io.PrintStream, java.util.Scanner, Console |
| `fitness.domain.AddOn` | record | 1 | 0 | 4 | — | — | 12 | Money° |
| `fitness.domain.ContractTerms` | record | 2 | 0 | 5 | — | — | 14 | PriceBreakdown°, SubscriptionStatus° |
| `fitness.domain.Member` | class | 0 | 0 | 6 | — | 4 | 11 |  |
| `fitness.domain.MembershipPlan` | class | 2 | 1 | 4 | 1 | 3 | 13 | Money°, PlanPricing |
| `fitness.domain.Money` | record | 0 | 0 | 13 | — | — | 16 |  |
| `fitness.domain.PriceBreakdown` | record | 1 | 0 | 3 | — | — | 12 | Money° |
| `fitness.domain.Subscription` | class | 8 | 3 | 5 | 1 | 1 | 14 | AddOn°, ContractTerms°, Member, MembershipPlan, Money°, PriceBreakdown°, SubscriptionStatus°, DiscountPolicy |
| `fitness.domain.SubscriptionOffer` | class | 5 | 4 | 3 | 1 | 1 | 3 | AddOn°, Member, MembershipPlan, Subscription, DiscountPolicy |
| `fitness.domain.SubscriptionStatus` | enum | 0 | 0 | 3 | — | — | 6 |  |
| `fitness.domain.discount.DiscountCardPolicy` | class | 3 | 2 | 1 | 1 | 1 | 5 | Member, Money°, DiscountPolicy |
| `fitness.domain.discount.DiscountPolicy` | interface | 2 | 1 | 6 | — | — | 1 | Member, Money° |
| `fitness.domain.discount.NoDiscountPolicy` | class | 3 | 2 | 0 | 1 | 1 | 2 | Member, Money°, DiscountPolicy |
| `fitness.domain.pricing.MultiplierPricing` | class | 2 | 1 | 1 | 1 | 1 | 3 | Money°, PlanPricing |
| `fitness.domain.pricing.PlanPricing` | interface | 1 | 0 | 5 | — | — | 1 | Money° |
| `fitness.domain.pricing.RegularPricing` | class | 2 | 1 | 1 | 1 | 1 | 2 | Money°, PlanPricing |
| `fitness.domain.pricing.SurchargePricing` | class | 2 | 1 | 1 | 1 | 1 | 3 | Money°, PlanPricing |
| `fitness.infrastructure.AuditLogger` | interface | 0 | 0 | 3 | — | — | 1 |  |
| `fitness.infrastructure.ConsoleAuditLogger` | class | 2 | 2 | 1 | 1 | 1 | 2 | java.io.PrintStream, AuditLogger |
| `fitness.infrastructure.ConsolePushNotifier` | class | 3 | 2 | 1 | 1 | 1 | 2 | java.io.PrintStream, SubscriptionNotifier, ContractTerms° |
| `fitness.infrastructure.DefaultTariffs` | class | 10 | 8 | 1 | — | — | 3 | AddOn°, MembershipPlan, Money°, DiscountCardPolicy, DiscountPolicy, MultiplierPricing, PlanPricing, RegularPricing, SurchargePricing, InMemoryTariffCatalog |
| `fitness.infrastructure.InMemorySubscriptionRepository` | class | 3 | 3 | 1 | 1 | 1 | 3 | SubscriptionRepository, Subscription, AuditLogger |
| `fitness.infrastructure.InMemoryTariffCatalog` | class | 5 | 4 | 2 | 1 | 1 | 10 | TariffCatalog, AddOn°, MembershipPlan, SubscriptionOffer, DiscountPolicy |
| `variant3_fitnessmembership.LegacyFitnessService` | class | 6 | 6 | 0 | 1 | 1 | 6 | java.io.InputStream, java.io.PrintStream, java.util.Scanner, Member, MembershipPlan, Subscription |
| `variant3_fitnessmembership.Member` | class | 0 | 0 | 2 | — | — | 1 |  |
| `variant3_fitnessmembership.MembershipPlan` | class | 0 | 0 | 2 | — | — | 1 |  |
| `variant3_fitnessmembership.Subscription` | class | 2 | 2 | 1 | — | — | 1 | Member, MembershipPlan |

° — незмінний value-тип (record/enum), не входить у CBO_core.
