package dev.ua.ikeepcalm.townymayors.listeners;

import com.palmergames.bukkit.towny.event.PreNewTownEvent;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.logging.Level;

public class TownCreationListener implements Listener {

    private final TownyMayors plugin;

    public TownCreationListener(TownyMayors plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTownCreate(PreNewTownEvent event) {
        try {
            Player player = event.getPlayer();
            if (player == null) return;

            UUID mayorUUID = player.getUniqueId();
            boolean freeTownCreation = BenefitsUtil.getBooleanBenefitForPlayer(mayorUUID, "free-town-creation");

            if (freeTownCreation) {
                event.setPrice(0);
                BenefitsUtil.logMessage("Player " + player.getName() + " created town for free due to free-town-creation benefit");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error handling town creation benefit", e);
        }
    }
}