package screen;

/**
 * Remembers whether the player has lost a life since a starting point, such
 * as the start of a stage. Only lives that are really lost count: hits
 * blocked by a shield, invulnerability or the Chameleon Hull, and falling
 * obstacles, never reach it.
 */
final class LifeLossTracker {

	/** Whether a life was lost since the last start. */
	private boolean lifeLost;

	/** Starts counting again from now. */
	void start() {
		this.lifeLost = false;
	}

	/** Records that the player lost a life. */
	void onLifeLost() {
		this.lifeLost = true;
	}

	/**
	 * @return Whether no life was lost since the last start.
	 */
	boolean isFlawless() {
		return !this.lifeLost;
	}
}
