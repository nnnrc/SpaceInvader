package entity.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BuffStatusTest {

    private static final double DELTA = 0.0001;

    private static BuffStatus status(final long remaining, final long total) {
        return new BuffStatus("Hyperfire", remaining, total,
                BuffStatus.Kind.BUFF);
    }

    @Test
    void newEffectHasAFullGauge() {
        assertEquals(1.0, status(5000, 5000).progress(), DELTA);
    }

    @Test
    void effectHalfwayThroughHasAHalfGauge() {
        assertEquals(0.5, status(2500, 5000).progress(), DELTA);
    }

    @Test
    void finishedEffectHasAnEmptyGauge() {
        assertEquals(0.0, status(0, 5000).progress(), DELTA);
    }

    @Test
    void negativeTimeLeftIsTreatedAsEmpty() {
        assertEquals(0.0, status(-100, 5000).progress(), DELTA);
    }

    @Test
    void zeroDurationDoesNotDivideByZero() {
        assertEquals(0.0, status(100, 0).progress(), DELTA);
    }
}
