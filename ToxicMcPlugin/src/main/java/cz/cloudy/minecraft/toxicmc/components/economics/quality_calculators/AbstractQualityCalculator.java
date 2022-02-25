package cz.cloudy.minecraft.toxicmc.components.economics.quality_calculators;

import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;

/**
 * @author Cloudy
 */
public abstract class AbstractQualityCalculator {
    public abstract void blockDropItemEvent(BlockDropItemEvent e);

    public abstract void entityDropItemEvent(EntityDropItemEvent e);

    public abstract void entityDeathEvent(EntityDeathEvent e);

    public static abstract class AbstractBlockQualityCalculator extends AbstractQualityCalculator {
        @Override
        public void entityDropItemEvent(EntityDropItemEvent e) {

        }

        @Override
        public void entityDeathEvent(EntityDeathEvent e) {

        }
    }

    public static abstract class AbstractEntityQualityCalculator extends AbstractQualityCalculator {
        @Override
        public void blockDropItemEvent(BlockDropItemEvent e) {

        }
    }
}
