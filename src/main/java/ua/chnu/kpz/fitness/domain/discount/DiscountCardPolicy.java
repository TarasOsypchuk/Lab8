package ua.chnu.kpz.fitness.domain.discount;

import java.math.BigDecimal;
import java.util.Objects;
import ua.chnu.kpz.fitness.domain.Member;
import ua.chnu.kpz.fitness.domain.Money;

/** Знижка власникам дисконтної карти (у legacy-коді — «магічні» 0.10 у сервісі). */
public final class DiscountCardPolicy implements DiscountPolicy {

    private final BigDecimal rate;

    public DiscountCardPolicy(BigDecimal rate) {
        Objects.requireNonNull(rate, "rate");
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Відсоток знижки має бути в межах [0; 1]");
        }
        this.rate = rate;
    }

    @Override
    public Money discountFor(Member member, Money amount) {
        return member.hasDiscountCard() ? amount.times(rate) : Money.ZERO;
    }
}
