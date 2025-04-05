package dev.ua.ikeepcalm.townymayors.listeners;

import com.palmergames.bukkit.towny.event.TownBlockClaimCostCalculationEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

import static dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil.getBenefitForPlayer;

public class TownClaimListener implements Listener {

    private final TownyMayors plugin;

    public TownClaimListener(TownyMayors plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTownClaimCalculation(TownBlockClaimCostCalculationEvent event) {
        Town town = event.getTown();
        Resident mayor = town.getMayor();

        if (mayor != null) {
            UUID mayorUUID = mayor.getUUID();
            int reduction = getBenefitForPlayer(mayorUUID, "claim-tax-reduction");
            double originalPrice = event.getPrice();
            double newPrice = originalPrice;
            int plotAmount = event.getAmountOfRequestedTownBlocks();

            if (reduction > 0) {
                newPrice = originalPrice * plotAmount * (1 - (reduction / 100.0));

                if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                    BenefitsUtil.logMessage("Reduced town block claim cost for " + town.getName() +
                            " from " + originalPrice + " to " + newPrice + " (" + reduction + "% reduction) for " + plotAmount + " chunks");
                }
            }

            event.setPrice(newPrice);
        }
    }
}
