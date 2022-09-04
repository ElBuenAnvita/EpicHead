package es.raxthelag.epichead.objects.tasks;

import es.raxthelag.epichead.objects.Home;
import es.raxthelag.epichead.objects.Task;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeTask implements Task {
    Player sender;
    Home home;
    Integer taskId;

    public HomeTask(Player sender, Home home, @NotNull Integer taskId) {
        this.sender = sender;
        this.home = home;
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

    public Home getHome() {
        return home;
    }

    @Override
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
