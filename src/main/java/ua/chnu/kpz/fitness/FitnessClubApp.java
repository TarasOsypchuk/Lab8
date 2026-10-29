package ua.chnu.kpz.fitness;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.time.Clock;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import ua.chnu.kpz.fitness.application.RegisterSubscriptionUseCase;
import ua.chnu.kpz.fitness.application.SubscriptionFactory;
import ua.chnu.kpz.fitness.cli.ConsoleCommandParser;
import ua.chnu.kpz.fitness.cli.ConsoleUi;
import ua.chnu.kpz.fitness.cli.ContractFormatter;
import ua.chnu.kpz.fitness.cli.SystemConsole;
import ua.chnu.kpz.fitness.infrastructure.ConsoleAuditLogger;
import ua.chnu.kpz.fitness.infrastructure.ConsolePushNotifier;
import ua.chnu.kpz.fitness.infrastructure.DefaultTariffs;
import ua.chnu.kpz.fitness.infrastructure.InMemorySubscriptionRepository;

/**
 * Composition Root: єдине місце, де конкретні реалізації «з'єднуються» між собою.
 * Високе зчеплення тут — його пряме призначення (див. звіт метрик).
 */
public final class FitnessClubApp {

    private FitnessClubApp() {
    }

    public static void main(String[] args) {
        PrintStream out = System.out;
        Charset inputCharset = System.console() != null ? System.console().charset() : Charset.defaultCharset();

        AtomicInteger memberSequence = new AtomicInteger(0);
        AtomicInteger subscriptionSequence = new AtomicInteger(1000);

        SubscriptionFactory factory = new SubscriptionFactory(
                DefaultTariffs.catalog(), memberSequence::incrementAndGet, subscriptionSequence::incrementAndGet);
        RegisterSubscriptionUseCase useCase = new RegisterSubscriptionUseCase(
                factory,
                new InMemorySubscriptionRepository(new ConsoleAuditLogger(out)),
                new ConsolePushNotifier(out));

        new ConsoleUi(
                new SystemConsole(new Scanner(System.in, inputCharset), out),
                new ConsoleCommandParser(Clock.systemDefaultZone()),
                useCase,
                new ContractFormatter()).run();
    }
}
