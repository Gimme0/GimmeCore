package me.gimme.gimmecore.scoreboard;

import me.gimme.gimmecore.util.TimeFormat;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TimerScoreboardManager implements Listener {

    public interface OnFinishCallback {
        void onFinish();
    }

    private static final String OBJECTIVE_TIMERS_NAME = "timers";

    private Plugin plugin;
    private Server server;
    private String header;

    private Map<UUID, Map<String, CountdownTimerTask>> timersByPlayer = new HashMap<>();
    private Map<String, CountdownTimerTask> eventTimersByTitle = new HashMap<>();
    private Map<String, Queue<Player>> playerEventSyncQueueByTitle = new HashMap<>();

    public TimerScoreboardManager(@NotNull Plugin plugin, String header) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.header = header;
    }

    /**
     * Starts a timer on the scoreboard of a player and calls the callback when the timer finishes.
     * If there was a timer already active with the same title it will be finished immediately and replaced by the new timer.
     *
     * @param player   the player to start the timer for
     * @param title    the title to use on the scoreboard
     * @param duration the duration of the countdown timer in seconds
     * @param score    the score on the scoreboard to use when displaying the timer, higher scores will sort higher
     * @param callback the callback when the countdown timer finishes
     */
    public void startPlayerTimer(@NotNull Player player, @NotNull String title, long duration, int score,
                                 @Nullable TimerScoreboardManager.OnFinishCallback callback) {
        finishPlayerTimer(player, title);
        timersByPlayer.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(title, new ScoreboardTimerTask(
                player.getUniqueId(), title, duration, score, callback).start());
    }

    /**
     * Starts an event timer on every online players scoreboard and calls the callback when the event timer finishes.
     * If the specified title is null, the duration automatically becomes 0.
     * If there was an event timer already active with the same title it will be canceled and replaced by the new timer.
     * Players joining after the event has started will still get the timer synced with everyone else.
     *
     * @param title    the title to use on the scoreboard, or null if the event has no duration
     * @param duration the duration of the event
     * @param callback the callback when the event finishes
     */
    public void startEventTimer(@Nullable String title, long duration, int score, @Nullable TimerScoreboardManager.OnFinishCallback callback) {
        if (title == null) {
            if (callback != null) callback.onFinish();
            return;
        }

        cancelEvent(title);

        for (Player player : server.getOnlinePlayers()) {
            startPlayerTimer(player, title, duration, score, null);
        }
        eventTimersByTitle.put(title, new CountdownTimerTask(plugin, duration) {
            @Override
            protected void onCount() { // Sync up any player that recently joined and does not have the timer yet
                Queue<Player> playerEventSyncQueue = playerEventSyncQueueByTitle.get(title);
                if (playerEventSyncQueue == null) return;

                while (playerEventSyncQueue.size() > 0) {
                    Player player = playerEventSyncQueue.poll();
                    startPlayerTimer(player, title, getSeconds(), score, null);
                }
                playerEventSyncQueueByTitle.remove(title);
            }

            @Override
            protected void onFinish() {
                eventTimersByTitle.remove(title);
                if (callback != null) callback.onFinish();
            }
        }.start());
    }

    /**
     * Cancels an active event timer.
     * All players' scoreboards are updated accordingly.
     * Returns true if an event with the specified title was active and is now canceled.
     *
     * @param title the title of the event to cancel
     * @return true if an event with the specified title was active and is now canceled
     */
    public boolean cancelEvent(@NotNull String title) {
        CountdownTimerTask eventTask = eventTimersByTitle.remove(title);
        if (eventTask == null) return false;
        eventTask.cancel();
        for (Player player : server.getOnlinePlayers()) {
            finishPlayerTimer(player, title);
        }
        return true;
    }

    private void finishPlayerTimer(@NotNull Player player, @NotNull String title) {
        Map<String, CountdownTimerTask> taskByTitle = timersByPlayer.get(player.getUniqueId());
        if (taskByTitle == null) return;
        CountdownTimerTask oldTask = taskByTitle.get(title);
        if (oldTask != null) oldTask.finish();
    }

    /**
     * Sets an entry on the specified player's timer scoreboard.
     * The specified entry is removed and the specified new entry is added.
     *
     * @param playerId the ID of the player who's scoreboard to update
     * @param entry    the previous entry, or null if no previous entry (only adds new entry)
     * @param newEntry the new entry to set, or null if the entry should just be removed
     * @param score    the score to set for the entry
     */
    private void setEntry(@NotNull UUID playerId, @Nullable String entry, @Nullable String newEntry, int score) {
        Player player = server.getPlayer(playerId);
        if (player == null) return;
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(OBJECTIVE_TIMERS_NAME);

        if (objective == null) return;

        if (entry != null) scoreboard.resetScores(entry);
        if (newEntry != null) {
            objective.getScore(newEntry).setScore(score);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else if (scoreboard.getEntries().isEmpty()) {
            objective.setDisplaySlot(null);
        }
    }

    /**
     * Registers the timer display objective and syncs any events that are ongoing
     * for the player.
     */
    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Scoreboard scoreboard = PerPlayerScoreboardProvider.setupScoreboard(player);
        if (scoreboard.getObjective(OBJECTIVE_TIMERS_NAME) == null)
            scoreboard.registerNewObjective(OBJECTIVE_TIMERS_NAME, "dummy", header);

        for (String eventTitle : eventTimersByTitle.keySet()) {
            playerEventSyncQueueByTitle.computeIfAbsent(eventTitle, k -> new ArrayDeque<>()).offer(player);
        }
    }

    private class ScoreboardTimerTask extends CountdownTimerTask {
        private UUID player;
        private String title;
        private String currentScoreName;
        private int score;
        private OnFinishCallback callback;

        private ScoreboardTimerTask(@NotNull UUID player, @NotNull String title, long seconds, int score,
                                    @Nullable TimerScoreboardManager.OnFinishCallback callback) {
            super(plugin, seconds);
            this.player = player;
            this.title = title;
            this.score = score;
            this.callback = callback;
        }

        @Override
        protected void onCount() {
            String newScoreName = ChatColor.translateAlternateColorCodes('&', title) +
                    ChatColor.BOLD +
                    TimeFormat.digitalTimeMinimalized(getSeconds());

            setEntry(player, currentScoreName, newScoreName, score);
            currentScoreName = newScoreName;
        }

        @Override
        protected void onFinish() {
            Map<String, CountdownTimerTask> taskByTitle = timersByPlayer.get(player);
            if (taskByTitle != null) taskByTitle.remove(title);
            setEntry(player, currentScoreName, null, score);
            if (callback != null) callback.onFinish();
        }
    }

}
