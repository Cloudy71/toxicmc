package cz.cloudy.minecraft.core.await;

import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

import java.util.*;

// TODO: Add timed consumers
public class Await {
    private static final Map<Class<? extends Event>, Map<Object, List<AwaitConsumer<?>>>> events =
            new HashMap<>();
    private static final Map<String, Map<UUID, List<AwaitConsumer<CommandData>>>> commands =
            new HashMap<>();
    private static final Set<AwaitConsumer<?>> dismissedConsumersList = new HashSet<>();

//    private static final List<AwaitConsumer<?>> dismissedConsumersList = new ArrayList<>();
//    private static AwaitConsumer<?> currentConsumer = null;

    @SafeVarargs
    public static <T extends PlayerEvent> void playerEvent(Player player, Class<T> eventType, AwaitConsumer<T>... consumers) {
        for (AwaitConsumer<T> consumer : consumers) {
            consumer.created();
        }
        events.computeIfAbsent(eventType, aClass -> new HashMap<>())
                .computeIfAbsent(player.getUniqueId(), o -> new ArrayList<>())
                .addAll(Arrays.stream(consumers).toList());
    }

    @SafeVarargs
    public static void playerCommand(Player player, String command, AwaitConsumer<CommandData>... consumers) {
        for (AwaitConsumer<CommandData> consumer : consumers) {
            consumer.created();
        }
        commands.computeIfAbsent(command, s -> new HashMap<>())
                .computeIfAbsent(player.getUniqueId(), uuid -> new ArrayList<>())
                .addAll(Arrays.stream(consumers).toList());
    }

    public static void dismissPlayerCommand(Player player, String command) {
        if (!commands.containsKey(command) || !commands.get(command).containsKey(player.getUniqueId()))
            return;

        for (AwaitConsumer<CommandData> consumer : commands.get(command).get(player.getUniqueId())) {
            consumer.dismiss();
        }
    }

    @SuppressWarnings("unchecked")
    public static void process(Event event) {
        if (!events.containsKey(event.getClass()))
            return;

        Map<Object, List<AwaitConsumer<?>>> consumerMap = events.get(event.getClass());
        List<AwaitConsumer<?>> consumerList;
        Object key = null;
        if (event instanceof PlayerEvent playerEvent)
            key = playerEvent.getPlayer().getUniqueId();

        consumerList = consumerMap.get(key);
        if (consumerList == null)
            return;

        List<AwaitConsumer<?>> localDismissList = new ArrayList<>();
        for (AwaitConsumer<?> consumer : consumerList) {

            // Don't run if it was already dismissed, just pass it into remove list.
            if (dismissedConsumersList.contains(consumer)) {
                localDismissList.add(consumer);
                continue;
            }
            ((AwaitConsumer<Event>) consumer).process(event);
            // If it was dismissed after consumer process, still pass it into remove list.
            if (dismissedConsumersList.contains(consumer))
                localDismissList.add(consumer);
        }
        for (AwaitConsumer<?> consumer : localDismissList) {
            consumerList.remove(consumer);
            dismissedConsumersList.remove(consumer);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public static void process(CommandData commandData) {
        if (!commandData.isPlayer() || !commands.containsKey(commandData.command().getName()))
            return;

        Map<UUID, List<AwaitConsumer<CommandData>>> consumerMap = commands.get(commandData.command().getName());
        List<AwaitConsumer<CommandData>> consumerList = consumerMap.get(commandData.getPlayer().getUniqueId());
        if (consumerList == null)
            return;

        List<AwaitConsumer<?>> localDismissList = new ArrayList<>();
        for (AwaitConsumer<?> consumer : consumerList) {

            // Don't run if it was already dismissed, just pass it into remove list.
            if (dismissedConsumersList.contains(consumer)) {
                localDismissList.add(consumer);
                continue;
            }
            ((AwaitConsumer<CommandData>) consumer).process(commandData);
            // If it was dismissed after consumer process, still pass it into remove list.
            if (dismissedConsumersList.contains(consumer))
                localDismissList.add(consumer);
        }
        for (AwaitConsumer<?> consumer : localDismissList) {
            consumerList.remove(consumer);
            dismissedConsumersList.remove(consumer);
        }
    }

    protected static void dismiss(AwaitConsumer<?> awaitConsumer) {
        if (awaitConsumer == null)
            return;

        dismissedConsumersList.add(awaitConsumer);
    }
}
