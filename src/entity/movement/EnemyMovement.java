package entity.movement;

import engine.DrawManager.SpriteType;

/**
 * How an Endless Mode enemy moves each frame. A new movement is a new class
 * plus one line in {@link #forSprite}; the enemy and the screen stay the same.
 */
public interface EnemyMovement {

    /** Distance an enemy moves in one frame, in pixels. */
    record Step(int dx, int dy) {
    }

    /**
     * Returns how far the enemy moves this frame.
     *
     * @param targetOffsetX Player's center X minus the enemy's center X.
     * @return Distance to move this frame.
     */
    Step next(int targetOffsetX);

    /** Whether the enemy fires on the frame just moved; on top of the random shots. */
    default boolean firesThisFrame() {
        return false;
    }

    /**
     * Picks the movement for an enemy type: A zigzags, B aims and dives,
     * C hovers and fires.
     *
     * @param spriteType Sprite the enemy spawns with.
     * @param fallSpeed  Base downwards speed, in pixels per frame.
     * @return A new movement for one enemy.
     */
    static EnemyMovement forSprite(final SpriteType spriteType,
                                   final int fallSpeed) {
        switch (spriteType) {
            case EnemyShipB1:
            case EnemyShipB2:
                return new DiveMovement(fallSpeed);
            case EnemyShipC1:
            case EnemyShipC2:
                return new HoverMovement(fallSpeed);
            default:
                return new ZigzagMovement(fallSpeed);
        }
    }
}
