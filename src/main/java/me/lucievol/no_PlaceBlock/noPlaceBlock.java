package me.lucievol.no_PlaceBlock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
// import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class noPlaceBlock extends JavaPlugin implements TabExecutor {

    private BukkitAudiences adventure;
    private final Set<Component> forbiddenItems = new HashSet<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        adventure = BukkitAudiences.create(this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);

        if (getCommand("forbiditem") != null) {
            Objects.requireNonNull(getCommand("forbiditem")).setExecutor(this);
        } else {
            getLogger().warning("Command 'forbiditem' could not be registered.");
        }

        if (getCommand("allowitem") != null) {
            Objects.requireNonNull(getCommand("allowitem")).setExecutor(this);
        } else {
            getLogger().warning("Command 'allowitem' could not be registered.");
        }

        getLogger().info("ForbiddenBlocks_Paper_1.21.4 has been enabled!");
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            // This command message should be made configurable.
            //I want this to be an optional feature, so I will need to set up a config file for this.
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) {
            // This command message should be made configurable.
            //I want this to be an optional feature, so I will need to set up a config file for this.
            player.sendMessage("You must hold an item with a display name.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            // This command message should be made configurable.
            //I want this to be an optional feature, so I will need to set up a config file for this.
            player.sendMessage("You must hold an item with a display name.");
            return true;
        }

        Component displayName = meta.displayName();

        if (command.getName().equalsIgnoreCase("forbiditem")) {
            forbiddenItems.add(displayName);
            //I want this to be an optional feature, so I will need to set up a config file for this.
            // This command message should be made configurable.
            player.sendMessage("This item is now forbidden to place.");
        } else if (command.getName().equalsIgnoreCase("allowitem")) {
            forbiddenItems.remove(displayName);
            //I want this to be an optional feature, so I will need to set up a config file for this.
            // This command message should be made configurable.
            player.sendMessage("This item is now allowed to place.");
        }

        return true;
    }
    // @Override
    // public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
    //     return List.of(); // No tab suggestions for now
    // }

    public static class BlockPlaceListener implements Listener {
        private final noPlaceBlock plugin;

        public BlockPlaceListener(noPlaceBlock plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            ItemStack item = event.getItemInHand();

            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && plugin.forbiddenItems.contains(meta.displayName())) {
                    event.setCancelled(true);
                    //I want this to be an optional feature, so I will need to set up a config file for this.
                    // This command message should be made configurable.
                    plugin.adventure.player(event.getPlayer())
                            .sendMessage(MiniMessage.miniMessage().deserialize("<red>You cannot place this item!"));
                }
            }
        }
    }
}
