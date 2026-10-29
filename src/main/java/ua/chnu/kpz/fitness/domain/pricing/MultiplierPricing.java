package ua.chnu.kpz.fitness.domain.pricing;

import java.math.BigDecimal;
import java.util.Objects;
import ua.chnu.kpz.fitness.domain.Money;

/** Тариф з коефіцієнтом (Day: ×0.80 — лише денні години відвідування). */
public final class MultiplierPricing implements PlanPricing {

    private final BigDecimal multiplier;

    public MultiplierPricing(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier");
        if (multiplier.signum() <= 0) {
            throw new IllegalArgumentException("Коефіцієнт має бути додатним");
        }
        this.multiplier = multiplier;
    }

    @Override
    public Money apply(Money termPrice) {
        return termPrice.times(multiplier);
    }
}
