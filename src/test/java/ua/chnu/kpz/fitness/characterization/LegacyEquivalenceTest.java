package ua.chnu.kpz.fitness.characterization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionCommand;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionUseCase;
import ua.chnu.kpz.fitness.application.SubscriptionFactory;
import ua.chnu.kpz.fitness.application.SubscriptionReceipt;
import ua.chnu.kpz.fitness.infrastructure.DefaultTariffs;
import ua.chnu.kpz.fitness.infrastructure.InMemorySubscriptionRepository;
import variant3_fitnessmembership.LegacyFitnessService;

/**
 * Характеризаційні тести: запускаємо ОРИГІНАЛЬНИЙ legacy-код і новий код на однакових
 * вхідних даних і перевіряємо, що суми збігаються. Це доказ того, що рефакторинг
 * не змінив бізнес-поведінку (крім свідомо задокументованих змін — ADR-0004).
 *
 * <p>Legacy-код жорстко задає: дисконтна карта є, персональний тренер увімкнений.
 */
class LegacyEquivalenceTest {

    private record Amounts(BigDecimal base, BigDecimal discount, BigDecimal toPay) {
    }

    private static Amounts runLegacy(String planCode) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(("Ivan\n" + planCode + "\n").getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            new LegacyFitnessService().registerSubscriptionConsole();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String output = buffer.toString(StandardCharsets.UTF_8);
        return new Amounts(amount(output, "Базова вартість"), amount(output, "Знижка"), amount(output, "До сплати"));
    }

    private static BigDecimal amount(String output, String label) {
        Matcher m = Pattern.compile(label + ": ([0-9.]+) грн").matcher(output);
        if (!m.find()) {
            throw new AssertionError("У виводі legacy-коду немає рядка '" + label + "':\n" + output);
        }
        return new BigDecimal(m.group(1)).setScale(2, RoundingMode.HALF_UP);
    }

    private static SubscriptionReceipt runRefactored(String planCode) {
        RegisterSubscriptionUseCase useCase = new RegisterSubscriptionUseCase(
                new SubscriptionFactory(DefaultTariffs.catalog(), () -> 42, () -> 1002),
                new InMemorySubscriptionRepository(message -> { }),
                terms -> { });
        return useCase.register(new RegisterSubscriptionCommand("Ivan", "+380509998877", true, planCode,
                List.of(DefaultTariffs.TRAINER), LocalDate.of(2026, 11, 1)));
    }

    @ParameterizedTest(name = "тариф {0}: legacy і рефакторинг дають однакові суми")
    @CsvSource({"Day", "Full", "VIP", "vip", "day"})
    void sameAmountsAsLegacy(String planCode) {
        Amounts legacy = runLegacy(planCode);
        SubscriptionReceipt refactored = runRefactored(planCode);

        assertEquals(legacy.base(), refactored.baseAmount(), "базова вартість");
        assertEquals(legacy.discount(), refactored.discountAmount(), "знижка");
        assertEquals(legacy.toPay(), refactored.finalCost(), "до сплати");
    }

    @Test
    void knownLegacyAmounts() {
        assertEquals(new BigDecimal("13068.00"), runLegacy("Day").toPay());
        assertEquals(new BigDecimal("15660.00"), runLegacy("Full").toPay());
        assertEquals(new BigDecimal("20160.00"), runLegacy("VIP").toPay());
    }

    /** Свідома зміна поведінки (ADR-0004): legacy мовчки рахує невідомий тариф як Full. */
    @Test
    void unknownPlanIsNowRejectedInsteadOfSilentlyChargedAsFull() {
        assertEquals(runLegacy("Full").toPay(), runLegacy("Gold").toPay());
        assertThrows(IllegalArgumentException.class, () -> runRefactored("Gold"));
    }
}
