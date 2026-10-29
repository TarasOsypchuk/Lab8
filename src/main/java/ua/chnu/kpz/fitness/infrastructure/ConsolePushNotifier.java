package ua.chnu.kpz.fitness.infrastructure;

import java.io.PrintStream;
import java.util.Objects;
import ua.chnu.kpz.fitness.application.port.SubscriptionNotifier;
import ua.chnu.kpz.fitness.domain.ContractTerms;

/** Адаптер push-сповіщень (імітація зовнішнього API виводом у консоль). */
public final class ConsolePushNotifier implements SubscriptionNotifier {

    private final PrintStream out;

    public ConsolePushNotifier(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    public void subscriptionActivated(ContractTerms terms) {
        out.println("[Push Notification] Повідомлення надіслано на пристрій клієнта "
                + terms.memberName() + " (" + terms.memberPhone() + "): абонемент #"
                + terms.subscriptionId() + " активовано з " + terms.startDate() + ".");
    }
}
