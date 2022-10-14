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
    private @Nullable String permission = null;
    private @Nullable String welcomeMessage = null;

    public Warp(Map<String, Object> deserialize) {
        this.name = deserialize.get("name").toString();
        this.displayName = deserialize.get("displayname").toString();
        if (deserialize.get("description") != null) this.description = deserialize.get("description").toString();
        this.spawnLocation = (Location) deserialize.get("spawn-location");
        if (deserialize.get("permission") != null) this.permission = deserialize.get("permission").toString();
        if (deserialize.get("welcome-message") != null) this.welcomeMessage = deserialize.get("welcome-message").toString();
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
        warp.put("welcome-message", this.welcomeMessage);

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

    public void setDescription(@Nullable String description) { this.description = description; }

    public void setPermission(@Nullable String permission) { this.permission = permission; }

    public void setWelcomeMessage(@Nullable String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
}
