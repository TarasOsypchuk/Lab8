package ua.chnu.kpz.fitness.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void registersValidMemberAndNormalizesInput() {
        Member member = Member.register(1, "  Олена  ", "+38 (067) 123-45-67", true);
        assertEquals("Олена", member.name());
        assertEquals("+380671234567", member.phone());
    }

    @Test
    void rejectsBlankNameInvalidPhoneAndNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () -> Member.register(1, " ", "+380671234567", false));
        assertThrows(IllegalArgumentException.class, () -> Member.register(1, "Іван", "123", false));
        assertThrows(IllegalArgumentException.class, () -> Member.register(0, "Іван", "+380671234567", false));
    }
}
