package ua.chnu.kpz.fitness.infrastructure;

import java.io.PrintStream;
import java.util.Objects;

/** Журнал, що пише у консоль (імітація лог-системи). */
public final class ConsoleAuditLogger implements AuditLogger {

    private final PrintStream out;

    public ConsoleAuditLogger(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    public void record(String message) {
        out.println(message);
    }
}
