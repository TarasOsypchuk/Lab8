package ua.chnu.kpz.fitness.domain;

import java.util.Objects;

/**
 * Незмінна структура розрахунку ціни: базова вартість, знижка, до сплати.
 * Інваріант {@code finalCost = baseAmount - discountAmount} перевіряється в конструкторі,
 * тож «зламаний» розрахунок неможливо створити (у legacy-коді поля
 * {@code baseAmount/discountAmount/finalCost} були public і незалежні одне від одного).
 */
public record PriceBreakdown(Money baseAmount, Money discountAmount, Money finalCost) {

    public PriceBreakdown {
        Objects.requireNonNull(baseAmount, "baseAmount");
        Objects.requireNonNull(discountAmount, "discountAmount");
        Objects.requireNonNull(finalCost, "finalCost");
        if (baseAmount.isNegative() || discountAmount.isNegative()) {
            throw new IllegalArgumentException("Суми не можуть бути від'ємними");
        }
        if (discountAmount.compareTo(baseAmount) > 0) {
            throw new IllegalArgumentException("Знижка не може перевищувати базову вартість");
        }
        if (!finalCost.equals(baseAmount.minus(discountAmount))) {
            throw new IllegalArgumentException("Підсумок не дорівнює базовій вартості мінус знижка");
        }
    }

    public static PriceBreakdown of(Money baseAmount, Money discountAmount) {
        return new PriceBreakdown(baseAmount, discountAmount, baseAmount.minus(discountAmount));
    }
}
