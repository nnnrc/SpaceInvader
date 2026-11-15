package screen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import audio.AudioManager;
import engine.Core;
import engine.GameSettings;
import engine.GameState;

/**
 * Checks that Esc pauses a Campaign stage and that quitting from the pause
 * menu ends the stage without clearing it.
 */
class GameScreenPauseTest {

    /** Component used as the source of the simulated key events. */
    private static final Canvas KEY_SOURCE = new Canvas();

    /** Mute state before the test, restored afterwards. */
    private boolean oldMute;

    @BeforeEach
    void muteAudio() {
        this.oldMute = AudioManager.isMuted();
        AudioManager.setMuted(true);
    }

    @AfterEach
    void restoreAudio() {
        AudioManager.stopBGM();
        AudioManager.setMuted(this.oldMute);
    }

    /**
     * Creates a running one-wave stage that accepts input at once.
     *
     * @return The stage.
     */
    private static GameScreen stage() {
        GameScreen screen = new GameScreen(new GameState(1, 0, 3, 0, 0),
                Arrays.asList(new GameSettings(1, 1, 60, 2000)), false, 448,
                520, 60);
        screen.initialize();
        screen.inputDelay = Core.getCooldown(0);
        screen.isRunning = true;
        return screen;
    }

    /**
     * Presses and releases a key, updating the stage while it is running.
     *
     * @param screen
     *            Stage to update.
     * @param code
     *            Key to press.
     */
    private static void tap(final GameScreen screen, final int code) {
        key(code, true);
        try {
            screen.update();
        } finally {
            key(code, false);
        }
        if (screen.isRunning)
            screen.update();
    }

    /**
     * Sends a key event to the game input.
     *
     * @param code
     *            Key code.
     * @param pressed
     *            Whether the key is pressed or released.
     */
    private static void key(final int code, final boolean pressed) {
        KeyEvent event = new KeyEvent(KEY_SOURCE,
                pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED,
                System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
        if (pressed)
            Core.getInputManager().keyPressed(event);
        else
            Core.getInputManager().keyReleased(event);
    }

    @Test
    void escPausesAndResumesTheStage() {
        GameScreen screen = stage();
        tap(screen, KeyEvent.VK_ESCAPE);
        assertTrue(screen.isPaused());
        assertTrue(screen.isRunning);

        tap(screen, KeyEvent.VK_ESCAPE);
        assertFalse(screen.isPaused());
        assertFalse(screen.isQuit());
        assertTrue(screen.isRunning);
    }

    @Test
    void choosingNoKeepsTheStageRunning() {
        GameScreen screen = stage();
        for (int code : new int[] { KeyEvent.VK_ESCAPE, KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE, KeyEvent.VK_SPACE })
            tap(screen, code);
        assertTrue(screen.isPaused());
        assertFalse(screen.isQuit());
        assertTrue(screen.isRunning);
    }

    @Test
    void confirmedQuitEndsTheStageWithoutClearingIt() {
        GameScreen screen = stage();
        for (int code : new int[] { KeyEvent.VK_ESCAPE, KeyEvent.VK_DOWN,
                KeyEvent.VK_SPACE, KeyEvent.VK_DOWN, KeyEvent.VK_SPACE })
            tap(screen, code);
        assertTrue(screen.isQuit());
        assertFalse(screen.isRunning);
        assertFalse(screen.isStageCleared());
    }
}
