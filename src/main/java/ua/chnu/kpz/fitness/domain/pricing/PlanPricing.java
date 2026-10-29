package ua.chnu.kpz.fitness.domain.pricing;

import ua.chnu.kpz.fitness.domain.Money;

/**
 * Стратегія тарифного ціноутворення.
 *
 * <p>GRASP Polymorphism: замість {@code if/else} за кодом тарифу кожен тариф отримує свою
 * реалізацію. GRASP Protected Variations: стабільний контракт, за яким сховано
 * нестабільний алгоритм (нові тарифи «Student», «Weekend» додаються новим класом
 * без зміни {@code MembershipPlan}, {@code Subscription} чи контролера).
 */
@FunctionalInterface
public interface PlanPricing {

    /**
     * @param termPrice ставка × кількість місяців
     * @return вартість тарифу за термін після тарифної модифікації
     */
    Money apply(Money termPrice);
}
