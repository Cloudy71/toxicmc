/*
  User: Cloudy
  Date: 30/01/2022
  Time: 14:14
*/

package cz.cloudy.minecraft.core.game;

import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;

/**
 * @author Cloudy
 */
@Component
public class ChestUtils {

    public Block getBaseBlock(Block refBlock) {
        if (refBlock == null || !(refBlock.getState() instanceof Chest chest))
            return null;

        if (chest.getInventory().getHolder() instanceof DoubleChest doubleChest) {
            return doubleChest.getLeftSide().getInventory().getLocation().getBlock();
        }
        return chest.getBlock();
    }
}
