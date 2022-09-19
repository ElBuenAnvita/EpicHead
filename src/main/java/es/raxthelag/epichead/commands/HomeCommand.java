package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Home;
import es.raxthelag.epichead.objects.tasks.HomeTask;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitTask;

@CommandAlias("home|h")
@Description("EpicHead admin console command")
public class HomeCommand extends BaseCommand {
    @Subcommand("list")
    @Description("Get help about EpicHead admin command")
    public void onList(Player player) {
        // TODO LIST HOMES.. IF POSSIBLE WITH TAGS
        // MessageUtil.sendMessage(player, "general.help", null);
    }

    @Default
    @CatchUnknown
    @Subcommand("teleport|tp")
    @CommandCompletion("@homes")
    public void onRequestTeleportHome(Player player, String name) {
        if (Main.getIfPlayerHasPendingTasks(player)) {
            MessageUtil.sendMessage(player, "error.home-pending-task", "<red>Ya tienes una tarea pendiente. Espera a acabarla para ejecutar otro comando.");
            return;
        }

        EpicPlayer epicPlayer = EpicPlayer.get(player);
        if (!epicPlayer.areHomesLoaded()) {
            MessageUtil.sendMessage(player, "general.home.error-homes-not-loaded", "Ocurrió un error");
            return;
        }

        Home home = epicPlayer.getHomes().stream().filter(h -> h.getName().equals(name)).findFirst().orElse(null);
        int delay = Main.getInstance().getConfig().getInt("times.home.teleport-delay", 0);

        if (home == null) {
            MessageUtil.sendMessage(player, "general.home.home-no-exist", "No existe <home>", Placeholder.unparsed("home", name));
            return;
        }

        if (delay == 0) {
            MessageUtil.sendMessage(player, "general.home.tp-success", "<home_prefix> <green>Has sido teleportado hacia tu <i>home</i> <yellow><home></yellow>", Placeholder.unparsed("home", home.getName()));
            player.teleport(home.getLocation());
            return;
        }

        MessageUtil.sendMessage(
                player,
                "general.home.delay-start",
                "No te muevas, serás teletransportado en <time> segundos",
                TagResolver.resolver(
                        Placeholder.unparsed("home", home.getName().toLowerCase()),
                        Placeholder.unparsed("time", delay + "")
                )
        );

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isOnline()) {
                MessageUtil.sendMessage(player, "general.home.tp-success", "<home_prefix> <green>Has sido teleportado hacia tu <i>home</i> <yellow><home></yellow>", Placeholder.unparsed("home", home.getName()));
                player.teleport(home.getLocation());

                Util.removeTask(player);
            }
        }, delay * 20L);

        Main.pendingTasks.put(player.getName(), new HomeTask(player, home, bukkitTask.getTaskId()));
    }

    @Subcommand("set|add")
    @CommandAlias("sethome")
    @CommandPermission("epichead.home.set")
    public void onNewHome(Player player, @Single String name) {
        EpicPlayer epicPlayer = EpicPlayer.get(player);
        if (!epicPlayer.areHomesLoaded()) {
            MessageUtil.sendMessage(player, "general.home.error-homes-not-loaded", "Ocurrió un error");
            return;
        }

        int maxHomes = getMaxHomesAllowed(player);
        if (maxHomes >= epicPlayer.getHomes().size()) {
            MessageUtil.sendMessage(player, "general.home.max-homes-reached", "Has alcanzado el límite de casas");
            return;
        }

        Main.debug(player.getName() + " has " + maxHomes + " max homes allowed.");

        if (epicPlayer.getHomes().stream().anyMatch(h -> h.getName().equals(name))) {
            MessageUtil.sendMessage(player, "general.home.home-already-exists", "Ya tienes una casa con este nombre", Placeholder.unparsed("home", name.toLowerCase()));
            return;
        }

        MessageUtil.sendMessage(player, "general.home.home-set-success", "Hogar establecido", Placeholder.unparsed("home", name.toLowerCase()));
        epicPlayer.addHome(new Home(null, name, player.getLocation()));
    }

    @Subcommand("delete|del|remove")
    @CommandAlias("delhome")
    @CommandPermission("epiclol.home.del")
    public void onRemoveHome(Player player, @Single String name) {
        EpicPlayer epicPlayer = EpicPlayer.get(player);
        if (!epicPlayer.areHomesLoaded()) {
            MessageUtil.sendMessage(player, "general.home.error-homes-not-loaded", "Ocurrió un error");
            return;
        }

        if (epicPlayer.getHomes().stream().noneMatch(h -> h.getName().equals(name))) {
            MessageUtil.sendMessage(player, "general.home.home-no-exist", "No existe el hogar <home>", Placeholder.unparsed("home", name.toLowerCase()));
            return;
        }

        MessageUtil.sendMessage(player, "general.home.home-del-success", "Hogar eliminado", Placeholder.unparsed("home", name.toLowerCase()));
        epicPlayer.getHomes().removeIf(h -> h.getName().equals(name.toLowerCase()));
    }

    private int getMaxHomesAllowed(Player player) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().startsWith("epichead.homes.") && permission.getValue()) {
                return Integer.parseInt(permission.getPermission().substring(permission.getPermission().lastIndexOf('.' + 1)));
            }
        }
        return -1;
    }
}