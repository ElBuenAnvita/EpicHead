package es.raxthelag.epichead.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.controllers.EconomyHolder;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.objects.Warp;
import es.raxthelag.epichead.util.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

@CommandAlias("epichead|eh")
@Description("EpicHead admin console command")
@CommandPermission("epiclol.admin")
public class EpicHeadCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @Subcommand("help")
    @Description("Get help about EpicHead admin command")
    public void onHelp(Player player) {
        MessageUtil.sendMessage(player, "general.help", null);
    }

    @Subcommand("setspawn")
    @CommandPermission("epiclol.admin.setspawn")
    public void onSetSpawn(Player player) {
        Main.getInstance().getConfig().set("spawn.location", player.getLocation());
        // Main.getInstance().getConfig().options().copyDefaults(true);
        Main.getInstance().saveConfig();

        MessageUtil.sendMessage(player, "general.spawn.set-success", "Guardado con éxito");
    }

    @Subcommand("warp add")
    @CommandPermission("epiclol.admin.warp.add")
    public void onNewWarp(Player player, String name, @Optional String displayName) {
        if (Main.warps.stream().anyMatch(w -> w.getName().equalsIgnoreCase(name))) {
            MessageUtil.sendMessage(player, "general.warp.warp-exist", "La warp ya existe", Placeholder.unparsed("warp", name));
            return;
        }

        Main.warps.add(new Warp(name.toLowerCase(), displayName, player.getLocation()));
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
    @CommandPermission("epiclol.admin.warp.set")
    @CommandCompletion("@warps")
    public void onDeleteWarp(Player player, @Single String name) {
        if (Main.warps.stream().noneMatch(w -> w.getName().equalsIgnoreCase(name))) {
            MessageUtil.sendMessage(player, "general.warp.warp-no-exist", "La warp no existe", Placeholder.unparsed("warp", name));
            return;
        }

        Main.warps.removeIf(w -> w.getName().equalsIgnoreCase(name));
        try { Main.getInstance().saveWarps(true); } catch (Exception e) { e.printStackTrace(); }

        MessageUtil.sendMessage(player, "general.warp.warp-del-success", "Se ha reubicado", Placeholder.unparsed("warp", name));
    }

    @Subcommand("eco add")
    @CommandPermission("epiclol.admin.eco.add")
    public void onEcoAdd(Player player, String playerName, @Single int amount) {
        EpicPlayer epicPlayer = EpicPlayer.get(playerName);
        OfflinePlayer offlinePlayer = null;
        if (epicPlayer.isLoaded() && epicPlayer.getUniqueId() != null) {
            offlinePlayer = Bukkit.getOfflinePlayer(epicPlayer.getUniqueId());
        } else { offlinePlayer = Bukkit.getOfflinePlayer(playerName); }

        // EconomyHolder.
    }
}
