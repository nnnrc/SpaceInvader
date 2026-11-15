package engine;

import java.awt.Insets;
import java.util.Locale;

import javax.swing.JFrame;

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

		// Lets the OS input method (e.g. a Korean IME) compose text while
		// this frame holds keyboard focus, instead of only raw key codes.
		enableInputMethods(true);
		addInputMethodListener(Core.getInputManager());
	}

	/**
	 * Switches the active input method's locale, so screens can offer a
	 * Korean/English input toggle without the player leaving the game to
	 * change their OS input method.
	 *
	 * @param locale
	 *               Locale to request from the input context.
	 */
	public final void selectInputLocale(final Locale locale) {
		if (getInputContext() != null)
			getInputContext().selectInputMethod(locale);
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
