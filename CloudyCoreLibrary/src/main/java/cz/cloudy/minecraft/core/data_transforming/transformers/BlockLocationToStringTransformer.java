/*
  User: Cloudy
  Date: 28/01/2022
  Time: 19:34
*/

package cz.cloudy.minecraft.core.data_transforming.transformers;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.data_transforming.DataTransformer;
import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author Cloudy
 */
public class BlockLocationToStringTransformer
        implements IDataTransformer<Block, String> {
    @Override
    public String transform0to1(Block value) {
        return ComponentLoader.get(DataTransformer.class)
                              .getDataTransformer(LocationToStringTransformer.class)
                              .transform0to1(value.getLocation());
    }

    @Override
    public Block transform1to0(String value) {
        Location location = ComponentLoader.get(DataTransformer.class)
                                           .getDataTransformer(LocationToStringTransformer.class)
                                           .transform1to0(value);
        return location.getWorld().getBlockAt(location);
    }
}
