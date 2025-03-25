package dev.ua.ikeepcalm.townymayors.commands.nations;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class MapcolorSubcommand implements CommandExecutor, TabCompleter {

    private final TownyMayors plugin;
    private final double COLOR_CHANGE_COST;
    private static final Pattern HEX_PATTERN = Pattern.compile("^#?([A-Fa-f0-9]{6})$");

    public MapcolorSubcommand(TownyMayors plugin) {
        this.plugin = plugin;
        this.COLOR_CHANGE_COST = plugin.getConfig().getDouble("default-costs.nation-mapcolor", 1280.0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Цю команду можуть використовувати лише гравці!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Використання: /nation mapcolor <#hex або колір>");
            player.sendMessage(ChatColor.RED + "Наприклад: /nation mapcolor #FF0000");
            return true;
        }

        try {
            Resident resident = TownyAPI.getInstance().getResident(player);
            if (resident == null) {
                player.sendMessage(ChatColor.RED + "Для цьго ви маєте перебувати в нації!");
                return true;
            }

            if (!resident.hasNation()) {
                player.sendMessage(ChatColor.RED + "Ви не належите до нації!");
                return true;
            }

            Nation nation = resident.getNation();
            if (nation == null) {
                player.sendMessage(ChatColor.RED + "Вашої нації не знайдено.");
                return true;
            }

            if (!nation.getKing().equals(resident)) {
                player.sendMessage(ChatColor.RED + "Тільки король/лідер нації може змінювати колір на мапі!");
                return true;
            }

            String colorCode = args[0];

            // Validate hex color code
            if (!isValidHexColor(colorCode)) {
                player.sendMessage(ChatColor.RED + "Неправильний шістнадцятковий код кольору. Формат має бути таким: #RRGGBB або RRGGBB.");
                player.sendMessage(ChatColor.RED + "Наприклад: #FF0000 або FF0000 для червоного");
                return true;
            }

            // Ensure the color code starts with #
            if (!colorCode.startsWith("#")) {
                colorCode = "#" + colorCode;
            }

            // Final color code to use
            final String finalColorCode = colorCode.replace("#", "").toLowerCase();

            // Check if player has free color change benefit
            boolean freeColorChange = BenefitsUtil.getBooleanBenefitForPlayer(player.getUniqueId(), "free-nation-color-change");

            if (freeColorChange) {
                // Perform free color change
                if (plugin.getConfig().getBoolean("debug.log-wealth-owners", false)) {
                    plugin.getLogger().log(Level.INFO,
                            "Player " + player.getName() + " changed nation map color to " + finalColorCode +
                                    " for free due to benefits");
                }

                // Ask for confirmation
                Confirmation.runOnAccept(() -> {
                            try {
                                String oldColor = nation.getMapColorHexCode();
                                nation.setMapColorHexCode(finalColorCode);
                                TownyMessaging.sendPrefixedNationMessage(nation,
                                        "Колір мапи нації змінено з " + oldColor + " на " + finalColorCode);
                            } catch (Exception e) {
                                player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                                plugin.getLogger().log(Level.WARNING, "Error changing nation map color", e);
                            }
                        })
                        .setTitle("Підтвердіть зміну кольору мапи нації на " + finalColorCode)
                        .sendTo(player);
            } else {
                // Check if nation has enough money
                if (TownyEconomyHandler.isActive() && COLOR_CHANGE_COST > 0) {
                    if (!nation.getAccount().canPayFromHoldings(COLOR_CHANGE_COST)) {
                        player.sendMessage(ChatColor.RED + "Ваша нація не може дозволити собі змінити колір мапи. Вартість: " +
                                TownyEconomyHandler.getFormattedBalance(COLOR_CHANGE_COST));
                        return true;
                    }

                    // Ask for confirmation
                    Confirmation.runOnAcceptAsync(() -> {
                                try {
                                    // Take money first
                                    nation.getAccount().withdraw(COLOR_CHANGE_COST, "Nation map color change cost");

                                    String oldColor = nation.getMapColorHexCode();
                                    nation.setMapColorHexCode(finalColorCode);
                                    TownyMessaging.sendPrefixedNationMessage(nation,
                                            "Колір мапи нації змінено з " + oldColor + " на " + finalColorCode);

                                    player.sendMessage(ChatColor.GREEN + "Змінено колір мапи нації на " + finalColorCode + " за " +
                                            TownyEconomyHandler.getFormattedBalance(COLOR_CHANGE_COST));
                                } catch (Exception e) {
                                    player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                                    plugin.getLogger().log(Level.WARNING, "Error changing nation map color", e);
                                }
                            })
                            .runOnCancel(() -> {
                                player.sendMessage(ChatColor.RED + "Скасовано!");
                            })
                            .setTitle("Підтвердіть зміну кольору мапи нації на " + finalColorCode)
                            .sendTo(player);
                } else {
                    // Economy not active or free color change for everyone
                    Confirmation.runOnAccept(() -> {
                                try {
                                    String oldColor = nation.getMapColorHexCode();
                                    nation.setMapColorHexCode(finalColorCode);
                                    TownyMessaging.sendPrefixedNationMessage(nation,
                                            "Колір мапи нації змінено з " + oldColor + " на " + finalColorCode);
                                } catch (Exception e) {
                                    player.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                                    plugin.getLogger().log(Level.WARNING, "Error changing nation map color", e);
                                }
                            })
                            .setTitle("Підтвердіть зміну кольору мапи нації на " + finalColorCode)
                            .sendTo(player);
                }
            }

            return true;
        } catch (TownyException e) {
            plugin.getLogger().log(Level.WARNING, "Error in nation mapcolor command", e);
            player.sendMessage(ChatColor.RED + e.getMessage());
            return true;
        }
    }

    private boolean isValidHexColor(String colorCode) {
        if (colorCode.startsWith("#")) {
            colorCode = colorCode.substring(1);
        }
        return HEX_PATTERN.matcher(colorCode).matches();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList(
                    "#FF0000", // Red
                    "#00FF00", // Green
                    "#0000FF", // Blue
                    "#FFFF00", // Yellow
                    "#FF00FF", // Magenta
                    "#00FFFF", // Cyan
                    "#FFFFFF", // White
                    "#000000"  // Black
            );
        }
        return Collections.emptyList();
    }
}