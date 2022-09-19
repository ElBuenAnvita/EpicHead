package es.raxthelag.epichead.objects.tasks;

import es.raxthelag.epichead.objects.Task;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BackTask implements Task {
    Player sender;
    Location location;
    Integer taskId;

    public BackTask(Player sender, Location location, @NotNull Integer taskId) {
        this.sender = sender;
        this.location = location;
        this.taskId = taskId;
    }

    @Override
    public Player getRecipient() {
        return null;
    }

    @Override
    public Player getSender() {
        return sender;
    }

    @Override
    public Integer getTaskId() {
        return taskId;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
