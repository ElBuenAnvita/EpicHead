package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.tasks.SpawnTask;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@CommandAlias("spawn")
@Description("Teleport to server spawn")
public class SpawnCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @CommandCompletion("@nothing")
    public void onExecute(Player p) {
        if (Main.getIfPlayerHasPendingTasks(p)) {
            MessageUtil.sendMessage(p, "error.spawn-pending-task", "<spawn_prefix> <red>Ya tienes una tarea pendiente. Espera a acabarla para ejecutar otro comando.");
            return;
        }

        Location spawn = Main.getInstance().getLocations().getLocation("spawn");
        int delay = Main.getInstance().getConfig().getInt("times.spawn.teleport-delay", 0);

        if (spawn == null) {
            MessageUtil.sendMessage(p, "general.spawn.tp-failed", "<spawn_prefix> <red>Ocurrió un error al llevarte al spawn");
            return;
        }

        if (delay == 0) {
            MessageUtil.sendMessage(p, "general.spawn.tp-success", "<spawn_prefix> <green>Has sido teletransportado al spawn");
            p.teleport(spawn);
            return;
        }

        MessageUtil.sendMessage(p, "general.spawn.delay-start", "<spawn_prefix> Serás enviado al spawn en <yellow><time> segundos</yellow>, ¡no te muevas!", Placeholder.unparsed("time", "" + delay));

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (p.isOnline()) {
                MessageUtil.sendMessage(p, "general.spawn.tp-success", "<spawn_prefix> <green>Has sido teletransportado al spawn");
                p.teleport(spawn);

                Util.removeTask(p);
            }
        }, delay * 20L);

        Main.pendingTasks.put(p.getName(), new SpawnTask(p, p, bukkitTask.getTaskId()));
    }
}
