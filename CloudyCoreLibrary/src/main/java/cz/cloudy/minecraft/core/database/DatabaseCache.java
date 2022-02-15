/*
  User: Cloudy
  Date: 02/02/2022
  Time: 22:06
*/

package cz.cloudy.minecraft.core.database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Cloudy
 */
@Component
public class DatabaseCache {
    private static final Map<Class<? extends DatabaseEntity>, Map<Object, DatabaseEntity>> primaryCache =
            new HashMap<>();
    private static final Map<Class<? extends DatabaseEntity>, Set<DatabaseEntity>> allCache =
            new HashMap<>();
    //    private static final Map<Class<? extends DatabaseEntity>, Map<Object, Map<Method, Object>>> joinCache =
//            new HashMap<>();  // TODO: Implement join cache including cache clear if value is Collection
    private static final Map<Method, Map<Object, Object>> joinCache = new HashMap<>(); // Method -> PrimaryValue -> Object

    @Component
    private DatabaseEntityMapper entityMapper;

    protected void addCacheForEntityType(Class<? extends DatabaseEntity> type) {
        primaryCache.put(type, new HashMap<>());
//        primaryCache.put(type, CacheBuilder.newBuilder().build());
    }

    protected void addEntity(@NotNull DatabaseEntity entity, Object primaryValue) {
        if (primaryValue == null)
            return;
        Map<Object, DatabaseEntity> map = primaryCache.get(entity.getClass());
        if (map == null)
            return;
        map.put(primaryValue, entity);
    }

    @Nullable
    protected <T> T getEntity(@NotNull Class<T> type, Object primaryValue) {
        if (primaryValue == null)
            return null;
        Map<Object, DatabaseEntity> map = primaryCache.get(type);
        if (map == null)
            return null;
        return (T) map.get(primaryValue);
    }

    protected void removeEntity(@NotNull DatabaseEntity entity, Object primaryValue) {
        if (primaryValue == null)
            return;
        Map<Object, DatabaseEntity> map = primaryCache.get(entity.getClass());
        if (map == null)
            return;
        map.remove(primaryValue);
    }

    protected void addAllCacheEntities(Class<? extends DatabaseEntity> type, @NotNull Set<DatabaseEntity> entities) {
        allCache.put(type, entities);
    }

    @Nullable
    protected <T> Set<T> getAllCacheEntities(@NotNull Class<T> type) {
        return (Set<T>) allCache.get(type);
    }

    protected void clearAllCacheEntities(Class<? extends DatabaseEntity> type) {
        allCache.remove(type);
    }

    private Object getPrimaryValue(@NotNull DatabaseEntity entity) {
        return ReflectionUtils.getValueOpt(entityMapper.getPrimaryKeyFieldScan(entity.getClass()).field(), entity).orElse(null);
    }

    protected void addJoinCache(@NotNull DatabaseEntity entity, Method method, Object value) {
        Object primaryValue = getPrimaryValue(entity);
        if (primaryValue == null)
            return;
        joinCache.computeIfAbsent(method, m -> new HashMap<>())
                .computeIfAbsent(primaryValue, o -> value);
//        joinCache.computeIfAbsent(entity.getClass(), c -> new HashMap<>())
//                .computeIfAbsent(primaryValue, o -> new HashMap<>())
//                .computeIfAbsent(method, m -> value);
    }

    protected Object getJoinCache(@NotNull DatabaseEntity entity, Method method) {
        Map<Object, Object> primaryValueMap = joinCache.get(method);
        if (primaryValueMap == null)
            return null;
        Object primaryValue = getPrimaryValue(entity);
        if (primaryValue == null)
            return null;
        return primaryValueMap.get(primaryValue);
//        Map<Object, Map<Method, Object>> primaryValueMap = joinCache.get(entity.getClass());
//        if (primaryValueMap == null)
//            return null;
//        Object primaryValue = getPrimaryValue(entity);
//        if (primaryValue == null)
//            return null;
//        Map<Method, Object> methodMap = primaryValueMap.get(primaryValue);
//        if (methodMap == null)
//            return null;
//        return methodMap.get(method);
    }

    protected void clearJoinCache(Method method, @NotNull DatabaseEntity comparingEntity) {
        Map<Object, Object> primaryValueMap = joinCache.get(method);
        if (primaryValueMap == null)
            return;
        List<Object> keysToRemove = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : primaryValueMap.entrySet()) {
            if (!entry.getValue().equals(comparingEntity))
                continue;
            keysToRemove.add(entry.getKey());
        }
        for (Object key : keysToRemove) {
            primaryValueMap.remove(key);
        }
//        Object primaryValue = getPrimaryValue(entity);
//        if (primaryValue == null)
//            return;
//        primaryValueMap.remove(primaryValue);
//        Map<Object, Map<Method, Object>> primaryValueMap = joinCache.get(entity.getClass());
//        if (primaryValueMap == null)
//            return;
//        Object primaryValue = getPrimaryValue(entity);
//        if (primaryValue == null)
//            return;
//        Map<Method, Object> methodMap = primaryValueMap.get(primaryValue);
//        if (methodMap == null)
//            return;
//        methodMap.remove(method);
    }

    protected void clearJoinCacheMethodAll(Method method) {
        joinCache.remove(method);
//        Map<Object, Map<Method, Object>> primaryValueMap = joinCache.get(type);
//        if (primaryValueMap == null)
//            return;
//
//        for (Map.Entry<Object, Map<Method, Object>> entry : primaryValueMap.entrySet()) {
//            entry.getValue().remove(method);
//        }
    }
}
