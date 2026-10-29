package ua.chnu.kpz.fitness.domain.discount;

import ua.chnu.kpz.fitness.domain.Member;
import ua.chnu.kpz.fitness.domain.Money;

/** Null Object: політика «без знижок» (напр. для акційних тарифів). */
public final class NoDiscountPolicy implements DiscountPolicy {

    @Override
    public Money discountFor(Member member, Money amount) {
        return Money.ZERO;
    }
}
