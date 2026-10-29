package ua.chnu.kpz.fitness.cli;

import java.util.List;
import ua.chnu.kpz.fitness.application.SubscriptionReceipt;

/**
 * GRASP Pure Fabrication: формує текст договору для консолі.
 * Форматування — відповідальність представлення, тому винесене з домену й контролера.
 */
public final class ContractFormatter {

    public String format(SubscriptionReceipt receipt) {
        List<String> addOns = receipt.addOns();
        return "Договір #" + receipt.subscriptionId() + " з " + receipt.memberName()
                + "\nТариф: " + receipt.planCode() + " — " + receipt.planTitle()
                + ", " + receipt.months() + " міс."
                + "\nДодаткові послуги: " + (addOns.isEmpty() ? "немає" : String.join(", ", addOns))
                + "\nДата початку: " + receipt.startDate()
                + "\nБазова вартість: " + receipt.baseAmount().toPlainString() + " грн"
                + "\nЗнижка: " + receipt.discountAmount().toPlainString() + " грн"
                + "\nДо сплати: " + receipt.finalCost().toPlainString() + " грн"
                + "\nСтатус: " + receipt.status();
    }
}
