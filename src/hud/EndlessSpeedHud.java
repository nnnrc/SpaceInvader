package hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/** Animates the Endless speed display without changing gameplay progression. */
public final class EndlessSpeedHud {
    private static final double SMOOTHING_SECONDS = 0.05;
    private static final double SNAP_DISTANCE = 0.001;
    private static final int BAR_WIDTH = 116;
    private static final int BAR_HEIGHT = 16;
    private static final int BAR_Y = 18;
    private static final Color MAX_COLOR = new Color(255, 215, 0);
    private static final Font LEVEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font MAX_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 19);

    private int level;
    private double displayedProgress;
    private boolean maximum;

    public EndlessSpeedHud(final int level, final double progress) {
        this.level = level;
        this.displayedProgress = progress;
        this.maximum = progress >= 1.0;
    }

    /** Uses elapsed seconds so the animation speed does not depend on FPS. */
    public void update(final int nextLevel, final double targetProgress,
            final double elapsedSeconds) {
        if (this.level != nextLevel && targetProgress < 1.0) {
            this.displayedProgress = targetProgress;
        } else {
            double blend = 1.0 - Math.exp(-Math.max(0.0, elapsedSeconds)
                    / SMOOTHING_SECONDS);
            this.displayedProgress += (targetProgress - this.displayedProgress) * blend;
            if (Math.abs(targetProgress - this.displayedProgress) < SNAP_DISTANCE)
                this.displayedProgress = targetProgress;
        }
        this.level = nextLevel;
        this.maximum = targetProgress >= 1.0;
    }

    /** Fits between the lives on the left and credits on the right. */
    public void draw(final Graphics2D source, final int screenWidth) {
        Graphics2D graphics = (Graphics2D) source.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setFont(LEVEL_FONT);
            FontMetrics metrics = graphics.getFontMetrics();
            int barX = screenWidth / 2 - 28 - BAR_WIDTH / 2;
            String label = "Speed Lv. " + this.level;
            graphics.setColor(Color.WHITE);
            graphics.drawString(label,
                    barX + (BAR_WIDTH - metrics.stringWidth(label)) / 2, 14);

            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            graphics.setColor(Color.DARK_GRAY);
            graphics.fillRect(barX, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
            graphics.setColor(this.maximum ? MAX_COLOR : Color.GREEN);
            graphics.fillRect(barX + 1, BAR_Y + 1,
                    (int) Math.round((BAR_WIDTH - 2) * this.displayedProgress),
                    BAR_HEIGHT - 2);
            graphics.setColor(this.maximum ? MAX_COLOR : Color.GRAY);
            graphics.drawRect(barX, BAR_Y, BAR_WIDTH - 1, BAR_HEIGHT - 1);

            if (this.maximum) {
                graphics.setFont(MAX_FONT);
                Rectangle textBounds = MAX_FONT.createGlyphVector(
                        graphics.getFontRenderContext(), "MAX").getPixelBounds(null, 0, 0);
                graphics.setColor(Color.BLACK);
                graphics.drawString("MAX",
                        barX + (BAR_WIDTH - textBounds.width) / 2 - textBounds.x,
                        BAR_Y + (BAR_HEIGHT - textBounds.height) / 2 - textBounds.y);
            }
        } finally {
            graphics.dispose();
        }
    }

    double getDisplayedProgress() {
        return this.displayedProgress;
    }
}
