package dev.ua.ikeepcalm.townymayors.listeners;

import com.palmergames.bukkit.towny.event.NationUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.ua.ikeepcalm.townymayors.TownyMayors;
import dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

import static dev.ua.ikeepcalm.townymayors.utils.BenefitsUtil.getBenefitForPlayer;

public class TaxListener implements Listener {

    private final TownyMayors plugin;

    public TaxListener(TownyMayors plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTownUpkeepCalculation(TownUpkeepCalculationEvent event) {
        Town town = event.getTown();
        Resident mayor = town.getMayor();

        if (mayor != null) {
            UUID mayorUUID = mayor.getUUID();
            int reduction = getBenefitForPlayer(mayorUUID, "daily-tax-reduction");
            if (reduction > 0) {
                double originalTax = event.getUpkeep();
                double newTax = originalTax * (1 - (reduction / 100.0));

                event.setUpkeep(newTax);

                if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                    BenefitsUtil.logMessage("Reduced town upkeep for " + town.getName() +
                            " from " + originalTax + " to " + newTax + " (" + reduction + "% reduction)");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onNationUpkeepCalculation(NationUpkeepCalculationEvent event) {
        Nation nation = event.getNation();
        if (nation != null) {
            Resident king = nation.getKing();

            if (king != null) {
                UUID kingUUID = king.getUUID();

                int reduction = getBenefitForPlayer(kingUUID, "nation-tax-reduction");

                if (reduction > 0) {
                    double originalTax = event.getUpkeep();
                    double newTax = originalTax * (1 - (reduction / 100.0));

                    event.setUpkeep(newTax);

                    if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                        BenefitsUtil.logMessage("Reduced nation upkeep for " + nation.getName() +
                                " from " + originalTax + " to " + newTax + " (" + reduction + "% reduction)");
                    }
                }
            }
        }
    }
}