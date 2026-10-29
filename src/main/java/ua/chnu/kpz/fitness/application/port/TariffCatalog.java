package ua.chnu.kpz.fitness.application.port;

import java.util.List;
import ua.chnu.kpz.fitness.domain.SubscriptionOffer;

/**
 * Порт каталогу тарифів (GRASP Indirection + Protected Variations).
 * Де зберігаються тарифи (код, файл, БД, адмін-панель) — деталь інфраструктури.
 */
public interface TariffCatalog {

    /**
     * @throws IllegalArgumentException якщо тариф або послуга невідомі
     */
    SubscriptionOffer offerFor(String planCode, List<String> addOnCodes);
}
