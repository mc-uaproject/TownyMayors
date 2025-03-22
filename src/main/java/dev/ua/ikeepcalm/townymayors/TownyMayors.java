package dev.ua.ikeepcalm.townymayors;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import dev.ua.ikeepcalm.townymayors.commands.MapcolorSubcommand;
import dev.ua.ikeepcalm.townymayors.commands.RenameSubcommand;
import dev.ua.ikeepcalm.townymayors.listeners.TaxListener;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class TownyMayors extends JavaPlugin {

    public static TownyMayors INSTANCE;

    private Towny towny;
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        INSTANCE = this;

        towny = (Towny) getServer().getPluginManager().getPlugin("Towny");
        if (towny == null) {
            getLogger().severe("Towny is not installed! Disabling TownyMayors...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        } else {
            getLogger().severe("LuckPerms is not installed! Disabling TownyMayors...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new TaxListener(this), this);

        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWN, "rename", new RenameSubcommand(this));
        TownyCommandAddonAPI.addSubCommand(TownyCommandAddonAPI.CommandType.TOWN, "mapcolor", new MapcolorSubcommand(this));

        getLogger().info("TownyMayors has been enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("TownyMayors has been disabled!");
    }

    public Towny getTowny() {
        return towny;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}