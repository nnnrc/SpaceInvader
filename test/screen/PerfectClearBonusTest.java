package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import audio.AudioManager;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.FallingObstacle;
import entity.Ship;
import entity.ShipType;
import hud.FloatingScoreHud;

/**
 * Checks the Perfect Clear bonus: clearing a stage without losing a life
 * gives the bonus and shows "PERFECT!", and only a life that is really lost
 * cancels it.
 */
class PerfectClearBonusTest {

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
	void clearingWithoutLosingALifeGivesTheBonus() throws Exception {
		GameScreen game = stage(1, 3);
		clear(game);

		assertTrue(game.isStageCleared());
		assertEquals(GameScreen.PERFECT_CLEAR_BONUS,
				game.getGameState().getScore());
		assertTrue(hud(game).isShowing(GameScreen.PERFECT_CLEAR_LABEL));
	}

	@Test
	void losingALifeCancelsTheBonus() throws Exception {
		GameScreen game = stage(1, 3);
		assertTrue(game.damagePlayer(1, "Test hit"));
		clear(game);

		assertTrue(game.isStageCleared());
		assertEquals(0, game.getGameState().getScore());
		assertFalse(hud(game).isShowing(GameScreen.PERFECT_CLEAR_LABEL));
	}

	@Test
	void hitBlockedByTheShieldKeepsTheBonus() throws Exception {
		GameScreen game = stage(1, 3);
		// Only the Heavy Tank can hold the Energy Shield.
		Ship tank = new Ship(ship(game).getPositionX(),
				ship(game).getPositionY(), ShipType.TANK);
		field("ship").set(game, tank);
		tank.setShieldActive(true);
		assertFalse(game.damagePlayer(1, "Blocked hit"));
		clear(game);

		assertEquals(GameScreen.PERFECT_CLEAR_BONUS,
				game.getGameState().getScore());
	}

	@Test
	void hitBlockedByTheChameleonHullKeepsTheBonus() throws Exception {
		GameScreen game = stage(1, 3);
		ship(game).activateChameleonHull();
		assertFalse(game.damagePlayer(1, "Blocked hit"));
		clear(game);

		assertEquals(GameScreen.PERFECT_CLEAR_BONUS,
				game.getGameState().getScore());
	}

	@Test
	void hitDuringInvulnerabilityKeepsTheBonus() throws Exception {
		GameScreen game = stage(1, 3);
		// Starts the recovery period without losing a life.
		ship(game).destroy();
		assertTrue(ship(game).isInvulnerable());
		assertFalse(game.damagePlayer(1, "Hit while invulnerable"));
		clear(game);

		assertEquals(3, game.getGameState().getLivesRemaining());
		assertEquals(GameScreen.PERFECT_CLEAR_BONUS,
				game.getGameState().getScore());
	}

	@Test
	void fallingObstacleKeepsTheBonus() throws Exception {
		GameScreen game = stage(1, 3);
		@SuppressWarnings("unchecked")
		Set<FallingObstacle> obstacles =
				(Set<FallingObstacle>) field("fallingObstacles").get(game);
		obstacles.add(new FallingObstacle(ship(game).getPositionX(),
				ship(game).getPositionY()));
		Method collide = GameScreen.class
				.getDeclaredMethod("manageFallingObstacleCollisions");
		collide.setAccessible(true);
		collide.invoke(game);
		assertTrue(obstacles.isEmpty(), "the obstacle hit the ship");
		clear(game);

		assertEquals(GameScreen.PERFECT_CLEAR_BONUS,
				game.getGameState().getScore());
	}

	@Test
	void everyStageIsANewChance() throws Exception {
		GameScreen first = stage(1, 3);
		assertTrue(first.damagePlayer(1, "Test hit"));
		clear(first);
		assertEquals(0, first.getGameState().getScore());

		// The next stage starts from the lives left after the first one.
		GameScreen second = stage(2, first.getGameState().getLivesRemaining());
		clear(second);
		assertEquals(GameScreen.PERFECT_CLEAR_BONUS,
				second.getGameState().getScore());
	}

	/** Creates a running one-wave stage that accepts input at once. */
	private static GameScreen stage(final int level, final int lives) {
		GameScreen screen = new GameScreen(new GameState(level, 0, lives, 0, 0),
				Arrays.asList(new GameSettings(1, 1, 60, 2000)), false, 448,
				520, 60);
		screen.initialize();
		screen.inputDelay = Core.getCooldown(0);
		screen.isRunning = true;
		return screen;
	}

	/** Removes every enemy and lets the stage notice it is cleared. */
	private static void clear(final GameScreen game) throws Exception {
		EnemyShipFormation formation =
				(EnemyShipFormation) field("enemyShipFormation").get(game);
		for (EnemyShip enemy : formation)
			if (!enemy.isDestroyed())
				formation.destroy(enemy);
		game.advanceStageIfReady();
	}

	private static Ship ship(final GameScreen game) throws Exception {
		return (Ship) field("ship").get(game);
	}

	private static FloatingScoreHud hud(final GameScreen game)
			throws Exception {
		return (FloatingScoreHud) field("floatingScoreHud").get(game);
	}

	private static Field field(final String name) throws Exception {
		Field field = GameScreen.class.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}
