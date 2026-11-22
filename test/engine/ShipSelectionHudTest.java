package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import currency.PlayerAssets;
import currency.PlayerAssetsState;
import entity.ShipType;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import screen.Screen;
import shop.ShopTestFixtures;

class ShipSelectionHudTest {
    @TempDir Path temp;
    private final Screen screen = new Screen(448, 520, 60);
    private final DrawManager drawing = Core.getDrawManager();
    private Field shopField;
    private Object previousShop;

    @BeforeEach
    void useAnIsolatedShop() throws Exception {
        shopField = Core.class.getDeclaredField("shopController");
        shopField.setAccessible(true);
        previousShop = shopField.get(null);
        PlayerAssets assets = new PlayerAssets();
        assertTrue(assets.restore(new PlayerAssetsState(0,
                Collections.singleton("ship.speedster"), Collections.emptyMap())));
        shopField.set(null, ShopTestFixtures.upgradeController(assets,
                new ShopTestFixtures.SaveFile(Core.getFileManager(), temp.resolve("assets.properties"))));
    }

    @AfterEach
    void restoreShop() throws Exception {
        shopField.set(null, previousShop);
    }

    @ParameterizedTest
    @EnumSource(ShipType.class)
    void frameAndBadgeStayOnTheEquippedShipWhileBrowsing(final ShipType equippedShip)
            throws Exception {
        BufferedImage initial = render(equippedShip, equippedShip);
        assertFrameAndBadge(initial, equippedShip);
        assertPreview(initial, equippedShip, true);
        ShipType browsedShip = ShipType.values()[(equippedShip.ordinal() + 1) % ShipType.values().length];
        BufferedImage image = render(equippedShip, browsedShip);
        assertFrameAndBadge(image, equippedShip);
        int baseline = screen.getHeight() / 3 + equippedShip.ordinal() * 85;
        assertPreview(image, equippedShip, false);
        assertPreview(image, browsedShip, true);
        assertTrue(hasColor(image, Color.WHITE, screen.getWidth() / 4,
                baseline - 14, screen.getWidth() / 2, 14), "The equipped name must stay white");
    }

    @Test
    void frameAndBadgeMoveWhenTheConfirmedShipChanges() throws Exception {
        assertFrameAndBadge(render(ShipType.TANK, ShipType.STANDARD), ShipType.TANK);
        assertFrameAndBadge(render(ShipType.STANDARD, ShipType.STANDARD), ShipType.STANDARD);
    }

    private BufferedImage render(final ShipType equippedShip, final ShipType highlightedShip)
            throws Exception {
        drawing.initDrawing(screen);
        try {
            drawing.drawShipSelection(screen, ShipType.values(), highlightedShip.ordinal(), equippedShip);
            Field buffer = DrawManager.class.getDeclaredField("backBuffer");
            buffer.setAccessible(true);
            return (BufferedImage) buffer.get(null);
        } finally {
            drawing.completeDrawing(screen);
        }
    }

    private void assertPreview(final BufferedImage image, final ShipType ship,
            final boolean visible) {
        int baseline = screen.getHeight() / 3 + ship.ordinal() * 85;
        boolean hasPreview = hasColor(image, Color.GREEN, screen.getWidth() / 2 - 13,
                baseline - 40, 26, 16);
        assertEquals(visible, hasPreview, "Only the highlighted ship should have a preview: " + ship);
    }

    private boolean hasColor(final BufferedImage image, final Color color,
            final int left, final int top, final int width, final int height) {
        for (int y = top; y < top + height; y++) {
            for (int x = left; x < left + width; x++) {
                if (image.getRGB(x, y) == color.getRGB())
                    return true;
            }
        }
        return false;
    }

    private void assertFrameAndBadge(final BufferedImage image, final ShipType equippedShip) {
        for (ShipType ship : ShipType.values()) {
            int top = screen.getHeight() / 3 + ship.ordinal() * 85 - 48;
            int frameLeft = (screen.getWidth() - 318) / 2;
            int border = image.getRGB(frameLeft, top + 30);
            if (ship == equippedShip) {
                assertEquals(Color.WHITE.getRGB(), border);
                assertEquals(Color.WHITE.getRGB(), image.getRGB(frameLeft + 1, top + 30));
            } else {
                assertNotEquals(Color.WHITE.getRGB(), border);
                assertNotEquals(Color.WHITE.getRGB(), image.getRGB(frameLeft + 1, top + 30));
            }

            boolean badgeVisible = false;
            int baseline = screen.getHeight() / 3 + ship.ordinal() * 85;
            for (int y = baseline - 12; y < baseline; y++) {
                for (int x = screen.getWidth() / 2 + 40; x < frameLeft + 318 - 8; x++)
                    badgeVisible |= image.getRGB(x, y) == Color.WHITE.getRGB();
            }
            assertEquals(ship == equippedShip, badgeVisible);
        }
    }
}
