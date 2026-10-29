package ua.chnu.kpz.fitness.application.port;

import ua.chnu.kpz.fitness.domain.ContractTerms;

/**
 * Порт сповіщень (GRASP Protected Variations): зовнішній push-API, SMS чи email
 * можна замінити, не змінюючи ні домен, ні контролер.
 */
public interface SubscriptionNotifier {

    void subscriptionActivated(ContractTerms terms);
}
