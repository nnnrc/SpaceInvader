package engine;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
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
	 * and focus; this list only exists so our own full-frame blit can
	 * carve their area out of what it paints (see
	 * {@link #clipExcludingOverlays(Rectangle)}), instead of fighting
	 * Swing's own repainting of them for the same pixels.
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
	 * visible name-input field), and starts carving its area out of our
	 * own full-frame blit (see {@link #clipExcludingOverlays(Rectangle)})
	 * so it does not get erased by that blit. The caller owns the
	 * component's bounds, font, focus and content; this only makes it
	 * visible and keeps it visible.
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
	 * Subtracts every visible overlay component's bounds from the given
	 * area, so a caller painting that area (our own full-frame blit) does
	 * not draw over any of them.
	 *
	 * This exists instead of forcing overlay components to repaint after
	 * our blit (which an earlier version of this class did via
	 * {@code Component.paintImmediately}): that approach called Swing
	 * painting from this game's own render thread, not the AWT event
	 * thread that Swing requires, and fought with the input method
	 * delivering composition events on that same event thread — in
	 * testing it caused visible lag and the game to lock up. Carving the
	 * overlay's pixels out of our own blit means we never touch them at
	 * all, so Swing can repaint that area on its own, correctly, entirely
	 * on the event thread, and our render thread never has to call into
	 * Swing per frame.
	 *
	 * @param bounds
	 *               Area to clip, in this frame's own coordinate space
	 *               (i.e. including window insets).
	 * @return The given area, minus every visible overlay component's
	 *         bounds.
	 */
	public final Shape clipExcludingOverlays(final Rectangle bounds) {
		Area area = new Area(bounds);
		Insets insets = getInsets();
		for (JComponent component : overlayComponents) {
			if (!component.isShowing())
				continue;
			Rectangle componentBounds = component.getBounds();
			componentBounds.translate(insets.left, insets.top);
			area.subtract(new Area(componentBounds));
		}
		return area;
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
