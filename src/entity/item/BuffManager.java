package entity.item;

import entity.Ship;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import audio.AudioManager;

public class BuffManager {

    private final Map<Item, Long> activeBuffs;

    public BuffManager() {
        this.activeBuffs = new HashMap<>();
    }

    public void addBuff(final Item item, final Ship ship) {
        addBuff(item, ship, System.currentTimeMillis());
    }

    /** Applies the item's effect; picking up an active item restarts its timer. */
    public void addBuff(final Item item, final Ship ship, final long now) {
        removeEarlierPickup(item);
        item.applyEffect(ship);

        if (item.getDuration() > 0)
            this.activeBuffs.put(item, now + item.getDuration());
    }

    /** Stops an earlier pickup's timer from ending the refreshed effect early. */
    private void removeEarlierPickup(final Item item) {
        this.activeBuffs.keySet().removeIf(
                active -> active.getClass() == item.getClass());
    }

    public void update(final Ship ship) {
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<Item, Long>> iterator =
                this.activeBuffs.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Item, Long> entry = iterator.next();

            if (currentTime >= entry.getValue()) {
                if (entry.getKey() instanceof ShieldItem)
                    AudioManager.playSFX("audio/sfx/status_shield_off.wav");
                else
                    AudioManager.playSFX("audio/sfx/item_buff_expire.wav");
                entry.getKey().removeEffect(ship);
                iterator.remove();
            }
        }
    }

    public void resetAllBuffs(final Ship ship) {
        for (Item item : this.activeBuffs.keySet()) {
            item.removeEffect(ship);
        }

        this.activeBuffs.clear();
    }

    /** Lists the active timed buffs with their time left, in no particular order. */
    public List<BuffStatus> getActiveBuffStatuses(final long now) {
        List<BuffStatus> statuses = new ArrayList<>();

        for (Map.Entry<Item, Long> entry : this.activeBuffs.entrySet()) {
            Item item = entry.getKey();
            long remaining = Math.max(0, entry.getValue() - now);

            statuses.add(new BuffStatus(item.getName(), remaining,
                    item.getDuration(), BuffStatus.Kind.BUFF));
        }

        return statuses;
    }

    public Map<String, Long> getActiveBuffsRemainingTime() {
        Map<String, Long> remainingTimes = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Item, Long> entry : this.activeBuffs.entrySet()) {
            long remaining = Math.max(
                    0,
                    (entry.getValue() - currentTime) / 1000
            );

            remainingTimes.put(entry.getKey().getName(), remaining);
        }

        return remainingTimes;
    }
}
