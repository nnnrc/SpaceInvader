package screen;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;

import campaign.CampaignStageData;
import engine.Core;
import engine.Frame;
import engine.GameState;
import engine.GameplayConfig;
import engine.Score;
import audio.AudioManager;
import records.Achievement;
import records.RecordsService;
import java.util.ArrayList;

/**
 * Implements the score screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class ScoreScreen extends Screen {

	/** Maximum number of high scores. */
	private static final int MAX_HIGH_SCORE_NUM = 7;

	/** Level (stage) that was being played when the game ended. */
	private int level;
	/** Checks if the last stage was failed (no lives left). */
	private boolean stageFailed;
    private final List<Achievement> runUnlocks;
  /** Checks if every campaign stage was cleared. */
  private boolean gameCleared;
	/** Checks if the game that just ended was the infinite mode. */
	private boolean infiniteMode;
    /** Text describing the mode played: difficulty (campaign) or infinite. */
    private String modeLabel;
	/** Current score. */
	private int score;
	/** Player lives left. */
	private int livesRemaining;
	/** Total bullets shot by the player. */
	private int bulletsShot;
	/** Total ships destroyed by the player. */
	private int shipsDestroyed;
	/** List of past high scores. */
	private List<Score> highScores;
	/** Checks if current score is a new high score. */
	private boolean isNewRecord;
	/** Player name for record input, typed freely into {@link #nameField}. */
	private String name = "";
	/** Whether the name has been confirmed (Enter) and saved already. */
	private boolean nameConfirmed;
	/**
	 * Visible text field the player types their name into directly. Shown
	 * and focused for the whole time a new record's name has not been
	 * confirmed yet; Swing handles both plain typing and IME composition
	 * on it natively (see Frame.addOverlayComponent/clipExcludingOverlays
	 * for how it coexists with this game's own per-frame drawing).
	 */
	private final JTextField nameField = new JTextField();

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 * @param gameState
	 *            Current game state.
	 */
    public ScoreScreen(final int width, final int height, final int fps, final GameState state) {
        this(width,height,fps,state,false,Collections.<Achievement>emptyList());
    }
    public ScoreScreen(final int width, final int height, final int fps,
            final GameState state, final boolean infiniteMode) {
        this(width,height,fps,state,infiniteMode,Collections.<Achievement>emptyList());
    }
    public ScoreScreen(final int width, final int height, final int fps,
            final GameState state, final List<Achievement> unlocks) {
        this(width,height,fps,state,false,unlocks);
    }
    public ScoreScreen(final int width, final int height, final int fps,
            final GameState gameState, final boolean infiniteMode, final List<Achievement> unlocks) {
        super(width,height,fps);
        this.runUnlocks = new ArrayList<Achievement>(unlocks);

		this.infiniteMode = infiniteMode;
        this.modeLabel = infiniteMode ? "infinite mode"
                : "mode: " + difficultyName(GameplayConfig.getDifficulty());
		this.level = gameState.getLevel();
		this.stageFailed = gameState.getLivesRemaining() == 0;
        this.gameCleared = !this.infiniteMode && !this.stageFailed
                && gameState.getLevel() > CampaignStageData.getTotalStages();
		this.score = gameState.getScore();
		this.livesRemaining = gameState.getLivesRemaining();
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
		this.isNewRecord = false;

		this.nameField.setFont(Core.getDrawManager().getKoreanFont()
				.deriveFont(14f));
		this.nameField.setBorder(null);
		// Matches the game's black background/green-on-black text instead
		// of Swing's default white field, since our own blit leaves this
		// exact area for Swing alone to paint (see
		// Frame.clipExcludingOverlays).
		this.nameField.setBackground(Color.BLACK);
		this.nameField.setForeground(Color.GREEN);
		this.nameField.setCaretColor(Color.GREEN);
		this.nameField.setFocusTraversalKeysEnabled(false);
		// Escape/Space must keep updating the shared InputManager even
		// while this field (not the main frame) has keyboard focus.
		this.nameField.addKeyListener(Core.getInputManager());
		((AbstractDocument) this.nameField.getDocument())
				.setDocumentFilter(new NameInputFilter());
		this.nameField.addActionListener(event -> confirmName());

		try {
			this.highScores = Core.getFileManager().loadHighScores();
			if (highScores.size() < MAX_HIGH_SCORE_NUM
					|| highScores.get(highScores.size() - 1).getScore()
					< this.score)
				this.isNewRecord = true;

		} catch (IOException e) {
			logger.warning("Couldn't load high scores!");
		}

		if (this.livesRemaining > 0){
			AudioManager.playBGM("audio/bgm/music_end_victory.wav");
		} else {
			AudioManager.playBGM("audio/bgm/music_end_defeat.wav");
		}

        if (this.isNewRecord) {
            AudioManager.playSFX("audio/sfx/new_record.wav");
            showNameField();
        }

        RecordsService recordsService = Core.getRecordsService();
        if (recordsService != null) {
            boolean hasNewAchievement = false;
            Achievement achievement;
            while ((achievement = recordsService.pollNotification()) != null) {
                if (this.runUnlocks.contains(achievement)) {
                    hasNewAchievement = true;
                }
            }
            if (hasNewAchievement && !this.isNewRecord) {
                AudioManager.playSFX("audio/sfx/achievement_unlock.wav");
            }
        }


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
	 * Disables the global mute key (normally toggled by pressing M on any
	 * screen) while the player is actively typing their name — otherwise
	 * a name containing "m"/"M" would toggle audio mute as a side effect
	 * every time that letter is typed.
	 */
	@Override
	protected boolean usesGlobalMuteKey() {
		return !(this.isNewRecord && !this.nameConfirmed);
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		draw();
		if (this.inputDelay.checkFinished()) {
			boolean nameEntryPending = this.isNewRecord && !this.nameConfirmed;

			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
				// Cancel (discarding an unconfirmed new record) and
				// return to the main menu.
				AudioManager.playSFX("audio/sfx/menu_back.wav");
				this.returnCode = 1;
				this.isRunning = false;
				hideNameField();
			} else if (!nameEntryPending
					&& inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
				// Play again. Disabled while a name is still being typed,
				// so an accidental Space while typing cannot end the
				// screen before Enter confirms the name.
				AudioManager.playSFX("audio/sfx/menu_select.wav");
				// The infinite mode (8) restarts itself instead of the campaign.
				this.returnCode = this.infiniteMode ? 8 : 2;
				this.isRunning = false;
				hideNameField();
			}
		}

	}

	/**
	 * Shows and focuses the real, visible name-input field, positioned to
	 * line up with whichever of the two name-input chrome layouts
	 * (plain game-over, or the achievement-unlock combined screen) this
	 * run is about to show.
	 */
	private void showNameField() {
		Frame frame = Core.getFrame();
		if (frame == null)
			return;
		Rectangle bounds = this.runUnlocks.isEmpty()
				? drawManager.getNameInputFieldBounds(this)
				: drawManager.getAchievementNameInputFieldBounds(this,
						this.isNewRecord,
						this.stageFailed && !this.infiniteMode
								? this.level : 0,
						this.runUnlocks.size(), this.modeLabel != null);
		this.nameField.setBounds(bounds);
		this.nameField.setText("");
		frame.addOverlayComponent(this.nameField);
		this.nameField.requestFocusInWindow();
	}

	/**
	 * Removes the name field (if still showing) and returns keyboard
	 * focus to the main game window. Must run whenever this screen is
	 * left, regardless of whether the name was ever confirmed — leaving
	 * the field showing and focused would carry its focus (and the input
	 * method engaged on it) into the next screen.
	 */
	private void hideNameField() {
		Frame frame = Core.getFrame();
		if (frame == null)
			return;
		frame.removeOverlayComponent(this.nameField);
		frame.requestFocusInWindow();
	}

	/**
	 * Confirms and saves the typed name (Enter), requiring at least one
	 * character. Does nothing on an empty field, so Enter cannot save a
	 * blank name.
	 */
	private void confirmName() {
		if (this.nameConfirmed)
			return;
		String typed = this.nameField.getText();
		if (typed == null || typed.isEmpty())
			return;

		this.name = typed;
		this.nameConfirmed = true;
		saveScore();
		hideNameField();
		AudioManager.playSFX("audio/sfx/menu_select.wav");
	}

	/**
	 * Saves the score as a high score.
	 */
	private void saveScore() {
		highScores.add(new Score(this.name, score));
		Collections.sort(highScores);
		if (highScores.size() > MAX_HIGH_SCORE_NUM)
			highScores.remove(highScores.size() - 1);

		try {
			Core.getFileManager().saveHighScores(highScores);
		} catch (IOException e) {
			logger.warning("Couldn't load high scores!");
		}
	}
    /** Converts a GameplayConfig difficulty value to its display name. */
    private static String difficultyName(final int difficulty) {
        switch (difficulty) {
            case GameplayConfig.EASY:  return "Easy";
            case GameplayConfig.HARD:  return "Hard";
            default:                   return "Normal";
        }
    }
	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);
        float accuracy = this.bulletsShot == 0 ? 1f : (float)this.shipsDestroyed / this.bulletsShot;
        if (!this.runUnlocks.isEmpty()) {
            drawManager.drawAchievementResults(this,this.score,this.livesRemaining,
                    this.shipsDestroyed,accuracy,this.isNewRecord,this.nameConfirmed,this.name,
                    this.runUnlocks,this.inputDelay.checkFinished(),
                    this.stageFailed && !this.infiniteMode ? this.level : 0, this.gameCleared,
                    this.modeLabel);
            drawManager.completeDrawing(this); return;
        }

		drawManager.drawGameOver(this, this.inputDelay.checkFinished(),
				this.isNewRecord,this.gameCleared,
				this.isNewRecord && !this.nameConfirmed);
		if (this.stageFailed && !this.infiniteMode)
			drawManager.drawStageFailed(this, this.level, this.isNewRecord);
        drawManager.drawResults(this, this.score, this.livesRemaining,
                this.shipsDestroyed, accuracy, this.isNewRecord);
        drawManager.drawModeLabel(this, this.modeLabel, this.isNewRecord);

		if (this.isNewRecord)
			drawManager.drawNameInput(this, this.nameConfirmed, this.name);

		drawManager.completeDrawing(this);
	}

}
