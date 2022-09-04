package es.raxthelag.epichead.objects;

import org.bukkit.Location;

import javax.annotation.Nullable;

public class Home {
    public @Nullable Integer id;
    public String name;
    public Location location;

    public Home(@Nullable Integer id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public @Nullable Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
