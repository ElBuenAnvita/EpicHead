package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.Warp;
import es.raxthelag.epichead.objects.tasks.WarpTask;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@CommandAlias("warp")
@Description("Transpórtate a ti mismo a la warp existente")
public class WarpCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @CommandCompletion("@warps")
    public void onCommand(Player player, @Single String warp) {
        if (Main.getIfPlayerHasPendingTasks(player)) {
            MessageUtil.sendMessage(player, "error.spawn-pending-task", "<red>Ya tienes una tarea pendiente. Espera a acabarla para ejecutar otro comando.");
            return;
        }

        java.util.Optional<Warp> warpOpt = Main.warps.stream().filter(w -> w.getName().equalsIgnoreCase(warp)).findFirst();
        if (!warpOpt.isPresent()) {
            MessageUtil.sendMessage(player, "general.warp.warp-no-exist", "La warp no existe", Placeholder.unparsed("warp", warp));
            return;
        }

        String permission = warpOpt.get().getPermission();
        if (permission != null) {
            if (!player.hasPermission(permission)) return;
        }

        Location spawn = warpOpt.get().getSpawnLocation();
        int delay = Main.getInstance().getConfig().getInt("times.warp.teleport-delay", 0);

        if (spawn == null) {
            MessageUtil.sendMessage(player, "general.warp.tp-failed", "<red>Ocurrió un error al llevarte a dicha warp", TagResolver.resolver(Placeholder.unparsed("warp", warp), Placeholder.parsed("warp_display", warpOpt.get().getDisplayName())));
            return;
        }

        if (delay == 0) {
            sendWelcomeMessage(player, warpOpt.get());
            player.teleport(spawn);
            return;
        }

        MessageUtil.sendMessage(player, "general.warp.delay-start", "<warp_prefix> Serás enviado a <warp_display> en <yellow><time> segundos</yellow>, ¡no te muevas!", TagResolver.resolver(Placeholder.unparsed("warp", warpOpt.get().getName()), Placeholder.parsed("warp_display", (warpOpt.get().getDisplayName() != null ? warpOpt.get().getDisplayName() : warpOpt.get().getName())), Placeholder.unparsed("time", ""+delay)));

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isOnline()) {
                sendWelcomeMessage(player, warpOpt.get());
                player.teleport(spawn);

                Util.removeTask(player);
            }
        }, delay * 20L);

        Main.pendingTasks.put(player.getName(), new WarpTask(player, warpOpt.get(), bukkitTask.getTaskId()));
    }

    public void sendWelcomeMessage(Player p, Warp warp) {
        if (warp.getWelcomeMessage() != null && !warp.getWelcomeMessage().isEmpty()) {
            MessageUtil.sendMessage(p, warp.getWelcomeMessage());
        } else {
            TagResolver tagResolver = TagResolver.resolver(Placeholder.unparsed("warp", warp.getName()), Placeholder.parsed("warp_display", (warp.getDisplayName() != null ? warp.getDisplayName() : warp.getName())));
            MessageUtil.sendMessage(p, "general.warp.generic-tp-success", "Teleportado a <warp>", tagResolver);
        }
    }
}
