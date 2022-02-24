package cz.cloudy.minecraft.toxicmc.components.economics.quality_calculators;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.WorldOnly;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.toxicmc.components.economics.QualityComponent;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Cloudy, Háně
 */
@Component
@WorldOnly(filter = "survival")
public class OreQualityCalculator extends AbstractQualityCalculator.AbstractBlockQualityCalculator {
    @Override
    public void blockDropItemEvent(BlockDropItemEvent e) {
        byte quality;
        String toolName;
        ItemStack handItem = e.getPlayer().getInventory().getItemInMainHand();
        toolName = handItem.getType().name();

        if (toolName.startsWith("NETHERITE"))
            quality = 100;

        else if (toolName.startsWith("DIAMOND"))
            quality = 90;

        else if (toolName.startsWith("IRON"))
            quality = 80;

        else if (toolName.startsWith("GOLDEN"))
            quality = 70;

        else if (toolName.startsWith("STONE"))
            quality = 60;

        else if (toolName.startsWith("WOODEN"))
            quality = 50;

        else
            quality = 40;

        for (Item item : e.getItems()) {
            QualityComponent.setQuality(item.getItemStack(), quality);
        }

    }
}
