package ua.chnu.kpz.fitness.cli;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionCommand;

/**
 * GRASP Pure Fabrication: перетворює текстові відповіді користувача на DTO-команду.
 * Лише синтаксичний розбір (так/ні, дата, код) — бізнес-перевірки виконує домен.
 */
public final class ConsoleCommandParser {

    static final String TRAINER_ADD_ON = "TRAINER";

    private static final Set<String> YES = Set.of("y", "yes", "т", "так", "+", "1");
    private static final Set<String> NO = Set.of("n", "no", "н", "ні", "-", "0");

    private final Clock clock;

    public ConsoleCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public RegisterSubscriptionCommand toCommand(String name, String phone, String discountCardAnswer,
                                                 String planCode, String trainerAnswer, String startDate) {
        List<String> addOns = yesNo(trainerAnswer) ? List.of(TRAINER_ADD_ON) : List.of();
        return new RegisterSubscriptionCommand(
                text(name), text(phone), yesNo(discountCardAnswer), text(planCode), addOns, date(startDate));
    }

    boolean yesNo(String raw) {
        String answer = text(raw).toLowerCase(Locale.ROOT);
        if (YES.contains(answer)) {
            return true;
        }
        if (NO.contains(answer)) {
            return false;
        }
        throw new IllegalArgumentException("Очікувалась відповідь y/n, отримано: '" + raw + "'");
    }

    LocalDate date(String raw) {
        String value = text(raw);
        if (value.isEmpty()) {
            return LocalDate.now(clock).plusDays(1);
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Дата має бути у форматі yyyy-MM-dd, отримано: '" + raw + "'");
        }
    }

    private static String text(String raw) {
        return raw == null ? "" : raw.strip();
    }
}
