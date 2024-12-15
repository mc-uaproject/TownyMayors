package dev.ua.ikeepcalm.townymayors.listeners;

import com.palmergames.bukkit.towny.event.TownClaimEvent;
import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.plot.toggle.PlotToggleExplosionEvent;
import com.palmergames.bukkit.towny.event.plot.toggle.PlotTogglePvpEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleMobsEvent;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ClaimListener implements Listener {

    private final FileConfiguration mayorsConfig;
    private final File mayorsFile;
    private final LuckPerms luckPerms;

    public ClaimListener(TownyMayors towny, LuckPerms luckPerms) {
        this.mayorsFile = new File(towny.getDataFolder(), "mayors.yml");
        this.luckPerms = luckPerms;
        this.mayorsConfig = YamlConfiguration.loadConfiguration(mayorsFile);
    }

    @EventHandler
    public void onPreClaimEvent(TownPreClaimEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        int chunkLimit = getChunkLimit(player);
        if (chunkLimit <= 0) {
            player.sendMessage(ChatColor.RED + "У вас має бути активна " + ChatColor.AQUA + "Wealth підписка" + ChatColor.RED + " для цього!");
            event.setCancelled(true);
            return;
        }
        String mayorName = player.getName();
        int claimedChunks = mayorsConfig.getInt(mayorName, 0);

        if (claimedChunks >= chunkLimit) {
            player.sendMessage(ChatColor.RED + "Ви вже претендуєте на максимальну кількість ділянок! На цьому рівні " + ChatColor.AQUA + "Wealth підписки" + ChatColor.RED + " ви не можете претендувати на більшу територію вашого міста!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlotClaimEvent(TownClaimEvent event) {
        Player player = event.getTown().getMayor().getPlayer();
        if (player == null) {
            return;
        }

        String mayorName = player.getName();
        int claimedChunks = mayorsConfig.getInt(mayorName, 0);
        mayorsConfig.set(mayorName, claimedChunks + 1);
        saveMayorsConfig();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlotUnclaimEvent(TownUnclaimEvent event) {
        Player player = Objects.requireNonNull(Objects.requireNonNull(event.getTown()).getMayor().getPlayer());
        String mayorName = player.getName();
        int claimedChunks = mayorsConfig.getInt(mayorName, 0);
        mayorsConfig.set(mayorName, Math.max(0, claimedChunks - 1));
        saveMayorsConfig();
    }

    @EventHandler
    public void onTownToggleMobsEvent(TownToggleMobsEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        int chunkLimit = getChunkLimit(player);
        if (chunkLimit <= 0) {
            player.sendMessage(ChatColor.RED + "У вас має бути активна " + ChatColor.AQUA + "Wealth підписка" + ChatColor.RED + " для цього!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlotTogglePvpEvent(PlotTogglePvpEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        int chunkLimit = getChunkLimit(player);
        if (chunkLimit <= 0) {
            player.sendMessage(ChatColor.RED + "У вас має бути активна " + ChatColor.AQUA + "Wealth підписка" + ChatColor.RED + " для цього!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlotToggleExplosionEvent(PlotToggleExplosionEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        int chunkLimit = getChunkLimit(player);
        if (chunkLimit <= 0) {
            player.sendMessage(ChatColor.RED + "У вас має бути активна " + ChatColor.AQUA + "Wealth підписка" + ChatColor.RED + " для цього!");
            event.setCancelled(true);
        }
    }

    public int getChunkLimit(Player player) {

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return 0;
        }

        List<String> permissions = user.getNodes().stream().filter(node -> node instanceof PermissionNode).map(node -> ((PermissionNode)node).getPermission()).toList();
        for (String permission : permissions) {

            if (permission.startsWith("townymayors.limit.")) {
                try {
                    return Integer.parseInt(permission.split("\\.")[2]);
                } catch (NumberFormatException e) {
                    Bukkit.getLogger().info("Invalid permission node: " + permission);
                }
            }
        }
        return 0;
    }

    private void saveMayorsConfig() {
        try {
            mayorsConfig.save(mayorsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
