package ua.chnu.kpz.fitness.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionCommand;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionUseCase;
import ua.chnu.kpz.fitness.application.SubscriptionFactory;
import ua.chnu.kpz.fitness.infrastructure.DefaultTariffs;
import ua.chnu.kpz.fitness.infrastructure.InMemorySubscriptionRepository;

class ConsoleUiTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T10:00:00Z"), ZoneOffset.UTC);

    /** Фейкова консоль: «вводить» заготовлені рядки та збирає вивід. */
    private static final class ScriptedConsole implements Console {
        final Deque<String> input;
        final List<String> output = new ArrayList<>();

        ScriptedConsole(String... lines) {
            input = new ArrayDeque<>(List.of(lines));
        }

        @Override
        public String readLine(String prompt) {
            return input.poll();
        }

        @Override
        public void println(String text) {
            output.add(text);
        }

        String all() {
            return String.join("\n", output);
        }
    }

    private static ConsoleUi ui(Console console, List<String> log) {
        RegisterSubscriptionUseCase useCase = new RegisterSubscriptionUseCase(
                new SubscriptionFactory(DefaultTariffs.catalog(), new AtomicInteger()::incrementAndGet,
                        new AtomicInteger(1000)::incrementAndGet),
                new InMemorySubscriptionRepository(log::add),
                terms -> log.add("push:" + terms.memberName()));
        return new ConsoleUi(console, new ConsoleCommandParser(CLOCK), useCase, new ContractFormatter());
    }

    @Test
    void fullScenarioPrintsContract() {
        ScriptedConsole console = new ScriptedConsole(
                "1", "Олена", "+380671234567", "y", "vip", "y", "2026-11-01", "0");
        List<String> log = new ArrayList<>();

        ui(console, log).run();

        String out = console.all();
        assertTrue(out.contains("Договір #1001 з Олена"));
        assertTrue(out.contains("До сплати: 20160.00 грн"));
        assertTrue(out.contains("Статус: Активний"));
        assertEquals(List.of("[База даних] Абонемент #1001 додано до таблиці subscriptions.", "push:Олена"), log);
    }

    @Test
    void errorIsShownAndUiKeepsWorking() {
        ScriptedConsole console = new ScriptedConsole(
                "1", "Олена", "+380671234567", "y", "Gold", "n", "", "0");
        List<String> log = new ArrayList<>();

        ui(console, log).run();

        assertTrue(console.all().contains("Помилка: Невідомий тип абонемента: 'Gold'"));
        assertTrue(console.all().contains("До побачення!"));
        assertTrue(log.isEmpty());
    }

    @Test
    void endOfInputExitsGracefully() {
        ScriptedConsole console = new ScriptedConsole();
        ui(console, new ArrayList<>()).run();
        assertTrue(console.all().contains("До побачення!"));
    }

    @Test
    void parserOnlyParsesSyntax() {
        ConsoleCommandParser parser = new ConsoleCommandParser(CLOCK);

        RegisterSubscriptionCommand c = parser.toCommand(" Іван ", "0501112233", "так", " Day ", "ні", "");

        assertEquals("Іван", c.memberName());
        assertTrue(c.hasDiscountCard());
        assertEquals("Day", c.planCode());
        assertTrue(c.addOnCodes().isEmpty());
        assertEquals(LocalDate.of(2026, 10, 29), c.startDate());
        assertFalse(parser.yesNo("N"));
        assertThrows(IllegalArgumentException.class, () -> parser.yesNo("може"));
        assertThrows(IllegalArgumentException.class, () -> parser.date("01.11.2026"));
    }
}
