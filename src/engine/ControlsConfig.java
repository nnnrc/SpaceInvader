package engine;

import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

public final class ControlsConfig {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int FIRE = 2;
    public static final int BUFF = 3;

    private static final int DEFAULT_LEFT_KEY = KeyEvent.VK_LEFT;
    private static final int DEFAULT_RIGHT_KEY = KeyEvent.VK_RIGHT;
    private static final int DEFAULT_FIRE_KEY = KeyEvent.VK_SPACE;

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(ControlsConfig.class);

    private ControlsConfig() {
    }

    public static int getKey(final int action) {
        switch (action) {
        case LEFT:
            return PREFS.getInt("left", DEFAULT_LEFT_KEY);
        case RIGHT:
            return PREFS.getInt("right", DEFAULT_RIGHT_KEY);
        case FIRE:
            return PREFS.getInt("fire", DEFAULT_FIRE_KEY);
        default:
            return KeyEvent.VK_UNDEFINED;
        }
    }

    public static void setKey(final int action, final int keyCode) {
        switch (action) {
        case LEFT:
            PREFS.putInt("left", keyCode);
            break;
        case RIGHT:
            PREFS.putInt("right", keyCode);
            break;
        case FIRE:
            PREFS.putInt("fire", keyCode);
            break;
        default:
            break;
        }
    }

    public static void resetToDefaults() {
        setKey(LEFT, DEFAULT_LEFT_KEY);
        setKey(RIGHT, DEFAULT_RIGHT_KEY);
        setKey(FIRE, DEFAULT_FIRE_KEY);
    }
}
