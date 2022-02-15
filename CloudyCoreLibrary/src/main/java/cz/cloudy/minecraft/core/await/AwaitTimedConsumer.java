package cz.cloudy.minecraft.core.await;

import cz.cloudy.minecraft.core.CoreRunnerPlugin;
import cz.cloudy.minecraft.core.LoggerFactory;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public interface AwaitTimedConsumer<T> extends AwaitConsumer<T> {
    Logger logger = LoggerFactory.getLogger(AwaitTimedConsumer.class); // TODO: Delete
    Map<AwaitConsumer<?>, Integer> tasks = new HashMap<>();

    @Override
    default void created() {
        if (tasks.containsKey(this))
            return;

        final AwaitTimedConsumer<T> self = this;
        tasks.put(this, Bukkit.getScheduler().scheduleSyncDelayedTask(
                CoreRunnerPlugin.singleton,
                () -> {
                    logger.info("TIME DISMISS");
                    timeout();
                    AwaitConsumer.super.dismiss();
                    tasks.remove(self);
                },
                ticks()
        ));
    }

    @Override
    default void dismiss() {
        AwaitConsumer.super.dismiss();
        Integer taskId;
        if ((taskId = tasks.get(this)) == null)
            return;

        logger.info("MANUAL DISMISS");
        Bukkit.getScheduler().cancelTask(taskId);
        tasks.remove(this);
    }

    @Override
    default void process(T obj) {
        AwaitConsumer.super.process(obj);
    }

    int ticks();

    default void timeout() {
    }
}
