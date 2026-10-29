package ua.chnu.kpz.fitness.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import ua.chnu.kpz.fitness.domain.discount.DiscountPolicy;

/**
 * Пропозиція клубу: обраний тариф + обрані послуги + чинна політика знижок.
 *
 * <p>GRASP Creator: пропозиція містить усі ініціалізуючі дані для абонемента
 * (тариф, послуги, правило знижки), тому саме вона створює {@link Subscription}
 * через {@link #subscribe}. Клієнтський код не збирає агрегат «вручну».
 */
public final class SubscriptionOffer {

    private final MembershipPlan plan;
    private final List<AddOn> addOns;
    private final DiscountPolicy discountPolicy;

    private SubscriptionOffer(MembershipPlan plan, List<AddOn> addOns, DiscountPolicy discountPolicy) {
        this.plan = plan;
        this.addOns = addOns;
        this.discountPolicy = discountPolicy;
    }

    public static SubscriptionOffer of(MembershipPlan plan, List<AddOn> addOns, DiscountPolicy discountPolicy) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(discountPolicy, "discountPolicy");
        return new SubscriptionOffer(plan, List.copyOf(addOns), discountPolicy);
    }

    public Subscription subscribe(int subscriptionId, Member member, LocalDate startDate) {
        return Subscription.open(subscriptionId, member, plan, addOns, startDate, discountPolicy);
    }
}
