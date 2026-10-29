package ua.chnu.kpz.fitness.domain.pricing;

import ua.chnu.kpz.fitness.domain.Money;

/** Тариф без модифікацій (Full): ціна = ставка × місяці. */
public final class RegularPricing implements PlanPricing {

    @Override
    public Money apply(Money termPrice) {
        return termPrice;
    }
}
