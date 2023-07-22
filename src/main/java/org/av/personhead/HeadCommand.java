package org.av.personhead;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

import static org.bukkit.Bukkit.getLogger;

public class HeadCommand implements CommandExecutor, TabCompleter {
    private final DataFileManager dataFileManager;

    public HeadCommand(DataFileManager dataFileManager) {
        this.dataFileManager = dataFileManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String subCommand = args[0].toLowerCase();

            if (sender instanceof Player player) {
                switch (subCommand) {
                    // Command /ph head
                    case "head" -> {
                        if (dataFileManager.getHeadCount(player, "count") < dataFileManager.getMaxHead()) {
                            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
                            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                            if (meta != null) {
                                meta.setOwningPlayer(player);
                                playerHead.setItemMeta(meta);
                                player.getInventory().addItem(playerHead);
                                dataFileManager.increaseHeadCount(player, "command");
                                player.sendMessage(ChatColor.GREEN + "This is your " + dataFileManager.getHeadCount(player, "count") + " head!");
                            } else {
                                player.sendMessage("Error with your meta");
                            }
                        } else {
                            player.sendMessage("You're reached limit of heads!");
                        }
                    }
                    // Command /ph count
                    case "count" -> {
                        player.sendMessage("Head amount by command: " + dataFileManager.getHeadCount(player, "count"));
                        player.sendMessage("Max of command heads: " + dataFileManager.getMaxHead());
                    }
                    // Command /ph total_count
                    case "total_count" -> player.sendMessage("Total heads amount: " + dataFileManager.getHeadCount(player, "total_count"));
                    // Command /ph reset
                    case "reset" -> {
                        resetHead(player);
                        player.sendMessage("Head was removed from the world");
                    }
                    // Command /ph reload
                    case "reload" -> {
                        if (player.isOp() || player.hasPermission("personhead.reload.use")) {
                            dataFileManager.loadFile();
                            player.sendMessage("Config reloaded");
                        } else {
                            player.sendMessage("You don't have permission to do that");
                        }
                    }
                    case "help" -> {
                        player.sendMessage("------------------//------------------");
                        player.sendMessage("Person Head plugin commands:");
                        player.sendMessage("/ph head - get 1 head (max: " + dataFileManager.getMaxHead() + ")");
                        player.sendMessage("/ph count - count of use \\ph head");
                        player.sendMessage("/ph total_count - count of all (command and drop) heads");
                        player.sendMessage("/ph reset - remove all placed on block heads (not reset counters!!!)");
                        player.sendMessage("/ph reload - reload config file");
                        player.sendMessage("/ph help - you use it now");
                        player.sendMessage("------------------//------------------");
                    }
                    default -> {
                        return false;
                    }
                }
            } else {
                if (subCommand.equals("reload")) {
                    dataFileManager.loadFile();
                    getLogger().info("Config reloaded");
                } else if (subCommand.equals("help")) {
                    System.out.println("------------------//------------------");
                    System.out.println("Person Head plugin commands:");
                    System.out.println("/ph head - get 1 head (max: " + dataFileManager.getMaxHead() + ")");
                    System.out.println("/ph count - count of use \\ph head");
                    System.out.println("/ph total_count - count of all (command and drop) heads");
                    System.out.println("/ph reset - remove all placed on block heads (not reset counters!!!)");
                    System.out.println("/ph reload - reload config file");
                    System.out.println("/ph help - you use it now");
                    System.out.println("------------------//------------------");
                } else {
                    getLogger().info("Not server-side command: " + subCommand);
                }
            }
            return true;
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First arg (subcommands)
            completions.add("head");
            completions.add("count");
            completions.add("total_count");
            completions.add("reset");
            completions.add("reload");
            completions.add("help");
        }
        return completions;
    }

    public void resetHead(Player player) {
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration dataConfig = dataFileManager.getDataConfig();

        if (dataConfig.contains(playerUUID)) {
            ConfigurationSection nestedConfig = dataConfig.getConfigurationSection(playerUUID);
            if (nestedConfig.contains("pos")) {
                List<String> currentValues = nestedConfig.getStringList("pos");
                for (String loc:currentValues) {
                    Block block = stringToLocation(loc).getBlock();
                    block.setType(Material.AIR);
                }
                List<String> newValues = new ArrayList<>();
                nestedConfig.set("pos", newValues);
            }
        }
        dataFileManager.saveDataConfig();
    }

    public Location stringToLocation(String locationString) {
        String[] propertyStrings = locationString.split(";");

        World world = Bukkit.getWorld(propertyStrings[0]);
        double x = Double.parseDouble(propertyStrings[1]);
        double y = Double.parseDouble(propertyStrings[2]);
        double z = Double.parseDouble(propertyStrings[3]);

        return new Location(world, x, y, z);
    }

}
