package es.raxthelag.epichead.objects;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public interface Task {
    /**
     * Gets the player who committed the Task.
     * @return Player
     */
    @Nullable Player getRecipient();

    /**
     * Gets the player who is reaching the Task.
     * @return Player
     */
    Player getSender();

    /**
     * Get the BukkitTask id associated with the task.
     * @return Integer - taskId
     */
    Integer getTaskId();

    /**
     * Set or replace the BukkitTask id associated with the task.
     * @param taskId the BukkitTask id associated
     */
    void setTaskId(Integer taskId);
}
