package entity.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class BuffHudTest {

    /** A time in the shown half of the blink: (0 / 250) % 2 == 0. */
    private static final long SHOWN_PHASE = 0;
    /** A time in the hidden half of the blink: (250 / 250) % 2 == 1. */
    private static final long HIDDEN_PHASE = 250;

    private static BuffStatus buff(final String name, final long remaining) {
        return new BuffStatus(name, remaining, 10000, BuffStatus.Kind.BUFF);
    }

    @Test
    void nothingIsShownWhenNoEffectIsActive() {
        assertTrue(BuffHud.visible(List.of(), SHOWN_PHASE).isEmpty());
    }

    @Test
    void effectEndingSoonestIsListedFirst() {
        List<BuffStatus> shown = BuffHud.visible(
                List.of(buff("Hyperfire", 8000), buff("Twin Cannon", 4000)),
                SHOWN_PHASE);

        assertEquals("Twin Cannon", shown.get(0).name());
        assertEquals("Hyperfire", shown.get(1).name());
    }

    @Test
    void effectInItsLastTwoSecondsBlinks() {
        List<BuffStatus> ending = List.of(buff("Hyperfire", 1500));

        assertEquals(1, BuffHud.visible(ending, SHOWN_PHASE).size());
        assertTrue(BuffHud.visible(ending, HIDDEN_PHASE).isEmpty());
    }

    @Test
    void effectWithTimeLeftDoesNotBlink() {
        List<BuffStatus> steady = List.of(buff("Hyperfire", 5000));

        assertEquals(1, BuffHud.visible(steady, HIDDEN_PHASE).size());
    }

    @Test
    void shortEffectDoesNotBlinkFromTheStart() {
        BuffStatus slow = new BuffStatus("Slowed", 1500, 2000,
                BuffStatus.Kind.DEBUFF);

        assertEquals(1, BuffHud.visible(List.of(slow), HIDDEN_PHASE).size());
    }
}
