package entity.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import entity.Ship;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuffManagerTest {

    private static final long START = 1000;

    @Test
    void newBuffStartsWithItsFullDuration() {
        BuffManager manager = new BuffManager();
        Hyperfire hyperfire = new Hyperfire(100, 100);

        manager.addBuff(hyperfire, new Ship(100, 100), START);
        List<BuffStatus> statuses = manager.getActiveBuffStatuses(START);

        assertEquals(1, statuses.size());
        BuffStatus status = statuses.get(0);
        assertEquals("Hyperfire", status.name());
        assertEquals(hyperfire.getDuration(), status.remainingMillis());
        assertEquals(hyperfire.getDuration(), status.totalMillis());
        assertEquals(BuffStatus.Kind.BUFF, status.kind());
    }

    @Test
    void timeLeftGoesDownAsTimePasses() {
        BuffManager manager = new BuffManager();
        Hyperfire hyperfire = new Hyperfire(100, 100);
        long half = hyperfire.getDuration() / 2;

        manager.addBuff(hyperfire, new Ship(100, 100), START);
        BuffStatus status = manager.getActiveBuffStatuses(START + half).get(0);

        assertEquals(hyperfire.getDuration() - half, status.remainingMillis());
    }

    @Test
    void pickingUpTheSameBuffAgainRestartsItsTimer() {
        BuffManager manager = new BuffManager();
        Ship ship = new Ship(100, 100);
        Hyperfire first = new Hyperfire(100, 100);
        Hyperfire second = new Hyperfire(100, 100);
        long half = first.getDuration() / 2;

        manager.addBuff(first, ship, START);
        manager.addBuff(second, ship, START + half);
        List<BuffStatus> statuses =
                manager.getActiveBuffStatuses(START + half);

        assertEquals(1, statuses.size());
        assertEquals(second.getDuration(), statuses.get(0).remainingMillis());
    }
}
