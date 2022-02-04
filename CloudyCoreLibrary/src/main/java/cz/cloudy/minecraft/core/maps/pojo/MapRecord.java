/*
  User: Cloudy
  Date: 01/02/2022
  Time: 15:49
*/

package cz.cloudy.minecraft.core.maps.pojo;

import cz.cloudy.minecraft.core.componentsystem.annotations.CheckConfiguration;
import cz.cloudy.minecraft.core.data_transforming.transformers.WorldToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import org.bukkit.World;

/**
 * @author Cloudy
 */
@Table("__map_record")
@CheckConfiguration("maps.mapController=true")
public class MapRecord
        extends DatabaseEntity {

    @Column("world")
    @Transform(WorldToStringTransformer.class)
//    @Index
//    @MultiIndex(0)
    @Size(32)
    protected World world;

    @Column("map_id")
//    @Index
    protected int mapId;

    @Column("usage_number")
    @Default("0")
//    @Index
//    @MultiIndex(0)
    protected int usage;

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }
}
