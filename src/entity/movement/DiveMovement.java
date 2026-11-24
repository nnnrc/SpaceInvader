package entity.movement;

/** Steers towards the player while high up, then dives straight down fast. */
public final class DiveMovement implements EnemyMovement {

    /** Distance, in pixels, fallen while still turning towards the player. */
    static final int AIM_DISTANCE = 200;
    private static final double FALL_SPEED_SCALE = 1.5;

    private final double fallSpeed;
    private final FallStep fallStep = new FallStep();
    private int fallen;

    public DiveMovement(final int fallSpeed) {
        this.fallSpeed = fallSpeed * FALL_SPEED_SCALE;
    }

    @Override
    public Step next(final int targetOffsetX) {
        int dx = this.fallen < AIM_DISTANCE ? Integer.signum(targetOffsetX) : 0;
        int dy = this.fallStep.next(this.fallSpeed);
        this.fallen += dy;
        return new Step(dx, dy);
    }
}
