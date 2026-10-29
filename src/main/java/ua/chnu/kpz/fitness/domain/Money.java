package ua.chnu.kpz.fitness.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object «гроші» (гривні, 2 знаки після коми).
 *
 * <p>Замінює {@code double} з legacy-коду: двійкова арифметика з плаваючою комою
 * дає похибки округлення, неприпустимі для фінансових розрахунків (див. ADR-0004).
 * Незмінний: кожна операція повертає новий об'єкт.
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

    private static final int SCALE = 2;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(long hryvnias) {
        return new Money(BigDecimal.valueOf(hryvnias));
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor));
    }

    public Money times(int multiplier) {
        return new Money(amount.multiply(BigDecimal.valueOf(multiplier)));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
