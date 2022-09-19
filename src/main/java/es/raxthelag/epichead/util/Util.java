package es.raxthelag.epichead.util;

import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.Task;
import org.bukkit.entity.Player;

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
}
