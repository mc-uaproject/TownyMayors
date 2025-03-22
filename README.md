# TownyMayors

A Bukkit/Spigot plugin that provides special benefits to town mayors with specific permissions.

## Overview

TownyMayors works with Towny and LuckPerms to provide benefits to players with specific permissions. These benefits include:

- Free town renaming
- Free town map color changes
- Town tax reductions
- Nation tax reductions

## Requirements

- [Towny](https://github.com/TownyAdvanced/Towny)
- [LuckPerms](https://luckperms.net/)

## Installation

1. Place the `TownyMayors.jar` file in your server's `plugins` folder
2. Restart your server or run `/reload confirm`
3. Configure the plugin in `plugins/TownyMayors/config.yml`

## Configuration

The plugin is highly configurable. Here's an example configuration:

```yaml
# Permission-based benefits configuration
permissions:
  # Permission groups that provide benefits
  groups:
    # Example: A player with "townymayors.wealth.tier1" permission will get these benefits
    - permission: "townymayors.wealth.tier1"
      benefits:
        free-rename: true
        free-color-change: true
        daily-tax-reduction: 50 # percentage reduction (100 = free)
        nation-tax-reduction: 50
    
    # Example: A player with "townymayors.wealth.tier2" permission will get these benefits
    - permission: "townymayors.wealth.tier2"
      benefits:
        free-rename: true
        free-color-change: true
        daily-tax-reduction: 75
        nation-tax-reduction: 75
    
    # Example: A player with "townymayors.wealth.tier3" permission will get these benefits  
    - permission: "townymayors.wealth.tier3"
      benefits:
        free-rename: true
        free-color-change: true
        daily-tax-reduction: 100 # Free
        nation-tax-reduction: 100
  
  # Default values if permissions don't match or player has no permission
  default:
    free-rename: false
    free-color-change: false
    daily-tax-reduction: 0
    nation-tax-reduction: 0
```

### Permissions

You can define any permission nodes you want in the configuration. Players with those permissions will receive the configured benefits.

Example permissions setup in LuckPerms:

```
/lp user <player> permission set townymayors.wealth.tier1
/lp user <player> permission set townymayors.wealth.tier2
/lp user <player> permission set townymayors.wealth.tier3
```

## Additional Notes

- The plugin will log additional information if debug options are enabled in the config.
- Make sure your LuckPerms setup is working correctly for the plugin to function properly.
- Town management actions (like renaming or changing map color) will automatically detect if the player has the appropriate permission and apply the benefits.

## Support

If you encounter any issues or have questions, please open an issue on the GitHub repository.