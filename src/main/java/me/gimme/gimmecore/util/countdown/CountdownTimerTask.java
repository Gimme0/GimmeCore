package me.gimme.gimmecore.util.countdown;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public abstract class CountdownTimerTask extends BukkitRunnable {

    private Plugin plugin;
    private long seconds;

    protected CountdownTimerTask(@NotNull Plugin plugin, long seconds) {
        this.plugin = plugin;
        this.seconds = seconds;
    }

    @Override
    public void run() {
        if (seconds > 0) {
            onCount();
            seconds--;
        }
        else finish();
    }

    public void finish() {
        cancel();
        onFinish();
    }

    protected abstract void onCount();
    protected abstract void onFinish();

    @NotNull
    public CountdownTimerTask start() {
        runTaskTimer(plugin, 0, 20);
        return this;
    }

    public long getSeconds() {
        return seconds;
    }
}
