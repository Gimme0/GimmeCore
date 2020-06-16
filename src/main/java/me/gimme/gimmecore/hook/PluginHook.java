package me.gimme.gimmecore.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public abstract class PluginHook<T extends Plugin> {

    protected T hookedPlugin = null;

    public PluginHook(@NotNull String pluginName, @NotNull PluginManager pluginManager) {
        Plugin plugin = pluginManager.getPlugin(pluginName);
        if (plugin == null) {
            Bukkit.getLogger().warning("Could not find " + pluginName + ". Some functionality might be disabled.");
            return;
        }

        this.hookedPlugin = (T) plugin;
    }

}
