package entity.movement;

/** Dives straight down, faster than the zigzag. */
public final class DiveMovement implements EnemyMovement {

    private static final double FALL_SPEED_SCALE = 1.5;

    private final double fallSpeed;
    private final FallStep fallStep = new FallStep();

    public DiveMovement(final int fallSpeed) {
        this.fallSpeed = fallSpeed * FALL_SPEED_SCALE;
    }

    @Override
    public Step next() {
        return new Step(0, this.fallStep.next(this.fallSpeed));
    }
}
