package dev.ua.ikeepcalm.townymayors.commands;

import dev.ua.ikeepcalm.townymayors.listeners.ClaimListener;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class GetChunkLimit implements CommandExecutor {

    private final ClaimListener claimListener;
    private final LuckPerms luckPerms;

    public GetChunkLimit(ClaimListener claimListener, LuckPerms luckPerms) {
        this.claimListener = claimListener;
        this.luckPerms = luckPerms;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (commandSender.isOp() || commandSender instanceof ConsoleCommandSender) {
        } else { commandSender.sendMessage(ChatColor.RED + "У вас немає прав для цього!"); return false;}

        if (args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "Неправильне використання! Використовуйте: /getchunklimit <нік гравця>");
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            commandSender.sendMessage("Такого гравця не існує!");
            return false;
        }

        commandSender.sendMessage(String.valueOf(claimListener.getChunkLimit(targetPlayer)));
        Bukkit.getLogger().info("Player " + targetPlayer.getName() + " has " + "townymayors.limit." + String.valueOf(claimListener.getChunkLimit(targetPlayer)));

        return true;
    }
}
