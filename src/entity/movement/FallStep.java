package entity.movement;

/** Turns a fractional speed into whole-pixel steps without losing the remainder. */
final class FallStep {

    private double remainder;

    int next(final double pixelsPerFrame) {
        double fall = pixelsPerFrame + this.remainder;
        int step = (int) fall;
        this.remainder = fall - step;
        return step;
    }
}
