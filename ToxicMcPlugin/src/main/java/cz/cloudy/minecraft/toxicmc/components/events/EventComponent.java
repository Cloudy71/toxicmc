/*
  User: Cloudy
  Date: 21/01/2022
  Time: 03:38
*/

package cz.cloudy.minecraft.toxicmc.components.events;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.componentsystem.annotations.*;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.messengersystem.ChatComponent;
import cz.cloudy.minecraft.messengersystem.types.LoginEventData;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.events.events.GameEvent;
import cz.cloudy.minecraft.toxicmc.components.events.events.GameEventNone;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * @author Cloudy
 */
@Component
@WorldOnly(filter = ToxicConstants.WORLDS_SURVIVAL)
public class EventComponent
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(EventComponent.class);

    @Component
    private ChatComponent messenger;

    private GameEvent gameEvent;

    @Override
    public void onLoad() {
        gameEvent = new GameEventNone();
        gameEvent.setup(getPlugin(), new String[0]);
        gameEvent.onStart();
    }

    @ActionListener(value = "MessengerSystem.onLogin", priority = 10)
    private void onLogin(LoginEventData data) {
        data.player().sendMessage(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktuální event: " + gameEvent.getMessageString());
    }

    @CommandListener("change_event")
    @CheckPermission(CheckPermission.OP)
    private void onChangeEvent(CommandData commandData) {
        String name = commandData.arguments()[0];
        String[] args = Arrays.copyOfRange(commandData.arguments(), 1, commandData.arguments().length);
        Class<? extends GameEvent> eventClass;
        try {
            eventClass = (Class<? extends GameEvent>) Class.forName("cz.cloudy.minecraft.toxicmc.components.events.events.GameEvent" + name);
        } catch (ClassNotFoundException e) {
            logger.error("Failed to change event: ", e);
            return;
        }

        GameEvent event = ReflectionUtils.newInstance(eventClass);
        if (event == null)
            return;

        if (!event.setup(getPlugin(), args)) {
            logger.warn("Failed to setup event.");
            return;
        }

        if (gameEvent != null)
            gameEvent.onEnd();
        gameEvent = event;
        String messagePrint = gameEvent.getMessagePrint();
        if (messagePrint != null) {
            messenger.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Nový event:");
            messenger.sendMessage(gameEvent.getMessagePrint());
        }
        gameEvent.onStart();
    }

    @CommandListener("spawn_mob")
    @CheckPermission(CheckPermission.OP)
    @CheckCondition(CheckCondition.ARGS_IS_1)
    @CheckCondition(CheckCondition.SENDER_IS_PLAYER)
    private void onSpawnMob(CommandData commandData) {
        String name = commandData.arguments()[0];
        EntityType entityType = EntityType.valueOf(name.toUpperCase());
        commandData.getPlayer().getWorld().spawnEntity(
                commandData.getPlayer().getLocation().add(commandData.getPlayer().getLocation().getDirection().multiply(2)),
                entityType
        );
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e) {
        gameEvent.onEntityDeathEvent(e);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        gameEvent.onEntityDamageEvent(e);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        gameEvent.onEntityDamageByEntityEvent(e);
    }

    @EventHandler
    public void onEntityShootBowEvent(EntityShootBowEvent e) {
        gameEvent.onEntityShootBowEvent(e);
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        gameEvent.onEntityExplodeEvent(e);
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        gameEvent.onPlayerMoveEvent(e);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        gameEvent.onPlayerQuitEvent(e);
    }

}
