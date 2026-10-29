package variant3_fitnessmembership;

import java.util.Scanner;

// LEGACY-КОД ВАРІАНТА 3 (без змін логіки).
// Опис предметної області: Мережа фітнес-клубів та продаж клубних карт.
// Завдання коду: Оформлення та розрахунок вартості абонементу — розрахунок ціни залежно від типу пакету (Day, Full, VIP), застосування дисконтної карти, додавання первинних послуг, формування тексту договору, запис у базу даних та надсилання пуш-сповіщення.

public class LegacyFitnessService {
    public void registerSubscriptionConsole() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Реєстрація абонемента фітнес-клубу ===");
        System.out.print("Введіть ім'я клієнта: ");
        String name = scanner.nextLine();
        System.out.print("Введіть тип абонемента (Day/Full/VIP): ");
        String planType = scanner.nextLine();

        Member member = new Member();
        member.id = 42; member.name = name; member.phone = "+380509998877"; member.hasDiscountCard = true;

        MembershipPlan plan = new MembershipPlan();
        plan.code = planType; plan.title = "Річний абонемент"; plan.baseMonthlyRate = 1200.0; plan.monthsDuration = 12;

        Subscription sub = new Subscription();
        sub.id = 1002;
        sub.member = member;
        sub.plan = plan;
        sub.personalTrainerIncluded = true;
        sub.startDate = "2026-11-01";
        sub.status = "Draft";

        double rawCost = sub.plan.baseMonthlyRate * sub.plan.monthsDuration;

        if ("VIP".equalsIgnoreCase(sub.plan.code)) {
            rawCost += 5000.0;
        } else if ("Day".equalsIgnoreCase(sub.plan.code)) {
            rawCost *= 0.8;
        }

        if (sub.personalTrainerIncluded) {
            rawCost += 3000.0;
        }

        sub.baseAmount = rawCost;

        double discount = 0.0;
        if (sub.member.hasDiscountCard) {
            discount = sub.baseAmount * 0.10;
        }
        sub.discountAmount = discount;
        sub.finalCost = sub.baseAmount - sub.discountAmount;
        sub.status = "Active";

        String contract = "Договір #" + sub.id + " з " + sub.member.name +
                "\nТариф: " + sub.plan.code +
                "\nБазова вартість: " + sub.baseAmount + " грн" +
                "\nЗнижка: " + sub.discountAmount + " грн" +
                "\nДо сплати: " + sub.finalCost + " грн";
        System.out.println(contract);

        System.out.println("[База даних] Абонемент #" + sub.id + " додано до таблиці subscriptions.");
        System.out.println("[Push Notification] Повідомлення надіслано на пристрій клієнта " + sub.member.name);
    }
}
