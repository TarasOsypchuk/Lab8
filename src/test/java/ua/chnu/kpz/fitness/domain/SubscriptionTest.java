package ua.chnu.kpz.fitness.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ua.chnu.kpz.fitness.domain.discount.DiscountCardPolicy;
import ua.chnu.kpz.fitness.domain.discount.NoDiscountPolicy;
import ua.chnu.kpz.fitness.domain.pricing.SurchargePricing;

class SubscriptionTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 1);
    private static final AddOn TRAINER = new AddOn("TRAINER", "Персональний тренер", Money.of(3000));
    private static final MembershipPlan VIP = MembershipPlan.of("VIP", "VIP", Money.of(1200), 12,
            new SurchargePricing(Money.of(5000)));
    private static final DiscountCardPolicy CARD = new DiscountCardPolicy(new BigDecimal("0.10"));

    private static Member member(boolean card) {
        return Member.register(42, "Олена", "+380671234567", card);
    }

    @Test
    void calculatesPriceAtCreation() {
        Subscription s = Subscription.open(1, member(true), VIP, List.of(TRAINER), START, CARD);

        assertEquals(Money.of(22400), s.price().baseAmount());
        assertEquals(Money.of(2240), s.price().discountAmount());
        assertEquals(Money.of(20160), s.price().finalCost());
    }

    @Test
    void noCardMeansNoDiscount() {
        Subscription s = Subscription.open(1, member(false), VIP, List.of(), START, CARD);
        assertEquals(Money.ZERO, s.price().discountAmount());
        assertEquals(Money.of(19400), s.price().finalCost());
    }

    @Test
    void startsAsDraftAndCanBeActivatedOnlyOnce() {
        Subscription s = Subscription.open(1, member(true), VIP, List.of(), START, new NoDiscountPolicy());
        assertFalse(s.isActive());
        assertEquals(SubscriptionStatus.DRAFT, s.contractTerms().status());

        s.activate();

        assertTrue(s.isActive());
        assertThrows(IllegalStateException.class, s::activate);
    }

    @Test
    void addOnsAreDefensivelyCopiedAndImmutable() {
        List<AddOn> source = new ArrayList<>(List.of(TRAINER));
        Subscription s = Subscription.open(1, member(true), VIP, source, START, CARD);

        source.clear();

        assertEquals(1, s.addOns().size());
        assertThrows(UnsupportedOperationException.class, () -> s.addOns().add(TRAINER));
    }

    @Test
    void cannotBeCreatedInInvalidState() {
        assertThrows(IllegalArgumentException.class,
                () -> Subscription.open(0, member(true), VIP, List.of(), START, CARD));
        assertThrows(NullPointerException.class,
                () -> Subscription.open(1, null, VIP, List.of(), START, CARD));
        assertThrows(NullPointerException.class,
                () -> Subscription.open(1, member(true), VIP, List.of(), null, CARD));
        assertThrows(IllegalArgumentException.class,
                () -> Subscription.open(1, member(true), VIP, List.of(TRAINER, TRAINER), START, CARD));
    }

    @Test
    void priceBreakdownInvariantCannotBeBroken() {
        assertThrows(IllegalArgumentException.class,
                () -> new PriceBreakdown(Money.of(100), Money.of(10), Money.of(100)));
        assertThrows(IllegalArgumentException.class,
                () -> PriceBreakdown.of(Money.of(100), Money.of(200)));
    }

    @Test
    void offerCreatesSubscriptionFromItsData() {
        SubscriptionOffer offer = SubscriptionOffer.of(VIP, List.of(TRAINER), CARD);
        Subscription s = offer.subscribe(7, member(true), START);

        ContractTerms terms = s.contractTerms();
        assertEquals(7, terms.subscriptionId());
        assertEquals("VIP", terms.planCode());
        assertEquals(List.of("Персональний тренер"), terms.addOnTitles());
        assertEquals(Money.of(20160), terms.price().finalCost());
    }
}
