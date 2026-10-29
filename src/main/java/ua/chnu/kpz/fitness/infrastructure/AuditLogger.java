package ua.chnu.kpz.fitness.infrastructure;

/** GRASP Pure Fabrication: технічний журнал подій, якого немає в предметній області. */
@FunctionalInterface
public interface AuditLogger {

    void record(String message);
}
