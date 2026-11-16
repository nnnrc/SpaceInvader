package engine;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
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
	 * Real Swing components (e.g. a visible name-input text field) that a
	 * screen has layered on top of the game's own drawing. The screen
	 * owning each component is responsible for its content, bounds, font
	 * and focus; this list only exists so those components keep being
	 * repainted after every one of our own full-frame blits (see
	 * {@link #paintOverlays()}).
	 */
	private final List<JComponent> overlayComponents = new ArrayList<JComponent>();

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

		// Tab still toggles the name field's input language for now (this
		// is being removed in favor of OS-level switching in a follow-up
		// change), so it must not be swallowed by Swing's focus traversal
		// instead.
		setFocusTraversalKeysEnabled(false);
	}

	/**
	 * Adds a Swing component on top of the game's own drawing (e.g. a
	 * visible name-input field), and starts repainting it after every
	 * frame our own blit draws (see {@link #paintOverlays()}) so it does
	 * not get erased by that blit. The caller owns the component's
	 * bounds, font, focus and content; this only makes it visible and
	 * keeps it visible.
	 *
	 * @param component
	 *                   Component to add.
	 */
	public final void addOverlayComponent(final JComponent component) {
		overlayComponents.add(component);
		getLayeredPane().add(component);
	}

	/**
	 * Removes a component previously added with
	 * {@link #addOverlayComponent(JComponent)}.
	 *
	 * @param component
	 *                   Component to remove.
	 */
	public final void removeOverlayComponent(final JComponent component) {
		overlayComponents.remove(component);
		getLayeredPane().remove(component);
	}

	/**
	 * Forces every overlay component to repaint itself immediately.
	 * Swing normally repaints a component only when it decides something
	 * about it changed (focus, caret blink, text edits); it has no way to
	 * know our own code just painted over it with a full-frame blit. This
	 * must run right after that blit, every frame, or an overlay
	 * component layered on top of our drawing would mostly not be visible.
	 */
	public final void paintOverlays() {
		for (JComponent component : overlayComponents)
			if (component.isShowing())
				component.paintImmediately(0, 0, component.getWidth(),
						component.getHeight());
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
