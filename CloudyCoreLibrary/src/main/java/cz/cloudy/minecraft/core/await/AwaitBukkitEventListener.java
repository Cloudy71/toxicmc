package cz.cloudy.minecraft.core.await;

import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AwaitBukkitEventListener implements Listener {
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        for (Map<Object, List<AwaitConsumer<?>>> map : Await.events.values()) {
            map.remove(e.getPlayer().getUniqueId());
        }

        for (Map<UUID, List<AwaitConsumer<CommandData>>> map : Await.commands.values()) {
            map.remove(e.getPlayer().getUniqueId());
        }
    }
}
