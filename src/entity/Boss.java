package entity;

import java.awt.Color;

import engine.DrawManager.SpriteType;

/**
 * Implements the boss, a strong enemy that moves left and right
 * and changes phase as its health goes down.
 *
 * Boss Developer 1: structure, health, damage, state, movement, phase, defeat.
 * Boss Developer 2: uses the getters below for attacks and events.
 */
public class Boss extends Entity {

    /**
     * Size in screen pixels of each sprite cell. Other entities use 2; the
     * boss is drawn larger so it stands out from the regular enemies.
     */
    public static final int SCALE = 3;
    /** Width of the boss in pixels (16 sprite cells). */
    public static final int WIDTH = 16 * SCALE;
    /** Height of the boss in pixels (7 sprite cells). */
    public static final int HEIGHT = 7 * SCALE;

    /** States the boss can be in. */
    public enum State {
        IDLE, MOVING, ATTACKING, HIT, DEFEATED
    }

    /** Kinds of bosses and their base values. */
    public enum BossType {
        /** Slow boss. */
        BOSS_A(850, 1, 0.5, 0.25, 500),
        /** Fast boss, gets stronger earlier. */
        BOSS_B(800, 3, 0.7, 0.4, 800);

        /** Maximum health of this kind of boss. */
        private final int maxHealth;
        /** Base horizontal speed in pixels per frame. */
        private final int speed;
        /** Phase 2 starts below this share of max health. */
        private final double phase2Threshold;
        /** Phase 3 starts below this share of max health. */
        private final double phase3Threshold;
        /** Score given to the player for defeating this boss (temporary). */
        private final int pointValue;

        BossType(final int maxHealth, final int speed,
                 final double phase2Threshold, final double phase3Threshold,
                 final int pointValue) {
            this.maxHealth = maxHealth;
            this.speed = speed;
            this.phase2Threshold = phase2Threshold;
            this.phase3Threshold = phase3Threshold;
            this.pointValue = pointValue;
        }

        public int getMaxHealth() {
            return this.maxHealth;
        }

        public int getSpeed() {
            return this.speed;
        }

        public double getPhase2Threshold() {
            return this.phase2Threshold;
        }

        public double getPhase3Threshold() {
            return this.phase3Threshold;
        }

        /** @return Score given for defeating this kind of boss. */
        public int getPointValue() {
            return this.pointValue;
        }
    }

    /** Normal color of the boss. */
    private static final Color NORMAL_COLOR = Color.MAGENTA;
    /** Color the boss flashes when it is hit. */
    private static final Color HIT_COLOR = Color.WHITE;
    /** Number of frames the hit effect lasts (about 0.1s at 60 FPS). */
    private static final int HIT_EFFECT_FRAMES = 6;
    /** Distance in pixels the boss sprite shakes sideways when hit. */
    private static final int HIT_SHAKE_DISTANCE = 2;

    /** Extra speed added in phase 2. */
    private static final int PHASE_2_SPEED_BONUS = 1;
    /** Extra speed added in phase 3. */
    private static final int PHASE_3_SPEED_BONUS = 2;

    /** Kind of this boss. */
    private final BossType type;
    /** Maximum health of the boss. */
    private final int maxHealth;
    /** Current health of the boss. */
    private int currentHealth;
    /** Horizontal speed in pixels per frame. */
    private int speed;
    /** Moving direction: 1 is right, -1 is left. */
    private int direction;
    /** Current state of the boss. */
    private State state;
    /** Current phase: 1, 2 or 3. */
    private int phase;
    /** Frames left in the hit effect, 0 when the boss is not flashing. */
    private int hitEffectFrames;

    /**
     * Constructor, creates the boss and resets its health and state.
     *
     * @param positionX
     *            Initial position of the boss in the X axis.
     * @param positionY
     *            Initial position of the boss in the Y axis.
     * @param type
     *            Kind of boss to create.
     */
    public Boss(final int positionX, final int positionY, final BossType type) {
        // Temporarily reuses the bonus ship sprite (16 x 7 cells, SCALE px each).
        super(positionX, positionY, WIDTH, HEIGHT, NORMAL_COLOR);
        this.spriteType = SpriteType.EnemyShipSpecial;

        this.type = type;
        this.maxHealth = type.getMaxHealth();
        this.currentHealth = this.maxHealth;
        this.speed = type.getSpeed();
        this.direction = 1;
        this.state = State.MOVING;
        this.phase = 1;
    }

