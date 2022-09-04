package es.raxthelag.epichead.controllers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import es.raxthelag.epichead.objects.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EpicPlayer {
    // private static HashMap<String, EpicPlayer> players = new HashMap<>();
    private static final Cache<String, EpicPlayer> players = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
    private int id;
    private final String name;
    private @Nullable Player player;
    private final String playerName;
    private @Nullable UUID uniqueId;
    private BigDecimal balance;
    private List<Home> homeList = new ArrayList<>();
    private @Nullable Location lastSeenLocation = null;
    private @Nullable Location deathLocation = null;
    private boolean isLoaded = false;
    private boolean homesLoaded = false;

    private EpicPlayer(String s) {
        this.player = null;
        this.name = s.toLowerCase();
        this.playerName = s;

        players.put(this.name, this);
    }

    private EpicPlayer(Player p) {
        this.player = p;
        this.name = p.getName().toLowerCase();
        this.playerName = p.getName();

        players.put(this.name, this);
    }

    public static EpicPlayer get(@NotNull String pl) {
        if (players.asMap().containsKey(pl.toLowerCase())) return players.getIfPresent(pl.toLowerCase());
        return new EpicPlayer(pl);
    }

    public static EpicPlayer get(@NotNull Player pl) {
        if (players.asMap().containsKey(pl.getName().toLowerCase())) return players.getIfPresent(pl.getName().toLowerCase());
        return new EpicPlayer(pl);
    }

    public static void remove(@NotNull String s) {
        players.invalidate(s.toLowerCase());
    }

    public String getName() { return name; }

    public String getPlayerName() { return playerName; }

    @Nullable
    public Player getPlayer() { return player; }

    @Nullable
    public UUID getUniqueId() { return uniqueId; }

    public void setPlayer(Player player) {
        this.player = player;
        this.uniqueId = player.getUniqueId();
    }

    public BigDecimal getBalance() { return balance; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }

    /**
     * Get if the player has been loaded with database information
     * or if a try has been done, the player does not have database information...
     *
     * This check is very important if you need to save in homes in database
     * or other information that uses playerId as foreign key in database
     * If this return false, you should create player in database
     * before trying any other saving operation
     *
     * @return true if loaded
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Set when player has been loaded with database information
     * @param loaded true if loaded
     */
    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    /** Get the player's homes has been loaded successfully
     * @return true if homes are loaded
     */
    public boolean areHomesLoaded() {
        return homesLoaded;
    }

    public void setHomesLoaded(boolean homesLoaded) {
        this.homesLoaded = homesLoaded;
    }

    public List<Home> getHomes() {
        return homeList;
    }

    public void addHome(Home home) {
        homeList.add(home);
    }

    public void setHomes(List<Home> homes) {
        this.homeList = homes;
    }

    @Nullable
    public Location getDeathLocation() {
        return deathLocation;
    }

    public void setDeathLocation(@Nullable Location deathLocation) {
        this.deathLocation = deathLocation;
    }

    @Nullable
    public Location getLastSeenLocation() {
        return lastSeenLocation;
    }

    public void setLastSeenLocation(@Nullable Location lastSeenLocation) {
        this.lastSeenLocation = lastSeenLocation;
    }

    public boolean isOnline() {
        if (player != null) {
            return player.isOnline();
        }
        Player p = Bukkit.getServer().getPlayerExact(this.getPlayerName());
        if (p == null) return false;

        return p.isOnline();
    }
}
