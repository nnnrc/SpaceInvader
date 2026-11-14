package screen;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import audio.AudioManager;
import engine.Cooldown;
import engine.Core;
import entity.Entity;
import entity.item.AnnihilatorItem;
import entity.item.ChameleonHull;
import entity.item.ClusterGrenadeItem;
import entity.item.Hyperfire;
import entity.item.ShieldItem;
import entity.item.TwinCanon;

/**
 * Shows the controls and the items on separate pages, so that new players
 * can learn how to play.
 */
public class HowToPlayScreen extends Screen {

    /** Number of pages: controls and items. */
    public static final int PAGE_COUNT = 2;
    /** Milliseconds between page changes. */
    private static final int PAGE_CHANGE_TIME = 200;

    /** Page currently shown, starting at 0. */
    private int page = 0;
    /** Time between page changes. */
    private Cooldown pageCooldown;
    /** One example of each item, drawn next to its description. */
    private List<Entity> previewItems;

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
        this.pageCooldown = Core.getCooldown(PAGE_CHANGE_TIME);
        this.pageCooldown.reset();

        this.previewItems = new ArrayList<Entity>();
        this.previewItems.add(new ShieldItem(0, 0));
        this.previewItems.add(new TwinCanon(0, 0));
        this.previewItems.add(new Hyperfire(0, 0));
        this.previewItems.add(new ChameleonHull(0, 0));
        this.previewItems.add(new ClusterGrenadeItem(0, 0));
        this.previewItems.add(new AnnihilatorItem(0, 0, null));
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
        if (this.inputDelay.checkFinished()
                && this.pageCooldown.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)
                    && this.page < PAGE_COUNT - 1) {
                this.page++;
                this.pageCooldown.reset();
                AudioManager.playSFX("audio/sfx/menu_move.wav");
            } else if (inputManager.isKeyDown(KeyEvent.VK_LEFT)
                    && this.page > 0) {
                this.page--;
                this.pageCooldown.reset();
                AudioManager.playSFX("audio/sfx/menu_move.wav");
            }
        }
        if (this.inputDelay.checkFinished()
                && inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            AudioManager.playSFX("audio/sfx/menu_back.wav");
            this.isRunning = false;
        }
        draw();
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawHowToPlay(this, this.page, PAGE_COUNT,
                this.previewItems);
        drawManager.completeDrawing(this);
    }
}
