package engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import entity.ShipType;
import org.junit.jupiter.api.Test;

class HangarConfigTest {

    @Test
    void savedShipIsUsedWhenItIsOwned() {
        assertEquals(ShipType.TANK,
                HangarConfig.resolveShip("TANK", type -> true));
    }

    @Test
    void standardShipIsUsedWhenNothingIsSaved() {
        assertEquals(ShipType.STANDARD,
                HangarConfig.resolveShip(null, type -> true));
    }

    @Test
    void standardShipIsUsedWhenTheSavedShipIsNotOwned() {
        assertEquals(ShipType.STANDARD,
                HangarConfig.resolveShip("TANK", type -> type != ShipType.TANK));
    }

    @Test
    void standardShipIsUsedWhenTheSavedNameIsUnknown() {
        assertEquals(ShipType.STANDARD,
                HangarConfig.resolveShip("UFO", type -> true));
    }

    @Test
    void savedColorIsUsedWhenItIsInRange() {
        assertEquals(3, HangarConfig.resolveColorIndex(3, 6));
    }

    @Test
    void firstColorIsUsedWhenTheSavedColorIsOutOfRange() {
        assertEquals(0, HangarConfig.resolveColorIndex(-1, 6));
        assertEquals(0, HangarConfig.resolveColorIndex(6, 6));
    }
}
