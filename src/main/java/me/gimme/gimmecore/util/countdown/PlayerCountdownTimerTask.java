package me.gimme.gimmecore.util.countdown;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerCountdownTimerTask extends CountdownTimerTask {
    private Server server;
    private UUID playerId;
    @Nullable private String title;
    @Nullable private String subtitle;
    @Nullable private String finishTitle;
    @Nullable private String finishSubtitle;

    public PlayerCountdownTimerTask(@NotNull Plugin plugin, long seconds, @NotNull Player player,
                                    @Nullable String title, @Nullable String subtitle,
                                    @Nullable String finishTitle, @Nullable String finishSubtitle) {
        super(plugin, seconds);

        this.server = plugin.getServer();
        this.playerId = player.getUniqueId();
        this.title = title;
        this.subtitle = subtitle;
        this.finishTitle = finishTitle;
        this.finishSubtitle = finishSubtitle;
    }

    @Override
    protected void onCount() {
        Player player = server.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;

        player.sendTitle(title, subtitle + " " + getSeconds() + "s", 0, 25, 10);
    }

    @Override
    protected void onFinish() {
        Player player = server.getPlayer(playerId);
        if (player == null || !player.isOnline()) return;

        player.sendTitle(finishTitle, finishSubtitle, 0, 20, 20);
    }
}
