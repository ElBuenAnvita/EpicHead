package es.raxthelag.epichead.util;

import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.Task;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import javax.annotation.Nullable;

public class Util {
    // public static boolean

    public static boolean isPlayerTasked(Player p) {
        // return Main.pendingTasks.get(p.getName()) != null;
        return Main.pendingTasks.containsKey(p.getName());
    }

    public static Task getTask(Player p) {
        if (!isPlayerTasked(p)) return null;
        return Main.pendingTasks.get(p.getName());
    }

    public static void removeTask(Player p) {
        Main.pendingTasks.remove(p.getName());
    }

    /**
     * Gets value from permission that starts with node permission given
     * @param player Player permission holder
     * @param permission Permission initials without final period (.)
     * @return First value got from permission list, else null
     */
    @Nullable
    public static String getPermissionValue(Player player, String permission) {
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            if (perm.getPermission().startsWith(permission + ".") && perm.getValue()) {
                // return Integer.parseInt(perm.getPermission().substring(perm.getPermission().lastIndexOf('.' + 1)));
                return perm.getPermission().substring(perm.getPermission().lastIndexOf('.') + 1);
            }
        }
        return null;
    }

    /**
     * Set value in player's permission with node permission given <b>(uses VaultAPI)</b>
     * @param player Player permission holder
     * @param permission Permission initials without final period (.)
     * @param value New value to set with this <code>permission</code>.
     */
    public static void setPermissionValue(Player player, String permission, String value) {
        // Removes all permissions like the one we're setting.
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            if (perm.getPermission().startsWith(permission + ".")) {
                Main.getInstance().getPermission().playerRemove(null, player, perm.getPermission());
            }
        }

        Main.getInstance().getPermission().playerAdd(null, player, (permission + "." + value ));
    }

    public static String getDurationSmall(long time) {
        String duration = "";
        if (time <= 0L) return "";

        time = (time - System.currentTimeMillis()) / 1000L; // / 60L
        float time2 = (float) time; // time2 in seconds and floated

        if (time2 > 31536000.0F) { // 1 year
            time2 = time2 / 31536000.0F;
            duration = (int)time2 + "a ";
            time2 = (time2 - (float)((int)time2)) * 31536000.0F;
        }

        if (time2 > 2628000.0F) { // 1 month
            time2 = time2 / 2628000.0F;
            duration = (int)time2 + "m ";
            time2 = (time2 - (float)((int)time2)) * 2628000.0F;
        }

        if (time2 > 86400.0F) { // 1 day
            time2 = time2 / 24.0F / 60.0F / 60.0F;
            duration = duration + (int)time2 + "d ";
            time2 = (time2 - (float)((int)time2)) * 24.0F * 60.0F * 60.0F;
        } else {
            duration = duration.replace(", ", "");
        }

        if (time2 > 3600.0F) { // 1 hour
            time2 = time2 / 60.0F / 60.0F;
            duration = duration + (int)time2 + "h ";
            time2 = (time2 - (float)((int)time2)) * 60.0F * 60.0F;
        } else {
            duration = duration.replace(", ", "");
        }

        if (time2 > 60.F) { // 1 min
            time2 = time2 / 60.0F;
            duration = duration + (int)time2 + "min ";
            time2 = (time2 - (float)((int)time2)) * 60.0F;
        } else {
            duration = duration.replace(", ", "");
        }

        if (time2 >= 1.0F) { // 1 seg
            duration = duration + (int)time2 + "s";
        }

        return duration;
    }
}
