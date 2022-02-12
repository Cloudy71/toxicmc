package cz.cloudy.minecraft.core.await;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface AwaitConsumer<T> {
    default void created() {
    }

    default void process(T obj) {
        accept(this, obj);
    }

    default void dismiss() {
        Await.dismiss(this);
    }

    void accept(AwaitConsumer<T> consumer, T obj);
}
