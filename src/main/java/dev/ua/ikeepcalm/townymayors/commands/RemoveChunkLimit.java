package dev.ua.ikeepcalm.townymayors.commands;

import dev.ua.ikeepcalm.townymayors.listeners.ClaimListener;
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

public class RemoveChunkLimit implements CommandExecutor {

    private final LuckPerms luckPerms;
    private final ClaimListener claimListener;

    public RemoveChunkLimit(LuckPerms luckPerms, ClaimListener claimListener) {
        this.luckPerms = luckPerms;
        this.claimListener = claimListener;
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
        if (user != null && !(claimListener.getChunkLimit(targetPlayer) <= 0)) {
            removeLimitNodes(user);
            commandSender.sendMessage("Група townymayors.limit." + claimListener.getChunkLimit(targetPlayer) + " була успішна видалена у гравця " + targetPlayer.getName() + "!");
        } else {
            commandSender.sendMessage(ChatColor.RED + "Значення групи townymayors.limit у гравця " + targetPlayer.getName() + " і так дорівнює нулю!");
            return false;
        }
        return true;
    }

    public void removeLimitNodes(User user) {
        user.data().toCollection().stream()
                .filter(node -> node.getKey().startsWith("townymayors.limit."))
                .forEach(user.data()::remove);

        luckPerms.getUserManager().saveUser(user);
    }
}
