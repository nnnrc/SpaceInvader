package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Checks the pause menu states: opening, moving, resuming, choosing Settings
 * and confirming a quit, using key states only.
 */
class PauseMenuTest {

    /** Presses and releases Esc. */
    private static PauseMenu.Action back(final PauseMenu menu) {
        PauseMenu.Action action = menu.handleKeys(true, false, false, false);
        menu.handleKeys(false, false, false, false);
        return action;
    }

    /** Presses and releases a down key. */
    private static PauseMenu.Action down(final PauseMenu menu) {
        PauseMenu.Action action = menu.handleKeys(false, false, true, false);
        menu.handleKeys(false, false, false, false);
        return action;
    }

    /** Presses and releases an up key. */
    private static PauseMenu.Action up(final PauseMenu menu) {
        PauseMenu.Action action = menu.handleKeys(false, true, false, false);
        menu.handleKeys(false, false, false, false);
        return action;
    }

    /** Presses and releases a select key. */
    private static PauseMenu.Action select(final PauseMenu menu) {
        PauseMenu.Action action = menu.handleKeys(false, false, false, true);
        menu.handleKeys(false, false, false, false);
        return action;
    }

    @Test
    void escOpensTheMenuWithResumeSelected() {
        PauseMenu menu = new PauseMenu();
        assertFalse(menu.isOpen());
        assertEquals(PauseMenu.Action.PAUSE, back(menu));
        assertTrue(menu.isOpen());
        assertEquals(PauseMenu.RESUME_OPTION, menu.getSelected());
    }

    @Test
    void otherKeysDoNothingWhileTheMenuIsClosed() {
        PauseMenu menu = new PauseMenu();
        assertEquals(PauseMenu.Action.NONE, down(menu));
        assertEquals(PauseMenu.Action.NONE, select(menu));
        assertFalse(menu.isOpen());
    }

    @Test
    void holdingEscActsOnlyOnce() {
        PauseMenu menu = new PauseMenu();
        assertEquals(PauseMenu.Action.PAUSE,
                menu.handleKeys(true, false, false, false));
        assertEquals(PauseMenu.Action.NONE,
                menu.handleKeys(true, false, false, false));
        assertTrue(menu.isOpen());
    }

    @Test
    void escOrResumeClosesTheMenu() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        assertEquals(PauseMenu.Action.RESUME, back(menu));
        assertFalse(menu.isOpen());

        back(menu);
        assertEquals(PauseMenu.Action.RESUME, select(menu));
        assertFalse(menu.isOpen());
    }

    @Test
    void quittingAsksForConfirmationWithNoSelected() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        down(menu);
        assertEquals(PauseMenu.Action.MOVE, down(menu));
        assertEquals(PauseMenu.QUIT_OPTION, menu.getSelected());
        assertEquals(PauseMenu.Action.MOVE, select(menu));
        assertTrue(menu.isConfirmingQuit());
        assertEquals(PauseMenu.CONFIRM_NO, menu.getSelected());
    }

    @Test
    void confirmingYesQuits() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        down(menu);
        down(menu);
        select(menu);
        down(menu);
        assertEquals(PauseMenu.Action.QUIT, select(menu));
        assertFalse(menu.isOpen());
    }

    @Test
    void choosingNoOrEscReturnsToTheMenu() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        down(menu);
        down(menu);
        select(menu);
        assertEquals(PauseMenu.Action.MOVE, select(menu));
        assertFalse(menu.isConfirmingQuit());
        assertTrue(menu.isOpen());
        assertEquals(PauseMenu.QUIT_OPTION, menu.getSelected());

        select(menu);
        assertEquals(PauseMenu.Action.MOVE, back(menu));
        assertFalse(menu.isConfirmingQuit());
        assertTrue(menu.isOpen());
    }

    @Test
    void upAndDownMoveThroughAllOptionsAndWrap() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        down(menu);
        assertEquals(PauseMenu.SETTINGS_OPTION, menu.getSelected());
        down(menu);
        assertEquals(PauseMenu.QUIT_OPTION, menu.getSelected());
        down(menu);
        assertEquals(PauseMenu.RESUME_OPTION, menu.getSelected());
        assertEquals(PauseMenu.Action.MOVE, up(menu));
        assertEquals(PauseMenu.QUIT_OPTION, menu.getSelected());
    }

    @Test
    void choosingSettingsKeepsTheMenuOpen() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        down(menu);
        assertEquals(PauseMenu.Action.SETTINGS, select(menu));
        assertTrue(menu.isOpen());
        assertFalse(menu.isConfirmingQuit());
        assertEquals(PauseMenu.SETTINGS_OPTION, menu.getSelected());
    }

    @Test
    void keysHeldAfterTheSettingsDoNotActUntilPressedAgain() {
        PauseMenu menu = new PauseMenu();
        back(menu);
        menu.waitForKeyRelease();
        assertEquals(PauseMenu.Action.NONE,
                menu.handleKeys(true, false, false, false));
        assertTrue(menu.isOpen());

        menu.handleKeys(false, false, false, false);
        assertEquals(PauseMenu.Action.RESUME, back(menu));
    }
}
