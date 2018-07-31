package com.naturamc;

import com.naturamc.utility.StringUtility;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Super class for all Language classes.
 * Holds the file configurations and some basic messages.
 *
 * Provides methods to load messages from the files.
 */
public abstract class Language {
    public String PREFIX;
    public String NAME;
    public String PLAIN_PREFIX;
    public String PLAIN_NAME;
    public String DEFAULT_NAME, DEFAULT_PLAIN_NAME;
    protected JavaPlugin plugin;
    protected File languageFile;
    protected FileConfiguration defaultLanguage;
    protected FileConfiguration language;
    protected String defaultLangName;
    private final String configurationKey = "languageFile";

    public Language(JavaPlugin plugin) {
        this.plugin = plugin;
        getLangFile(plugin.getConfig());

        PREFIX = getString("prefix");
        NAME = getString("name");
        PLAIN_PREFIX = ChatColor.stripColor(PREFIX);
        PLAIN_NAME = ChatColor.stripColor(NAME);
        // default is the value assigned to unknown games in bStats
        DEFAULT_NAME = StringUtility.color(defaultLanguage.getString("name", "undefined"));
        DEFAULT_PLAIN_NAME = ChatColor.stripColor(DEFAULT_NAME);
        loadMessages();
    }

    /**
     * Load all messages from the language file
     *
     * Load implementation specific messages here.
     * Gets called in the constructor after loading the language files.
     */
    protected abstract void loadMessages();

    /**
     * Try loading the language file specified in the
     * passed file configuration.
     *
     * The required set option is 'languageFile'. Possible options
     * are:
     * 'default'/'default.yml': loads the english language file from inside the jar
     * 'lang_xx.yml': will try to load the given file inside the namespaces language folder
     *
     * @param config configuration of the module
     */
    protected void getLangFile(FileConfiguration config) {
        // load default language
        try {
            defaultLangName = "language/lang_en.yml";
            defaultLanguage = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(defaultLangName), "UTF-8"));
        } catch (UnsupportedEncodingException e2) {
            plugin.getLogger().warning("Failed to load default language file for: " + plugin.getDescription().getName());
            plugin.getLogger().warning("     It should be in'" + defaultLangName + "'");
            e2.printStackTrace();
        }

        String fileName = config.getString(configurationKey);
        if (fileName != null && (fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml"))) {
            language = defaultLanguage;
            return;
        }

        if (fileName == null || !fileName.endsWith(".yml")) {
            plugin.getLogger().warning("Language file for '" + plugin.getDescription().getName() + "' is not specified or not valid.");
            plugin.getLogger().warning("Did you forget to give the file ending '.yml'?");
            plugin.getLogger().warning("Should be set in the configuration file as '" + configurationKey + "'");
            plugin.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
            return;
        }

        languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + fileName);
        if (!languageFile.exists()) {
            plugin.getLogger().warning("The setting " + configurationKey + " = " + fileName + " points to a non existing file!");
            plugin.getLogger().warning("Falling back to the default file...");
            language = defaultLanguage;
            return;
        }

        // File exists
        try {
            language = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            plugin.getLogger().warning("Error in the language file '" + fileName + "', see stacktrace:");
            e.printStackTrace();
            plugin.getLogger().warning("falling back to default language file...");
            language = defaultLanguage;
        }
    }


    /**
     * Find all string messages that are missing in the language file.
     *
     * This method compares all message keys that hold a String in the default english
     * file with all set keys in the used language file. All missing keys are
     * collected and returned.
     *
     * @return list of all missing keys (can be empty list)
     */
    public List<String> findMissingStringMessages() {

        List<String> toReturn = new ArrayList<>();

        if (defaultLanguage.equals(language)) return toReturn;

        for (String key : defaultLanguage.getKeys(true)) {
            if (defaultLanguage.isString(key)) {
                if (!language.isString(key)) {
                    // there is a message missing
                    toReturn.add(key);
                }
            }
        }
        return toReturn;
    }

    /**
     * Find all string messages that are missing in the language file.
     *
     * This method compares all message keys that hold a list in the default english
     * file with all set keys in the used language file. All missing keys are
     * collected and returned.
     *
     * @return list of all missing keys (can be empty list)
     */
    public List<String> findMissingListMessages() {
        List<String> toReturn = new ArrayList<>();
        if (defaultLanguage.equals(language)) return toReturn;
        for (String key : defaultLanguage.getKeys(true)) {
            if (defaultLanguage.isList(key)) {
                if (!language.isList(key)) {
                    // there is a list missing
                    toReturn.add(key);
                }
            }
        }
        return toReturn;
    }


    /**
     * Load list messages from the language file
     *
     * If the requested path is not valid for the chosen
     * language file the corresponding list from the default
     * file is returned.
     * ChatColor can be translated here.
     *
     * @param path  path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    protected List<String> getStringList(String path, boolean color) {
        List<String> toReturn;

        // load from default file if path is not valid in specific language file
        if (!language.isList(path)) {
            toReturn = defaultLanguage.getStringList(path);
            if (toReturn == null) {
                throw new IllegalArgumentException("The language key '" + path + "' is not a valid list!");
            }
            if (color) {
                toReturn = StringUtility.color(toReturn);
            }
            return toReturn;
        }

        // load from language file
        toReturn = language.getStringList(path);
        if (color && toReturn != null) {
            for (int i = 0; i < toReturn.size(); i++) {
                toReturn.set(i, ChatColor.translateAlternateColorCodes('&', toReturn.get(i)));
            }
        }
        return toReturn;
    }

    protected List<String> getStringList(String path) {
        return getStringList(path, true);
    }

    /**
     * Get a message from the language file
     *
     * If the requested path is not valid for the
     * configured language file the corresponding
     * message from the default file is returned.
     * ChatColor is translated when reading the message.
     *
     * @param path  path to the message
     * @param color if set, color the loaded message
     * @return message
     */
    protected String getString(String path, boolean color) {
        String toReturn;
        if (!language.isString(path)) {
            toReturn = defaultLanguage.getString(path);
            if (toReturn == null)
                throw new IllegalArgumentException("The language key '" + path + "' is not a valid string!");
            if (color) {
                return ChatColor.translateAlternateColorCodes('&', defaultLanguage.getString(path));
            }
            return toReturn;
        }
        toReturn = language.getString(path);
        if (!color) return toReturn;
        return ChatColor.translateAlternateColorCodes('&', toReturn);
    }

    protected String getString(String path) {
        return getString(path, true);
    }
}
