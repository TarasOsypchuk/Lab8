package ua.chnu.kpz.fitness.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Незмінний «знімок» умов договору, який видає {@link Subscription#contractTerms()}.
 *
 * <p>Дозволяє зовнішнім шарам (сповіщення, квитанція, договір) читати дані абонемента,
 * не отримуючи доступу до самої сутності й не порушуючи Закон Деметри
 * (звернення до полів record не є порушенням — див. примітку в завданні).
 */
public record ContractTerms(
        int subscriptionId,
        String memberName,
        String memberPhone,
        String planCode,
        String planTitle,
        int months,
        List<String> addOnTitles,
        LocalDate startDate,
        PriceBreakdown price,
        SubscriptionStatus status) {

    public ContractTerms {
        addOnTitles = List.copyOf(addOnTitles);
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(status, "status");
    }
}
