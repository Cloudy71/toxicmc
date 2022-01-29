/*
  User: Cloudy
  Date: 21/01/2022
  Time: 21:29
*/

package cz.cloudy.minecraft.toxicmc.components.events.events;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.math.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.slf4j.Logger;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Cloudy
 */
// TODO: Enderman teleporting after hit
// TODO: Spider: Spawning web after hit
public class GameEventHardcoreMonsters
        extends GameEvent {
    private static final Logger logger = LoggerFactory.getLogger(GameEventHardcoreMonsters.class);

    private Map<UUID, Byte> skeletonMap = new HashMap<>();
    private List<Integer>   tntList     = new ArrayList<>();

    @Override
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public String getName() {
        return "Hardcore Monsters";
    }

    @Override
    public String getDescription() {
        return "- Hráči mají polovinu damage do monster\n" +
               "- Po zabítí monstra se spawne minizombie\n" +
               "- Skeletoni pushuji po zásahu šípem\n" +
               "- Creepeři spawnují TNT po smrti hráčem\n" +
               "- Spawnuté TNT vybuchují po sekundě (no block damage)\n" +
               "- Skeletoni spawnují šípy po smrti hráčem";
    }

    @Override
    public ZonedDateTime getCreatedDate() {
        return ZonedDateTime.of(2021, 1, 21, 18, 0, 0, 0, ZoneOffset.ofHours(1));
    }

    @Override
    public String getAuthor() {
        return "Cloudy";
    }

    @Override
    public void onEntityDeathEvent(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Monster) ||
            e.getEntity().getLastDamageCause() == null ||
            !(e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageByEntityEvent) ||
            !(damageByEntityEvent.getDamager() instanceof Player p))
            return;

        if (!(e.getEntity() instanceof ZombieVillager)) {
            ZombieVillager zombie = (ZombieVillager) p.getWorld().spawnEntity(e.getEntity().getLocation(), EntityType.ZOMBIE_VILLAGER);
            zombie.setBaby();
        }

        if (e.getEntity() instanceof Creeper c) {
            Vector[] moves = new Vector[] {
                    new Vector(2, 0, 0),
                    new Vector(-2, 0, 0),
                    new Vector(0, 0, 2),
                    new Vector(0, 0, -2)
            };
            for (Vector move : moves) {
                TNTPrimed tnt = c.getWorld().spawn(
                        c.getLocation().add(move),
                        TNTPrimed.class
                );
                tnt.setFuseTicks(20);
                tnt.setYield(4);
                tnt.setIsIncendiary(false);
                tntList.add(tnt.getEntityId());
            }
        } else if (e.getEntity() instanceof Skeleton s) {
            skeletonMap.remove(s.getUniqueId());
            for (int i = 0; i < 16; ++i) {
                final int fi = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(
                        getPlugin(),
                        () -> {
                            int start = (int) Math.floor(fi / 8d);
                            double target = start * 8d + Math.random() * 8d;
                            Vector targetPosition = s.getLocation().toVector().add(new Vector(
                                    fi % 8 == 0 || fi % 8 == 4 || fi % 8 == 6 ? target : (fi % 8 == 2 || fi % 8 == 5 || fi % 8 == 7 ? -target : 0),
                                    s.getLocation().toVector().getY(),
                                    fi % 8 == 1 || fi % 8 == 4 || fi % 8 == 7 ? target : (fi % 8 == 3 || fi % 8 == 5 || fi % 8 == 6 ? -target : 0)
                            ));
                            Location spawnLocation = s.getLocation().clone().add(new Vector(0, 10, 0));
                            Arrow arrow = s.getWorld().spawn(spawnLocation, Arrow.class);
                            arrow.setVelocity(spawnLocation.toVector().subtract(targetPosition).normalize().multiply(3));
                            arrow.setDamage(4);
                            arrow.setTicksLived(40);
                        },
                        1 + i
                );
            }
        }
    }

    @Override
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Monster &&
            e.getDamager() instanceof Player) {
            e.setDamage(e.getFinalDamage() / 2d);
//            e.setDamage(e.getFinalDamage() * 8d);
        } else if (e.getEntity() instanceof Player p &&
                   e.getDamager() instanceof Arrow arrow &&
                   arrow.getShooter() instanceof Skeleton shooter &&
                   arrow.hasMetadata("arrowType") &&
                   arrow.getMetadata("arrowType").stream().anyMatch(metadataValue -> metadataValue.asByte() == 0)) {
            Vector vec = p.getLocation().toVector().subtract(shooter.getLocation().toVector()).normalize().multiply(6).add(new Vector(0, 1d, 0));
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    getPlugin(),
                    () -> p.setVelocity(vec),
                    1
            );
        }
    }

    @Override
    public void onEntityShootBowEvent(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Skeleton s) || !(e.getProjectile() instanceof Arrow arrow))
            return;

        if (arrow.hasMetadata("arrowType"))
            return;

        if (!skeletonMap.containsKey(e.getEntity().getUniqueId()))
            skeletonMap.put(e.getEntity().getUniqueId(), (byte) 0);
        byte type = skeletonMap.get(e.getEntity().getUniqueId());

        arrow.setMetadata("arrowType", new FixedMetadataValue(getPlugin(), type));
        if (type == 1) {
            for (int i = 0; i < 6; ++i) {
                final int ii = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(
                        getPlugin(),
                        () -> {
                            Arrow newArrow = arrow.getWorld().spawn(
                                    arrow.getLocation().clone(),
                                    Arrow.class
                            );

                            newArrow.setVelocity(
                                    arrow.getVelocity().clone()
                                         .add(VectorUtils.getRightDirection(arrow.getLocation().getDirection()).multiply(0.5 * (ii + 1))));
                            newArrow.setDamage(arrow.getDamage());
                            newArrow.setShooter(s);
                            newArrow.setBounce(arrow.doesBounce());
                            newArrow.setCritical(arrow.isCritical());
                            newArrow.setMetadata("arrowType", new FixedMetadataValue(getPlugin(), type));
                        },
                        i * 2 + 1
                );
            }
        }

        skeletonMap.replace(e.getEntity().getUniqueId(), (byte) (type == 0 ? 1 : 0));
    }

    @Override
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        if (!tntList.contains(e.getEntity().getEntityId()))
            return;

        tntList.remove(Integer.valueOf(e.getEntity().getEntityId()));
        e.blockList().clear();
    }
}
