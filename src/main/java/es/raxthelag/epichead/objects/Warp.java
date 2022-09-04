package es.raxthelag.epichead.objects;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@SerializableAs("Warp")
public class Warp implements ConfigurationSerializable {
    private String name;
    private String displayName;
    private @Nullable String description = null;
    private Location spawnLocation;
    /* private boolean showTitle = false;
    private @Nullable String title = null;
    private @Nullable String subtitle = null; */
    private @Nullable String permission = null;
    private @Nullable String welcomeMessage = null;

    public Warp(Map<String, Object> deserialize) {
        this.name = deserialize.get("name").toString();
        this.displayName = deserialize.get("displayname").toString();
        this.description = deserialize.get("description").toString();
        this.spawnLocation = (Location) deserialize.get("spawn-location");
        // this.spawnLocation = Location.deserialize((Map<String, Object>) deserialize.get("spawn-location"));
        /* this.showTitle = Boolean.parseBoolean(deserialize.get("show-title").toString());
        this.title = deserialize.get("title").toString();
        this.subtitle = deserialize.get("subtitle").toString(); */
        this.permission = deserialize.get("permission").toString();
        this.welcomeMessage = deserialize.get("welcome-message").toString();
    }

    public Warp(String name, String displayName, Location location) {
        this.name = name;
        this.displayName = displayName;
        this.spawnLocation = location;
        this.permission = "epichead.warp." + name;
    }

    public Warp(String name, Location location) {
        this.name = name;
        this.displayName = name;
        this.spawnLocation = location;
        this.permission = "epichead.warp." + name;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> warp = new HashMap<>();
        warp.put("name", this.name);
        warp.put("displayname", this.displayName);
        warp.put("description", this.description);
        warp.put("spawn-location", this.spawnLocation);
        warp.put("permission", this.permission);

        /* warp.put("show-title", this.showTitle);
        warp.put("title", this.title);
        warp.put("subtitle", this.subtitle); */

        return warp;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    /* public boolean isTitleEnabled() { return showTitle; }

    @Nullable
    public String getTitle() { return title; }

    @Nullable
    public String getSubtitle() { return subtitle; } */

    @Nullable
    public String getPermission() {
        return permission;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getWelcomeMessage() { return welcomeMessage; }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    /* public void setShowTitle(boolean showTitle) { this.showTitle = showTitle; }

    public void setTitle(@Nullable String title) { this.title = title; }

    public void setSubtitle(@Nullable String subtitle) { this.subtitle = subtitle; } */

    public void setDescription(@Nullable String description) { this.description = description; }

    public void setPermission(@Nullable String permission) { this.permission = permission; }

    public void setWelcomeMessage(@Nullable String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
}
