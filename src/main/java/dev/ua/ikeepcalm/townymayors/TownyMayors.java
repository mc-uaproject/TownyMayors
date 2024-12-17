package dev.ua.ikeepcalm.townymayors;

import com.palmergames.bukkit.towny.Towny;
import dev.ua.ikeepcalm.townymayors.commands.*;
import dev.ua.ikeepcalm.townymayors.listeners.ClaimListener;
import dev.ua.ikeepcalm.townymayors.tasks.MayorTask;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TownyMayors extends JavaPlugin {

    Towny towny = (Towny) getServer().getPluginManager().getPlugin("Towny");
    private LuckPerms luckPerms;
    private ClaimListener claimListener;

    @Override
    public void onEnable() {
        if (towny == null) {
            getLogger().severe("Towny is not installed! Disabling TownyMayors...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new ClaimListener(this, luckPerms, this), this);
        new MayorTask(this, luckPerms).runTaskTimer(this, 0, 20 * 60 * 180);

        claimListener = new ClaimListener(this, luckPerms, this);
        Objects.requireNonNull(getCommand("getchunklimit")).setExecutor(new GetChunkLimit(claimListener, luckPerms));
        Objects.requireNonNull(getCommand("setchunklimit")).setExecutor(new SetChunkLimit(luckPerms, this, claimListener));
        Objects.requireNonNull(getCommand("removechunklimit")).setExecutor(new RemoveChunkLimit(luckPerms, claimListener));

        //unnecessary
        //Objects.requireNonNull(getCommand("grantwealth")).setExecutor(new GrantWealth(luckPerms));
        //Objects.requireNonNull(getCommand("removewealth")).setExecutor(new RemoveWealth(luckPerms));

        getLogger().info("TownyMayors has been enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("TownyMayors has been disabled!");
    }

}
