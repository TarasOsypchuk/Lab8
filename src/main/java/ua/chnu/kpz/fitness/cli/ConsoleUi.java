package ua.chnu.kpz.fitness.cli;

import java.util.Objects;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionCommand;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionUseCase;
import ua.chnu.kpz.fitness.application.SubscriptionReceipt;

/**
 * Тонкий консольний клієнт (Thin Console UI).
 *
 * <p>Лише: показати меню → зчитати відповіді → зібрати DTO (через парсер) →
 * викликати контролер → вивести результат. Жодних обчислень і бізнес-правил.
 */
public final class ConsoleUi {

    private final Console console;
    private final ConsoleCommandParser parser;
    private final RegisterSubscriptionUseCase registerSubscription;
    private final ContractFormatter formatter;

    public ConsoleUi(Console console, ConsoleCommandParser parser,
                     RegisterSubscriptionUseCase registerSubscription, ContractFormatter formatter) {
        this.console = Objects.requireNonNull(console, "console");
        this.parser = Objects.requireNonNull(parser, "parser");
        this.registerSubscription = Objects.requireNonNull(registerSubscription, "registerSubscription");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public void run() {
        console.println("=== Мережа фітнес-клубів: реєстрація абонементів ===");
        while (true) {
            console.println("");
            console.println("1 - Оформити абонемент");
            console.println("0 - Вихід");
            String choice = console.readLine("> ");
            if (choice == null || "0".equals(choice.strip())) {
                console.println("До побачення!");
                return;
            }
            if ("1".equals(choice.strip())) {
                registerFlow();
            } else {
                console.println("Невідома команда: " + choice);
            }
        }
    }

    private void registerFlow() {
        String name = console.readLine("Ім'я клієнта: ");
        String phone = console.readLine("Телефон (+380XXXXXXXXX): ");
        String card = console.readLine("Є дисконтна карта? (y/n): ");
        String plan = console.readLine("Тип абонемента (Day/Full/VIP): ");
        String trainer = console.readLine("Додати персонального тренера? (y/n): ");
        String date = console.readLine("Дата початку (yyyy-MM-dd, Enter = завтра): ");
        try {
            RegisterSubscriptionCommand command = parser.toCommand(name, phone, card, plan, trainer, date);
            SubscriptionReceipt receipt = registerSubscription.register(command);
            console.println("");
            console.println(formatter.format(receipt));
        } catch (IllegalArgumentException | IllegalStateException e) {
            console.println("Помилка: " + e.getMessage());
        }
    }
}
