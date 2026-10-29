package ua.chnu.kpz.fitness.domain.pricing;

import java.util.Objects;
import ua.chnu.kpz.fitness.domain.Money;

/** Тариф з фіксованою надбавкою (VIP: +5000 грн за весь термін). */
public final class SurchargePricing implements PlanPricing {

    private final Money surcharge;

    public SurchargePricing(Money surcharge) {
        Objects.requireNonNull(surcharge, "surcharge");
        if (surcharge.isNegative()) {
            throw new IllegalArgumentException("Надбавка не може бути від'ємною");
        }
        this.surcharge = surcharge;
    }

    @Override
    public Money apply(Money termPrice) {
        return termPrice.plus(surcharge);
    }
}
