package entity;

import java.awt.Color;

/**
 * Standalone checks for {@link FloatingText} movement, alpha fade-out, and
 * expiration lifecycle.
 */
public final class FloatingTextTest {

    private FloatingTextTest() {
    }

    private static void check(final boolean condition, final String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static boolean approxEqual(final double a, final double b,
                                       final double eps) {
        return Math.abs(a - b) <= eps;
    }

    public static void main(final String[] args) {
        checkDefaultConstructor();
        checkCustomConstructorAndUpdate();
        checkNonPositiveDeltaAndExpiration();
        checkZeroOrNegativeDurationAlpha();

        System.out.println("FloatingText checks passed.");
    }

    private static void checkDefaultConstructor() {
        FloatingText popup = new FloatingText(120.0, 240.0, "+100", Color.YELLOW);

        check(approxEqual(popup.getX(), 120.0, 0.001),
                "Initial X should match constructor argument");
        check(approxEqual(popup.getY(), 240.0, 0.001),
                "Initial Y should match constructor argument");
        check("+100".equals(popup.getText()),
                "Text should match constructor argument");
        check(Color.YELLOW.equals(popup.getColor()),
                "Color should match constructor argument");
        check(approxEqual(popup.getRemainingTime(), 0.9, 0.001),
                "Default duration should be 0.9s");
        check(approxEqual(popup.getAlpha(), 1.0, 0.001),
                "Initial alpha should be 1.0");
        check(!popup.isExpired(),
                "Popup should not be expired initially");
    }

    private static void checkCustomConstructorAndUpdate() {
        // upwardSpeed = 40.0, durationSeconds = 1.0 -> after 0.25s:
        // y drops by 40 * 0.25 = 10.0 (from 200.0 to 190.0), remainingTime = 0.75, alpha = 0.75
        FloatingText popup = new FloatingText(50.0, 200.0, "SHIELD!", Color.CYAN,
                1.0, 40.0);

        popup.update(0.25);

        check(approxEqual(popup.getY(), 190.0, 0.001),
                "Y should decrease by upwardSpeed * deltaSeconds");
        check(approxEqual(popup.getRemainingTime(), 0.75, 0.001),
                "Remaining time should decrease by deltaSeconds");
        check(approxEqual(popup.getAlpha(), 0.75, 0.001),
                "Alpha should match remainingTime / totalDuration");
        check(!popup.isExpired(),
                "Popup should still be active before duration ends");
    }

    private static void checkNonPositiveDeltaAndExpiration() {
        FloatingText popup = new FloatingText(10.0, 100.0, "+50", Color.WHITE,
                0.5, 20.0);

        popup.update(0.0);
        popup.update(-0.5);
        check(approxEqual(popup.getY(), 100.0, 0.001),
                "Non-positive delta should not move popup");
        check(approxEqual(popup.getRemainingTime(), 0.5, 0.001),
                "Non-positive delta should not reduce remainingTime");
        check(!popup.isExpired(),
                "Popup should not expire on non-positive delta");

        popup.update(0.6);
        check(popup.isExpired(),
                "Popup should be expired after elapsed duration");
        check(approxEqual(popup.getRemainingTime(), 0.0, 0.001),
                "Remaining time should clamp at 0.0");
        check(approxEqual(popup.getAlpha(), 0.0, 0.001),
                "Alpha should be 0.0 when expired");

        // Further updates once expired should have no effect.
        double yAfterExpiry = popup.getY();
        popup.update(0.2);
        check(approxEqual(popup.getY(), yAfterExpiry, 0.001),
                "Expired popup should not keep moving");
    }

    private static void checkZeroOrNegativeDurationAlpha() {
        FloatingText instant = new FloatingText(0.0, 0.0, "TEST", Color.RED,
                0.0, 10.0);
        check(approxEqual(instant.getAlpha(), 0.0, 0.001),
                "Alpha should be 0.0 when totalDuration <= 0");
        check(instant.isExpired(),
                "Popup with 0 duration should be expired");
    }
}
