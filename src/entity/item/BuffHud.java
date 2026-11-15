package entity.item;

import entity.PlayerStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Builds the list of effects the HUD shows: item buffs and the slow debuff. */
public final class BuffHud {

    static final String SLOW_NAME = "Slowed";
    private static final long BLINK_THRESHOLD_MS = 2000;
    private static final long BLINK_PERIOD_MS = 250;

    private BuffHud() {
    }

    /** Returns the active item buffs plus the slow debuff, if the player is slowed. */
    public static List<BuffStatus> collect(final BuffManager buffManager,
                                           final PlayerStatus playerStatus, final long now) {
        List<BuffStatus> statuses =
                new ArrayList<>(buffManager.getActiveBuffStatuses(now));

        long slowLeft = playerStatus.getSlowRemainingMillis();
        if (slowLeft > 0)
            statuses.add(new BuffStatus(SLOW_NAME, slowLeft,
                    PlayerStatus.getSlowDuration(), BuffStatus.Kind.DEBUFF));

        return statuses;
    }

    /** Sorts effects by time left and drops those in the hidden half of their blink. */
    public static List<BuffStatus> visible(final List<BuffStatus> statuses,
                                           final long now) {
        List<BuffStatus> sorted = new ArrayList<>(statuses);
        sorted.sort(Comparator.comparingLong(BuffStatus::remainingMillis));

        List<BuffStatus> shown = new ArrayList<>();
        for (BuffStatus status : sorted) {
            if (!isBlinkedOut(status, now))
                shown.add(status);
        }
        return shown;
    }

    private static boolean isBlinkedOut(final BuffStatus status,
                                        final long now) {
        boolean blinking = status.remainingMillis() <= BLINK_THRESHOLD_MS;
        return blinking && (now / BLINK_PERIOD_MS) % 2 != 0;
    }
}
