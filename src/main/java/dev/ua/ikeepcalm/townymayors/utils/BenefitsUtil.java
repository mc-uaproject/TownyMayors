package dev.ua.ikeepcalm.townymayors.utils;

import dev.ua.ikeepcalm.townymayors.TownyMayors;
import net.luckperms.api.model.user.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class BenefitsUtil {

    public static int getBenefitForPlayer(UUID playerUUID, String benefitKey) {
        User user = TownyMayors.INSTANCE.getLuckPerms().getUserManager().getUser(playerUUID);
        if (user == null) {
            return 0;
        }

        List<Map<?, ?>> groups = TownyMayors.INSTANCE.getConfig().getMapList("permissions.groups");
        if (groups.isEmpty()) {
            return 0;
        }

        int highestBenefit = TownyMayors.INSTANCE.getConfig().getInt("permissions.default." + benefitKey, 0);

        for (Map<?, ?> group : groups) {
            String permission = (String) group.get("permission");

            if (permission == null || permission.isEmpty()) {
                continue;
            }

            if (user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()) {
                Map<String, Object> benefits = (Map<String, Object>) group.get("benefits");
                if (benefits != null && benefits.containsKey(benefitKey)) {
                    Object value = benefits.get(benefitKey);
                    int benefitValue = 0;
                    
                    if (value instanceof Integer) {
                        benefitValue = (Integer) value;
                    } else if (value instanceof Double) {
                        benefitValue = ((Double) value).intValue();
                    } else if (value instanceof String) {
                        try {
                            benefitValue = Integer.parseInt((String) value);
                        } catch (NumberFormatException e) {
                            logMessage("Invalid numeric value for benefit " + benefitKey + ": " + value, Level.WARNING);
                            continue;
                        }
                    }
                    
                    if (benefitValue > highestBenefit) {
                        highestBenefit = benefitValue;
                    }
                }
            }
        }

        return highestBenefit;
    }

    @SuppressWarnings("unchecked")
    public static boolean getBooleanBenefitForPlayer(UUID playerUUID, String benefitKey) {
        User user = TownyMayors.INSTANCE.getLuckPerms().getUserManager().getUser(playerUUID);
        if (user == null) {
            return false;
        }

        List<Map<?, ?>> groups = TownyMayors.INSTANCE.getConfig().getMapList("permissions.groups");
        if (groups.isEmpty()) {
            return false;
        }

        boolean highestBenefit = TownyMayors.INSTANCE.getConfig().getBoolean("permissions.default." + benefitKey, false);

        for (Map<?, ?> group : groups) {
            String permission = (String) group.get("permission");
            if (permission == null || permission.isEmpty()) {
                continue;
            }
            if (user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()) {
                Map<String, Object> benefits = (Map<String, Object>) group.get("benefits");
                if (benefits != null && benefits.containsKey(benefitKey)) {
                    Object value = benefits.get(benefitKey);
                    boolean benefitValue = false;
                    
                    if (value instanceof Boolean) {
                        benefitValue = (Boolean) value;
                    } else if (value instanceof String) {
                        benefitValue = Boolean.parseBoolean((String) value);
                    }
                    
                    if (benefitValue) {
                        highestBenefit = true;
                    }
                }
            }
        }

        return highestBenefit;
    }

    private static void logMessage(String message, Level level) {
        TownyMayors.INSTANCE.getLogger().log(level, message);
    }

    public static void logMessage(String message) {
        Level level = getConfiguredLogLevel();
        TownyMayors.INSTANCE.getLogger().log(level, message);
    }

    private static Level getConfiguredLogLevel() {
        String configLevel = TownyMayors.INSTANCE.getConfig().getString("debug.log-level", "INFO").toUpperCase();
        try {
            return Level.parse(configLevel);
        } catch (IllegalArgumentException e) {
            return Level.INFO;
        }
    }
}