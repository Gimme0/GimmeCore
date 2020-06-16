package me.gimme.gimmecore.manager;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Manages player actions that require warmups. Warmup actions are actions that will occur after a set delay unless
 * canceled early by things such as movement or damage. A countdown is displayed on the screen while active.
 */
public class WarmupActionManager implements Listener {

    private Plugin plugin;
    private Map<UUID, WarmupActionTask> taskByPlayer = new HashMap<>();

    public WarmupActionManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Schedules the player to teleport after the specified delay, unless they move outside the block
     * that they started on.
     *
     * @param player                   the player performing the action
     * @param warmup                   the warmup time in seconds
     * @param cancelableByMovement     if it can be canceled by movement
     * @param cancelableByDamage       if it can be canceled by any damage
     * @param cancelableByPlayerDamage if it can be canceled by player damage
     * @param timeToCDMessage          a function that takes the warmup time (in seconds) left and converts it to a
     *                                 message to be displayed on the screen
     * @param action                   an action to be run after the warmup
     */
    public void startWarmupAction(@NotNull Player player, int warmup, boolean cancelableByMovement,
                                  boolean cancelableByDamage, boolean cancelableByPlayerDamage,
                                  @NotNull Function<Number, String> timeToCDMessage, @NotNull Runnable action) {
        cancelTask(player);

        if (warmup <= 0) {
            action.run();
        } else {
            taskByPlayer.put(player.getUniqueId(),
                    new WarmupActionTask(player, warmup, cancelableByMovement, cancelableByDamage,
                            cancelableByPlayerDamage, timeToCDMessage, action).start());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        WarmupActionTask task = taskByPlayer.get(player.getUniqueId());
        if (task == null || !task.cancelableByMovement) return;
        if (isSameBlock(task.startLocation, player.getLocation())) return;

        cancelTask(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        WarmupActionTask task = taskByPlayer.get(player.getUniqueId());
        if (task == null || !task.cancelableByMovement) return;
        if (isSameBlock(task.startLocation, player.getLocation())) return;

        cancelTask(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();

        WarmupActionTask task = taskByPlayer.get(player.getUniqueId());
        if (task == null || !task.cancelableByDamage) return;

        cancelTask(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (!event.getDamager().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();

        WarmupActionTask task = taskByPlayer.get(player.getUniqueId());
        if (task == null || !task.cancelableByPlayerDamage) return;

        cancelTask(player);
    }

    private boolean cancelTask(Player player) {
        WarmupActionTask task = taskByPlayer.get(player.getUniqueId());
        if (task == null) return false;

        task.cancel();
        return true;
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockZ() == loc2.getBlockZ() &&
                loc1.getBlockY() == loc2.getBlockY();
    }

    private class WarmupActionTask extends BukkitRunnable {

        private Location startLocation;
        private Player player;
        private int secondsLeft;
        private Function<Number, String> timeToCDMessage;
        private Runnable action;

        private boolean cancelableByMovement;
        private boolean cancelableByDamage;
        private boolean cancelableByPlayerDamage;

        private WarmupActionTask(@NotNull Player player, int warmup, boolean cancelableByMovement,
                                 boolean cancelableByDamage, boolean cancelableByPlayerDamage,
                                 Function<Number, String> timeToCDMessage, Runnable action) {
            this.startLocation = player.getLocation();
            this.player = player;
            this.secondsLeft = warmup;
            this.timeToCDMessage = timeToCDMessage;
            this.action = action;

            this.cancelableByMovement = cancelableByMovement;
            this.cancelableByDamage = cancelableByDamage;
            this.cancelableByPlayerDamage = cancelableByPlayerDamage;
        }

        @Override
        public void run() {
            player.sendTitle("", timeToCDMessage.apply(secondsLeft),
                    0, 25, 10);

            if (secondsLeft-- <= 0) {
                finish();
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            player.resetTitle();
            taskByPlayer.remove(player.getUniqueId());
        }

        private void finish() {
            cancel();
            action.run();
        }

        private WarmupActionTask start() {
            runTaskTimer(plugin, 0, 20);
            return this;
        }

    }

}
