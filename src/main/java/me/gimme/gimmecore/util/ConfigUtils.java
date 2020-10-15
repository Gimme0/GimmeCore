package me.gimme.gimmecore.util;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ConfigUtils {

    /**
     * Returns the YAML config at the specified file path. If the file does not exist, a new one is created from the
     * defaults resource with that name embedded in the specified plugin's jar file.
     *
     * @param plugin   the plugin that has the resource with the default config
     * @param filePath the path to the config file
     * @return the loaded YAML config
     */
    public static YamlConfiguration getYamlConfig(@NotNull Plugin plugin, @NotNull String filePath) {
        saveDefaultConfig(plugin, filePath, filePath);

        File configFile = new File(plugin.getDataFolder(), filePath);
        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

    /**
     * If it does not already exist, creates a new config file at the specified file path from the default resource in
     * the jar located at the specified resource path.
     *
     * @param plugin the plugin containing the config file and resource
     * @param path   the path to the resource and the path to create the file at if not already existing, relative to
     *               the plugin's data folder
     */
    public static void saveDefaultConfig(@NotNull Plugin plugin, @NotNull String path) {
        saveDefaultConfig(plugin, path, path);
    }

    /**
     * If it does not already exist, creates a new config file at the specified file path from the default resource in
     * the jar located at the specified resource path.
     *
     * @param plugin       the plugin containing the config file and resource
     * @param filePath     the path to create the file at if not already existing, relative to the plugin's data folder
     * @param resourcePath the path to the resource config to use as default
     */
    public static void saveDefaultConfig(@NotNull Plugin plugin, @NotNull String filePath, @NotNull String resourcePath) {
        File configFile = new File(plugin.getDataFolder(), filePath);
        if (!configFile.isFile()) {
            try {
                Files.createDirectories(configFile.toPath().getParent());
                plugin.saveResource(resourcePath, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reloads the config at the specified path to include any changes made in the file since the last load.
     *
     * @param plugin the plugin containing the config file and resource
     * @param path   the path to the resource config to get default values from, and the path to the file to reload,
     *               relative to the plugin's data folder
     * @return
     */
    public static YamlConfiguration reloadConfig(@NotNull Plugin plugin, @NotNull String path) {
        return reloadConfig(plugin, path, path);
    }

    /**
     * Reloads the config at the specified path to include any changes made in the file since the last load.
     *
     * @param plugin       the plugin containing the config file and resource
     * @param filePath     the path to the file to reload, relative to the plugin's data folder
     * @param resourcePath the path to the resource config to get default values from
     * @return
     */
    public static YamlConfiguration reloadConfig(@NotNull Plugin plugin, @NotNull String filePath, String resourcePath) {
        File file = new File(plugin.getDataFolder(), filePath);

        YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(file);

        final InputStream defConfigStream = plugin.getResource(resourcePath);
        if (defConfigStream == null) return newConfig;

        newConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));

        return newConfig;
    }

    /**
     * Serializes and saves the specified object to the json file at the specified file path.
     *
     * @param pluginDataFolder the plugin data folder where the json file should be created
     * @param filePath         the file path of the json file
     * @param object           the object to save
     * @throws IOException
     * @throws IllegalArgumentException if the file path has the wrong extension
     */
    public static void saveToJson(File pluginDataFolder, String filePath, Object object) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        saveToJson(pluginDataFolder, filePath, object, gson);
    }

    /**
     * Serializes and saves the specified object to the json file at the specified file path. The gson can be used to
     * register type adapters or add other serialization settings.
     *
     * @param pluginDataFolder the plugin data folder where the json file should be created
     * @param filePath         the file path of the json file
     * @param object           the object to save
     * @param gson             the Gson object to use in the serializing
     * @throws IOException
     * @throws IllegalArgumentException if the file path has the wrong extension
     */
    public static void saveToJson(File pluginDataFolder, String filePath, Object object, Gson gson) throws IOException {
        if (!filePath.endsWith(".json")) {
            if (filePath.contains("."))
                throw new IllegalArgumentException("The file path needs to end with \".json\" or have no extension.");

            filePath += ".json";
        }

        String json = gson.toJson(object);

        File jsonFile = new File(pluginDataFolder, filePath);
        Files.createDirectories(jsonFile.toPath().getParent());
        Files.write(jsonFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Deserializes and returns an object from the json file at the specified file path.
     *
     * @param pluginDataFolder the plugin data folder where the json file exists
     * @param filePath         the file path of the json file
     * @param <T>              the type of the loaded object
     * @return the loaded object
     * @throws IOException
     */
    @Nullable
    public static <T> T loadFromJson(File pluginDataFolder, String filePath) throws IOException {
        Gson gson = new GsonBuilder().create();
        return loadFromJson(pluginDataFolder, filePath, gson);
    }

    /**
     * Deserializes and returns an object from the json file at the specified file path.
     *
     * @param pluginDataFolder the plugin data folder where the json file exists
     * @param filePath         the file path of the json file
     * @param gson             the Gson object to use in the deserializing
     * @param <T>              the type of the loaded object
     * @return the loaded object
     * @throws IOException
     */
    @Nullable
    public static <T> T loadFromJson(File pluginDataFolder, String filePath, Gson gson) throws IOException {
        File jsonFile = new File(pluginDataFolder, filePath);

        if (!jsonFile.isFile()) return null;
        Reader reader = new FileReader(jsonFile);

        T o = gson.fromJson(reader, new TypeToken<T>() {
        }.getType());

        reader.close();
        return o;
    }

}
