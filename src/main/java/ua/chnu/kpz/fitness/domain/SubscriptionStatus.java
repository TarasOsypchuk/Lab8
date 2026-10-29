package ua.chnu.kpz.fitness.domain;

/** Життєвий цикл абонемента. Замінює «магічні рядки» "Draft"/"Active" з legacy-коду. */
public enum SubscriptionStatus {
    DRAFT("Чернетка"),
    ACTIVE("Активний");

    private final String label;

    SubscriptionStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
