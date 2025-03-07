# ForbiddenBlocks Plugin

## Description
The ForbiddenBlocks plugin restricts the placement of certain items in the game. Players can forbid or allow items through commands, enhancing gameplay and preventing unwanted block placements protecting vulnerable itemname and lore.

## Installation Instructions
1. Download the ForbiddenBlocks plugin JAR file.
2. Place the JAR file in the `plugins` directory of your Minecraft server.
3. Restart the server to enable the plugin.

## Usage
### Commands
- `/forbiditem`: Forbids the placement of the item currently held by the player.
- `/allowitem`: Allows the placement of the previously forbidden item currently held by the player.
- `/togglemessages`: Toggles the messages that inform players about placement restrictions.
- `/listforbidden`: Lists all items currently forbidden from placement.
- `/reloadconfig`: Reloads the configuration file to apply any changes.

### Permissions
- `no_placeblock.forbid`: Allows players to forbid items from being placed.
- `no_placeblock.allow`: Allows players to allow previously forbidden items to be placed.
- `no_placeblock.togglemessages`: Allows players to toggle block placement messages.
- `no_placeblock.list`: Allows players to list all forbidden items.
- `no_placeblock.reload`: Allows players to reload the configuration.

## Configuration
The configuration file (`config.yml`) allows customization of message formatting and options:
- **Messages**: Customize the messages sent to players for various actions, using color and formatting tags.
- **Options**: Enable or disable message sending with the `sendMessages` option.
- **Forbidden Items**: A list of items that are currently forbidden from placement.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue to discuss improvements.

## License
This project is licensed under the MIT License.
