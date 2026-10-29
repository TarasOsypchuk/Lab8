package ua.chnu.kpz.fitness.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ua.chnu.kpz.fitness.domain.discount.DiscountPolicy;

/**
 * Абонемент — корінь агрегату.
 *
 * <ul>
 *   <li><b>Information Expert</b>: абонемент знає свій тариф, послуги й клієнта, тому сам
 *       рахує {@link PriceBreakdown} і сам керує переходом статусу ({@link #activate()}).</li>
 *   <li><b>Creator</b>: створюється тільки статичним фабричним методом {@link #open},
 *       який перевіряє всі інваріанти — невалідний абонемент створити неможливо;
 *       абонемент сам створює свій {@link PriceBreakdown} (агрегує його).</li>
 *   <li>Інкапсуляція: усі поля {@code private}, сеттерів немає, список послуг —
 *       незмінна копія ({@link List#copyOf}).</li>
 * </ul>
 */
public final class Subscription {

    private final int id;
    private final Member member;
    private final MembershipPlan plan;
    private final List<AddOn> addOns;
    private final LocalDate startDate;
    private final PriceBreakdown price;
    private SubscriptionStatus status;

    private Subscription(int id, Member member, MembershipPlan plan, List<AddOn> addOns,
                         LocalDate startDate, PriceBreakdown price) {
        this.id = id;
        this.member = member;
        this.plan = plan;
        this.addOns = addOns;
        this.startDate = startDate;
        this.price = price;
        this.status = SubscriptionStatus.DRAFT;
    }

    /**
     * Static Factory Method: відкриває абонемент у статусі {@link SubscriptionStatus#DRAFT}
     * з уже розрахованою ціною.
     */
    public static Subscription open(int id, Member member, MembershipPlan plan, List<AddOn> addOns,
                                    LocalDate startDate, DiscountPolicy discountPolicy) {
        if (id <= 0) {
            throw new IllegalArgumentException("Номер абонемента має бути додатним");
        }
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(discountPolicy, "discountPolicy");
        List<AddOn> services = List.copyOf(addOns);
        Set<String> codes = new HashSet<>();
        for (AddOn addOn : services) {
            if (!codes.add(addOn.code())) {
                throw new IllegalArgumentException("Послугу " + addOn.title() + " додано двічі");
            }
        }

        Money base = plan.termPrice();
        for (AddOn addOn : services) {
            base = base.plus(addOn.price());
        }
        Money discount = discountPolicy.discountFor(member, base);

        return new Subscription(id, member, plan, services, startDate, PriceBreakdown.of(base, discount));
    }

    /** Активує абонемент. Повторна активація — порушення бізнес-правила. */
    public void activate() {
        if (status != SubscriptionStatus.DRAFT) {
            throw new IllegalStateException("Абонемент #" + id + " уже активовано");
        }
        status = SubscriptionStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    /** Незмінний знімок умов договору для зовнішніх шарів. */
    public ContractTerms contractTerms() {
        List<String> titles = addOns.stream().map(AddOn::title).toList();
        return new ContractTerms(id, member.name(), member.phone(), plan.code(), plan.title(),
                plan.months(), titles, startDate, price, status);
    }

    public int id() {
        return id;
    }

    public PriceBreakdown price() {
        return price;
    }

    public List<AddOn> addOns() {
        return addOns;
    }
}
