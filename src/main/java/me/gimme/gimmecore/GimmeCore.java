package me.gimme.gimmecore;

import me.gimme.gimmecore.manager.WarmupActionManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GimmeCore extends JavaPlugin {

    public static final String PLUGIN_NAME = "GimmeCore";

    private WarmupActionManager warmupActionManager;

    /**
     * @return the warmup action manager
     */
    public WarmupActionManager getWarmupActionManager() {
        return warmupActionManager;
    }

    @Override
    public void onEnable() {
        warmupActionManager = new WarmupActionManager(this);
        registerListener(new WarmupActionManager(this));
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

}
