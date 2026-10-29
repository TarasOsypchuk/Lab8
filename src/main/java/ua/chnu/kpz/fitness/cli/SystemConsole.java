package ua.chnu.kpz.fitness.cli;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

/** Реальна консоль на базі {@link Scanner} та {@link PrintStream}. */
public final class SystemConsole implements Console {

    private final Scanner in;
    private final PrintStream out;

    public SystemConsole(Scanner in, PrintStream out) {
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        return in.hasNextLine() ? in.nextLine() : null;
    }

    @Override
    public void println(String text) {
        out.println(text);
    }
}
