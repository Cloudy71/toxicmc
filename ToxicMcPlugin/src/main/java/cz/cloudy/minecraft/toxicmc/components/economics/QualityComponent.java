package cz.cloudy.minecraft.toxicmc.components.economics;

import com.cronutils.utils.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.WorldOnly;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.game.TextUtils;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.quality_calculators.AbstractQualityCalculator;
import cz.cloudy.minecraft.toxicmc.components.economics.quality_calculators.AnimalQualityCalculator;
import cz.cloudy.minecraft.toxicmc.components.economics.quality_calculators.OreQualityCalculator;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;

import java.util.*;

/**
 * @author Háně
 */
@Component
@WorldOnly(filter = ToxicConstants.WORLDS_ECONOMY)
public class QualityComponent implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(QualityComponent.class);

    private final Map<Keyed, Class<? extends AbstractQualityCalculator>> qualityCalculators = new HashMap<>();

    @Override
    public void onStart() {
        for (Material value : Material.values()) {
            if (value.name().endsWith("ORE"))
                qualityCalculators.put(value, OreQualityCalculator.class);
        }
        for (EntityType value : EntityType.values()) {
            if (value.getEntityClass() == null)
                continue;
            if (Animals.class.isAssignableFrom(value.getEntityClass()))
                qualityCalculators.put(value, AnimalQualityCalculator.class);
        }
    }

    public static byte getQuality(ItemStack item) {
        List<net.kyori.adventure.text.Component> lore;
        if ((lore = item.lore()) == null)
            return 100;

        byte quality = 100;
        for (net.kyori.adventure.text.Component component : lore) {
            String text = TextUtils.get(component);
            if (!text.startsWith(ToxicConstants.ITEM_LORE_QUALITY))
                continue;

            quality = Byte.parseByte(text.substring(ToxicConstants.ITEM_LORE_QUALITY.length() + ChatColor.GREEN.toString().length(), text.length() - 1));
        }

        return quality;
    }

    public static void setQuality(ItemStack item, byte quality) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(item.getItemMeta());

        List<net.kyori.adventure.text.Component> lore = item.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        int len = lore.size();
        for (int i = 0; i < len; ++i) {
            String text = ((TextComponent) lore.get(i).compact()).content();
            if (!text.startsWith(ToxicConstants.ITEM_LORE_QUALITY))
                continue;

            lore.remove(i);
            lore.add(i, TextUtils.get(ToxicConstants.ITEM_LORE_QUALITY + getPercentageColor(quality) + quality + "%"));
            item.lore(lore);
            return;
        }
        lore.add(TextUtils.get(ToxicConstants.ITEM_LORE_QUALITY + getPercentageColor(quality) + quality + "%"));
        item.lore(lore);
    }

    @EventHandler(ignoreCancelled = true)
    private void onBlockDropItemEvent(BlockDropItemEvent e) {
        Material material = e.getBlockState().getType();
        Class<? extends AbstractQualityCalculator> qualityCalculator = qualityCalculators.get(material);
        if (qualityCalculator == null)
            return;

        ComponentLoader.get(qualityCalculator).blockDropItemEvent(e);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityDropItemEvent(EntityDropItemEvent e) {
        EntityType entityType = e.getEntity().getType();
        Class<? extends AbstractQualityCalculator> qualityCalculator = qualityCalculators.get(entityType);
        if (qualityCalculator == null)
            return;

        ComponentLoader.get(qualityCalculator).entityDropItemEvent(e);
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityDeathEvent(EntityDeathEvent e) {
        EntityType entityType = e.getEntity().getType();
        Class<? extends AbstractQualityCalculator> qualityCalculator = qualityCalculators.get(entityType);
        if (qualityCalculator == null)
            return;

        ComponentLoader.get(qualityCalculator).entityDeathEvent(e);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPrepareItemCraftEvent(PrepareItemCraftEvent e) {
        if (e.getInventory().getResult() == null)
            return;

        ItemStack[] matrix = e.getInventory().getMatrix();
        byte lowestQuality = 100;
        for (ItemStack itemStack : matrix) {
            if (itemStack == null)
                continue;
            byte quality = getQuality(itemStack);
            if (quality >= lowestQuality)
                continue;
            lowestQuality = quality;
        }
        setQuality(e.getInventory().getResult(), lowestQuality);
    }

    private static ChatColor getPercentageColor(byte percentage) {
        if (percentage >= 90)
            return ChatColor.GREEN;

        if (percentage > 70)
            return ChatColor.DARK_GREEN;

        if (percentage > 40)
            return ChatColor.YELLOW;

        return ChatColor.RED;
    }

}
