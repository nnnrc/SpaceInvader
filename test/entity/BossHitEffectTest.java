package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;

import org.junit.jupiter.api.Test;

/**
 * Checks the hit effect of the boss: a short white flash and shake that
 * does not change its position, hitbox or damage.
 */
class BossHitEffectTest {

    /** Play area wide enough that the boss never reaches a border. */
    private static final int LEFT_LIMIT = 0;
    private static final int RIGHT_LIMIT = 10_000;

    @Test
    void bossFlashesWhiteAndShakesWhenHit() {
        Boss boss = new Boss(500, 50, Boss.BossType.BOSS_A);
        assertFalse(boss.isHitEffectActive());
        assertEquals(0, boss.getHitShakeOffset());

        boss.takeDamage(10);

        assertTrue(boss.isHitEffectActive());
        assertEquals(Color.WHITE, boss.getColor());
        assertNotEquals(0, boss.getHitShakeOffset());
    }

    @Test
    void hitEffectEndsAfterAFewFrames() {
        Boss boss = new Boss(500, 50, Boss.BossType.BOSS_A);
        boss.takeDamage(10);

        for (int frame = 0; frame < 6; frame++)
            boss.update(LEFT_LIMIT, RIGHT_LIMIT);

        assertFalse(boss.isHitEffectActive());
        assertEquals(Color.MAGENTA, boss.getColor());
        assertEquals(0, boss.getHitShakeOffset());
    }

    @Test
    void hitEffectDoesNotChangePositionOrDamage() {
        Boss hitBoss = new Boss(500, 50, Boss.BossType.BOSS_A);
        Boss otherBoss = new Boss(500, 50, Boss.BossType.BOSS_A);

        hitBoss.takeDamage(10);
        hitBoss.update(LEFT_LIMIT, RIGHT_LIMIT);
        otherBoss.update(LEFT_LIMIT, RIGHT_LIMIT);

        assertEquals(otherBoss.getPositionX(), hitBoss.getPositionX());
        assertEquals(otherBoss.getPositionY(), hitBoss.getPositionY());
        assertEquals(otherBoss.getWidth(), hitBoss.getWidth());
        assertEquals(hitBoss.getMaxHealth() - 10, hitBoss.getCurrentHealth());
    }

    @Test
    void defeatedBossHasNoHitEffect() {
        Boss boss = new Boss(500, 50, Boss.BossType.BOSS_B);
        boss.takeDamage(boss.getMaxHealth());

        assertTrue(boss.isDefeated());
        assertFalse(boss.isHitEffectActive());
        assertEquals(0, boss.getHitShakeOffset());
    }
}
