package me.gimme.gimmecore.language;

import com.google.common.base.Strings;
import me.gimme.gimmecore.util.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class LanguageManager {

    private FileConfiguration languageConfig;
    private String languageFilePath;
    private String colorCode = "&";
    private String placeholderCode = "%";
    private String arraySplitCode = "###";

    public LanguageManager(Plugin plugin) {
        this(plugin, "language.yml");
    }

    public LanguageManager(Plugin plugin, String languageFilePath) {
        this.languageFilePath = languageFilePath;

        languageConfig = ConfigUtils.getYamlConfig(plugin, languageFilePath);
    }

    /**
     * Sets the color code to be used.
     *
     * @param colorCode the color code to be used
     */
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    /**
     * Sets the placeholder code to be used (e.g. "%" for placeholders like "%player%").
     *
     * @param placeholderCode the placeholder code to be used
     */
    public void setPlaceholderCode(String placeholderCode) {
        this.placeholderCode = placeholderCode;
    }

    /**
     * Sets the placeholder code to be used (e.g. "###" for arrays like "word1###word2###word3").
     *
     * @param arraySplitCode the array split code to be used
     */
    public void setArraySplitCode(String arraySplitCode) {
        this.arraySplitCode = arraySplitCode;
    }

    /**
     * Gets the language string from the specified language path.
     *
     * @param languagePath the path to the language string
     * @return the language string from the specified language path
     */
    public Text get(LanguagePath languagePath) {
        String text = languageConfig.getString(languagePath.getPath());
        if (text == null) {
            Bukkit.getLogger().warning("Path not found in " + languageFilePath + ": \"" + languagePath.getPath() + "\"");
            text = "";
        }
        return new Text(text);
    }

    /**
     * Result class that represents the language string and can replace placeholders.
     */
    public class Text {
        private String text;

        protected Text(@NotNull String text) {
            if (!Strings.isNullOrEmpty(colorCode))
                text = ChatColor.translateAlternateColorCodes(colorCode.charAt(0), text);
            this.text = text;
        }

        /**
         * Replaces a placeholder string with the specified replacement.
         *
         * @param placeholder the placeholder string to be replaced
         * @param replacement the replacement for the placeholder
         * @return this, for chaining
         */
        @NotNull
        public Text replace(@NotNull PlaceholderString placeholder, String replacement) {
            if (!Strings.isNullOrEmpty(placeholderCode))
                text = text.replaceAll(placeholderCode + placeholder.getPlaceholder() + placeholderCode, replacement);
            return this;
        }

        @Override
        public String toString() {
            return text;
        }

        @NotNull
        public String[] toStringArray() {
            return text.split(arraySplitCode);
        }
    }

}
