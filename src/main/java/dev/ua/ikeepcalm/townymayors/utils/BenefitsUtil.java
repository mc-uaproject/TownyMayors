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

        for (Map<?, ?> group : groups) {
            String permission = (String) group.get("permission");

            if (permission == null || permission.isEmpty()) {
                continue;
            }

            if (user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()) {
                Map<String, Object> benefits = (Map<String, Object>) group.get("benefits");
                if (benefits != null && benefits.containsKey(benefitKey)) {
                    Object value = benefits.get(benefitKey);
                    if (value instanceof Integer) {
                        return (Integer) value;
                    } else if (value instanceof Double) {
                        return ((Double) value).intValue();
                    } else if (value instanceof String) {
                        try {
                            return Integer.parseInt((String) value);
                        } catch (NumberFormatException e) {
                            logMessage("Invalid numeric value for benefit " + benefitKey + ": " + value, Level.WARNING);
                        }
                    }
                }
            }
        }

        return TownyMayors.INSTANCE.getConfig().getInt("permissions.default." + benefitKey, 0);
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

        for (Map<?, ?> group : groups) {
            String permission = (String) group.get("permission");
            if (permission == null || permission.isEmpty()) {
                continue;
            }
            if (user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()) {
                Map<String, Object> benefits = (Map<String, Object>) group.get("benefits");
                if (benefits != null && benefits.containsKey(benefitKey)) {
                    Object value = benefits.get(benefitKey);
                    if (value instanceof Boolean) {
                        return (Boolean) value;
                    } else if (value instanceof String) {
                        return Boolean.parseBoolean((String) value);
                    }
                }
            }
        }

        return TownyMayors.INSTANCE.getConfig().getBoolean("permissions.default." + benefitKey, false);
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
