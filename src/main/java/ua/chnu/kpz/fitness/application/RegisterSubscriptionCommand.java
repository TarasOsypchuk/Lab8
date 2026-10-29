package ua.chnu.kpz.fitness.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * DTO-команда «оформити абонемент», яку CLI передає контролеру.
 * Містить лише «сирі» дані запиту без бізнес-логіки.
 */
public record RegisterSubscriptionCommand(
        String memberName,
        String phone,
        boolean hasDiscountCard,
        String planCode,
        List<String> addOnCodes,
        LocalDate startDate) {

    public RegisterSubscriptionCommand {
        Objects.requireNonNull(addOnCodes, "addOnCodes");
        addOnCodes = List.copyOf(addOnCodes);
    }
}
