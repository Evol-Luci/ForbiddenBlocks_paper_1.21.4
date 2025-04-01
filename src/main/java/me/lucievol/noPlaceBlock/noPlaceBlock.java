package me.lucievol.noPlaceBlock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;


public final class noPlaceBlock extends JavaPlugin {

    private BukkitAudiences adventure;
    private final Map<String, Set<String>> forbiddenItems = new HashMap<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {

        adventure = BukkitAudiences.create(this);
        saveDefaultConfig();
        config = getConfig();
        loadForbiddenItems();

        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);

        registerCommand("forbiditem");
        registerCommand("allowitem");
        registerCommand("togglemessages");
        registerCommand("listforbidden");
        registerCommand("reloadconfig");

        getLogger().info("ForbiddenBlocks_1.0_Paper_1.21.4 has been enabled!");
    }

    @Override
    public void onDisable() {
        saveForbiddenItems();
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    private void registerCommand(String cmd) {
        if (getCommand(cmd) != null) {
            Objects.requireNonNull(getCommand(cmd)).setExecutor(this);
        } else {
            getLogger().warning("Command '" + cmd + "' could not be registered.");
        }
    }

    private void sendMessage(CommandSender sender, String path) {
        if (config.getBoolean("options.sendMessages", true)) {
            String rawMessage = config.getString(path, "<red>Message not found!</red>");
            Component formattedMessage = MiniMessage.miniMessage().deserialize(rawMessage);

            if (sender instanceof Player player) {
                adventure.player(player).sendMessage(formattedMessage);
            } else {
                sender.sendMessage(MiniMessage.miniMessage().stripTags(rawMessage));
            }
        }
    }

    private void loadForbiddenItems() {
        forbiddenItems.clear();
        Map<String, Object> savedItems = config.getConfigurationSection("playerForbiddenItems").getValues(false);
        if (savedItems != null) {
            for (Map.Entry<String, Object> entry : savedItems.entrySet()) {
                String playerName = entry.getKey();
                List<?> itemList = (List<?>) entry.getValue();
                if (itemList != null) {
                    Set<String> itemSet = new HashSet<>();
                    for (Object itemObj : itemList) {
                        if (itemObj instanceof String) {
                            itemSet.add((String) itemObj);
                        }
                    }
                    forbiddenItems.put(playerName, itemSet);
                }
            }
        }

        // Load global forbidden items
        List<?> globalItems = config.getList("playerForbiddenItems.global");
        if (globalItems != null) {
            Set<String> globalItemSet = new HashSet<>();
            for (Object itemObj : globalItems) {
                if (itemObj instanceof String) {
                    globalItemSet.add((String) itemObj);
                }
            }
            forbiddenItems.put("global", globalItemSet);
        }
    }

    private void saveForbiddenItems() {
        Map<String, List<String>> serializableForbiddenItems = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : forbiddenItems.entrySet()) {
            serializableForbiddenItems.put(entry.getKey(), new java.util.ArrayList<>(entry.getValue()));
        }
        config.set("playerForbiddenItems", serializableForbiddenItems);
        saveConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                            String @NotNull [] args) {

        if (!(sender instanceof Player player)) {
            sendMessage(sender, "messages.noPermission");
            return true;
        }

        String cmdName = command.getName().toLowerCase();

        switch (cmdName) {
            case "togglemessages":
                boolean current = config.getBoolean("options.sendMessages", true);
                config.set("options.sendMessages", !current);
                saveConfig();
                player.sendMessage("Messages are now " + (current ? "disabled" : "enabled") + ".");
                return true;

            case "listforbidden":
                String targetName = player.getName();
                if (args.length > 0) {
                    targetName = args[0];
                }

                Set<String> targetForbiddenItems = forbiddenItems.get(targetName);
                if (targetForbiddenItems == null || targetForbiddenItems.isEmpty()) {
                    player.sendMessage("No items are currently forbidden for " + targetName + ".");
                } else {
                    player.sendMessage("Forbidden items for " + targetName + ":");
                    for (String item : targetForbiddenItems) {
                        player.sendMessage("    - " + MiniMessage.miniMessage().deserialize(item));
                    }
                }
                return true;

            case "reloadconfig":
                reloadConfig();
                config = getConfig();
                loadForbiddenItems();
                player.sendMessage("Configuration and forbidden items reloaded.");
                return true;

            case "forbiditem":
            case "allowitem":
                String itemName;
                String targetPlayerName = player.getName();

                // Determine target player and item name based on arguments
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("global")) {
                        targetPlayerName = "global";
                        itemName = (args.length > 1) ? args[1] : null;
                    } else {
                        targetPlayerName = args[0];
                        itemName = (args.length > 1) ? args[1] : null;
                    }
                } else {
                    itemName = null; // Use item in hand
                }

                // Get item from hand if not specified in arguments
                ItemStack item = player.getInventory().getItemInMainHand();
                if (itemName == null) {
                    if (!item.hasItemMeta() || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
                        sendMessage(player, "messages.mustHoldNamedItem");
                        return true;
                    }
                    itemName = MiniMessage.miniMessage()
                            .serialize(Objects.requireNonNull(item.getItemMeta().displayName()));
                }

                // Modify forbidden items
                Set<String> playerForbiddenItems = forbiddenItems.computeIfAbsent(targetPlayerName, k -> new HashSet<>());

                if (cmdName.equals("forbiditem")) {
                    playerForbiddenItems.add(itemName);
                    sendMessage(player, "messages.itemForbidden");
                } else {
                    playerForbiddenItems.remove(itemName);
                    sendMessage(player, "messages.itemAllowed");
                }

                saveForbiddenItems();
                return true;
        }
        return false;
    }

    public static class BlockPlaceListener implements Listener {
        private final noPlaceBlock plugin;

        public BlockPlaceListener(noPlaceBlock plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            ItemStack item = event.getItemInHand();
            Player player = event.getPlayer();

            if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                String displayName = MiniMessage.miniMessage()
                        .serialize(Objects.requireNonNull(item.getItemMeta().displayName()));
                boolean isForbiddenForPlayer = plugin.forbiddenItems.containsKey(player.getName()) && plugin.forbiddenItems.get(player.getName()).contains(displayName);
                boolean isForbiddenGlobally = plugin.forbiddenItems.containsKey("global") && plugin.forbiddenItems.get("global").contains(displayName);

                if (isForbiddenForPlayer || isForbiddenGlobally) {
                    event.setCancelled(true);
                    if (plugin.getConfig().getBoolean("options.sendMessages", true)) {
                        plugin.adventure.player(event.getPlayer())
                                .sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig()
                                        .getString("messages.cannotPlace", "<red>You are not allowed to place this block!")));
                    }
                }
            }
        }
    }
}
