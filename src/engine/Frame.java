package engine;

import java.awt.Insets;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JTextField;

import screen.Screen;

/**
 * Implements a frame to show screens on.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
@SuppressWarnings("serial")
public class Frame extends JFrame {

	/** Frame width. */
	private int width;
	/** Frame height. */
	private int height;
	/** Screen currently shown. */
	private Screen currentScreen;
	/**
	 * Invisible text field that receives keyboard focus while the player
	 * is composing a Korean name. A plain AWT Frame does not reliably
	 * support IME composition (Hangul syllable building) on every
	 * platform, but Swing's own text components do, since every Korean/
	 * Japanese/Chinese-capable Java app already relies on them for that.
	 * This borrows that working support instead of reimplementing it.
	 */
	private final JTextField imeProxy = new JTextField();

	/**
	 * Initializes the new frame.
	 *
	 * @param width
	 *               Frame width.
	 * @param height
	 *               Frame height.
	 */
	public Frame(final int width, final int height) {
		setSize(width, height);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setLocationRelativeTo(null);
		setVisible(true);

		Insets insets = getInsets();
		this.width = width - insets.left - insets.right;
		this.height = height - insets.top + insets.bottom;
		setTitle("Invaders");

		addKeyListener(Core.getInputManager());
		getContentPane().addMouseListener(Core.getInputManager());
		getContentPane().addMouseMotionListener(Core.getInputManager());

		// Tab is used in-game (e.g. to switch the name input language), so
		// it must not be swallowed by Swing's focus traversal instead.
		setFocusTraversalKeysEnabled(false);

		// The proxy is kept off-screen and invisible; it only exists to
		// borrow Swing's IME support, never to be seen or clicked.
		imeProxy.setBounds(-100, -100, 1, 1);
		imeProxy.setOpaque(false);
		imeProxy.setBorder(null);
		imeProxy.setFocusTraversalKeysEnabled(false);
		// Not focusable until Korean mode explicitly requests it: Swing's
		// default focus-traversal policy can hand a newly-focusable
		// descendant initial/default focus on window activation on its
		// own, which would silently steal focus (and, by extension, the
		// window's shared input context) away from normal gameplay.
		imeProxy.setFocusable(false);
		// Key codes (Tab, arrows, digits, Escape...) must keep working
		// through the shared InputManager even while the proxy has focus.
		imeProxy.addKeyListener(Core.getInputManager());
		getLayeredPane().add(imeProxy);
	}

	/**
	 * Gives keyboard focus to the invisible IME proxy field and clears it,
	 * so the player can start composing a Korean syllable from scratch.
	 * Call this when the name field switches into Korean input mode.
	 *
	 * Focus alone only lets composition work if it happens; it does not
	 * pick which language is actually active, so this also asks the
	 * shared input context to switch to Korean. That only succeeds if a
	 * Korean input source is installed/enabled in the OS; otherwise
	 * keystrokes keep producing plain Latin characters.
	 */
	public final void focusKoreanInput() {
		imeProxy.setText("");
		imeProxy.setFocusable(true);
		imeProxy.requestFocusInWindow();
		if (imeProxy.getInputContext() != null)
			imeProxy.getInputContext().selectInputMethod(Locale.KOREAN);
	}

	/**
	 * Returns keyboard focus to the game window itself and clears the IME
	 * proxy, so raw key codes (e.g. the existing Up/Down letter cycling)
	 * go back to working exactly as before. Also switches the shared
	 * input context back to English, and makes the proxy unfocusable
	 * again so it can never end up holding focus (and the input context
	 * stuck on Korean) outside of Korean input mode. Call this when
	 * leaving Korean input mode, including when leaving the screen that
	 * offered it altogether.
	 */
	public final void focusGameInput() {
		imeProxy.setText("");
		if (imeProxy.getInputContext() != null)
			imeProxy.getInputContext().selectInputMethod(Locale.ENGLISH);
		// Order matters: dropping focusable while the proxy still owns
		// focus makes Swing transfer focus away on its own; requesting
		// focus for the frame afterward makes that explicit rather than
		// leaving it to whatever Swing's traversal policy picks.
		imeProxy.setFocusable(false);
		requestFocusInWindow();
	}

	/**
	 * Returns whatever the IME proxy field currently holds: either a
	 * Hangul syllable still being composed from jamo keystrokes, or one
	 * already committed. Swing shows both the same way through
	 * {@code getText()}, so the caller does not need to tell them apart.
	 *
	 * @return Current proxy field text, never null.
	 */
	public final String getComposingName() {
		return imeProxy.getText();
	}

	/**
	 * Clears the IME proxy field, so a finished or abandoned syllable does
	 * not linger into the next name slot.
	 */
	public final void clearComposingName() {
		imeProxy.setText("");
	}

	/**
	 * Sets current screen.
	 *
	 * @param screen
	 *               Screen to show.
	 * @return Return code of the finished screen.
	 */
	public final int setScreen(final Screen screen) {
		currentScreen = screen;
		currentScreen.initialize();
		return currentScreen.run();
	}

	/**
	 * Getter for frame width.
	 *
	 * @return Frame width.
	 */
	public final int getWidth() {
		return this.width;
	}

	/**
	 * Getter for frame height.
	 *
	 * @return Frame height.
	 */

	public final int getHeight() {
		return this.height;
	}
}
