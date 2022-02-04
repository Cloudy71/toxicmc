/*
  User: Cloudy
  Date: 01/02/2022
  Time: 00:00
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import org.bukkit.World;

/**
 * @author Cloudy
 */
@Table("banner_part")
public class BannerPart
        extends DatabaseEntity {

    @Column("banner")
    @ForeignKey
    protected Banner banner;

    @Column("map_id")
    protected int mapId;

    @Column("is_used")
    @Default("1")
    protected boolean used;

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
