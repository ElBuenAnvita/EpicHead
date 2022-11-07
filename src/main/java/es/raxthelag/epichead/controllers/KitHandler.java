package es.raxthelag.epichead.controllers;

import es.raxthelag.epichead.Main;
import es.raxthelag.epichead.objects.Kit;
import es.raxthelag.epichead.util.InventoryUtil;
import es.raxthelag.epichead.util.MessageUtil;
import es.raxthelag.epichead.util.Util;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitHandler {
    private HashMap<String, Kit> kitList;

    public KitHandler() {
        reloadKits();
    }

    public void reloadKits() {
        // kitList.clear();
        kitList = new HashMap<>();
        FileConfiguration fileConfiguration = Main.getInstance().getKits();
        ConfigurationSection section = fileConfiguration.getConfigurationSection("kits");

        if (section == null) return;
        for (String key : section.getKeys(true)) {
            if (!(section.get(key) instanceof Kit)) continue;
            Kit currKit = (Kit) section.get(key);
            if (currKit == null) continue;
            kitList.put(currKit.getName().toLowerCase(), currKit);
        }

        Main.debug("Lodaded " + kitList.size() + " kits successfully.");
    }

    @Nullable
    public Kit getKit(String name) {
        return kitList.get(name);
    }

    public Collection<Kit> getKits() { return kitList.values(); }

    public Set<String> getKitsNames() { return kitList.keySet(); }

    public void addKit(Kit kit) { kitList.put(kit.getName().toLowerCase(), kit); }

    public void removeKit(String name) {
        kitList.remove(name);
    }

    public boolean canAffordKit(Kit kit, EpicPlayer player) {
        return player.getBalance().compareTo(kit.getPrice()) >= 0;
    }

    public long getNextUse(Kit kit, EpicPlayer player) throws Exception {
        if (!player.isOnline()) {
            throw new Exception("Player is online");
        }

        Permission permission = Main.getInstance().getPermission();
        final Calendar time = new GregorianCalendar();

        // If player has exempt in delay for all kits / for this kit
        if (permission.playerHas(null, player.getPlayer(), "epiclol.kit.exemptalldelay") || permission.playerHas(null, player.getPlayer(), "epiclol.kit." + kit.getName().toLowerCase() + ".exemptdelay")) {
            return 0L;
        }

        double delay = kit.getDelay();
        long last_received = 0;
        String timestamp = getKitTimestamp(kit, player);
        if (timestamp != null && !timestamp.isEmpty()) {
            last_received = Long.parseLong(timestamp);
        }

        Calendar delayTime = new GregorianCalendar();
        delayTime.setTimeInMillis(last_received);
        delayTime.add(Calendar.SECOND, (int) delay);
        delayTime.add(Calendar.MILLISECOND, (int) ((delay * 1000.0) % 1000.0));

        if (last_received == 0L || last_received > time.getTimeInMillis()) {
            return 0L;
        } else if (delay < 0d) {
            // If the kit has a negative kit time, it can only be used once.
            return -1;
        } else if (delayTime.before(time)) {
            return 0L;
        } else {
            return delayTime.getTimeInMillis();
        }
    }

    private String getKitTimestamp(Kit kit, EpicPlayer epicPlayer) {
        Player p = epicPlayer.getPlayer();
        if (p == null) return null;
        return Util.getPermissionValue(p, "epiclol.kit." + kit.getName().toLowerCase() + ".ltr");
    }

    public void giveKit(Kit kit, EpicPlayer epicPlayer) throws Exception {
        Player player = epicPlayer.getPlayer();
        if (player == null) throw new Exception("yml-general.kit.error-give-player-null");

        Map<Integer, ItemStack> overflowed = InventoryUtil.addAllItems(player.getInventory(), kit.getItemList().toArray(new ItemStack[0]));
        if (overflowed != null) throw new Exception("yml-general.kit.error-full-inventory");

        Util.setPermissionValue(player, "epiclol.kit." + kit.getName().toLowerCase() + ".ltr", System.currentTimeMillis()+"");
    }

    public void chargeKit(Kit kit, EpicPlayer epicPlayer) {
        if (epicPlayer.getPlayer() == null) return;

        if (kit.getPermission() != null && !epicPlayer.getPlayer().hasPermission(kit.getPermission())) {
            MessageUtil.sendMessage(epicPlayer.getPlayer(), "general.kit.no-permission-kit", "No tienes acceso a este kit.", TagResolver.resolver(Placeholder.unparsed("kit_name", kit.getName()), Placeholder.parsed("kit_displayname", kit.getDisplayName())));
        }

        if (!canAffordKit(kit, epicPlayer)) {
            MessageUtil.sendMessage(epicPlayer.getPlayer(), "general.kit.cannot-afford", "No tienes suficiente dinero", TagResolver.resolver(Placeholder.unparsed("kit_name", kit.getName()), Placeholder.parsed("kit_displayname", kit.getDisplayName())));
            return;
        }

        try {
            long nextUse = getNextUse(kit, epicPlayer);
            if (nextUse == -1) {
                MessageUtil.sendMessage(
                        epicPlayer.getPlayer(),
                        "general.kit.already-claimed-only-once",
                        "Este kit solo se puede reclamar una vez",
                        TagResolver.resolver(
                                Placeholder.unparsed("kit_name", kit.getName()),
                                Placeholder.parsed("kit_displayname", kit.getDisplayName())
                        )
                );
                return;
            } else if (nextUse != 0L) {
                String duration = Util.getDurationSmall(nextUse);
                MessageUtil.sendMessage(
                        epicPlayer.getPlayer(),
                        "general.kit.already-claimed-delay",
                        "Tienes que esperar <duration> para volver a reclamar este kit",
                        TagResolver.resolver(
                                Placeholder.unparsed("kit_name", kit.getName()),
                                Placeholder.parsed("kit_displayname", kit.getDisplayName()),
                                Placeholder.unparsed("duration", duration)
                        )
                );
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            epicPlayer.withdraw(kit.getPrice().doubleValue());
            giveKit(kit, epicPlayer);
            MessageUtil.sendMessage(
                    epicPlayer.getPlayer(),
                    "general.kit.claimed",
                    "Has reclamado el kit <kit>",
                    TagResolver.resolver(
                            Placeholder.unparsed("kit", kit.getName()),
                            Placeholder.parsed("kit_displayname", kit.getDisplayName())
                    )
            );
        } catch (Exception e) {
            MessageUtil.sendExceptionMessage(epicPlayer.getPlayer(), e);
        }
    }

    public void saveKits() {
        File file = new File(Main.getInstance().getDataFolder(), "kits.yml");
        FileConfiguration fileConfiguration = Main.getInstance().getKits();
        ConfigurationSection section = fileConfiguration.getConfigurationSection("kits");
        if (section != null) {
            for (Kit kit : this.getKits()) {
                section.set(kit.getName().toLowerCase(), kit);
            }
        }
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
