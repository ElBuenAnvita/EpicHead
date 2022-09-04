package es.raxthelag.epichead.objects.tasks;

import es.raxthelag.epichead.objects.Task;
import es.raxthelag.epichead.objects.TpaType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaTask implements Task {
    Player sender;
    Player recipient;
    TpaType tpaType;
    Integer taskId;

    /* public TpaTask(Player sender, Player recipient, TpaType tpaType) {
        this.sender = sender;
        this.recipient = recipient;
        this.tpaType = tpaType;
    } */

    public TpaTask(Player sender, Player recipient, TpaType tpaType, @NotNull Integer taskId) {
        this.sender = sender;
        this.recipient = recipient;
        this.tpaType = tpaType;
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

    public TpaType getTpaType() {
        return tpaType;
    }
}
