package ua.chnu.kpz.fitness.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import ua.chnu.kpz.fitness.domain.pricing.MultiplierPricing;
import ua.chnu.kpz.fitness.domain.pricing.RegularPricing;
import ua.chnu.kpz.fitness.domain.pricing.SurchargePricing;

class MembershipPlanTest {

    private static final Money RATE = Money.of(1200);

    @Test
    void eachPricingStrategyProducesItsOwnTermPrice() {
        assertEquals(Money.of(14400), MembershipPlan.of("Full", "Full", RATE, 12, new RegularPricing()).termPrice());
        assertEquals(Money.of(11520), MembershipPlan.of("Day", "Day", RATE, 12,
                new MultiplierPricing(new BigDecimal("0.80"))).termPrice());
        assertEquals(Money.of(19400), MembershipPlan.of("VIP", "VIP", RATE, 12,
                new SurchargePricing(Money.of(5000))).termPrice());
    }

    @Test
    void newPlanNeedsOnlyNewStrategy() {
        // Protected Variations: «студентський» тариф без жодних змін у MembershipPlan
        MembershipPlan student = MembershipPlan.of("Student", "Студентський", RATE, 12,
                termPrice -> termPrice.times(new BigDecimal("0.5")));
        assertEquals(Money.of(7200), student.termPrice());
    }

    @Test
    void rejectsInvalidPlan() {
        assertThrows(IllegalArgumentException.class,
                () -> MembershipPlan.of("Full", "Full", RATE, 0, new RegularPricing()));
        assertThrows(IllegalArgumentException.class,
                () -> MembershipPlan.of("", "Full", RATE, 12, new RegularPricing()));
        assertThrows(IllegalArgumentException.class,
                () -> MembershipPlan.of("Full", "Full", Money.ZERO, 12, new RegularPricing()));
    }
}
