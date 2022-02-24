package cz.cloudy.minecraft.toxicmc.components.economics.quality_calculators;

import com.cronutils.utils.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.Benchmarked;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cron;
import cz.cloudy.minecraft.core.componentsystem.annotations.WorldOnly;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.game.EntityUtils;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.QualityComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cloudy, Háně
 */
@Component
@WorldOnly(filter = "survival")
public class AnimalQualityCalculator extends AbstractQualityCalculator.AbstractEntityQualityCalculator implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(AnimalQualityCalculator.class);

    private final EntityUtils entityUtils = ComponentLoader.get(EntityUtils.class);

    private final Map<Integer, Byte> qualityMap = new HashMap<>();
    private final Set<Integer> entitiesBlackList = new HashSet<>();

    @Override
    public void entityDropItemEvent(EntityDropItemEvent e) {
        logger.info("a");
//        if(!qualityMap.containsKey(e.getEntity().getEntityId())){
//            QualityComponent.setQuality(e.getItemDrop().getItemStack(), (byte) 100);
//            return;
//        }
//        byte quality = qualityMap.get(e.getEntity().getEntityId());
//        QualityComponent.setQuality(e.getItemDrop().getItemStack(), quality);
    }

    @Benchmarked
    @Override
    public void entityDeathEvent(EntityDeathEvent e) {
        for (ItemStack drop : e.getDrops()) {
            if (!qualityMap.containsKey(e.getEntity().getEntityId())) {
                QualityComponent.setQuality(drop, (byte) 100);
                continue;
            }
            byte quality = qualityMap.get(e.getEntity().getEntityId());
            QualityComponent.setQuality(drop, quality);
        }
        qualityMap.remove(e.getEntity().getEntityId());
    }

    @Benchmarked
    @Cron("* * * * * *")
    private void entitiesCheck() {
        for (String survivalWorld : ToxicConstants.SURVIVAL_WORLDS) {
            World world = Bukkit.getWorld(survivalWorld);
            Preconditions.checkNotNull(world);

            Collection<Animals> animals = world.getEntitiesByClass(Animals.class);
            boolean isDay = world.isDayTime();
            for (Animals animal : animals)
                checkAnimal(animal, isDay);
            entitiesBlackList.clear();
        }
    }

    private void checkAnimal(Animals animal, boolean isDay) {
        byte quality = getQuality(animal, isDay);

        if (quality == (byte) 100)
            return;

        if (qualityMap.get(animal.getEntityId()) == null) {
            qualityMap.put(animal.getEntityId(), quality);
            return;
        }
        byte qualityOld = qualityMap.get(animal.getEntityId());
        if (qualityOld <= quality)
            return;

        qualityMap.put(animal.getEntityId(), quality);
    }

    private byte getQuality(Animals animal, boolean isDay) {
        byte quality = 100;

        if (entitiesBlackList.contains(animal.getEntityId()))
            quality -= 20;
        else {
            Set<Entity> nearbyEntities = entityUtils.getNearbyEntities(animal, 2);
            if (nearbyEntities.size() > 6) {
                quality -= 20;
                entitiesBlackList.addAll(nearbyEntities.stream().map(Entity::getEntityId).collect(Collectors.toSet()));
            }
        }

        if (isDay && !animal.isInDaylight()) {
            quality -= 10;
        }

        return quality;
    }
}
