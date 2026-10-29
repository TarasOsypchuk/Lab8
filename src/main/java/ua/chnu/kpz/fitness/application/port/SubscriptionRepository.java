package ua.chnu.kpz.fitness.application.port;

import java.util.Optional;
import ua.chnu.kpz.fitness.domain.Subscription;

/**
 * Порт сховища абонементів (GRASP Indirection / Pure Fabrication / Protected Variations).
 * Контролер не знає, чи це in-memory колекція, JDBC чи JPA.
 */
public interface SubscriptionRepository {

    void save(Subscription subscription);

    Optional<Subscription> findById(int id);
}
