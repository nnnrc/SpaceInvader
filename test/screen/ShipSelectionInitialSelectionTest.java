package screen;

import static org.junit.jupiter.api.Assertions.assertSame;

import entity.ShipType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ShipSelectionInitialSelectionTest {
    @ParameterizedTest
    @EnumSource(ShipType.class)
    void cursorStartsOnTheEquippedShip(final ShipType equippedShip) {
        ShipSelectionScreen screen = new ShipSelectionScreen(448, 520, 60, equippedShip);

        assertSame(equippedShip, screen.getSelectedShip());
        assertSame(equippedShip, screen.getEquippedShip());
    }

    @Test
    void existingConstructorStillStartsOnTheStandardShip() {
        ShipSelectionScreen screen = new ShipSelectionScreen(448, 520, 60);

        assertSame(ShipType.STANDARD, screen.getSelectedShip());
        assertSame(ShipType.STANDARD, screen.getEquippedShip());
    }
}
