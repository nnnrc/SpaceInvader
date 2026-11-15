package engine;

import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

/**
 * Manages keyboard input for the provided screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class InputManager implements KeyListener, MouseListener,
		MouseMotionListener, InputMethodListener {

	/** Number of recognised keys. */
	private static final int NUM_KEYS = 256;
	/** Array with the jeys marked as pressed or not. */
	private static boolean[] keys;

	private volatile int lastPressedKey = KeyEvent.VK_UNDEFINED;
	/** Singleton instance of the class. */
	private static InputManager instance;

	/** Text committed by an input method (e.g. a finished Hangul syllable) since the last consume. */
	private volatile String committedText = "";
	/** Text currently being composed by an input method (e.g. an in-progress Hangul syllable). */
	private volatile String composingText = "";

	/** Mouse position relative to the window content. */
	private volatile int mouseX = -1;
	private volatile int mouseY = -1;

	/** Left mouse button state. */
	private volatile boolean mousePressed = false;

	/**
	 * Private constructor.
	 */
	private InputManager() {
		keys = new boolean[NUM_KEYS];
	}

	/**
	 * Returns shared instance of InputManager.
	 *
	 * @return Shared instance of InputManager.
	 */
	protected static InputManager getInstance() {
		if (instance == null)
			instance = new InputManager();
		return instance;
	}

	/**
	 * Returns true if the provided key is currently pressed.
	 *
	 * @param keyCode
	 *                Key number to check.
	 * @return Key state.
	 */
	public boolean isKeyDown(final int keyCode) {
		return keys[keyCode];
	}

	public int consumeLastPressedKey() {
		int keyCode = lastPressedKey;
		lastPressedKey = KeyEvent.VK_UNDEFINED;
		return keyCode;
	}

	public void clearLastPressedKey() {
		lastPressedKey = KeyEvent.VK_UNDEFINED;
	}

	/**
	 * Returns the text an input method (e.g. a Korean IME) has composed so
	 * far but not yet committed, such as a Hangul syllable still being
	 * built from jamo keystrokes.
	 *
	 * @return In-progress composition text, never null.
	 */
	public String getComposingText() {
		return composingText;
	}

	/**
	 * Returns and clears the text an input method has committed since the
	 * last call, such as a finished Hangul syllable.
	 *
	 * @return Newly committed text, never null.
	 */
	public String consumeCommittedText() {
		String text = committedText;
		committedText = "";
		return text;
	}

	/**
	 * Discards any in-progress input method composition, so a stale
	 * partial syllable does not linger after the caller moves on.
	 */
	public void clearComposingText() {
		composingText = "";
	}

	/**
	 * Changes the state of the key to pressed.
	 *
	 * @param key
	 *            Key pressed.
	 */
	@Override
	public void keyPressed(final KeyEvent key) {
		if (key.getKeyCode() >= 0 && key.getKeyCode() < NUM_KEYS) {
			if (!keys[key.getKeyCode()]) {
				lastPressedKey = key.getKeyCode();
			}
			keys[key.getKeyCode()] = true;
		}
	}

	/**
	 * Changes the state of the key to not pressed.
	 *
	 * @param key
	 *            Key released.
	 */
	@Override
	public void keyReleased(final KeyEvent key) {
		if (key.getKeyCode() >= 0 && key.getKeyCode() < NUM_KEYS)
			keys[key.getKeyCode()] = false;
	}

	/**
	 * Does nothing.
	 *
	 * @param key
	 *            Key typed.
	 */
	@Override
	public void keyTyped(final KeyEvent key) {

	}

	/**
	 * Receives composed and committed text from an active input method
	 * (e.g. the OS Korean IME), splitting it into the already-committed
	 * part and the still-being-composed part.
	 *
	 * @param event
	 *            Input method event.
	 */
	@Override
	public void inputMethodTextChanged(final InputMethodEvent event) {
		AttributedCharacterIterator text = event.getText();
		if (text != null) {
			int committedCount = event.getCommittedCharacterCount();
			StringBuilder committed = new StringBuilder();
			StringBuilder composing = new StringBuilder();
			int i = 0;
			for (char c = text.first(); c != CharacterIterator.DONE; c = text
					.next(), i++) {
				if (i < committedCount)
					committed.append(c);
				else
					composing.append(c);
			}
			if (committed.length() > 0)
				committedText += committed.toString();
			composingText = composing.toString();
		}
		event.consume();
	}

	/**
	 * Does nothing; the caret within an in-progress composition is not
	 * used by this game.
	 *
	 * @param event
	 *            Input method event.
	 */
	@Override
	public void caretPositionChanged(final InputMethodEvent event) {
		event.consume();
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public boolean isMousePressed() {
		return mousePressed;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mousePressed = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mousePressed = false;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseX = -1;
		mouseY = -1;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}
}
