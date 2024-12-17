package dev.ua.ikeepcalm.townymayors.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
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

public class SetChunkLimit implements CommandExecutor {

    private final LuckPerms luckPerms;
    private final TownyMayors townyMayors;
    private final ClaimListener claimListener;

    public SetChunkLimit(LuckPerms luckPerms, TownyMayors townyMayors, ClaimListener claimListener) {
        this.luckPerms = luckPerms;
        this.townyMayors = townyMayors;
        this.claimListener = claimListener;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender.isOp() || commandSender instanceof ConsoleCommandSender)) {
            commandSender.sendMessage(ChatColor.RED + "У вас немає прав для цього!");
            return false;
        }

        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Неправильне використання! Використовуйте: /getchunklimit <нік гравця> <число>");
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            commandSender.sendMessage(ChatColor.RED + "Гравця '" + args[0] + "' не існує!");
            return false;
        }

        int limit;
        try {
            limit = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(ChatColor.RED + "'" + args[1] + "' не є числом!");
            return false;
        }

        if (!isMayor(targetPlayer)) {
            commandSender.sendMessage(ChatColor.RED + "Гравець не є власником жодного міста!");
            return false;
        }

        User user = luckPerms.getUserManager().getUser(targetPlayer.getUniqueId());
        if (user == null) {
            commandSender.sendMessage(ChatColor.RED + "Сталася помилка під час отримання даних користувача!");
            return false;
        }

        String playerPath = "players." + targetPlayer.getUniqueId() + ".chunklimit";

        int currentLimit = claimListener.getChunkLimit(targetPlayer);
        if (currentLimit <= 0) {
            commandSender.sendMessage(ChatColor.RED + "У гравця немає Wealth, щоб видавати йому додаткові чанки міста!");
            return false;
        }

        int newLimit = currentLimit + limit;
        townyMayors.getConfig().set(playerPath, newLimit);
        townyMayors.saveConfig();

        commandSender.sendMessage("Ліміт чанків міста гравця " + targetPlayer.getName() + " був успішно збільшений на " + limit + ". Новий ліміт: " + newLimit);

        return true;
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

}