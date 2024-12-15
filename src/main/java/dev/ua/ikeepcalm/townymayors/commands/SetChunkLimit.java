package dev.ua.ikeepcalm.townymayors.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SetChunkLimit implements CommandExecutor {

    private final LuckPerms luckPerms;
    private final GrantWealth grantWealth;

    public SetChunkLimit(GrantWealth grantWealth, LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
        this.grantWealth = grantWealth;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (commandSender.isOp() || commandSender instanceof ConsoleCommandSender) {
        } else { commandSender.sendMessage(ChatColor.RED + "У вас немає прав для цього!"); return false;}

        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Неправильне використання! Використовуйте: /getchunklimit <нік гравця> <число>");
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            commandSender.sendMessage("Гравця " + "'" + args[0] + "' не існує!");
            return false;
        }

        try {
            int limit = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(ChatColor.RED + "'" + args[1] + "'" + "не є числом!");
        }

        if (!(grantWealth.isMayor(targetPlayer))) {
            commandSender.sendMessage(ChatColor.RED + "Гравець не є власником жодного міста!");
            return false;
        }

        User user = luckPerms.getUserManager().getUser(targetPlayer.getUniqueId());

        if (user == null) {
            commandSender.sendMessage(ChatColor.RED + "Сталася помилка під час отримання даних користувача!");
            return false;
        }

        removeNodesStartsWith(user, "townymayors.limit.");
        addNode(targetPlayer.getUniqueId(), "townymayors.limit." + Integer.parseInt(args[1]));

        grantWealth.setTownBonusBlocks(grantWealth.getTownNameByUuid(targetPlayer.getUniqueId()), Integer.parseInt(args[1]));
        commandSender.sendMessage("Ліміт чанків міста гравця " + targetPlayer.getName() + " був успішно змінений на " + Integer.parseInt(args[1]));

        return true;
    }

    private void removeNodesStartsWith(User user, String prefix) {
        user.data().toCollection().stream()
                .filter(node -> node.getKey().startsWith(prefix))
                .forEach(user.data()::remove);

        luckPerms.getUserManager().saveUser(user);
    }

    private void addNode(UUID userUuid, String prefix) {
        luckPerms.getUserManager().modifyUser(userUuid, user -> {
            Node node = Node.builder(prefix)
                    .build();
            user.data().add(node);
        });
    }

}
