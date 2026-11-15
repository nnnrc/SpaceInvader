package boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import entity.Boss;

/**
 * Checks the health ratio used to draw the boss health bar.
 */
class BossHudInfoTest {

    @Test
    void newBossHasAFullHealthBar() {
        Boss boss = new Boss(0, 0, Boss.BossType.BOSS_A);
        BossHudInfo hudInfo = new BossHudInfo(boss);

        assertEquals(1.0, hudInfo.getHealthRatio(), 0.0001);
        assertTrue(hudInfo.isVisible());
    }

    @Test
    void healthBarShrinksWhenTheBossTakesDamage() {
        Boss boss = new Boss(0, 0, Boss.BossType.BOSS_A);
        boss.takeDamage(boss.getMaxHealth() / 2);
        BossHudInfo hudInfo = new BossHudInfo(boss);

        assertEquals(0.5, hudInfo.getHealthRatio(), 0.01);
    }

    @Test
    void defeatedBossHasAnEmptyHiddenHealthBar() {
        Boss boss = new Boss(0, 0, Boss.BossType.BOSS_B);
        boss.takeDamage(boss.getMaxHealth());
        BossHudInfo hudInfo = new BossHudInfo(boss);

        assertEquals(0.0, hudInfo.getHealthRatio(), 0.0001);
        assertFalse(hudInfo.isVisible());
    }
}
