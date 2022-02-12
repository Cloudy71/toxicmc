package cz.cloudy.minecraft.core.particles;

import org.bukkit.Bukkit;

import java.util.List;

public class ParticleJob {
    protected List<Integer> taskIds;

    protected ParticleJob(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }

    public void stop() {
        taskIds.forEach(integer -> Bukkit.getScheduler().cancelTask(integer));
        taskIds.clear();
        taskIds = null;
    }
}
