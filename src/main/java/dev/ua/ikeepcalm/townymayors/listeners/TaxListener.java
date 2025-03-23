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
            double originalTax = event.getUpkeep();
            double newTax = originalTax;

            if (reduction > 0) {
                newTax = originalTax * (1 - (reduction / 100.0));

                if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                    BenefitsUtil.logMessage("Reduced town upkeep for " + town.getName() +
                            " from " + originalTax + " to " + newTax + " (" + reduction + "% reduction)");
                }
            }

            if (town.isNeutral()) {
                boolean freeNeutrality = BenefitsUtil.getBooleanBenefitForPlayer(mayorUUID, "free-neutrality");

                if (freeNeutrality) {
                    double neutralityCost = plugin.getConfig().getDouble("default-costs.town-neutrality-upkeep", 0.0);

                    if (neutralityCost > 0) {
                        newTax = Math.max(0, newTax - neutralityCost);

                        if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                            BenefitsUtil.logMessage("Removed neutrality cost for " + town.getName() +
                                    " - mayor has free-neutrality benefit. Original: " + originalTax + " Final: " + newTax);
                        }
                    } else {
                        double estimatedNeutralityCost = originalTax * 0.25;
                        newTax = Math.max(0, newTax - estimatedNeutralityCost);

                        if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                            BenefitsUtil.logMessage("Estimated and removed neutrality cost for " + town.getName() +
                                    " - mayor has free-neutrality benefit. Estimated cost: " + estimatedNeutralityCost);
                        }
                    }
                }
            }

            event.setUpkeep(newTax);
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
                double originalTax = event.getUpkeep();
                double newTax = originalTax;

                if (reduction > 0) {
                    newTax = originalTax * (1 - (reduction / 100.0));

                    if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                        BenefitsUtil.logMessage("Reduced nation upkeep for " + nation.getName() +
                                " from " + originalTax + " to " + newTax + " (" + reduction + "% reduction)");
                    }
                }

                if (nation.isNeutral()) {
                    boolean freeNationNeutrality = BenefitsUtil.getBooleanBenefitForPlayer(kingUUID, "free-nation-neutrality");

                    if (freeNationNeutrality) {
                        double neutralityCost = plugin.getConfig().getDouble("default-costs.nation-neutrality-upkeep", 0.0);

                        if (neutralityCost > 0) {
                            newTax = Math.max(0, newTax - neutralityCost);

                            if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                                BenefitsUtil.logMessage("Removed neutrality cost for nation " + nation.getName() +
                                        " - king has free-nation-neutrality benefit. Original: " + originalTax + " Final: " + newTax);
                            }
                        } else {
                            double estimatedNeutralityCost = originalTax * 0.25;
                            newTax = Math.max(0, newTax - estimatedNeutralityCost);

                            if (plugin.getConfig().getBoolean("debug.log-tax-reductions", false)) {
                                BenefitsUtil.logMessage("Estimated and removed neutrality cost for nation " + nation.getName() +
                                        " - king has free-nation-neutrality benefit. Estimated cost: " + estimatedNeutralityCost);
                            }
                        }
                    }
                }

                event.setUpkeep(newTax);
            }
        }
    }
}