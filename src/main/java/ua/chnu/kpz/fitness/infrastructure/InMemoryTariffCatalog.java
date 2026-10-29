package ua.chnu.kpz.fitness.infrastructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import ua.chnu.kpz.fitness.application.port.TariffCatalog;
import ua.chnu.kpz.fitness.domain.AddOn;
import ua.chnu.kpz.fitness.domain.MembershipPlan;
import ua.chnu.kpz.fitness.domain.SubscriptionOffer;
import ua.chnu.kpz.fitness.domain.discount.DiscountPolicy;

/**
 * Каталог тарифів у пам'яті. Пошук за кодом нечутливий до регістру
 * (як {@code equalsIgnoreCase} у legacy-коді), але невідомий код — помилка,
 * а не «мовчазний» тариф Full (див. ADR-0004).
 */
public final class InMemoryTariffCatalog implements TariffCatalog {

    private final Map<String, MembershipPlan> plans = new LinkedHashMap<>();
    private final Map<String, AddOn> addOns = new LinkedHashMap<>();
    private final DiscountPolicy discountPolicy;

    public InMemoryTariffCatalog(List<MembershipPlan> plans, List<AddOn> addOns, DiscountPolicy discountPolicy) {
        this.discountPolicy = Objects.requireNonNull(discountPolicy, "discountPolicy");
        for (MembershipPlan plan : plans) {
            this.plans.put(key(plan.code()), plan);
        }
        for (AddOn addOn : addOns) {
            this.addOns.put(key(addOn.code()), addOn);
        }
    }

    @Override
    public SubscriptionOffer offerFor(String planCode, List<String> addOnCodes) {
        MembershipPlan plan = plans.get(key(planCode));
        if (plan == null) {
            throw new IllegalArgumentException("Невідомий тип абонемента: '" + planCode
                    + "'. Доступні: " + String.join(", ", planCodes()));
        }
        List<AddOn> chosen = new ArrayList<>();
        for (String code : addOnCodes) {
            AddOn addOn = addOns.get(key(code));
            if (addOn == null) {
                throw new IllegalArgumentException("Невідома послуга: '" + code + "'");
            }
            chosen.add(addOn);
        }
        return SubscriptionOffer.of(plan, chosen, discountPolicy);
    }

    public List<String> planCodes() {
        return plans.values().stream().map(MembershipPlan::code).toList();
    }

    private static String key(String code) {
        return code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
    }
}
