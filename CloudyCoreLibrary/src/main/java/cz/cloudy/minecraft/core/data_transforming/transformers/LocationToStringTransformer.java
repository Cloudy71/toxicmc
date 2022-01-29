/*
  User: Cloudy
  Date: 28/01/2022
  Time: 19:17
*/

package cz.cloudy.minecraft.core.data_transforming.transformers;

import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.text.DecimalFormat;

/**
 * @author Cloudy
 */
public class LocationToStringTransformer
        implements IDataTransformer<Location, String> {
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.##");

    @Override
    public String transform0to1(Location value) {
        double x = value.getX();
        double y = value.getY();
        double z = value.getZ();
        float pitch = value.getPitch();
        float yaw = value.getYaw();
        return value.getWorld().getName() + "," + decimalFormat.format(x) + "," + decimalFormat.format(y) + "," + decimalFormat.format(z) +
               (pitch != 0f || yaw != 0f ? ("," + decimalFormat.format(pitch) + (yaw != 0f ? "," + decimalFormat.format(yaw) : "")) : "");
    }

    @Override
    public Location transform1to0(String value) {
        String[] data = value.split(",");
        if (data.length == 4)
            return new Location(
                    Bukkit.getWorld(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Double.parseDouble(data[3])
            );
        else
            return new Location(
                    Bukkit.getWorld(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Double.parseDouble(data[3]),
                    Float.parseFloat(data[4]),
                    data.length == 6 ? Float.parseFloat(data[5]) : 0f
            );
    }
}
