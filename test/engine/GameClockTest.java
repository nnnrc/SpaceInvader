package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Checks that the game clock stops while the game is paused. */
class GameClockTest {

	/** Wall time the clock reads, moved by hand in the tests. */
	private long wallTime;

	@BeforeEach
	void useManualTime() {
		this.wallTime = 10_000;
		GameClock.setTimeSource(() -> this.wallTime);
		GameClock.reset();
	}

	@AfterEach
	void useSystemTime() {
		GameClock.reset();
		GameClock.useSystemTime();
	}

	@Test
	@DisplayName("The clock follows the wall time while not paused")
	void followsWallTime() {
		long start = GameClock.now();
		this.wallTime += 500;
		assertEquals(start + 500, GameClock.now());
	}

	@Test
	@DisplayName("The clock does not move while paused")
	void stopsWhilePaused() {
		GameClock.pause();
		long pausedTime = GameClock.now();
		this.wallTime += 3_000;

		assertTrue(GameClock.isPaused());
		assertEquals(pausedTime, GameClock.now());
	}

	@Test
	@DisplayName("After resuming, the paused time is skipped")
	void continuesAfterResume() {
		long start = GameClock.now();
		this.wallTime += 1_000;
		GameClock.pause();
		this.wallTime += 5_000;
		GameClock.resume();
		this.wallTime += 200;

		assertFalse(GameClock.isPaused());
		assertEquals(start + 1_200, GameClock.now());
	}

	@Test
	@DisplayName("Pausing or resuming twice has no extra effect")
	void repeatedCallsAreIgnored() {
		long start = GameClock.now();
		GameClock.pause();
		this.wallTime += 1_000;
		GameClock.pause();
		this.wallTime += 1_000;
		GameClock.resume();
		GameClock.resume();

		assertEquals(start, GameClock.now());
	}

	@Test
	@DisplayName("A cooldown finishes later by the time spent paused")
	void cooldownWaitsDuringPause() {
		Cooldown cooldown = new Cooldown(1_000);
		cooldown.reset();

		this.wallTime += 600;
		GameClock.pause();
		this.wallTime += 5_000;
		assertFalse(cooldown.checkFinished());

		GameClock.resume();
		this.wallTime += 300;
		assertFalse(cooldown.checkFinished());

		this.wallTime += 200;
		assertTrue(cooldown.checkFinished());
	}

	@Test
	@DisplayName("Reset forgets earlier pauses")
	void resetClearsPausedTime() {
		GameClock.pause();
		this.wallTime += 2_000;
		GameClock.reset();

		assertFalse(GameClock.isPaused());
		assertEquals(this.wallTime, GameClock.now());
	}

	@Test
	@DisplayName("Time spent outside the game does not count as game time")
	void runOutsideGameKeepsTheGameTime() {
		GameClock.pause();
		long gameTime = GameClock.now();

		GameClock.runOutsideGame(() -> {
			assertFalse(GameClock.isPaused());
			this.wallTime += 3_000;
			assertEquals(gameTime + 3_000, GameClock.now());
		});

		assertTrue(GameClock.isPaused());
		assertEquals(gameTime, GameClock.now());
		this.wallTime += 1_000;
		GameClock.resume();
		assertEquals(gameTime, GameClock.now());
	}
}
