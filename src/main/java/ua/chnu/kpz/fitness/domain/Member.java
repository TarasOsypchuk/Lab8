package ua.chnu.kpz.fitness.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Клієнт фітнес-клубу.
 *
 * <p>GRASP Creator / Static Factory Method: об'єкт створюється лише через
 * {@link #register}, який гарантує валідний стан (непорожнє ім'я, коректний телефон).
 * Поля {@code private final}, сеттерів немає — на відміну від legacy-класу з public-полями.
 */
public final class Member {

    private static final Pattern PHONE = Pattern.compile("\\+?\\d{10,13}");

    private final int id;
    private final String name;
    private final String phone;
    private final boolean discountCardHolder;

    private Member(int id, String name, String phone, boolean discountCardHolder) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.discountCardHolder = discountCardHolder;
    }

    public static Member register(int id, String name, String phone, boolean hasDiscountCard) {
        if (id <= 0) {
            throw new IllegalArgumentException("Ідентифікатор клієнта має бути додатним");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ім'я клієнта не може бути порожнім");
        }
        Objects.requireNonNull(phone, "phone");
        String normalizedPhone = phone.replaceAll("[\\s()-]", "");
        if (!PHONE.matcher(normalizedPhone).matches()) {
            throw new IllegalArgumentException("Некоректний номер телефону: " + phone);
        }
        return new Member(id, name.strip(), normalizedPhone, hasDiscountCard);
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String phone() {
        return phone;
    }

    public boolean hasDiscountCard() {
        return discountCardHolder;
    }
}
