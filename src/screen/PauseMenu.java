package screen;

import java.awt.event.KeyEvent;

import audio.AudioManager;
import engine.InputManager;

/**
 * Keeps the state of the pause menu shown during a game: whether it is open,
 * which option is selected and whether quitting is being confirmed. It only
 * reacts to the key states passed to it, so it can be tested without a
 * screen.
 */
public final class PauseMenu {

    /** What the screen has to do after the keys of one frame. */
    public enum Action {
        /** Nothing changed. */
        NONE,
        /** The menu has just opened: the game must stop. */
        PAUSE,
        /** The selection moved to another option. */
        MOVE,
        /** The menu has closed: the game must go on. */
        RESUME,
        /** Quitting was confirmed: the run must end. */
        QUIT
    }

    /** Options of the pause menu, in order. */
    public static final String[] OPTIONS = { "Resume", "Quit to Main Menu" };
    /** Options of the quit confirmation, in order. */
    public static final String[] CONFIRM_OPTIONS = { "No", "Yes" };
    /** Index of the option that resumes the game. */
    public static final int RESUME_OPTION = 0;
    /** Index of the option that asks to quit. */
    public static final int QUIT_OPTION = 1;
    /** Index of the confirmation option that cancels quitting. */
    public static final int CONFIRM_NO = 0;
    /** Index of the confirmation option that confirms quitting. */
    public static final int CONFIRM_YES = 1;

    /** Whether the menu is shown. */
    private boolean open;
    /** Whether the player is asked to confirm quitting. */
    private boolean confirmingQuit;
    /** Index of the selected option in the current list. */
    private int selected;
    /** Whether the back key was down on the previous frame. */
    private boolean backWasDown;
    /** Whether an up key was down on the previous frame. */
    private boolean upWasDown;
    /** Whether a down key was down on the previous frame. */
    private boolean downWasDown;
    /** Whether a select key was down on the previous frame. */
    private boolean selectWasDown;

    /**
     * Reads the keys of one frame and plays the matching feedback: the
     * background music pauses while the menu is open, and the menu sounds
     * are the same as on the other menu screens. Esc opens and closes the
     * menu, Up / W and Down / S move the selection, and Space / Enter choose.
     *
     * @param input
     *            Keyboard state of the game.
     * @return What the screen has to do.
     */
    public Action update(final InputManager input) {
        Action action = handleKeys(input.isKeyDown(KeyEvent.VK_ESCAPE),
                input.isKeyDown(KeyEvent.VK_UP)
                        || input.isKeyDown(KeyEvent.VK_W),
                input.isKeyDown(KeyEvent.VK_DOWN)
                        || input.isKeyDown(KeyEvent.VK_S),
                input.isKeyDown(KeyEvent.VK_SPACE)
                        || input.isKeyDown(KeyEvent.VK_ENTER));
        switch (action) {
        case PAUSE:
            AudioManager.pauseBGM();
            AudioManager.playSFX("audio/sfx/menu_select.wav");
            break;
        case MOVE:
            AudioManager.playSFX("audio/sfx/menu_move.wav");
            break;
        case RESUME:
            AudioManager.resumeBGM();
            AudioManager.playSFX("audio/sfx/menu_select.wav");
            break;
        case QUIT:
            AudioManager.stopBGM();
            AudioManager.resumeBGM();
            AudioManager.playSFX("audio/sfx/menu_back.wav");
            break;
        default:
            break;
        }
        return action;
    }

    /**
     * Reacts to the keys of one frame. Each key acts once when it is pressed,
     * not on every frame it is held.
     *
     * @param backDown
     *            Whether the back key (Esc) is down.
     * @param upDown
     *            Whether a key moving the selection up is down.
     * @param downDown
     *            Whether a key moving the selection down is down.
     * @param selectDown
     *            Whether a key choosing the selected option is down.
     * @return What the screen has to do.
     */
    public Action handleKeys(final boolean backDown, final boolean upDown,
            final boolean downDown, final boolean selectDown) {
        boolean back = backDown && !this.backWasDown;
        boolean up = upDown && !this.upWasDown;
        boolean down = downDown && !this.downWasDown;
        boolean select = selectDown && !this.selectWasDown;
        this.backWasDown = backDown;
        this.upWasDown = upDown;
        this.downWasDown = downDown;
        this.selectWasDown = selectDown;

        if (!this.open)
            return back ? open() : Action.NONE;
        if (back)
            return back();
        if (select)
            return select();
        if (up != down) {
            this.selected = this.selected == 0 ? 1 : 0;
            return Action.MOVE;
        }
        return Action.NONE;
    }

    /**
     * @return Whether the menu is shown.
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * @return Whether the player is asked to confirm quitting.
     */
    public boolean isConfirmingQuit() {
        return this.confirmingQuit;
    }

    /**
     * @return Index of the selected option in the list currently shown.
     */
    public int getSelected() {
        return this.selected;
    }

    /**
     * Opens the menu with Resume selected.
     *
     * @return The pause action.
     */
    private Action open() {
        this.open = true;
        this.confirmingQuit = false;
        this.selected = RESUME_OPTION;
        return Action.PAUSE;
    }

    /**
     * Goes back one step: from the confirmation to the menu, or from the
     * menu to the game.
     *
     * @return What the screen has to do.
     */
    private Action back() {
        if (this.confirmingQuit) {
            this.confirmingQuit = false;
            this.selected = QUIT_OPTION;
            return Action.MOVE;
        }
        this.open = false;
        return Action.RESUME;
    }

    /**
     * Chooses the selected option. Quitting always asks first, with No
     * selected so that a second press cannot end the run by mistake.
     *
     * @return What the screen has to do.
     */
    private Action select() {
        if (!this.confirmingQuit) {
            if (this.selected == RESUME_OPTION) {
                this.open = false;
                return Action.RESUME;
            }
            this.confirmingQuit = true;
            this.selected = CONFIRM_NO;
            return Action.MOVE;
        }
        if (this.selected == CONFIRM_YES) {
            this.open = false;
            this.confirmingQuit = false;
            return Action.QUIT;
        }
        this.confirmingQuit = false;
        this.selected = QUIT_OPTION;
        return Action.MOVE;
    }
}
