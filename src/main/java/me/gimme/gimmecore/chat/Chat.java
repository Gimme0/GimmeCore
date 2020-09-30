package me.gimme.gimmecore.chat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class Chat {
    public static void sendActionBar(@NotNull Player player, @NotNull String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));
    }

    public static void hideActionBar(@NotNull Player player) {
        sendActionBar(player, "");
    }

    public static void broadcastActionBar(@NotNull String text) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            sendActionBar(player, text);
        }
    }

    public static void sendProgressBar(@NotNull Plugin plugin, @Nullable Chat.StopCondition stopCondition,
                                       @NotNull Player player, int durationTicks, @Nullable String title,
                                       @NotNull BarColor color, @NotNull BarFlag... flags) {
        sendProgressBar(plugin, stopCondition, Collections.singletonList(player), durationTicks, title, color, flags);
    }

    public static void sendProgressBar(@NotNull Plugin plugin, @Nullable Chat.StopCondition stopCondition,
                                       @NotNull Iterable<? extends Player> players, int durationTicks,
                                       @Nullable String title, @NotNull BarColor color, @NotNull BarFlag... flags) {
        BossBar bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID, flags);

        for (Player player : players) {
            bossBar.addPlayer(player);
        }

        new BukkitRunnable() {
            int ticks = durationTicks;

            @Override
            public void run() {
                if (ticks <= 0 || (stopCondition != null && stopCondition.shouldStop())) {
                    bossBar.removeAll();
                    cancel();
                    return;
                }

                bossBar.setProgress((double) ticks / durationTicks);

                ticks--;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public static void broadcastProgressBar(@NotNull Plugin plugin, @Nullable Chat.StopCondition stopCondition,
                                            int durationTicks, @Nullable String title, @NotNull BarColor color,
                                            @NotNull BarFlag... flags) {
        sendProgressBar(plugin, stopCondition, Bukkit.getServer().getOnlinePlayers(), durationTicks, title, color, flags);
    }

    public interface StopCondition {
        boolean shouldStop();
    }
}
