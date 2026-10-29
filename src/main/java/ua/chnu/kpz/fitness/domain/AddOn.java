package ua.chnu.kpz.fitness.domain;

import java.util.Objects;

/**
 * Первинна (додаткова) послуга до абонемента, напр. персональний тренер.
 * Незмінний Value Object. У legacy-коді це був булевий прапорець
 * {@code personalTrainerIncluded} і «магічне число» 3000.0 у сервісі.
 */
public record AddOn(String code, String title, Money price) {

    public AddOn {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Код послуги не може бути порожнім");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Назва послуги не може бути порожньою");
        }
        Objects.requireNonNull(price, "price");
        if (price.isNegative()) {
            throw new IllegalArgumentException("Ціна послуги не може бути від'ємною");
        }
    }
}
