package engine;

import entity.ShipType;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

/** Saves the ship and color chosen in the hangar between game sessions. */
public final class HangarConfig {

    private static final String SHIP_KEY = "hangarShip";
    private static final String COLOR_KEY = "hangarColor";

    private HangarConfig() {
    }

    /** Returns the saved ship, or the standard ship if none is saved or it is not owned. */
    public static ShipType loadShip(final Predicate<ShipType> available) {
        return resolveShip(prefs().get(SHIP_KEY, null), available);
    }

    public static void saveShip(final ShipType ship) {
        prefs().put(SHIP_KEY, ship.name());
    }

    /** Returns the saved color index, or 0 if none is saved or it is out of range. */
    public static int loadColorIndex(final int colorCount) {
        return resolveColorIndex(prefs().getInt(COLOR_KEY, 0), colorCount);
    }

    public static void saveColorIndex(final int index) {
        prefs().putInt(COLOR_KEY, index);
    }

    static ShipType resolveShip(final String saved,
                                final Predicate<ShipType> available) {
        for (ShipType type : ShipType.values()) {
            if (type.name().equals(saved) && available.test(type))
                return type;
        }
        return ShipType.STANDARD;
    }

    static int resolveColorIndex(final int saved, final int colorCount) {
        if (saved < 0 || saved >= colorCount)
            return 0;
        return saved;
    }

    /** Looked up on each call, so tests of the resolve methods never open the real store. */
    private static Preferences prefs() {
        return Preferences.userNodeForPackage(HangarConfig.class);
    }
}
