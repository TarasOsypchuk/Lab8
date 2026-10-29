package ua.chnu.kpz.fitness.infrastructure;

import java.math.BigDecimal;
import java.util.List;
import ua.chnu.kpz.fitness.domain.AddOn;
import ua.chnu.kpz.fitness.domain.MembershipPlan;
import ua.chnu.kpz.fitness.domain.Money;
import ua.chnu.kpz.fitness.domain.discount.DiscountCardPolicy;
import ua.chnu.kpz.fitness.domain.pricing.MultiplierPricing;
import ua.chnu.kpz.fitness.domain.pricing.RegularPricing;
import ua.chnu.kpz.fitness.domain.pricing.SurchargePricing;

/**
 * Конфігурація тарифів клубу (частина Composition Root).
 *
 * <p>Єдине місце, де «живуть» числа з legacy-коду: 1200 грн/міс × 12 міс,
 * Day ×0.8, VIP +5000, тренер +3000, дисконтна карта −10%.
 * Зміна цін — зміна лише цього класу (або заміна його на читання з файлу/БД).
 */
public final class DefaultTariffs {

    public static final String TRAINER = "TRAINER";

    private static final Money MONTHLY_RATE = Money.of(1200);
    private static final int MONTHS = 12;

    private DefaultTariffs() {
    }

    public static InMemoryTariffCatalog catalog() {
        List<MembershipPlan> plans = List.of(
                MembershipPlan.of("Day", "Річний абонемент (денний)", MONTHLY_RATE, MONTHS,
                        new MultiplierPricing(new BigDecimal("0.80"))),
                MembershipPlan.of("Full", "Річний абонемент (повний)", MONTHLY_RATE, MONTHS,
                        new RegularPricing()),
                MembershipPlan.of("VIP", "Річний абонемент VIP", MONTHLY_RATE, MONTHS,
                        new SurchargePricing(Money.of(5000))));
        List<AddOn> addOns = List.of(
                new AddOn(TRAINER, "Персональний тренер", Money.of(3000)));
        return new InMemoryTariffCatalog(plans, addOns, new DiscountCardPolicy(new BigDecimal("0.10")));
    }
}
