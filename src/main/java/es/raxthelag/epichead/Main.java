package es.raxthelag.epichead;

import co.aikar.commands.PaperCommandManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import es.raxthelag.epichead.commands.*;
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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static final List<TagResolver> customTags = new ArrayList<>();
    public static final List<Warp> warps = new ArrayList<>();

    //                          Target, Task
    public static final HashMap<String, Task> pendingTasks = new HashMap<>();
    //                        Target, Map<Player, TPAType>
    public static Cache<String, Map.Entry<String, TpaType>> pendingTpas = null;

    private YamlConfiguration messagesConfig;
    private YamlConfiguration locationsConfig;
    private PaperCommandManager commandManager;
    private DataConnection dataConnection;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Plugin startup logic
        this.saveDefaultsConfig();
        this.loadConfigInUTF();
        this.loadMessagesInUTF();

        // Since Vault is already a dependency, then we will just registrate our Economy class.
        economy = new EconomyHolder();
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.High);

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
        commandManager.registerCommand(new HomeCommand());
        commandManager.registerCommand(new EconCommands());
        commandManager.registerCommand(new BackCommand());

        this.loadCommandCompletions();

        try {
            this.dataConnection = new DataConnection();
        } catch (Exception e) {
            this.getPluginLoader().disablePlugin(this);
        }

        // Load warps and spawn when worlds are loaded already.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.loadLocationsInUTF();
            this.loadWarps();
        }, 1L);
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
        File locationsFile = new File(this.getDataFolder(), "locations.yml");
        if (!locationsFile.exists()) {
            this.saveResource("locations.yml", false);
        }
        File acfMessagesFile = new File(this.getDataFolder(), "acf-messages.yml");
        if (!acfMessagesFile.exists()) {
            this.saveResource("acf-messages.yml", false);
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

    public void loadLocationsInUTF() {
        File file = new File(this.getDataFolder(), "locations.yml");
        this.locationsConfig = new YamlConfiguration();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            this.locationsConfig.load(reader);
        } catch (Exception e) {
            this.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Couldn't load locations! Error in parsing locations!");
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
        // ConfigurationSection section = getConfig().getConfigurationSection("warps");
        ConfigurationSection section = getLocations().getConfigurationSection("warps");
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

        try {
            Locale esLocale = new Locale("es");
            commandManager.addSupportedLanguage(esLocale);
            commandManager.getLocales().loadYamlLanguageFile("acf-messages.yml", esLocale);
            commandManager.getLocales().setDefaultLocale(esLocale);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveWarps(boolean forceConfigSave) {
        HashMap<String, Warp> section = new HashMap<>();
        warps.forEach(w -> section.put(w.getName(),w));

        getLocations().createSection("warps", section);
        // if (forceConfigSave) saveConfig();
        if (forceConfigSave) {
            File file = new File(this.getDataFolder(), "locations.yml");
            try {
                getLocations().save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* ConfigurationSection section = getConfig().getConfigurationSection("warps");
        if (section == null) throw new Exception("Section is null");

        warps.forEach(warp -> section.set(warp.getName(), warp));
        if (forceConfigSave) saveConfig(); */
    }

    public void saveLocations() {
        saveWarps(true);
    }

    public FileConfiguration getMessages() {
        return this.messagesConfig;
    }

    public FileConfiguration getLocations() {
        return this.locationsConfig;
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
