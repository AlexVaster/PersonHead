package org.av.personhead;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class DataFileManager {
    private final JavaPlugin plugin;
    private FileConfiguration dataConfig;
    private File dataFile;

    public DataFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadFile() {
        dataFile = new File(plugin.getDataFolder(), "config.yml");

        if (!dataFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public void saveDataConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void increaseHeadCount(Player player, String type) {
        FileConfiguration dataConfig = getDataConfig();
        String playerUUID = player.getUniqueId().toString();

        if (!dataConfig.contains(playerUUID)) {
            dataConfig.createSection(playerUUID);
        }
        ConfigurationSection nestedConfig = dataConfig.getConfigurationSection(playerUUID);
        if (!nestedConfig.contains("total_count")) {
            nestedConfig.createSection("total_count");
        }
        if (!nestedConfig.contains("count")) {
            nestedConfig.createSection("count");
        }
        if (type.equals("command")) {
            nestedConfig.set("count", getHeadCount(player, "count") + 1);
        }
        nestedConfig.set("total_count", getHeadCount(player, "total_count") + 1);
        saveDataConfig();
    }

    public int getHeadCount(Player player, String type) {
        FileConfiguration dataConfig = getDataConfig();
        String playerUUID = player.getUniqueId().toString();
        if (dataConfig.contains(playerUUID)) {
            ConfigurationSection nestedConfig = dataConfig.getConfigurationSection(playerUUID);
            return nestedConfig.getInt(type, 0);
        }
        return 0;
    }

    public int getMaxHead() {
        FileConfiguration dataConfig = getDataConfig();
        return dataConfig.getInt("max_heads", 10);
    }

}
