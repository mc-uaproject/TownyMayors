package dev.ua.ikeepcalm.townymayors.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GrantWealth implements CommandExecutor {

    private final LuckPerms luckPerms;
    private final List<String> validWealthTypes = Arrays.asList("wealth1", "wealth2", "wealth3", "wealth4");

    public GrantWealth(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() || sender instanceof ConsoleCommandSender) {
        } else { sender.sendMessage(ChatColor.RED + "У вас немає прав для цього!"); return false;}
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Використання: /grantwealth <нік гравця> <тип wealth>");
                sender.sendMessage(ChatColor.RED + "Типи wealth: wealth1, wealth2, wealth3, wealth4");
                return false;
            }

            String playerName = args[0];
            String wealthType = args[1].toLowerCase();

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Гравця з таким ніком не знайдено.");
                return false;
            }

            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Гравець повинен бути онлайн для виконання цієї команди.");
                return false;
            }

            if (!validWealthTypes.contains(wealthType)) {
                sender.sendMessage(ChatColor.RED + "Неправильний тип wealth! Використовуйте wealth1, wealth2, wealth3, wealth4");
                return false;
            }

            User user = luckPerms.getUserManager().getUser(targetPlayer.getUniqueId());
            if (hasWealthGroupNode(user, "group.wealth")) {
                sender.sendMessage(ChatColor.RED + "У Гравця вже є wealth! Щоб використати команду, спочатку заберіть у гравця wealth за допомогою /removewealth <нік гравця>");
                return false;
            }

            int limitValue;
            switch (wealthType) {
                case "wealth1":
                    limitValue = 255;
                    break;
                case "wealth2":
                    limitValue = 512;
                    break;
                case "wealth3":
                    limitValue = 812;
                    break;
                case "wealth4":
                    limitValue = 1024;
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Неправильний тип wealth!");
                    return false;
            }

            UUID playerUuid = targetPlayer.getUniqueId();

            addWealthNode(playerUuid, "group." + wealthType);

            addLimitNode(playerUuid, "townymayors.limit." + limitValue);

            if (isMayor(targetPlayer)) {
                setTownBonusBlocks((getTownNameByUuid(playerUuid)), limitValue);
            }

            sender.sendMessage(ChatColor.GREEN + "Гравцю " + targetPlayer.getName() + " було видано групу " + ChatColor.AQUA + wealthType + ChatColor.GREEN + " на 30 днів");
            Bukkit.getLogger().info("Group " + wealthType + " was granted to player " + targetPlayer.getName() + " with limit " + limitValue);

            return true;
    }

    private void addWealthNode(UUID userUuid, String permission) {
        luckPerms.getUserManager().modifyUser(userUuid, user -> {
            Node node = Node.builder(permission)
                    .expiry(Duration.ofDays(30))
                    .build();
            user.data().add(node);
        });
    }

    private void addLimitNode(UUID userUuid, String permission) {
        luckPerms.getUserManager().modifyUser(userUuid, user -> {
            Node node = Node.builder(permission)
                    .build();
            user.data().add(node);
        });
    }

    public boolean isMayor(Player player) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            return false;
        }
        Town town = resident.getTownOrNull();
        if (town == null) {
            return false;
        }
        return town.getMayor().equals(resident);
    }

    private boolean hasWealthGroupNode(User user, String keyPrefix) {
        return user.data().toCollection().stream()
                .anyMatch(node -> node.getKey().startsWith(keyPrefix));
    }

    public void setTownBonusBlocks(String townName, int bonusBlocks) {
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null) {
            return;
        }
        town.setBonusBlocks(bonusBlocks);
        town.save();
    }

    public String getTownNameByUuid(UUID playerUuid) {
        Resident resident = TownyAPI.getInstance().getResident(playerUuid);
        assert resident != null;
        Town town = resident.getTownOrNull();
        if (town != null) {
            return town.getName();
        } else { return null; }
    }
}
