/*
  User: Cloudy
  Date: 02/02/2022
  Time: 22:06
*/

package cz.cloudy.minecraft.core.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author Cloudy
 */
@Component
public class DatabaseCache {
    private static final Cache<Class<? extends DatabaseEntity>, Cache<Object, DatabaseEntity>> primaryCache =
            CacheBuilder.newBuilder()
                        .build();
    private static final Cache<Class<? extends DatabaseEntity>, Set<DatabaseEntity>>           allCache     =
            CacheBuilder.newBuilder()
                        .build();

    protected void addCacheForEntityType(Class<? extends DatabaseEntity> type) {
        primaryCache.put(type, CacheBuilder.newBuilder().build());
    }

    protected void addEntity(@NotNull DatabaseEntity entity, Object primaryValue) {
        if (primaryValue == null)
            return;
        Cache<Object, DatabaseEntity> map = primaryCache.getIfPresent(entity.getClass());
        if (map == null)
            return;
        map.put(primaryValue, entity);
    }

    @Nullable
    protected <T> T getEntity(@NotNull Class<T> type, Object primaryValue) {
        if (primaryValue == null)
            return null;
        Cache<Object, DatabaseEntity> map = primaryCache.getIfPresent(type);
        if (map == null)
            return null;
        return (T) map.getIfPresent(primaryValue);
    }

    protected void removeEntity(@NotNull DatabaseEntity entity, Object primaryValue) {
        if (primaryValue == null)
            return;
        Cache<Object, DatabaseEntity> map = primaryCache.getIfPresent(entity.getClass());
        if (map == null)
            return;
        map.invalidate(primaryValue);
    }

    protected void addAllCacheEntities(Class<? extends DatabaseEntity> type, @NotNull Set<DatabaseEntity> entities) {
        allCache.put(type, entities);
    }

    @Nullable
    protected <T> Set<T> getAllCacheEntities(@NotNull Class<T> type) {
        return (Set<T>) allCache.getIfPresent(type);
    }

    protected void clearAllCacheEntities(Class<? extends DatabaseEntity> type) {
        allCache.invalidate(type);
    }
}
