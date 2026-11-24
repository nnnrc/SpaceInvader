package entity;

import engine.DrawManager.SpriteType;
import entity.movement.EnemyMovement;
import entity.movement.ZigzagMovement;

/**
 * Enemy ship of the infinite mode. How it moves depends on its type
 * (see {@link EnemyMovement#forSprite}).
 */
public class ZigzagEnemyShip extends EnemyShip {

	private final EnemyMovement movement;

	/**
	 * Constructor, establishes the ship's properties.
	 *
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 * @param spriteType
	 *            Sprite type, which also decides how the ship moves.
	 * @param fallSpeed
	 *            Base downwards speed, in pixels per frame.
	 */
	public ZigzagEnemyShip(final int positionX, final int positionY,
			final SpriteType spriteType, final int fallSpeed) {
		super(positionX, positionY, spriteType);

		this.movement = EnemyMovement.forSprite(spriteType, fallSpeed);
	}

	/**
	 * Advances the ship one frame along its movement.
	 *
	 * @param targetX
	 *            Center X of the player's ship, which some movements follow.
	 */
	public final void moveOneFrame(final int targetX) {
		int centerX = this.positionX + this.width / 2;
		EnemyMovement.Step step = this.movement.next(targetX - centerX);
		move(step.dx(), step.dy());
	}

	/**
	 * Whether the ship fires on the frame just moved, as set by its movement.
	 *
	 * @return True if the ship should fire now.
	 */
	public final boolean firesThisFrame() {
		return this.movement.firesThisFrame();
	}

	/**
	 * Getter for the amplitude of the zigzag.
	 *
	 * @return Sideways distance the ship covers from its center.
	 */
	public static int getZigzagAmplitude() {
		return ZigzagMovement.AMPLITUDE;
	}
}
