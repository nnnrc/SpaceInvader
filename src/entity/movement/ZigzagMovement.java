package entity.movement;

/** Falls slowly while swinging left and right around the spawn point. */
public final class ZigzagMovement implements EnemyMovement {

    /** Distance, in pixels, the enemy travels sideways before turning back. */
    public static final int AMPLITUDE = 40;
    private static final int LATERAL_SPEED = 2;
    private static final double FALL_SPEED_SCALE = 0.8;

    private final double fallSpeed;
    private final FallStep fallStep = new FallStep();
    /** Sideways distance from the spawn point. */
    private int offsetX;
    /** 1 is right and -1 is left. */
    private int direction;

    public ZigzagMovement(final int fallSpeed) {
        this.fallSpeed = fallSpeed * FALL_SPEED_SCALE;
        this.direction = Math.random() < 0.5 ? 1 : -1;
    }

    @Override
    public Step next(final int targetOffsetX) {
        if (Math.abs(this.offsetX + this.direction * LATERAL_SPEED) > AMPLITUDE) {
            this.direction = -this.direction;
        }
        int dx = this.direction * LATERAL_SPEED;
        this.offsetX += dx;
        return new Step(dx, this.fallStep.next(this.fallSpeed));
    }
}
