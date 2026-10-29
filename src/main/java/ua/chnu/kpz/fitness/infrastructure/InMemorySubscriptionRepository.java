package ua.chnu.kpz.fitness.infrastructure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ua.chnu.kpz.fitness.application.port.SubscriptionRepository;
import ua.chnu.kpz.fitness.domain.Subscription;

/**
 * GRASP Pure Fabrication: in-memory імітація таблиці {@code subscriptions}.
 * Замінюється на JDBC/JPA-реалізацію без змін у контролері та домені.
 */
public final class InMemorySubscriptionRepository implements SubscriptionRepository {

    private final Map<Integer, Subscription> table = new LinkedHashMap<>();
    private final AuditLogger audit;

    public InMemorySubscriptionRepository(AuditLogger audit) {
        this.audit = Objects.requireNonNull(audit, "audit");
    }

    @Override
    public void save(Subscription subscription) {
        Objects.requireNonNull(subscription, "subscription");
        table.put(subscription.id(), subscription);
        audit.record("[База даних] Абонемент #" + subscription.id() + " додано до таблиці subscriptions.");
    }

    @Override
    public Optional<Subscription> findById(int id) {
        return Optional.ofNullable(table.get(id));
    }
}
