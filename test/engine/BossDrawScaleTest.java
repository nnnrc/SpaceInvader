package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import entity.Boss;
import entity.Entity;
import entity.EnemyShip;

/**
 * Checks that the boss is drawn and hit at {@link Boss#SCALE}, while the
 * other entities keep their 2px sprite cells.
 */
class BossDrawScaleTest {

    /** Size of the image the entities are drawn on. */
    private static final int CANVAS_SIZE = 200;
    /** Where the entities are drawn on the image. */
    private static final int ORIGIN = 20;

    @Test
    void hitboxUsesTheBossScale() {
        Boss boss = new Boss(0, 0, Boss.BossType.BOSS_A);

        assertEquals(3, Boss.SCALE);
        assertEquals(16 * Boss.SCALE, boss.getWidth());
        assertEquals(7 * Boss.SCALE, boss.getHeight());
    }

    @Test
    void everyBossTypeHasTheSameLargerSize() {
        for (Boss.BossType type : Boss.BossType.values()) {
            Boss boss = new Boss(0, 0, type);
            assertEquals(Boss.WIDTH, boss.getWidth(), type.name());
            assertEquals(Boss.HEIGHT, boss.getHeight(), type.name());
        }
    }

    /**
     * The boss reuses the bonus ship sprite, so drawing both shows the
     * scale directly: the boss must come out 3/2 the size of the bonus
     * ship, which still uses 2px cells.
     */
    @Test
    void bossIsDrawnLargerThanTheBonusShipWithTheSameSprite()
            throws Exception {
        Rectangle bonusShip = drawnBounds(new EnemyShip());
        Rectangle boss = drawnBounds(new Boss(0, 0, Boss.BossType.BOSS_A));

        assertEquals(bonusShip.width * Boss.SCALE / 2, boss.width);
        assertEquals(bonusShip.height * Boss.SCALE / 2, boss.height);
    }

    /** What the player sees must stay inside what the player can hit. */
    @Test
    void drawnBossStaysInsideItsHitbox() throws Exception {
        Boss boss = new Boss(0, 0, Boss.BossType.BOSS_A);
        Rectangle drawn = drawnBounds(boss);

        assertTrue(drawn.x >= ORIGIN && drawn.y >= ORIGIN);
        assertTrue(drawn.x + drawn.width <= ORIGIN + boss.getWidth());
        assertTrue(drawn.y + drawn.height <= ORIGIN + boss.getHeight());
    }

    /** Sprite cells are filled squares, with no gaps between them. */
    @Test
    void bossCellsAreSolidSquares() throws Exception {
        BufferedImage image = draw(new Boss(0, 0, Boss.BossType.BOSS_A));

        for (int x = 0; x < CANVAS_SIZE; x++) {
            for (int y = 0; y < CANVAS_SIZE; y++) {
                if (image.getRGB(x, y) == Color.BLACK.getRGB())
                    continue;
                int cellX = ORIGIN + (x - ORIGIN) / Boss.SCALE * Boss.SCALE;
                int cellY = ORIGIN + (y - ORIGIN) / Boss.SCALE * Boss.SCALE;
                for (int dx = 0; dx < Boss.SCALE; dx++)
                    for (int dy = 0; dy < Boss.SCALE; dy++)
                        assertEquals(image.getRGB(x, y),
                                image.getRGB(cellX + dx, cellY + dy),
                                "gap in the cell at " + cellX + "," + cellY);
            }
        }
    }

    /** Draws one entity at {@link #ORIGIN} on a black image. */
    private static BufferedImage draw(final Entity entity) throws Exception {
        BufferedImage image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE,
                BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        DrawManager drawManager = DrawManager.getInstance();
        Field field = DrawManager.class.getDeclaredField("backBufferGraphics");
        field.setAccessible(true);
        Object previous = field.get(null);
        field.set(null, graphics);
        try {
            drawManager.drawEntity(entity, ORIGIN, ORIGIN);
        } finally {
            field.set(null, previous);
            graphics.dispose();
        }
        return image;
    }

    /** Smallest rectangle holding every non-black pixel of the drawing. */
    private static Rectangle drawnBounds(final Entity entity)
            throws Exception {
        BufferedImage image = draw(entity);
        Rectangle bounds = null;
        for (int x = 0; x < CANVAS_SIZE; x++)
            for (int y = 0; y < CANVAS_SIZE; y++)
                if (image.getRGB(x, y) != Color.BLACK.getRGB()) {
                    Rectangle pixel = new Rectangle(x, y, 1, 1);
                    bounds = bounds == null ? pixel : bounds.union(pixel);
                }
        assertTrue(bounds != null, "nothing was drawn");
        return bounds;
    }
}
