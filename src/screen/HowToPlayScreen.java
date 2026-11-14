package screen;

import java.awt.event.KeyEvent;

import audio.AudioManager;

/**
 * Shows the controls and items so that new players can learn how to play.
 */
public class HowToPlayScreen extends Screen {

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public HowToPlayScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.returnCode = 1;
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();
        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();
        draw();
        if (this.inputDelay.checkFinished()
                && inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            AudioManager.playSFX("audio/sfx/menu_back.wav");
            this.isRunning = false;
        }
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawHowToPlay(this);
        drawManager.completeDrawing(this);
    }
}
