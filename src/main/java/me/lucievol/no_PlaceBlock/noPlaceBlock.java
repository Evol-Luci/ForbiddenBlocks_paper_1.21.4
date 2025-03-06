package me.lucievol.no_PlaceBlock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class noPlaceBlock extends JavaPlugin implements TabExecutor {

    private BukkitAudiences adventure;
    private final Set<String> forbiddenItems = new HashSet<>();
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

        getLogger().info("ForbiddenBlocks_Paper_1.21.4 has been enabled!");
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
                sender.sendMessage(MiniMessage.miniMessage().stripTags(rawMessage)); // Removes MiniMessage tags for console
            }
        }
    }


    private void loadForbiddenItems() {
        List<String> savedItems = config.getStringList("forbiddenItems");
        forbiddenItems.clear();
        forbiddenItems.addAll(savedItems);
    }

    private void saveForbiddenItems() {
        config.set("forbiddenItems", List.copyOf(forbiddenItems));
        saveConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

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
                if (forbiddenItems.isEmpty()) {
                    player.sendMessage("No items are currently forbidden.");
                } else {
                    player.sendMessage("Forbidden items:");
                    for (String item : forbiddenItems) {
                        player.sendMessage("- " + MiniMessage.miniMessage().deserialize(item));
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
                ItemStack item = player.getInventory().getItemInMainHand();
                if (!item.hasItemMeta() || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
                    sendMessage(player, "messages.mustHoldNamedItem");
                    return true;
                }

                String displayName = MiniMessage.miniMessage().serialize(Objects.requireNonNull(item.getItemMeta().displayName()));

                if (cmdName.equals("forbiditem")) {
                    forbiddenItems.add(displayName);
                    saveForbiddenItems();
                    sendMessage(player, "messages.itemForbidden");
                } else {
                    forbiddenItems.remove(displayName);
                    saveForbiddenItems();
                    sendMessage(player, "messages.itemAllowed");
                }
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

            if (item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                String displayName = MiniMessage.miniMessage().serialize(Objects.requireNonNull(item.getItemMeta().displayName()));
                if (plugin.forbiddenItems.contains(displayName)) {
                    event.setCancelled(true);
                    if (plugin.getConfig().getBoolean("options.sendMessages", true)) {
                        plugin.adventure.player(event.getPlayer())
                                .sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.cannotPlace", "<red>Message not found!")));
                    }
                }
            }
        }
    }
}
