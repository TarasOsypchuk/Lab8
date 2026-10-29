package ua.chnu.kpz.fitness.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import ua.chnu.kpz.fitness.application.port.SubscriptionNotifier;
import ua.chnu.kpz.fitness.application.port.SubscriptionRepository;
import ua.chnu.kpz.fitness.domain.ContractTerms;
import ua.chnu.kpz.fitness.domain.Subscription;
import ua.chnu.kpz.fitness.infrastructure.DefaultTariffs;

/** Контролер тестується з фейковими портами — це можливо завдяки Indirection. */
class RegisterSubscriptionUseCaseTest {

    private static final class FakeRepository implements SubscriptionRepository {
        final Map<Integer, Subscription> saved = new HashMap<>();

        @Override
        public void save(Subscription subscription) {
            saved.put(subscription.id(), subscription);
        }

        @Override
        public Optional<Subscription> findById(int id) {
            return Optional.ofNullable(saved.get(id));
        }
    }

    private static final class FakeNotifier implements SubscriptionNotifier {
        final List<ContractTerms> sent = new ArrayList<>();

        @Override
        public void subscriptionActivated(ContractTerms terms) {
            sent.add(terms);
        }
    }

    private final FakeRepository repository = new FakeRepository();
    private final FakeNotifier notifier = new FakeNotifier();
    private final AtomicInteger ids = new AtomicInteger(1000);
    private final RegisterSubscriptionUseCase useCase = new RegisterSubscriptionUseCase(
            new SubscriptionFactory(DefaultTariffs.catalog(), () -> 42, ids::incrementAndGet),
            repository, notifier);

    private static RegisterSubscriptionCommand command(String plan, boolean card, List<String> addOns) {
        return new RegisterSubscriptionCommand("Олена", "+380671234567", card, plan, addOns,
                LocalDate.of(2026, 11, 1));
    }

    @Test
    void registersActivatesSavesAndNotifies() {
        SubscriptionReceipt receipt = useCase.register(command("VIP", true, List.of(DefaultTariffs.TRAINER)));

        assertEquals(1001, receipt.subscriptionId());
        assertEquals(new BigDecimal("20160.00"), receipt.finalCost());
        assertEquals("Активний", receipt.status());
        assertTrue(repository.findById(1001).orElseThrow().isActive());
        assertEquals(1, notifier.sent.size());
        assertEquals("+380671234567", notifier.sent.get(0).memberPhone());
    }

    @Test
    void unknownPlanIsRejectedAndNothingIsSaved() {
        assertThrows(IllegalArgumentException.class, () -> useCase.register(command("Gold", true, List.of())));
        assertTrue(repository.saved.isEmpty());
        assertTrue(notifier.sent.isEmpty());
    }

    @Test
    void unknownAddOnIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.register(command("Full", true, List.of("SAUNA"))));
    }
}
