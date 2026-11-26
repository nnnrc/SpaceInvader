package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Checks that the ship carries at most two grenades (SCRUM-43).
 */
class ShipGrenadeLimitTest {

    /** Altitude at which a launched grenade explodes. */
    private static final int DETONATION_Y = 100;

    @Test
    void newShipHasNoGrenades() {
        Ship ship = new Ship(100, 100);

        assertEquals(0, ship.getGrenadeCount());
    }

    @Test
    void shipCanCarryTwoGrenades() {
        Ship ship = new Ship(100, 100);
        ship.addGrenades(1);
        ship.addGrenades(1);

        assertEquals(2, ship.getGrenadeCount());
    }

    @Test
    void thirdPickupKeepsTheCountAtTwo() {
        Ship ship = new Ship(100, 100);
        ship.addGrenades(1);
        ship.addGrenades(1);
        ship.addGrenades(1);

        assertEquals(2, ship.getGrenadeCount());
    }

    @Test
    void addingSeveralGrenadesAtOnceIsCappedAtTwo() {
        Ship ship = new Ship(100, 100);
        ship.addGrenades(5);

        assertEquals(2, ship.getGrenadeCount());
    }

    @Test
    void launchingAGrenadeFreesASlot() {
        Ship ship = new Ship(100, 100);
        ship.addGrenades(2);
        Set<Grenade> grenades = new HashSet<Grenade>();

        assertTrue(ship.launchGrenade(grenades, DETONATION_Y));
        assertEquals(1, ship.getGrenadeCount());
        assertEquals(1, grenades.size());

        ship.addGrenades(1);
        assertEquals(2, ship.getGrenadeCount());
    }
}
