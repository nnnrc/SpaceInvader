package entity.movement;

/**
 * Falls a set distance, stops to fire a few shots, then keeps falling.
 * Counts frames instead of time, so pausing the game also pauses the hover.
 */
public final class HoverMovement implements EnemyMovement {

    /** Distance, in pixels, fallen before the enemy stops. */
    static final int HOVER_DISTANCE = 120;
    /** Frames the enemy stays still (2 seconds at 60 fps). */
    static final int HOVER_FRAMES = 120;
    /** Frames between two shots while hovering. */
    static final int FIRE_INTERVAL = 40;
    private static final double FALL_SPEED_SCALE = 0.8;

    private final double fallSpeed;
    private final FallStep fallStep = new FallStep();
    private int fallen;
    private int hoverFrame;
    private boolean fires;

    public HoverMovement(final int fallSpeed) {
        this.fallSpeed = fallSpeed * FALL_SPEED_SCALE;
    }

    @Override
    public Step next() {
        boolean hovering = this.fallen >= HOVER_DISTANCE
                && this.hoverFrame < HOVER_FRAMES;
        if (hovering) {
            this.hoverFrame++;
            this.fires = this.hoverFrame % FIRE_INTERVAL == 0;
            return new Step(0, 0);
        }
        this.fires = false;
        int dy = this.fallStep.next(this.fallSpeed);
        this.fallen += dy;
        return new Step(0, dy);
    }

    @Override
    public boolean firesThisFrame() {
        return this.fires;
    }
}
