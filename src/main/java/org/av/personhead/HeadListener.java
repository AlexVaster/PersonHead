package org.av.personhead;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class HeadListener implements Listener {
    private final DataFileManager dataFileManager;


    public HeadListener(DataFileManager dataFileManager) {
        this.dataFileManager = dataFileManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD) {
            Location blockLocation = block.getLocation();
            writeHeadLocationToFile(playerUUID, blockLocation);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        Block block = event.getBlock();
        if (block.getType() == Material.PLAYER_HEAD) {
            Location blockLocation = block.getLocation();
            removeHeadLocationFromFile(playerUUID, blockLocation);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            if (creeper.isPowered()) {
                for (Player player : event.getEntity().getWorld().getPlayers()) {
                    if (player.getLocation().distance(event.getLocation()) <= 5) {
                        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skin = (SkullMeta) playerHead.getItemMeta();
                        skin.setOwningPlayer(player);
                        playerHead.setItemMeta(skin);
                        Location playerLocation = player.getLocation();
                        playerLocation.getWorld().dropItem(playerLocation, playerHead);
                        dataFileManager.increaseHeadCount(player, "event");
                        player.sendMessage("Your head dropped!");
                    }
                }
            }
        }
    }

    private void writeHeadLocationToFile(String playerUUID , Location blockLocation) {
        FileConfiguration dataConfig = dataFileManager.getDataConfig();
        if (dataConfig.contains(playerUUID)) {
            ConfigurationSection nestedConfig = dataConfig.getConfigurationSection(playerUUID);
            if (!nestedConfig.contains("pos")) {
                nestedConfig.set("pos", "");
            }
            List<String> currentValues = nestedConfig.getStringList("pos");
            String blockPlaceLocation =
                    blockLocation.getWorld().getName() + ";" +
                            blockLocation.getX() + ";" +
                            blockLocation.getY() + ";" +
                            blockLocation.getZ();
            currentValues.add(blockPlaceLocation);
            // Append new location of block
            nestedConfig.set("pos", currentValues);
        } else {
            ConfigurationSection nestedConfig = dataConfig.createSection(playerUUID);
            dataFileManager.saveDataConfig();

            List<String> newValues = new ArrayList<>();
            String blockPlaceLocation =
                    blockLocation.getWorld().getName() + ";" +
                            blockLocation.getX() + ";" +
                            blockLocation.getY() + ";" +
                            blockLocation.getZ();
            newValues.add(blockPlaceLocation);
            // Create empty List with first position
            nestedConfig.set("pos", newValues);
        }
        dataFileManager.saveDataConfig();
    }

    private void removeHeadLocationFromFile(String playerUUID , Location blockLocation) {
        FileConfiguration dataConfig = dataFileManager.getDataConfig();
        ConfigurationSection nestedConfig = dataConfig.getConfigurationSection(playerUUID);
        if (dataConfig.contains(playerUUID) && nestedConfig.contains("pos")) {
            List<String> currentValues = nestedConfig.getStringList("pos");
            String blockPlaceLocation =
                    blockLocation.getWorld().getName() + ";" +
                    blockLocation.getX() + ";" +
                    blockLocation.getY() + ";" +
                    blockLocation.getZ();
            currentValues.remove(blockPlaceLocation);
            nestedConfig.set("pos", currentValues);
        }
        dataFileManager.saveDataConfig();
    }
}
