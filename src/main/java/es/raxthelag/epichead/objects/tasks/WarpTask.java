package es.raxthelag.epichead.objects.tasks;

import es.raxthelag.epichead.objects.Task;
import es.raxthelag.epichead.objects.Warp;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarpTask implements Task {
    Player sender;
    Warp warp;
    Integer taskId;

    public WarpTask(Player sender, Warp warp, @NotNull Integer taskId) {
        this.sender = sender;
        this.warp = warp;
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

    @Override
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
