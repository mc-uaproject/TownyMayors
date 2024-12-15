package dev.ua.ikeepcalm.townymayors.tasks;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MayorTask extends BukkitRunnable {

    private File countdownFile;
    private FileConfiguration countdownConfig;
    private final Map<UUID, Long> countdowns = new HashMap<>();
    private Set<UUID> alreadyWarned = new HashSet<>();
    private final LuckPerms luckPerms;
    private final TownyMayors plugin;

    public MayorTask(TownyMayors plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.luckPerms = luckPerms;
        TownyAPI.getInstance().getDataSource().loadAll();
        loadCountdownFile();
    }

    private void loadCountdownFile() {
        countdownFile = new File(plugin.getDataFolder(), "countdowns.yml");
        if (!countdownFile.exists()) {
            countdownFile.getParentFile().mkdirs();
            plugin.saveResource("countdowns.yml", false);
        }
        countdownConfig = YamlConfiguration.loadConfiguration(countdownFile);
        for (String key : countdownConfig.getKeys(false)) {
            UUID mayorUUID = UUID.fromString(key);
            long deletionTime = countdownConfig.getLong(key);
            countdowns.put(mayorUUID, deletionTime);
        }
    }

    private void saveCountdownsToFile() {
        for (Map.Entry<UUID, Long> entry : countdowns.entrySet()) {
            countdownConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            countdownConfig.save(countdownFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
         List<Town> towns = TownyAPI.getInstance().getTowns();
        for (Town town : towns) {
            Resident mayor = town.getMayor();
            if (mayor == null || mayor.getPlayer() == null) continue;
            Player player = mayor.getPlayer();
            UUID mayorUUID = player.getUniqueId();

            UserManager userManager = luckPerms.getUserManager();
            CompletableFuture<User> userFuture = userManager.loadUser(mayorUUID);
            userFuture.thenAcceptAsync(user -> {
                Set<String> groups = user.getNodes().stream()
                        .filter(NodeType.INHERITANCE::matches)
                        .map(NodeType.INHERITANCE::cast)
                        .map(InheritanceNode::getGroupName)
                        .collect(Collectors.toSet());
                if (groups.contains("wealth1") || groups.contains("wealth2") || groups.contains("wealth3") || groups.contains("wealth4")) {
                    resetCountdown(town);
                } else {
                    startCountdown(town, player);
                    Bukkit.getLogger().info("Countdown has started for " + town.getName() + "!");
                }
            });
        }
    }

    private void startCountdown(Town town, Player player) {
        UUID mayorUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L;

        if (!countdowns.containsKey(mayorUUID)) {
            long deletionTime = currentTime + oneWeekInMillis;
            countdowns.put(mayorUUID, deletionTime);
            countdownConfig.set(mayorUUID.toString(), deletionTime);

            Bukkit.getLogger().info("Starting countdown for town " + town.getName());
            long timeLeft = deletionTime - currentTime;
            player.sendMessage(ChatColor.RED + "Ваше місто буде видалено через" + ChatColor.AQUA + " " + timeLeft / 1000 / 60 / 60 + " годин" + ChatColor.RED + " через відсутність підписки " + ChatColor.AQUA + "Wealth" + ChatColor.RED + "!");
            alreadyWarned.add(mayorUUID);
            saveCountdownsToFile();
        } else if (currentTime >= countdowns.get(mayorUUID)) {
            deleteTown(town);
        } else {
            long timeLeft = countdowns.get(mayorUUID) - currentTime;
            if (!alreadyWarned.contains(mayorUUID)) {
                player.sendMessage(ChatColor.RED + "Ваше місто буде видалено через" + ChatColor.AQUA + " " + timeLeft / 1000 / 60 / 60 + " годин" + ChatColor.RED + " через відсутність підписки " + ChatColor.AQUA + "Wealth" + ChatColor.RED + "!");
                alreadyWarned.add(mayorUUID);
            }
        }
    }

    private void resetCountdown(Town town) {
        Resident mayor = town.getMayor();
        UUID mayorUUID = mayor.getUUID();
        if (countdowns.containsKey(mayorUUID)) {
            countdowns.remove(mayorUUID);
            countdownConfig.set(mayorUUID.toString(), null);
            saveCountdownsToFile();

            mayor.getPlayer().sendMessage("Ваше місто було збережено від видалення, у вас наявна підписка " + ChatColor.AQUA + "Wealth" + ChatColor.GREEN + "!");
            Bukkit.getLogger().info("Countdown reset for mayor of " + town.getName() + ".");
        }
    }

    private void deleteTown(Town town) {
        try {
            TownyAPI.getInstance().getDataSource().removeTown(town, DeleteTownEvent.Cause.ADMIN_COMMAND);
            countdowns.remove(town.getMayor().getUUID());
            countdownConfig.set(town.getMayor().getUUID().toString(), null);
            saveCountdownsToFile();
            Bukkit.getLogger().info("Town " + town.getName() + " has been deleted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}