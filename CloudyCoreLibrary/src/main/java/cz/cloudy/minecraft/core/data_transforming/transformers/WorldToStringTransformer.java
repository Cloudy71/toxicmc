/*
  User: Cloudy
  Date: 28/01/2022
  Time: 19:16
*/

package cz.cloudy.minecraft.core.data_transforming.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * @author Cloudy
 */
public class WorldToStringTransformer implements IDataTransformer<World, String> {
    @Override
    public String transform0to1(World value) {
        return value.getName();
    }

    @Override
    public World transform1to0(String value) {
        return Bukkit.getWorld(value);
    }
}
