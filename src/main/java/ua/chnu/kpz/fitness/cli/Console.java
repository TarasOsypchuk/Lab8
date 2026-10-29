package ua.chnu.kpz.fitness.cli;

/**
 * GRASP Pure Fabrication + Indirection: абстракція консолі.
 * Відокремлює CLI від {@code Scanner}/{@code System.out} і робить його тестованим.
 */
public interface Console {

    /** @return введений рядок або {@code null}, якщо введення закінчилось (EOF) */
    String readLine(String prompt);

    void println(String text);
}
