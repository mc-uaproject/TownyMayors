package dev.ua.ikeepcalm.townymayors.commands.nations;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Translatable;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class RenameSubcommand implements CommandExecutor, TabCompleter {

    private final TownyMayors plugin;
    private final double RENAME_COST;

    public RenameSubcommand(TownyMayors plugin) {
        this.plugin = plugin;
        this.RENAME_COST = plugin.getConfig().getDouble("default-costs.nation-rename", 640.0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Цю команду можуть використовувати лише гравці.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Використання: /nation rename <name>");
            return true;
        }

        try {
            Resident resident = TownyAPI.getInstance().getResident(player);
            if (resident == null) {
                player.sendMessage(ChatColor.RED + "Ви повинні бути жителем нації, щоб використовувати цю команду.");
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
                player.sendMessage(ChatColor.RED + "Тільки король/лідер може перейменувати націю.");
                return true;
            }

            String newName = args[0];

            // Validate name
            if (!newName.matches("^[a-zA-Zа-яА-ЯіІїЇєЄґҐ]+$")) {
                player.sendMessage(ChatColor.RED + "Назва містить недійсні символи. Дозволені тільки літери.");
                return true;
            }

            // Check if nation with this name already exists
            if (TownyUniverse.getInstance().getNation(newName) != null) {
                player.sendMessage(ChatColor.RED + "Нація з такою назвою вже існує.");
                return true;
            }

            // Check if player has free rename benefit
            boolean freeRename = BenefitsUtil.getBooleanBenefitForPlayer(player.getUniqueId(), "free-nation-rename");

            if (freeRename) {
                // Perform free rename
                if (plugin.getConfig().getBoolean("debug.log-wealth-owners", false)) {
                    plugin.getLogger().log(Level.INFO,
                            "Player " + player.getName() + " renamed nation from " + nation.getName() +
                                    " to " + newName + " for free due to benefits");
                }

                // Ask for confirmation
                Confirmation.runOnAccept(() -> {
                            try {
                                String oldName = nation.getName();
                                TownyUniverse.getInstance().getDataSource().renameNation(nation, newName);
                                nation.save();
                                TownyMessaging.sendPrefixedNationMessage(nation,
                                        Translatable.of("msg_nation_renamed", oldName, newName).defaultLocale());
                            } catch (TownyException e) {
                                player.sendMessage(ChatColor.RED + e.getMessage());
                            }
                        })
                        .setTitle(Translatable.of("msg_confirm_rename_nation", nation.getName(), newName).defaultLocale())
                        .sendTo(player);
            } else {
                // Check if nation has enough money
                if (TownyEconomyHandler.isActive() && RENAME_COST > 0) {
                    if (!nation.getAccount().canPayFromHoldings(RENAME_COST)) {
                        player.sendMessage(ChatColor.RED + "Ваша нація не може дозволити собі перейменування. Вартість: " +
                                TownyEconomyHandler.getFormattedBalance(RENAME_COST));
                        return true;
                    }

                    // Ask for confirmation
                    Confirmation.runOnAcceptAsync(() -> {
                                try {
                                    // Take money first
                                    nation.getAccount().withdraw(RENAME_COST, "Nation rename cost");

                                    String oldName = nation.getName();
                                    TownyUniverse.getInstance().getDataSource().renameNation(nation, newName);
                                    nation.save();
                                    TownyMessaging.sendPrefixedNationMessage(nation,
                                            Translatable.of("msg_nation_renamed", oldName, newName).defaultLocale());

                                    player.sendMessage(ChatColor.GREEN + "Націю перейменовано на " + newName + " за " +
                                            TownyEconomyHandler.getFormattedBalance(RENAME_COST));
                                } catch (TownyException e) {
                                    player.sendMessage(ChatColor.RED + e.getMessage());
                                }
                            })
                            .runOnCancel(() -> {
                                player.sendMessage(ChatColor.RED + "Перейменування нації скасовано.");
                            })
                            .setTitle(Translatable.of("msg_confirm_rename_nation", nation.getName(), newName).defaultLocale())
                            .sendTo(player);
                } else {
                    // Economy not active or free rename for everyone
                    Confirmation.runOnAccept(() -> {
                                try {
                                    String oldName = nation.getName();
                                    TownyUniverse.getInstance().getDataSource().renameNation(nation, newName);
                                    nation.save();
                                    TownyMessaging.sendPrefixedNationMessage(nation,
                                            Translatable.of("msg_nation_renamed", oldName, newName).defaultLocale());
                                } catch (TownyException e) {
                                    player.sendMessage(ChatColor.RED + e.getMessage());
                                }
                            })
                            .setTitle(Translatable.of("msg_confirm_rename_nation", nation.getName(), newName).defaultLocale())
                            .sendTo(player);
                }
            }



            return true;
        } catch (TownyException e) {
            plugin.getLogger().log(Level.WARNING, "Error in nation rename command", e);
            player.sendMessage(ChatColor.RED + e.getMessage());
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender instanceof Player player) {
                try {
                    Resident resident = TownyAPI.getInstance().getResident(player);
                    if (resident != null && resident.hasNation()) {
                        return Collections.singletonList(resident.getNation().getName());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Collections.emptyList();
    }
}
