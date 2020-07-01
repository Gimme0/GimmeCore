package me.gimme.gimmecore.util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveFile<T> {
    public interface DataSupplier<T> {
        T fetchData();
    }

    private Plugin plugin;
    private String filePath;
    private Class<T> clazz;

    public SaveFile(@NotNull Plugin plugin, @NotNull String filePath, @NotNull Class<T> clazz) {
        this.plugin = plugin;
        this.filePath = filePath;
        this.clazz = clazz;
    }

    public void save(@NotNull T data) {
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(data);

        try {
            Path path = getSavePath();
            Files.createDirectories(path.getParent());
            Files.write(path, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public T load() {
        if (existsSaveFile()) {
            try {
                FileReader reader = new FileReader(getSavePath().toString());
                JsonElement element = new JsonParser().parse(reader);
                reader.close();

                return new Gson().fromJson(element, clazz);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void autosave(@NotNull Plugin plugin, long period, @NotNull DataSupplier<T> dataSupplier) {
        new BukkitRunnable() {
            @Override
            public void run() {
                save(dataSupplier.fetchData());
            }
        }.runTaskTimer(plugin, period, period);
    }

    @NotNull
    private Path getSavePath() {
        return Paths.get(plugin.getDataFolder().getAbsolutePath(), filePath);
    }

    private boolean existsSaveFile() {
        File saveFile = getSavePath().toFile();
        return saveFile.exists() && saveFile.isFile();
    }
}
