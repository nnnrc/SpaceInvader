package engine;

import java.util.function.LongSupplier;

/**
 * Game time in milliseconds that stops while the game is paused.
 *
 * Gameplay timers (cooldowns, item durations, the laser charge) read this
 * clock instead of the wall clock, so pausing the game does not use up their
 * time. After a pause, every timer continues with the time it had left.
 */
public final class GameClock {

	/** Clock the game time is measured against. */
	private static LongSupplier source = System::currentTimeMillis;
	/** Wall time when the current pause started, or -1 if not paused. */
	private static long pausedAt = -1;
	/** Total time spent paused since the last reset. */
	private static long pausedTotal = 0;

	private GameClock() {
	}

	/**
	 * Returns the current game time.
	 *
	 * @return Wall time minus the time spent paused, in milliseconds.
	 */
	public static synchronized long now() {
		long wallTime = isPaused() ? pausedAt : source.getAsLong();
		return wallTime - pausedTotal;
	}

	/** Stops the game time. Does nothing if it is already stopped. */
	public static synchronized void pause() {
		if (!isPaused())
			pausedAt = source.getAsLong();
	}

	/** Starts the game time again. Does nothing if it is not stopped. */
	public static synchronized void resume() {
		if (isPaused()) {
			pausedTotal += source.getAsLong() - pausedAt;
			pausedAt = -1;
		}
	}

	/**
	 * Checks whether the game time is stopped.
	 *
	 * @return True while the game is paused.
	 */
	public static synchronized boolean isPaused() {
		return pausedAt >= 0;
	}

	/**
	 * Runs a task, such as the settings opened from the pause menu, with the
	 * clock moving, so the cooldowns of its menus work. When the task ends,
	 * the clock is stopped again at the game time it showed before, so the
	 * time spent in the task does not count as game time.
	 *
	 * @param task
	 *            Task to run while the game is paused.
	 */
	public static void runOutsideGame(final Runnable task) {
		long gameTime = now();
		resume();
		try {
			task.run();
		} finally {
			synchronized (GameClock.class) {
				pausedAt = source.getAsLong();
				pausedTotal = pausedAt - gameTime;
			}
		}
	}

	/** Starts the game time again and forgets earlier pauses. */
	public static synchronized void reset() {
		pausedAt = -1;
		pausedTotal = 0;
	}

	/**
	 * Replaces the wall clock, so tests can move time without waiting.
	 *
	 * @param newSource
	 *            Clock to read, in milliseconds.
	 */
	static synchronized void setTimeSource(final LongSupplier newSource) {
		source = newSource;
	}

	/** Goes back to the system clock after a test. */
	static synchronized void useSystemTime() {
		source = System::currentTimeMillis;
	}
}
