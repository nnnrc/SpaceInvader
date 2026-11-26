package entity.item;

/** What the HUD needs to show one active effect and its time left. */
public record BuffStatus(String name, long remainingMillis, long totalMillis,
                         Kind kind) {

    public enum Kind { BUFF, DEBUFF }

    /**
     * Returns the share of time left, used to size the HUD gauge.
     *
     * @return 1.0 when the effect has just started, 0.0 when it has run out.
     */
    public double progress() {
        if (this.totalMillis <= 0)
            return 0.0;
        double ratio = (double) this.remainingMillis / this.totalMillis;
        return Math.max(0.0, Math.min(1.0, ratio));
    }
}
