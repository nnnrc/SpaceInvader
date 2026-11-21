package hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class EndlessSpeedHudTest {
    @Test
    void movesSmoothlyAndSettlesAfterEachDefeat() {
        EndlessSpeedHud hud = new EndlessSpeedHud(1, 0.0);
        hud.update(1, 0.05, 1.0 / 60);
        assertTrue(hud.getDisplayedProgress() > 0.0);
        assertTrue(hud.getDisplayedProgress() < 0.05);
        hud.update(1, 0.05, 0.2);
        assertEquals(0.05, hud.getDisplayedProgress(), 0.000001);
    }

    @Test
    void animationDependsOnElapsedTimeRatherThanFrameCount() {
        EndlessSpeedHud thirtyFps = new EndlessSpeedHud(1, 0.0);
        EndlessSpeedHud sixtyFps = new EndlessSpeedHud(1, 0.0);
        for (int frame = 0; frame < 3; frame++)
            thirtyFps.update(1, 0.5, 1.0 / 30);
        for (int frame = 0; frame < 6; frame++)
            sixtyFps.update(1, 0.5, 1.0 / 60);
        assertEquals(thirtyFps.getDisplayedProgress(),
                sixtyFps.getDisplayedProgress(), 0.000001);
    }

    @Test
    void startsAnEmptyBarAtLevelTwoInsteadOfAnimatingBackwards() {
        EndlessSpeedHud hud = new EndlessSpeedHud(1, 0.95);
        hud.update(2, 0.0, 1.0 / 60);
        assertEquals(0.0, hud.getDisplayedProgress(), 0.000001);
        hud.update(2, 0.05, 1.0 / 60);
        assertTrue(hud.getDisplayedProgress() > 0.0);
        assertTrue(hud.getDisplayedProgress() < 0.05);
    }

    @Test
    void fillsTheLastPartSmoothlyAtMaxAndStaysFull() {
        EndlessSpeedHud hud = new EndlessSpeedHud(2, 0.95);
        hud.update(3, 1.0, 1.0 / 60);
        assertTrue(hud.getDisplayedProgress() > 0.95);
        assertTrue(hud.getDisplayedProgress() < 1.0);
        hud.update(3, 1.0, 0.2);
        assertEquals(1.0, hud.getDisplayedProgress(), 0.000001);
        hud.update(3, 1.0, 10.0);
        assertEquals(1.0, hud.getDisplayedProgress(), 0.000001);
    }

    @Test
    void doesNotAdvanceWithoutElapsedTimeAndDoesNotCarryOverToANewRun() {
        EndlessSpeedHud hud = new EndlessSpeedHud(1, 0.4);
        hud.update(1, 0.5, 0.0);
        assertEquals(0.4, hud.getDisplayedProgress(), 0.000001);
        assertEquals(0.0, new EndlessSpeedHud(1, 0.0).getDisplayedProgress(), 0.000001);
    }

    @Test
    void drawsAContinuousGreenBarWithAnUnfilledRemainder() {
        BufferedImage image = render(new EndlessSpeedHud(1, 0.5));
        for (int x = 139; x < 194; x++)
            assertEquals(Color.GREEN.getRGB(), image.getRGB(x, 21));
        assertEquals(Color.DARK_GRAY.getRGB(), image.getRGB(250, 21));
    }

    @Test
    void drawsAGoldFullBarWithMaxCenteredInsideIt() {
        BufferedImage image = render(new EndlessSpeedHud(3, 1.0));
        assertEquals(new Color(255, 215, 0).getRGB(), image.getRGB(140, 21));
        assertEquals(new Color(255, 215, 0).getRGB(), image.getRGB(250, 21));
        int firstTextX = image.getWidth();
        int lastTextX = -1;
        for (int y = 19; y < 33; y++) {
            for (int x = 139; x < 253; x++) {
                if (image.getRGB(x, y) == Color.BLACK.getRGB()) {
                    firstTextX = Math.min(firstTextX, x);
                    lastTextX = Math.max(lastTextX, x);
                }
            }
        }
        assertTrue(lastTextX >= firstTextX, "MAX must be visible inside the bar");
        assertEquals(195.5, (firstTextX + lastTextX) / 2.0, 1.0);
    }

    private BufferedImage render(final EndlessSpeedHud hud) {
        BufferedImage image = new BufferedImage(448, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            hud.draw(graphics, image.getWidth(), new Font("Monospaced", Font.PLAIN, 14));
        } finally {
            graphics.dispose();
        }
        return image;
    }
}
