package dev.ua.ikeepcalm.townymayors.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RemoveWealth implements CommandExecutor {

    private final LuckPerms luckPerms;

    public RemoveWealth(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (commandSender.isOp() || commandSender instanceof ConsoleCommandSender) {
        } else { commandSender.sendMessage(ChatColor.RED + "У вас немає прав для цього!"); return false;}

        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Використання: /removewealth <нік гравця>");
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID playerUuid = targetPlayer.getUniqueId();
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            commandSender.sendMessage(ChatColor.RED + "Гравця не знайдено.");
            return false;
        }

        User user = luckPerms.getUserManager().getUser(playerUuid);
        if (user != null) {
            removeWealthNodes(user);
            removeLimitNodes(user);
            commandSender.sendMessage("Група Wealth була успішна видалена у гравця " + targetPlayer.getName() + "!");
        }
        return true;
    }

    public void removeWealthNodes(User user) {
        user.data().toCollection().stream()
                .filter(node -> node.getKey().startsWith("group.wealth"))
                .forEach(user.data()::remove);

        luckPerms.getUserManager().saveUser(user);
    }
    public void removeLimitNodes(User user) {
        user.data().toCollection().stream()
                .filter(node -> node.getKey().startsWith("townymayors.limit."))
                .forEach(user.data()::remove);

        luckPerms.getUserManager().saveUser(user);
    }
}