package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Warp;
import es.raxthelag.epichead.util.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Locale;

@CommandAlias("epichead|eh")
@Description("EpicHead admin console command")
@CommandPermission("epiclol.admin")
public class EpicHeadCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @Subcommand("help")
    @Description("Get help about EpicHead admin command")
    public void onHelp(CommandSender sender) {
        MessageUtil.sendMessage(sender, "general.help", null);
    }

    @Subcommand("setspawn")
    @CommandPermission("epiclol.admin.setspawn")
    public void onSetSpawn(Player player) {
        Main.getInstance().getLocations().set("spawn", player.getLocation());
        // Main.getInstance().getConfig().options().copyDefaults(true);
        Main.getInstance().saveLocations();

        MessageUtil.sendMessage(player, "general.spawn.set-success", "Guardado con éxito");
    }

    @Subcommand("warp add")
    @CommandPermission("epiclol.admin.warp.add")
    public void onNewWarp(Player player, String name, @Optional String displayName) {
        if (Main.warps.stream().anyMatch(w -> w.getName().equalsIgnoreCase(name))) {
            MessageUtil.sendMessage(player, "general.warp.warp-exist", "La warp ya existe", Placeholder.unparsed("warp", name));
            return;
        }

        Main.warps.add(
                new Warp(
                        name.toLowerCase(),
                        (displayName != null) ? displayName : name.toLowerCase(),
                        player.getLocation()
                )
        );

        try { Main.getInstance().saveWarps(true); } catch (Exception e) { e.printStackTrace(); }

        MessageUtil.sendMessage(player, "general.warp.warp-add-success", "Se ha creado", Placeholder.unparsed("warp", name));
    }

    @Subcommand("warp relocate|set|rel")
    @CommandPermission("epiclol.admin.warp.set")
    @CommandCompletion("@warps")
    public void onRelocateWarp(Player player, @Single String name) {
        java.util.Optional<Warp> warp = Main.warps.stream().filter(w -> w.getName().equalsIgnoreCase(name)).findFirst();
        if (!warp.isPresent()) {
            MessageUtil.sendMessage(player, "general.warp.warp-no-exist", "La warp no existe", Placeholder.unparsed("warp", name));
            return;
        }

        warp.get().setSpawnLocation(player.getLocation());
        try { Main.getInstance().saveWarps(true); } catch (Exception e) { e.printStackTrace(); }

        MessageUtil.sendMessage(player, "general.warp.warp-rel-success", "Se ha reubicado", Placeholder.unparsed("warp", name));
    }

    @Subcommand("warp delete|del")
    @CommandPermission("epiclol.admin.warp.del")
    @CommandCompletion("@warps")
    public void onDeleteWarp(CommandSender sender, @Single String name) {
        if (Main.warps.stream().noneMatch(w -> w.getName().equalsIgnoreCase(name))) {
            MessageUtil.sendMessage(sender, "general.warp.warp-no-exist", "La warp no existe", Placeholder.unparsed("warp", name));
            return;
        }

        Main.warps.removeIf(w -> w.getName().equalsIgnoreCase(name));
        try { Main.getInstance().saveWarps(true); } catch (Exception e) { e.printStackTrace(); }

        MessageUtil.sendMessage(sender, "general.warp.warp-del-success", "Se ha eliminado", Placeholder.unparsed("warp", name));
    }

    @Subcommand("eco add|give|deposit")
    @CommandPermission("epiclol.admin.eco.add")
    @CommandCompletion("@players @nothing")
    public void onEcoAdd(CommandSender sender, String playerName, @Single double amount) {
        EpicPlayer epicPlayer = EpicPlayer.get(playerName);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                if (!epicPlayer.isLoaded()) {
                    if (!Main.getInstance().getDataConnection().playerExistInDatabase(epicPlayer)) {
                        MessageUtil.sendMessage(sender, "general.eco.error-player-offline-and-inexistent", "Jugador inexistente", Placeholder.unparsed("player", playerName));
                        return;
                    }
                    Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                }
                epicPlayer.deposit(amount, true);

                MessageUtil.sendMessage(
                        sender,
                        "general.eco.admin.deposit-success",
                        "Deposito exitoso",
                        TagResolver.resolver(
                                Placeholder.unparsed("player", epicPlayer.getPlayerName()),
                                Placeholder.unparsed("player_balance", Main.getInstance().getEconomy().format(epicPlayer.getBalance().doubleValue())),
                                Placeholder.unparsed("amount", Main.getInstance().getEconomy().format(amount))
                        )
                );

                if (!epicPlayer.isOnline()) EpicPlayer.remove(epicPlayer.getName());
            } catch (SQLException e) {
                e.printStackTrace();
                MessageUtil.sendMessage(sender, "general.eco.error-generic", "Error. Revise la consola.");
            }
        });

        /* OfflinePlayer offlinePlayer = null;
        if (epicPlayer.isLoaded() && epicPlayer.getUniqueId() != null) {
            offlinePlayer = Bukkit.getOfflinePlayer(epicPlayer.getUniqueId());
        } else { offlinePlayer = Bukkit.getOfflinePlayer(playerName); } */
        // EconomyHolder.
    }

    @Subcommand("eco remove|withdraw")
    @CommandPermission("epiclol.admin.eco.remove")
    @CommandCompletion("@players @nothing")
    public void onEcoRemove(CommandSender sender, String playerName, @Single double amount) {
        EpicPlayer epicPlayer = EpicPlayer.get(playerName);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            try {
                if (!epicPlayer.isLoaded()) {
                    if (!Main.getInstance().getDataConnection().playerExistInDatabase(epicPlayer)) {
                        MessageUtil.sendMessage(sender, "general.eco.error-player-offline-and-inexistent", "Jugador inexistente", Placeholder.unparsed("player", playerName));
                        return;
                    }
                    Main.getInstance().getDataConnection().loadPlayer(epicPlayer);
                }
                epicPlayer.withdraw(amount, true);

                MessageUtil.sendMessage(
                        sender,
                        "general.eco.admin.withdraw-success",
                        "Retiro exitoso",
                        TagResolver.resolver(
                                Placeholder.unparsed("player", epicPlayer.getPlayerName()),
                                Placeholder.unparsed("player_balance", Main.getInstance().getEconomy().format(epicPlayer.getBalance().doubleValue())),
                                Placeholder.unparsed("amount", Main.getInstance().getEconomy().format(amount))
                        )
                );

                if (!epicPlayer.isOnline()) EpicPlayer.remove(epicPlayer.getName());
            } catch (SQLException e) {
                e.printStackTrace();
                MessageUtil.sendMessage(sender, "general.eco.error-generic", "Error. Revise la consola.");
            }
        });
    }
}
