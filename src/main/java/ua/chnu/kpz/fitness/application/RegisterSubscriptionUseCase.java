package ua.chnu.kpz.fitness.application;

import java.util.Objects;
import ua.chnu.kpz.fitness.application.port.SubscriptionNotifier;
import ua.chnu.kpz.fitness.application.port.SubscriptionRepository;
import ua.chnu.kpz.fitness.domain.ContractTerms;
import ua.chnu.kpz.fitness.domain.Subscription;

/**
 * GRASP Controller (Use-Case Controller / Application Service) для системної операції
 * «оформити абонемент».
 *
 * <p>Перший об'єкт за межами CLI: приймає DTO, отримує доменний об'єкт, делегує йому
 * бізнес-операцію ({@link Subscription#activate()}), зберігає результат і ініціює
 * сповіщення. Не містить ні обчислень, ні консольного вводу/виводу.
 */
public final class RegisterSubscriptionUseCase {

    private final SubscriptionFactory factory;
    private final SubscriptionRepository repository;
    private final SubscriptionNotifier notifier;

    public RegisterSubscriptionUseCase(SubscriptionFactory factory,
                                       SubscriptionRepository repository,
                                       SubscriptionNotifier notifier) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.notifier = Objects.requireNonNull(notifier, "notifier");
    }

    public SubscriptionReceipt register(RegisterSubscriptionCommand command) {
        Subscription subscription = factory.create(command);
        subscription.activate();
        repository.save(subscription);
        ContractTerms terms = subscription.contractTerms();
        notifier.subscriptionActivated(terms);
        return SubscriptionReceipt.from(terms);
    }
}
