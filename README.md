# ExpBottle

[![Java CI with Maven](https://github.com/wilddip/ExpBottle/actions/workflows/release.yml/badge.svg)](https://github.com/wilddip/ExpBottle/actions/workflows/release.yml)

A simple Spigot plugin that allows players to store their experience points in bottles and retrieve them later.

## Features

*   Withdraw experience into bottles using `/exp get [levels|all]`.
*   Send experience directly to other players using `/exp send <player> [levels|all]`.
*   Split experience into multiple bottles of a fixed size using `/exp split <levels>`.
*   Throw custom bottles to gain the stored experience.
*   Configurable command name and aliases.
*   Configurable messages (via `messages` section in `config.yml`).
*   Configurable sound notifications for receiving experience via `/exp send`.
*   Configurable action on full inventory when using `/exp get` (`error` or `drop`).
*   Permission based system (`expbottle.use`, `expbottle.get`, `expbottle.send`, `expbottle.split`, `expbottle.help`, `expbottle.reload`).
*   Reload command `/exp reload` to apply configuration changes without restarting.

## Commands

*   `/exp` - Shows your current experience information.
*   `/exp get [levels|all]` - Creates an experience bottle. Use a number for specific levels, `all` or no argument for all your XP.
*   `/exp send <player> [levels|all]` - Sends experience points to another player.
*   `/exp split <levels>` - Creates multiple bottles, each containing the specified number of levels.
*   `/exp help` - Shows the help menu.
*   `/exp reload` - Reloads the plugin configuration.

## Permissions

*   `expbottle.use` - Allows using the base `/exp` command (shows info).
*   `expbottle.get` - Allows withdrawing experience using `/exp get`.
*   `expbottle.send` - Allows sending experience using `/exp send`.
*   `expbottle.split` - Allows splitting experience using `/exp split`.
*   `expbottle.help` - Allows viewing the help menu with `/exp help`.
*   `expbottle.reload` - Allows reloading the plugin configuration.

## Configuration

See the `config.yml` file for detailed configuration options, including messages, sound settings, and command behavior.

## Authors and Credits

The original concept and initial versions of the plugin were created by **BrannStroom**.
This version of ExpBottle is maintained and significantly refactored by **wilddip**.

## Building

This project uses Maven. To build the plugin, run:

```bash
mvn clean package
```

The resulting JAR file will be in the `target` directory.