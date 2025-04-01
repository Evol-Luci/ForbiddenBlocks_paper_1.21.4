# ForbiddenBlocks Plugin

## Description
The ForbiddenBlocks plugin enhances your Minecraft server's gameplay by restricting the placement of specific items based on their display names. This plugin allows server administrators and players with appropriate permissions to forbid or allow the placement of items, providing a powerful tool to control building and prevent unwanted constructions. Restrictions can be applied globally or to specific players, offering flexible management of block placement.

## Installation Instructions
1. Download the ForbiddenBlocks plugin JAR file.
2. Place the JAR file in the `plugins` directory of your Minecraft server.
3. Restart the server to enable the plugin.

## Usage
### Commands
- `/forbiditem [player|global] [itemName]`: Forbids the placement of the item currently held by the player, or the item specified by `itemName`. You can specify a player name to forbid the item only for that player, or use `global` to forbid it for everyone. If `itemName` is not provided, the item in hand will be used. The item is identified by its display name.
- `/allowitem [player|global] [itemName]`: Allows the placement of a previously forbidden item. Similar to `/forbiditem`, you can specify a player or use `global`, and optionally provide an `itemName`. If `itemName` is not provided, the item in hand will be used.
- `/togglemessages`: Toggles the messages that inform players when they try to place a forbidden block. This allows players to disable or enable these notifications for themselves.
- `/listforbidden [player]`: Lists all items currently forbidden for placement. Optionally, specify a player name to see their specific forbidden items, otherwise it will show global forbidden items.
- `/reloadconfig`: Reloads the configuration file (`config.yml`) to apply any changes made without restarting the server.

### Permissions
- `no_placeblock.forbid`: Allows players to use the `/forbiditem` command to forbid items from being placed.
- `no_placeblock.allow`: Allows players to use the `/allowitem` command to allow previously forbidden items to be placed.
- `no_placeblock.togglemessages`: Allows players to use the `/togglemessages` command to toggle block placement messages.
- `no_placeblock.list`: Allows players to use the `/listforbidden` command to list all forbidden items.
- `no_placeblock.reload`: Allows players to use the `/reloadconfig` command to reload the server configuration.

## Configuration
The configuration file (`config.yml`) is located in the plugin's folder and allows customization of various aspects:
- **Messages**: Customize the messages sent to players for different actions. You can use MiniMessage formatting tags to add colors, styles, and more. See the configuration file for examples.
- **Options**:
    - `sendMessages`: A boolean option to globally enable or disable sending messages to players when they are restricted from placing blocks.
- **playerForbiddenItems**: This section manages the forbidden items:
    - `global`: Lists items forbidden for all players. Add item display names here to globally restrict them.
    - `PlayerName`: You can create sections for individual players using their names. Under each player's name, you can list item display names that are forbidden specifically for that player.

## Contributing
Contributions to the ForbiddenBlocks plugin are welcome! If you have suggestions for improvements or find bugs, please submit a pull request or open an issue to discuss them.

## License
This project is licensed under the MIT License.