    /**
     * Moves the boss and turns it around at the borders of the play area.
     * Call once per frame.
     *
     * @param leftLimit
     *            Smallest X position the boss may reach.
     * @param rightLimit
     *            Largest X position the right edge of the boss may reach.
     */
    public final void update(final int leftLimit, final int rightLimit) {
        if (this.state == State.DEFEATED)
            return;

        // A hit only lasts one frame, then the boss keeps moving.
        if (this.state == State.HIT)
            this.state = State.MOVING;

        updateHitEffect();

        this.positionX += this.speed * this.direction;

        if (this.positionX < leftLimit) {
            this.positionX = leftLimit;
            this.direction = 1;
        } else if (this.positionX + this.width > rightLimit) {
            this.positionX = rightLimit - this.width;
            this.direction = -1;
        }
    }

    /**
     * Applies damage from a player attack.
     *
     * To detect events, compare getPhase() before and after this call
     * (phase changed) and check isDefeated() after it (defeated).
     *
     * @param damage
     *            Amount of health to remove.
     */
    public final void takeDamage(final int damage) {
        if (this.state == State.DEFEATED)
            return;

        this.currentHealth -= damage;
        if (this.currentHealth < 0)
            this.currentHealth = 0;
        if (this.currentHealth > this.maxHealth)
            this.currentHealth = this.maxHealth;

        this.state = State.HIT;

        // A defeated boss does not change phase.
        if (this.currentHealth > 0) {
            updatePhase();
            startHitEffect();
        } else {
            defeat();
        }
    }

    /**
     * Starts the hit effect: the boss turns white and shakes for a few
     * frames. A new hit restarts the effect.
     */
    private void startHitEffect() {
        this.hitEffectFrames = HIT_EFFECT_FRAMES;
        setColor(HIT_COLOR);
    }

    /**
     * Counts down the hit effect and gives the boss its normal color back
     * when the effect is over. Called once per frame from update().
     */
    private void updateHitEffect() {
        if (this.hitEffectFrames == 0)
            return;
        this.hitEffectFrames--;
        if (this.hitEffectFrames == 0)
            setColor(NORMAL_COLOR);
    }

    /**
     * Updates the phase from the current health ratio, using the
     * thresholds of this kind of boss.
     * Each threshold is applied only once and the phase never goes back.
     */
    private void updatePhase() {
        double healthRatio = (double) this.currentHealth / this.maxHealth;

        if (this.phase < 3 && healthRatio < this.type.getPhase3Threshold()) {
            this.phase = 3;
            this.speed = this.type.getSpeed() + PHASE_3_SPEED_BONUS;
        } else if (this.phase < 2 && healthRatio < this.type.getPhase2Threshold()) {
            this.phase = 2;
            this.speed = this.type.getSpeed() + PHASE_2_SPEED_BONUS;
        }
    }

    /**
     * Marks the boss as defeated and stops its movement.
     */
    private void defeat() {
        this.state = State.DEFEATED;
        this.spriteType = SpriteType.Explosion;
        // The explosion is drawn without the hit effect.
        this.hitEffectFrames = 0;
        setColor(NORMAL_COLOR);
    }

    public final BossType getType() {
        return this.type;
    }

    public final int getMaxHealth() {
        return this.maxHealth;
    }

    public final int getCurrentHealth() {
        return this.currentHealth;
    }

    public final int getSpeed() {
        return this.speed;
    }

    public final int getDirection() {
        return this.direction;
    }

    public final State getState() {
        return this.state;
    }

    /** Lets the attack system mark the boss as attacking or moving. */
    public final void setState(final State state) {
        if (this.state != State.DEFEATED)
            this.state = state;
    }

    /** @return Current phase: 1, 2 or 3. */
    public final int getPhase() {
        return this.phase;
    }

    /** @return True while the boss is flashing after a hit. */
    public final boolean isHitEffectActive() {
        return this.hitEffectFrames > 0;
    }

    /**
     * Returns how far to move the boss sprite sideways while it shakes.
     * Only the drawing moves: the position and the hitbox stay the same.
     *
     * @return Offset in pixels, 0 when no hit effect is active.
     */
    public final int getHitShakeOffset() {
        if (this.hitEffectFrames == 0)
            return 0;
        // Moves right and left on every other frame.
        return this.hitEffectFrames % 2 == 0 ? HIT_SHAKE_DISTANCE
                : -HIT_SHAKE_DISTANCE;
    }

    /** @return True when the boss has been defeated. */
    public final boolean isDefeated() {
        return this.state == State.DEFEATED;
    }
}
