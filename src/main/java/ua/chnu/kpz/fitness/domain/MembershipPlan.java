package ua.chnu.kpz.fitness.domain;

import java.util.Objects;
import ua.chnu.kpz.fitness.domain.pricing.PlanPricing;

/**
 * Тарифний план (Day / Full / VIP …).
 *
 * <p>GRASP Information Expert: план володіє місячною ставкою, тривалістю та правилом
 * ціноутворення, тому саме він рахує вартість за весь термін ({@link #termPrice()}).
 * GRASP Polymorphism: відмінності між тарифами інкапсульовані у {@link PlanPricing},
 * а не в ланцюжку {@code if ("VIP".equalsIgnoreCase(...)) ... else if ("Day"...)}.
 */
public final class MembershipPlan {

    private final String code;
    private final String title;
    private final Money monthlyRate;
    private final int months;
    private final PlanPricing pricing;

    private MembershipPlan(String code, String title, Money monthlyRate, int months, PlanPricing pricing) {
        this.code = code;
        this.title = title;
        this.monthlyRate = monthlyRate;
        this.months = months;
        this.pricing = pricing;
    }

    public static MembershipPlan of(String code, String title, Money monthlyRate, int months, PlanPricing pricing) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Код тарифу не може бути порожнім");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Назва тарифу не може бути порожньою");
        }
        Objects.requireNonNull(monthlyRate, "monthlyRate");
        Objects.requireNonNull(pricing, "pricing");
        if (monthlyRate.isNegative() || monthlyRate.equals(Money.ZERO)) {
            throw new IllegalArgumentException("Місячна ставка має бути додатною");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("Тривалість абонемента має бути додатною");
        }
        return new MembershipPlan(code.strip(), title.strip(), monthlyRate, months, pricing);
    }

    /** Вартість тарифу за весь термін з урахуванням тарифної модифікації. */
    public Money termPrice() {
        return pricing.apply(monthlyRate.times(months));
    }

    public String code() {
        return code;
    }

    public String title() {
        return title;
    }

    public int months() {
        return months;
    }
}
