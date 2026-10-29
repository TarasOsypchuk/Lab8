package ua.chnu.kpz.fitness.application;

import java.util.Objects;
import java.util.function.IntSupplier;
import ua.chnu.kpz.fitness.application.port.TariffCatalog;
import ua.chnu.kpz.fitness.domain.Member;
import ua.chnu.kpz.fitness.domain.Subscription;
import ua.chnu.kpz.fitness.domain.SubscriptionOffer;

/**
 * Збирає агрегат {@link Subscription} з DTO-команди.
 *
 * <p>GRASP Pure Fabrication: у предметній області «фабрики» немає, клас існує, щоб
 * контролер лишався тонким. Саме створення делеговано Creator-ам домену:
 * {@link Member#register} і {@link SubscriptionOffer#subscribe}.
 * Генератори ідентифікаторів передаються як {@link IntSupplier} (у реальній системі —
 * sequence БД).
 */
public final class SubscriptionFactory {

    private final TariffCatalog catalog;
    private final IntSupplier memberIds;
    private final IntSupplier subscriptionIds;

    public SubscriptionFactory(TariffCatalog catalog, IntSupplier memberIds, IntSupplier subscriptionIds) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.memberIds = Objects.requireNonNull(memberIds, "memberIds");
        this.subscriptionIds = Objects.requireNonNull(subscriptionIds, "subscriptionIds");
    }

    public Subscription create(RegisterSubscriptionCommand command) {
        Objects.requireNonNull(command, "command");
        SubscriptionOffer offer = catalog.offerFor(command.planCode(), command.addOnCodes());
        Member member = Member.register(memberIds.getAsInt(), command.memberName(),
                command.phone(), command.hasDiscountCard());
        return offer.subscribe(subscriptionIds.getAsInt(), member, command.startDate());
    }
}
