/*
  User: Cloudy
  Date: 31/01/2022
  Time: 21:14
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.data_transforming.transformers.WorldToStringTransformer;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.types.Int2;
import org.bukkit.World;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Cloudy
 */
@Table("banner")
public class Banner
        extends DatabaseEntity {

    @Column("owner")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    @Null
    protected UUID owner;

    @Column("image_path")
    @Size(64)
    protected String imagePath;

    @Column("world")
    @Size(32)
    @Transform(WorldToStringTransformer.class)
    protected World world;

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    // ================================================

    private static LoadingCache<Long, Set<BannerPart>> bannerPartCache =
            CacheBuilder.newBuilder()
                        .build(
                                new CacheLoader<Long, Set<BannerPart>>() {
                                    @Override
                                    public Set<BannerPart> load(Long key) throws Exception {
                                        return ComponentLoader.get(Database.class).findEntities(
                                                                      BannerPart.class,
                                                                      "banner.id = :id AND is_used = 1",
                                                                      ImmutableMap.of("id", key),
                                                                      FetchLevel.Primitive
                                                              ).stream()
                                                              .sorted(Comparator.comparingLong(BannerPart::getId))
                                                              .collect(Collectors.toCollection(LinkedHashSet::new));
                                    }
                                }
                        );

    private Int2 placeSize;

    public Set<BannerPart> getBannerParts(boolean refresh) {
        if (refresh)
            bannerPartCache.refresh(getId());
        return bannerPartCache.getUnchecked(getId());
    }

    public Set<BannerPart> getBannerParts() {
        return getBannerParts(false);
    }

    public Int2 getPlaceSize() {
        return placeSize;
    }

    public void setPlaceSize(Int2 placeSize) {
        this.placeSize = placeSize;
    }
}
