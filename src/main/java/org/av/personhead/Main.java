package org.av.personhead;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        DataFileManager dataFileManager = new DataFileManager(this);
        dataFileManager.loadFile();
        getLogger().info("Plugin PersonHead activated!");

        getServer().getPluginManager().registerEvents(new HeadListener(dataFileManager), this);
        getCommand("personhead").setExecutor(new HeadCommand(dataFileManager));
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin PersonHead deactivated!");
    }

}
