package entity.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.DrawManager.SpriteType;
import org.junit.jupiter.api.Test;

class EnemyMovementTest {

    @Test
    void eachEnemyTypeGetsItsOwnMovement() {
        assertInstanceOf(ZigzagMovement.class,
                EnemyMovement.forSprite(SpriteType.EnemyShipA1, 1));
        assertInstanceOf(DiveMovement.class,
                EnemyMovement.forSprite(SpriteType.EnemyShipB1, 1));
        assertInstanceOf(HoverMovement.class,
                EnemyMovement.forSprite(SpriteType.EnemyShipC1, 1));
    }

    @Test
    void zigzagStaysWithinItsAmplitudeWhileFalling() {
        ZigzagMovement zigzag = new ZigzagMovement(1);
        int x = 0;
        int y = 0;
        for (int frame = 0; frame < 200; frame++) {
            EnemyMovement.Step step = zigzag.next();
            x += step.dx();
            y += step.dy();
            assertTrue(Math.abs(x) <= ZigzagMovement.AMPLITUDE);
        }
        assertTrue(y > 0);
    }

    @Test
    void diveFallsStraightAndFasterThanZigzag() {
        DiveMovement dive = new DiveMovement(2);
        ZigzagMovement zigzag = new ZigzagMovement(2);
        int diveY = 0;
        int zigzagY = 0;
        for (int frame = 0; frame < 60; frame++) {
            EnemyMovement.Step step = dive.next();
            assertEquals(0, step.dx());
            diveY += step.dy();
            zigzagY += zigzag.next().dy();
        }
        assertTrue(diveY > zigzagY);
    }

    @Test
    void hoverStopsFiresThenKeepsFalling() {
        HoverMovement hover = new HoverMovement(3);
        int fallen = 0;
        while (fallen < HoverMovement.HOVER_DISTANCE) {
            assertFalse(hover.firesThisFrame());
            fallen += hover.next().dy();
        }

        int shots = 0;
        for (int frame = 0; frame < HoverMovement.HOVER_FRAMES; frame++) {
            assertEquals(new EnemyMovement.Step(0, 0), hover.next());
            if (hover.firesThisFrame()) {
                shots++;
            }
        }
        assertEquals(HoverMovement.HOVER_FRAMES / HoverMovement.FIRE_INTERVAL, shots);

        assertTrue(hover.next().dy() > 0);
        assertFalse(hover.firesThisFrame());
    }
}
