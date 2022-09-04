package es.raxthelag.epichead.objects.tasks;

import es.raxthelag.epichead.objects.Task;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnTask implements Task {
    Player sender;
    Player recipient;
    Integer taskId;

    /* public SpawnTask(Player sender, Player recipient) {
        this.sender = sender;
        this.recipient = recipient;
    } */

    public SpawnTask(Player sender, Player recipient, @NotNull Integer taskId) {
        this.sender = sender;
        this.recipient = recipient;
        this.taskId = taskId;
    }

    @Override
    public Player getRecipient() {
        return recipient;
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
