package entity;

import java.awt.Color;

public class FloatingText {
    private double x;
    private double y;
    private final String text;
    private final Color color;
    private double remainingTime;
    private final double totalDuration;
    private final double upwardSpeed;

    public FloatingText(final double x, final double y, final String text,
                        final Color color, final double durationSeconds,
                        final double upwardSpeed) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.totalDuration = durationSeconds;
        this.remainingTime = durationSeconds;
        this.upwardSpeed = upwardSpeed;
    }

    public FloatingText(final double x, final double y, final String text,
                        final Color color) {
        this(x, y, text, color, 0.9, 40.0);
    }

    public void update(final double deltaSeconds) {
        if (deltaSeconds <= 0 || isExpired()) {
            return;
        }
        y -= upwardSpeed * deltaSeconds;
        remainingTime -= deltaSeconds;
        if (remainingTime < 0) {
            remainingTime = 0;
        }
    }

    public boolean isExpired() {
        return remainingTime <= 0;
    }

    public float getAlpha() {
        if (totalDuration <= 0) {
            return 0f;
        }
        return (float) Math.max(0.0, Math.min(1.0, remainingTime / totalDuration));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    public double getRemainingTime() {
        return remainingTime;
    }
}
