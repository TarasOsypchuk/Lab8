package ua.chnu.kpz.fitness.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import ua.chnu.kpz.fitness.domain.ContractTerms;

/**
 * DTO-результат use case, який контролер повертає в CLI.
 * CLI не бачить доменних сутностей — лише цей незмінний record.
 */
public record SubscriptionReceipt(
        int subscriptionId,
        String memberName,
        String planCode,
        String planTitle,
        int months,
        List<String> addOns,
        LocalDate startDate,
        BigDecimal baseAmount,
        BigDecimal discountAmount,
        BigDecimal finalCost,
        String status) {

    public SubscriptionReceipt {
        addOns = List.copyOf(addOns);
    }

    public static SubscriptionReceipt from(ContractTerms terms) {
        return new SubscriptionReceipt(
                terms.subscriptionId(),
                terms.memberName(),
                terms.planCode(),
                terms.planTitle(),
                terms.months(),
                terms.addOnTitles(),
                terms.startDate(),
                terms.price().baseAmount().amount(),
                terms.price().discountAmount().amount(),
                terms.price().finalCost().amount(),
                terms.status().label());
    }
}
