package ua.chnu.kpz.fitness.domain.discount;

import ua.chnu.kpz.fitness.domain.Member;
import ua.chnu.kpz.fitness.domain.Money;

/**
 * Політика знижок. GRASP Protected Variations: правила знижок змінюються найчастіше
 * (акції, сезонні знижки, програми лояльності), тому сховані за стабільним інтерфейсом.
 */
@FunctionalInterface
public interface DiscountPolicy {

    /** @return сума знижки (не більша за {@code amount}) */
    Money discountFor(Member member, Money amount);
}
