package ua.chnu.kpz.fitness.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalizesScaleSoEqualAmountsAreEqual() {
        assertEquals(Money.of("100"), Money.of("100.000"));
        assertEquals("100.00", Money.of(100).toString());
    }

    @Test
    void arithmeticIsExactUnlikeDouble() {
        // у double: 1199.99 * 12 = 14399.880000000001
        assertEquals(Money.of("14399.88"), Money.of("1199.99").times(12));
        assertEquals(Money.of("10368.00"), Money.of(14400).times(new BigDecimal("0.8")).times(new BigDecimal("0.9")));
    }

    @Test
    void plusMinusAndNegative() {
        assertEquals(Money.of(150), Money.of(100).plus(Money.of(50)));
        assertTrue(Money.of(10).minus(Money.of(20)).isNegative());
    }
}
