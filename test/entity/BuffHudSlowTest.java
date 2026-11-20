package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.item.BuffHud;
import entity.item.BuffManager;
import entity.item.BuffStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks the Slowed entry in the buff HUD; lives in entity to use a fake clock. */
class BuffHudSlowTest {

    private static final long START = 1000000L;

    /** Lets the test move time forward without waiting. */
    private static final class FakeClockStatus extends PlayerStatus {
        private long now = START;

        private FakeClockStatus() {
            super(3, 3, true);
        }

        @Override
        long currentTimeMillis() {
            return this.now;
        }

        private void advance(final long milliseconds) {
            this.now += milliseconds;
        }
    }

    @Test
    void slowedPlayerGetsADebuffEntry() {
        FakeClockStatus status = new FakeClockStatus();
        status.applySlow(new Ship(100, 100));
        status.advance(500);

        List<BuffStatus> statuses =
                BuffHud.collect(new BuffManager(), status, status.now);

        assertEquals(1, statuses.size());
        BuffStatus slowed = statuses.get(0);
        assertEquals("Slowed", slowed.name());
        assertEquals(BuffStatus.Kind.DEBUFF, slowed.kind());
        assertEquals(1500, slowed.remainingMillis());
        assertEquals(PlayerStatus.getSlowDuration(), slowed.totalMillis());
    }

    @Test
    void playerWhoIsNotSlowedGetsNoEntry() {
        FakeClockStatus status = new FakeClockStatus();

        assertTrue(BuffHud.collect(new BuffManager(), status, status.now)
                .isEmpty());
    }

    @Test
    void slowedEntryDisappearsWhenTheSlowEnds() {
        FakeClockStatus status = new FakeClockStatus();
        status.applySlow(new Ship(100, 100));
        status.advance(PlayerStatus.getSlowDuration());

        assertTrue(BuffHud.collect(new BuffManager(), status, status.now)
                .isEmpty());
    }
}
