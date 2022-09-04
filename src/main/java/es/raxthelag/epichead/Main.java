package es.raxthelag.epichead;

import co.aikar.commands.PaperCommandManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import es.raxthelag.epichead.commands.EpicHeadCommand;
import es.raxthelag.epichead.commands.SpawnCommand;
import es.raxthelag.epichead.commands.TpaCommand;
import es.raxthelag.epichead.commands.WarpCommand;
import es.raxthelag.epichead.controllers.DataConnection;
import es.raxthelag.epichead.controllers.EconomyHolder;
import es.raxthelag.epichead.controllers.EpicPlayer;
import es.raxthelag.epichead.listeners.PlayerListener;
import es.raxthelag.epichead.objects.Home;
import es.raxthelag.epichead.objects.Task;
import es.raxthelag.epichead.objects.TpaType;
import es.raxthelag.epichead.objects.Warp;
import es.raxthelag.epichead.util.MessageUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static final List<TagResolver> customTags = new ArrayList<>();
    public static final List<Warp> warps = new ArrayList<>();

    //                          Target, Task
    public static final HashMap<String, Task> pendingTasks = new HashMap<>();
    //                        Target, Map<Player, TPAType>
    public static Cache<String, Map<String, TpaType>> pendingTpas = null;

    private YamlConfiguration messagesConfig;
    private PaperCommandManager commandManager;
    private DataConnection dataConnection;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Since Vault is already a dependency, then we will just registrate our Economy class.
        economy = new EconomyHolder();
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.High);

        // Plugin startup logic
        this.saveDefaultsConfig();
        this.loadConfigInUTF();
        this.loadMessagesInUTF();

        this.loadCustomTags(true);
        this.loadCaches();

        // EventHandlers
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        // Commands
        commandManager = new PaperCommandManager(this);
        // BukkitCommandManager manager = new BukkitCommandManager(this);
        commandManager.registerCommand(new EpicHeadCommand());
        commandManager.registerCommand(new SpawnCommand());
        commandManager.registerCommand(new TpaCommand());
        commandManager.registerCommand(new WarpCommand());

        this.loadCommandCompletions();
        this.dataConnection = new DataConnection();

        Bukkit.getScheduler().runTaskLater(this, this::loadWarps, 1L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void saveDefaultsConfig() {
        this.saveDefaultConfig();
        File file = new File(this.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            this.saveResource("messages.yml", false);
            // try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void loadConfigInUTF() {
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) return;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            this.getConfig().load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMessagesInUTF() {
        File file = new File(this.getDataFolder(), "messages.yml");
        this.messagesConfig = new YamlConfiguration();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            this.messagesConfig.load(reader);
        } catch (Exception e) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Couldn't load messages! Error in parsing messages!");
            e.printStackTrace();
        }
    }

    public void loadCustomTags(boolean setNewMiniMessage) {
        ConfigurationSection section = this.messagesConfig.getConfigurationSection("custom-tags");
        if (section == null) return;

        for (String key : section.getKeys(true)) {
            String value = section.getString(key);
            customTags.add(Placeholder.parsed(key, (value != null) ? value : ""));
        }

        if (setNewMiniMessage) {
            MessageUtil.setMiniMessage(MiniMessage
                    .builder()
                    .editTags(t -> t.resolvers(this.getCustomTags()))
                    .build());
        }
    }

    public void loadCaches() {
        /* Tpa Cache */
        int secondsExpire = this.getConfig().getInt("times.tpa.expire-petition", 60);
        pendingTpas = CacheBuilder
                .newBuilder()
                .expireAfterWrite(secondsExpire, TimeUnit.SECONDS)
                .build();
    }

    public void loadWarps() {
        ConfigurationSection section = getConfig().getConfigurationSection("warps");
        if (section == null) return;

        for (String key : section.getKeys(true)) {
            if (!(section.get(key) instanceof Warp)) continue;
            warps.add((Warp) section.get(key));
        }
    }

    public void loadCommandCompletions() {
        commandManager.getCommandCompletions().registerCompletion("warps", c -> {
            // return ImmutableList.of()
            return warps.stream().map(Warp::getName).collect(Collectors.toList());
        });

        commandManager.getCommandCompletions().registerCompletion("homes", c -> {
            if (c.getSender() instanceof Player) {
                return EpicPlayer.get((Player) c.getSender()).getHomes().stream().map(Home::getName).collect(Collectors.toList());
            }
            return ImmutableList.of();
        });
    }

    public void saveWarps(boolean forceConfigSave) {
        HashMap<String, Warp> section = new HashMap<>();
        warps.forEach(w -> section.put(w.getName(),w));

        getConfig().createSection("warps", section);
        if (forceConfigSave) saveConfig();

        /* ConfigurationSection section = getConfig().getConfigurationSection("warps");
        if (section == null) throw new Exception("Section is null");

        warps.forEach(warp -> section.set(warp.getName(), warp));
        if (forceConfigSave) saveConfig(); */
    }

    public FileConfiguration getMessages() {
        return this.messagesConfig;
    }

    public Economy getEconomy() { return economy; }

    public static Main getInstance() {
        return instance;
    }

    public List<TagResolver> getCustomTags() {
        return customTags;
    }

    public DataConnection getDataConnection() {
        return dataConnection;
    }

    public static boolean getIfPlayerHasPendingTasks(Player p) {
        return pendingTasks.containsKey(p.getName());
    }

    public static void debug(String s) {
        if (Main.getInstance().getConfig().getBoolean("debug", false)) {
            Bukkit.getLogger().info(s);
        }
    }
}
