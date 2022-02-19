package cz.cloudy.minecraft.toxicmc.components.events.events;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.Benchmarked;
import cz.cloudy.minecraft.core.game.EntityUtils;
import cz.cloudy.minecraft.core.game.MetaUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cloudy, Háně
 */
public class GameEventCovid extends GameEvent {
    private static final Logger logger = LoggerFactory.getLogger(GameEventCovid.class);
    private static final String TASK_NAME = "COVID_TASK";

    private final EntityUtils entityUtils = ComponentLoader.get(EntityUtils.class);
    private final MetaUtils metaUtils = ComponentLoader.get(MetaUtils.class);
    private final List<UUID> infectedMap = new ArrayList<>();
    private final List<UUID> safeMap = new ArrayList<>();

    @Override
    public ChatColor getColor() {
        return ChatColor.GREEN;
    }

    @Override
    public String getName() {
        return "Covid-19";
    }

    @Override
    public String getDescription() {
        return "- Pokud se hráč na 2 bloky přiblíží k Zombie nebo již nakaženému hráči, infikuje ho Covid.\n" +
                "- Po nějaké době co se hráč nakazí Covidem, zapůsobí na něj jed.\n" +
                "- Hráč se může proti Covidu bránit koženou helmou.";
    }

    @Override
    public ZonedDateTime getCreatedDate() {
        return ZonedDateTime.of(2022, 2, 19, 13, 28, 0, 0, ZoneOffset.ofHours(1));
    }

    @Override
    public String getAuthor() {
        return "Cloudy, Háně";
    }

    private void clearPlayer(Player player) {
        infectedMap.remove(player.getUniqueId());
        safeMap.remove(player.getUniqueId());
        Integer taskId = metaUtils.getMetadata(player, TASK_NAME);
        if (taskId != null)
            Bukkit.getScheduler().cancelTask(taskId);
        player.removeMetadata(TASK_NAME, getPlugin());
    }

    @Override
    public void onEnd() {
        for (UUID uuid : infectedMap) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;
            clearPlayer(player);
        }

        for (UUID uuid : safeMap) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;
            clearPlayer(player);
        }
    }

    @Override
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        ItemStack helmet;
        if ((helmet = e.getPlayer().getInventory().getHelmet()) != null && helmet.getType() == Material.LEATHER_HELMET)
            return;

        ItemStack[] checkHelmet = new ItemStack[] {null};
        Set<Entity> nearbyEntities = entityUtils.getNearbyEntities(e.getPlayer(), 2f)
                .stream()
                .filter(entity -> (entity instanceof Player player && !safeMap.contains(player.getUniqueId()) &&
                        ((checkHelmet[0] = player.getInventory().getHelmet()) == null || checkHelmet[0].getType() != Material.LEATHER_HELMET)) ||
                        entity instanceof Zombie)
                .collect(Collectors.toSet());

        if (nearbyEntities.isEmpty() || nearbyEntities.size() == 1 ||
                nearbyEntities.stream().noneMatch(
                        entity -> entity instanceof Zombie ||
                                (entity instanceof Player player && infectedMap.contains(player.getUniqueId()))
                ))
            return;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Zombie)
                continue;

            Player player = (Player) entity;
            if (infectedMap.contains(player.getUniqueId()))
                continue;

            infectedMap.add(player.getUniqueId());
            int infectionTaskId = Bukkit.getScheduler().runTaskLater(
                    getPlugin(),
                    () -> {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 8 * 20, 2));
                        infectedMap.remove(player.getUniqueId());
                        safeMap.add(player.getUniqueId());
                        int safeTaskId = Bukkit.getScheduler().runTaskLater(
                                getPlugin(),
                                () -> {
                                    safeMap.remove(player.getUniqueId());
                                    player.removeMetadata(TASK_NAME, getPlugin());
                                },
                                5 * 60 * 20
                        ).getTaskId();
                        player.setMetadata(TASK_NAME, new FixedMetadataValue(getPlugin(), safeTaskId));
                    },
                    5 * 60 * 20
            ).getTaskId();
            player.setMetadata(TASK_NAME, new FixedMetadataValue(getPlugin(), infectionTaskId));
        }
    }

    @Override
    public void onEntityDeathEvent(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player player))
            return;

        clearPlayer(player);
    }

    @Override
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        clearPlayer(e.getPlayer());
    }
}
