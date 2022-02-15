package cz.cloudy.minecraft.core.database;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.CollectionUtils;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.annotation.Join;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.types.FieldScan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

@Aspect
public class DatabaseAspect {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAspect.class);

    protected static final Map<Class<? extends DatabaseEntity>, List<Method>> cachingMethods = new HashMap<>();

    protected static void clearJoinsFor(Class<? extends DatabaseEntity> type, @Nullable DatabaseEntity singleEntity) {
        if (!cachingMethods.containsKey(type))
            return;
        logger.info("CLR_CACHE: {}, {}", type, singleEntity);
        DatabaseCache cache = ComponentLoader.get(DatabaseCache.class);
        for (Method method : cachingMethods.get(type)) {
            if (singleEntity != null) {
                cache.clearJoinCache(method, singleEntity);
                break;
            }
            cache.clearJoinCacheMethodAll(method);
        }
    }

    @Around("@annotation(cz.cloudy.minecraft.core.database.annotation.Join)")
    public Object aroundJoin(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!(joinPoint.getTarget() instanceof DatabaseEntity databaseEntity))
            throw new RuntimeException("Join annotation can be used only inside DatabaseEntity class.");
        if (databaseEntity.fetchLevel.isLowerThan(FetchLevel.Primitive))
            databaseEntity.load(FetchLevel.Primitive);

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Object thisObject = joinPoint.getTarget();
        Join join = methodSignature.getMethod().getAnnotation(Join.class);
//        logger.info("JOIN_CACHE: {}, {}", thisObject, join.table().getSimpleName());

        Object value = joinPoint.proceed();
        if (value != null)
            return value;

        Database database = ComponentLoader.get(Database.class);
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        DatabaseCache cache = ComponentLoader.get(DatabaseCache.class);
        Object cacheObject;
        if ((cacheObject = cache.getJoinCache(databaseEntity, methodSignature.getMethod())) != null)
            return cacheObject;

        List<Method> methodList = cachingMethods.computeIfAbsent(join.table(), clazz -> new ArrayList<>());
        if (!methodList.contains(methodSignature.getMethod()))
            methodList.add(methodSignature.getMethod());

        Map<String, Object> parameterMap = new HashMap<>();
        for (FieldScan fieldScan : entityMapper.getFieldScansForEntityClass(databaseEntity.getClass())) {
            if (fieldScan.foreignKey() != null) // TODO: Implement foreign key usage
                continue;

            String paramName = fieldScan.column().value();
            Object paramValue = entityMapper.getFieldScanDatabaseValue(databaseEntity, fieldScan);
            parameterMap.put(paramName, paramValue);
        }

        if (Collection.class.isAssignableFrom(methodSignature.getReturnType())) {
            if (!CollectionUtils.isCollectionTypeSupported(methodSignature.getReturnType()))
                throw new RuntimeException("Join annotation can be used only on Set or List return type.");

            Set<DatabaseEntity> entities = new HashSet<>(database.findEntities(
                    join.table(),
                    join.where(),
                    parameterMap,
                    FetchLevel.Primitive
            ));
            cacheObject = CollectionUtils.getCollection(methodSignature.getReturnType(), entities);
        } else {
            cacheObject = database.findEntity(
                    join.table(),
                    join.where(),
                    parameterMap,
                    FetchLevel.Primitive
            );
        }
//        logger.info("JOIN_FETCH: {}, {}", methodSignature.getReturnType().getSimpleName(), cacheObject);

        cache.addJoinCache(databaseEntity, methodSignature.getMethod(), cacheObject);
        return cacheObject;
    }
}
