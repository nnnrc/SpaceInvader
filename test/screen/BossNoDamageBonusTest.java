package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import audio.AudioManager;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.Boss;
import entity.Ship;
import hud.FloatingScoreHud;

/**
 * Checks the boss no-damage bonus: defeating a boss without losing a life
 * after it appeared gives half of its points once and shows "NO DAMAGE!".
 */
class BossNoDamageBonusTest {

	/** Mute state before the test, restored afterwards. */
	private boolean oldMute;

	@BeforeEach
	void muteAudio() {
		this.oldMute = AudioManager.isMuted();
		AudioManager.setMuted(true);
	}

	@AfterEach
	void restoreAudio() {
		AudioManager.stopBGM();
		AudioManager.setMuted(this.oldMute);
	}

	@Test
	void bossAGivesHalfItsPointsAsABonus() throws Exception {
		assertEquals(250, Boss.BossType.BOSS_A.getPointValue() / 2);
		assertBonusOnEveryStageOf(Boss.BossType.BOSS_A);
	}

	@Test
	void bossBGivesHalfItsPointsAsABonus() throws Exception {
		assertEquals(400, Boss.BossType.BOSS_B.getPointValue() / 2);
		assertBonusOnEveryStageOf(Boss.BossType.BOSS_B);
	}

	@Test
	void losingALifeAfterTheBossAppearsCancelsTheBonus() throws Exception {
		GameScreen game = bossStage(GameScreen.BOSS_LEVELS[0]);
		Boss boss = boss(game);
		assertTrue(game.damagePlayer(1, "Test hit"));
		defeat(game, boss);

		assertEquals(boss.getType().getPointValue(),
				game.getGameState().getScore());
		assertFalse(hud(game).isShowing(label(boss)));
	}

	@Test
	void hitBlockedByTheChameleonHullKeepsTheBonus() throws Exception {
		GameScreen game = bossStage(GameScreen.BOSS_LEVELS[0]);
		Boss boss = boss(game);
		ship(game).activateChameleonHull();
		assertFalse(game.damagePlayer(1, "Blocked hit"));
		defeat(game, boss);

		int points = boss.getType().getPointValue();
		assertEquals(points + points / 2, game.getGameState().getScore());
	}

	@Test
	void bonusIsGivenOnlyOncePerBoss() throws Exception {
		GameScreen game = bossStage(GameScreen.BOSS_LEVELS[0]);
		Boss boss = boss(game);
		defeat(game, boss);
		int scoreAfterDefeat = game.getGameState().getScore();
		// More hits on the defeated boss must not give anything again.
		defeat(game, boss);

		int points = boss.getType().getPointValue();
		assertEquals(points + points / 2, scoreAfterDefeat);
		assertEquals(scoreAfterDefeat, game.getGameState().getScore());
	}

	/**
	 * Defeats the boss on every stage that has this boss type, without
	 * losing a life, and checks the score and the label.
	 *
	 * @param type
	 *            Boss type to check.
	 */
	private static void assertBonusOnEveryStageOf(final Boss.BossType type)
			throws Exception {
		boolean found = false;
		for (int level : GameScreen.BOSS_LEVELS) {
			if (GameScreen.bossTypeForLevel(level) != type)
				continue;
			found = true;
			GameScreen game = bossStage(level);
			Boss boss = boss(game);
			defeat(game, boss);

			int points = type.getPointValue();
			assertEquals(points + points / 2, game.getGameState().getScore(),
					"Stage " + level);
			assertTrue(hud(game).isShowing(label(boss)), "Stage " + level);
		}
		assertTrue(found, "a stage has " + type);
	}

	/**
	 * Creates a running boss stage that accepts input at once.
	 *
	 * @param level
	 *            Boss stage to play.
	 * @return The stage.
	 */
	private static GameScreen bossStage(final int level) {
		GameScreen screen = new GameScreen(new GameState(level, 0, 3, 0, 0),
				Arrays.asList(new GameSettings(1, 1, 60, 2000)), false, 448,
				520, 60);
		screen.initialize();
		screen.inputDelay = Core.getCooldown(0);
		screen.isRunning = true;
		return screen;
	}

	private static Boss boss(final GameScreen game) throws Exception {
		Method getBossOnScreen = GameScreen.class
				.getDeclaredMethod("getBossOnScreen");
		getBossOnScreen.setAccessible(true);
		Boss boss = (Boss) getBossOnScreen.invoke(game);
		assertNotNull(boss, "the boss appeared");
		return boss;
	}

	/**
	 * Deals enough damage to defeat the boss in one hit.
	 *
	 * @param game
	 *            Stage with the boss.
	 * @param boss
	 *            Boss to defeat.
	 */
	private static void defeat(final GameScreen game, final Boss boss)
			throws Exception {
		Method damageBoss = GameScreen.class.getDeclaredMethod("damageBoss",
				int.class);
		damageBoss.setAccessible(true);
		damageBoss.invoke(game, boss.getMaxHealth());
		assertTrue(boss.isDefeated());
	}

	private static String label(final Boss boss) {
		return GameScreen.NO_DAMAGE_LABEL + " +"
				+ boss.getType().getPointValue() / 2;
	}

	private static Ship ship(final GameScreen game) throws Exception {
		return (Ship) read(game, "ship");
	}

	private static FloatingScoreHud hud(final GameScreen game)
			throws Exception {
		return (FloatingScoreHud) read(game, "floatingScoreHud");
	}

	private static Object read(final Object target, final String name)
			throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(target);
	}
}
