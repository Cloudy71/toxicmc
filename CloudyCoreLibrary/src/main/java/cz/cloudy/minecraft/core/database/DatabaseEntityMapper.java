/*
  User: Cloudy
  Date: 08/01/2022
  Time: 03:56
*/

package cz.cloudy.minecraft.core.database;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.data_transforming.DataTransformer;
import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.types.ClassScan;
import cz.cloudy.minecraft.core.database.types.FieldScan;
import cz.cloudy.minecraft.core.types.Pair;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Cloudy
 */
@Component
public class DatabaseEntityMapper
        implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEntityMapper.class);

    protected static final Map<Class<? extends DatabaseEntity>, ClassScan>       mappedClasses = new HashMap<>();
    protected static final Map<Class<? extends DatabaseEntity>, List<FieldScan>> mappedFields  = new HashMap<>();

    protected static final com.google.common.collect.Table<Class<? extends DatabaseEntity>, Object, DatabaseEntity> entityTable =
            HashBasedTable.create();

    @Component
    private Database database;

    @Component
    private DataTransformer dataTransformer;

    @Override
    public void onClassScan(Class<?>[] classes) {
        for (Class<?> clazz : classes) {
            if (!DatabaseEntity.class.isAssignableFrom(clazz) || clazz == DatabaseEntity.class)
                continue;
            logger.info("Mapping \"{}\" database entity", clazz.getSimpleName());
            mapEntityClass((Class<? extends DatabaseEntity>) clazz);
        }
    }

    public Class<?> getFieldScanDatabaseClass(FieldScan fieldScan) {
        if (fieldScan.transform() != null)
            return dataTransformer.getUnknownDataTransformer(fieldScan.transform().value()).getTypes().getValue();
        return fieldScan.field().getType();
    }

    public Object getFieldScanDatabaseValue(DatabaseEntity entity, FieldScan fieldScan) {
        Object value = ReflectionUtils.getValue(fieldScan.field(), entity).orElse(null);
        if (value == null)
            return null;
        if (fieldScan.transform() != null)
            return ((IDataTransformer) dataTransformer.getUnknownDataTransformer(fieldScan.transform().value())).transform0to1(value);
        return value;
    }

    protected Object getTransformedValue(FieldScan fieldScan, Object value) {
        Class<?> type = fieldScan.field().getType();
        if (fieldScan.transform() != null) {
            IDataTransformer transformer = dataTransformer.getUnknownDataTransformer(fieldScan.transform().value());
            if (value.getClass() == transformer.getTypes().getValue())
                value = transformer.transform1to0(value);
        } else if (value.getClass() == Integer.class) {
            if (type == Byte.class || type == byte.class)
                value = ((Integer) value).byteValue();
            else if (type == Short.class || type == short.class)
                value = ((Integer) value).shortValue();
        } else if (value.getClass() == Timestamp.class) {
            Timestamp timestamp = (Timestamp) value;
            if (type == ZonedDateTime.class)
                value = ZonedDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.ofHours(1));
        } else if (value.getClass() == LocalDateTime.class) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            if (type == ZonedDateTime.class)
                value = ZonedDateTime.of(localDateTime, ZoneOffset.ofHours(1));
        }

        return value;
    }

    protected void mapEntityFields(Class<? extends DatabaseEntity> clazz, List<Class<? extends DatabaseEntity>> childClasses) {
        childClasses.add(clazz);
        // TODO: Fix mapping, with two entities, abstract class does not map second enttiy object with primary key
//        if (!mappedFields.containsKey(clazz)) {
        for (Field field : clazz.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column == null)
                continue;

            PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
            Lazy lazy = field.getAnnotation(Lazy.class);
            Size size = field.getAnnotation(Size.class);
            Null nullable = field.getAnnotation(Null.class);
            AutoIncrement autoIncrement = field.getAnnotation(AutoIncrement.class);
            Protected protected_ = field.getAnnotation(Protected.class);
            Transform transform = field.getAnnotation(Transform.class);
            Default default_ = field.getAnnotation(Default.class);
            Index index = field.getAnnotation(Index.class);
            MultiIndex multiIndex = field.getAnnotation(MultiIndex.class);

            // TODO: This is important since mapper can't handle Lazy inits with primary key... Fix in future
            Preconditions.checkState(lazy == null || primaryKey == null, "Primary key field cannot be lazily fetched");
            Preconditions.checkState(primaryKey == null || foreignKey == null, "Primary key field cannot be foreign object yet");
            Preconditions.checkState(primaryKey == null || (index == null && multiIndex == null), "Primary key fields are automatically indexed");
            field.setAccessible(true);

            for (Class<? extends DatabaseEntity> childClass : childClasses) {
                if ((mappedFields.containsKey(childClass) &&
                     mappedFields.get(childClass).stream().anyMatch(fieldScan -> fieldScan.column().value().equals(column.value()))) ||
                    (primaryKey != null && getPrimaryKeyFieldScan(childClass) != null))
                    continue;
                ClassScan classScan = mappedClasses.getOrDefault(childClass, null);
                FieldScan fieldScan = new FieldScan(
                        classScan,
                        field,
                        column,
                        primaryKey,
                        foreignKey,
                        lazy,
                        size,
                        nullable,
                        autoIncrement,
                        protected_,
                        transform,
                        default_,
                        index,
                        multiIndex
                );
                mappedFields.computeIfAbsent(childClass, aClass -> new ArrayList<>()).add(fieldScan);
            }
        }
//        }
        if (DatabaseEntity.class.isAssignableFrom(clazz.getSuperclass())) {
            mapEntityFields((Class<? extends DatabaseEntity>) clazz.getSuperclass(), childClasses);
        }
    }

    public void mapEntityClass(Class<? extends DatabaseEntity> clazz) {
        if (mappedClasses.containsKey(clazz))
            return;

        mapEntityClassInternal(clazz);
        mapEntityFields(clazz, new ArrayList<>());
    }

    private void mapEntityClassInternal(Class<? extends DatabaseEntity> clazz) {
        if (mappedClasses.containsKey(clazz))
            return;

        Table table = clazz.getAnnotation(Table.class);
        if (table == null)
            return;

        ClassScan classScan = new ClassScan(
                clazz,
                table
        );
        mappedClasses.put(clazz, classScan);

        if (DatabaseEntity.class.isAssignableFrom(clazz.getSuperclass()) && clazz.getSuperclass() != DatabaseEntity.class)
            mapEntityClassInternal((Class<? extends DatabaseEntity>) clazz.getSuperclass());
    }

    public List<FieldScan> getFieldScansForEntityClass(Class<? extends DatabaseEntity> clazz) {
        if (!mappedFields.containsKey(clazz))
            return Collections.emptyList();

        return new ArrayList<>(mappedFields.get(clazz));
    }

    public ClassScan getClassScanForEntityClass(Class<? extends DatabaseEntity> clazz) {
        if (!mappedClasses.containsKey(clazz))
            return null;

        return mappedClasses.get(clazz);
    }

    public Set<Pair<ClassScan, Set<FieldScan>>> getMappedDatabaseEntityClasses() {
        Set<Pair<ClassScan, Set<FieldScan>>> set = new HashSet<>();
        for (ClassScan clazz : mappedClasses.values()) {
            set.add(new Pair<>(clazz, new HashSet<>(mappedFields.get(clazz.clazz()))));
        }
        return set;
    }

    public Set<Pair<ClassScan, Set<FieldScan>>> getMappedUnConstructedDatabaseEntityClasses() {
        Set<Pair<ClassScan, Set<FieldScan>>> set = new HashSet<>();
        for (Pair<ClassScan, Set<FieldScan>> mappedDatabaseEntityClass : getMappedDatabaseEntityClasses()) {
            if (mappedDatabaseEntityClass.getKey().isConstructed())
                continue;

            set.add(mappedDatabaseEntityClass);
        }
        return set;
    }

    public FieldScan getPrimaryKeyFieldScan(Class<? extends DatabaseEntity> clazz) {
        if (!mappedFields.containsKey(clazz))
            return null;
        List<FieldScan> scans = mappedFields.get(clazz);
        return scans.stream()
                    .filter(fieldScan -> fieldScan.primaryKey() != null)
                    .findFirst()
                    .orElse(null);
    }

    protected <T extends DatabaseEntity> T mapDataToNewEntity(Class<T> clazz, Map<String, Object> data, String dataPrefix, FetchLevel fetchLevel) {
        T entity;
        try {
            entity = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Failed to instantiate entity", e);
            return null;
        }
        entity.replicated = true;
        entity.fetchLevel = fetchLevel;
        mapDataToEntity(entity, data, dataPrefix, fetchLevel);
        FieldScan primaryKeyField = getPrimaryKeyFieldScan(clazz);
        entityTable.put(
                clazz,
                ReflectionUtils.getValue(primaryKeyField.field(), entity).orElseThrow(),
                entity
        );
        // Check for FetchLevel of object
        // To avoid having Primitive fetch level assigned even though entity is fully loaded
        if (fetchLevel == FetchLevel.Primitive) {
            List<FieldScan> fieldScans = getFieldScansForEntityClass(clazz);
            boolean lazyObjectExists = false;
            for (FieldScan fieldScan : fieldScans) {
                if (fieldScan.lazy() == null)
                    continue;
                lazyObjectExists = true;
                break;
            }
            if (!lazyObjectExists)
                entity.fetchLevel = FetchLevel.Full;
        }
//        List<FieldScan> fieldScans = getFieldScansForEntityClass(clazz);
//        boolean fullStatus = true;
//        for (FieldScan fieldScan : fieldScans) {
//            Object value;
//            try {
//                value = fieldScan.field().get(entity);
//            } catch (IllegalAccessException e) {
//                logger.error("Failed to get value from field " + fieldScan.column().value(), e);
//                fullStatus = false;
//                break;
//            }
//            fullStatus = value != null &&
//                         (!DatabaseEntity.class.isAssignableFrom(value.getClass()) || ((DatabaseEntity) value).isFetched());
//
//            if (!fullStatus)
//                break;
//        }
//        if (fullStatus)
//            entity.fetchLevel = FetchLevel.Full;
        return entity;
    }

    protected void mapDataToEntity(DatabaseEntity entity, Map<String, Object> data, String dataPrefix, FetchLevel fetchLevel) {
        List<FieldScan> fields = getFieldScansForEntityClass(entity.getClass());
        fields = fields.stream()
                       .sorted((o1, o2) -> o1.primaryKey() != null ? -1
                               : (o2.primaryKey() != null ? 1 : (o1.lazy() == null ? -1 : (o2.lazy() == null ? 1 : 0))))
                       .collect(Collectors.toList());
        // TODO: Go field by field and search for values with correct field key
        // TODO: It's better than filling values by data map, because we could get non primary key attribute for foreign object first
        for (FieldScan field : fields) {
            String columnName = dataPrefix + field.column().value();
            Class<? extends DatabaseEntity> foreignObjectClass = null;
            FieldScan foreignObjectPrimaryKeyField = null;
            if (field.foreignKey() != null) {
                foreignObjectClass = (Class<? extends DatabaseEntity>) field.field().getType();
                foreignObjectPrimaryKeyField = getPrimaryKeyFieldScan(foreignObjectClass);
                columnName += "__" + foreignObjectPrimaryKeyField.column().value();
            }
            if (!data.containsKey(columnName))
                continue;

            Object dataValue;
            if (foreignObjectClass != null) {
                Object primaryKeyValue = getTransformedValue(foreignObjectPrimaryKeyField, data.get(columnName));
                FetchLevel newFetchLevel = field.lazy() == null ? fetchLevel : (fetchLevel == FetchLevel.Full ? FetchLevel.Primitive : FetchLevel.None);
                if (entityTable.contains(foreignObjectClass, primaryKeyValue)) {
                    dataValue = entityTable.get(foreignObjectClass, primaryKeyValue);
                    Preconditions.checkNotNull(dataValue);
                    mapDataToEntity(
                            (DatabaseEntity) dataValue,
                            data,
                            dataPrefix + field.column().value() + "__",
                            newFetchLevel
                    );
                } else
                    dataValue = mapDataToNewEntity(
                            foreignObjectClass,
                            data,
                            dataPrefix + field.column().value() + "__",
                            newFetchLevel
                    );

            } else {
                dataValue = data.get(columnName);
            }

            ReflectionUtils.setValue(field.field(), entity, getTransformedValue(field, dataValue));
        }

    }

    private <T extends DatabaseEntity> T getEntityFromCache(Class<T> clazz, Object primaryKey, FetchLevel fetchLevel) {
        if (!entityTable.contains(clazz, primaryKey))
            return null;

        T entity = (T) entityTable.get(clazz, primaryKey);
        Preconditions.checkNotNull(entity);
        if (entity.fetchLevel.isLowerThan(fetchLevel))
            loadEntity(entity, fetchLevel);
        return entity;
    }

    protected <T extends DatabaseEntity> T findEntity(Class<T> clazz, Object primaryKey, FetchLevel fetchLevel) {
        // TODO: Find entity and if fetchLevel is lower than selected, use fetchEntity
        T cachedEntity = getEntityFromCache(clazz, primaryKey, fetchLevel);
        if (cachedEntity != null)
            return cachedEntity;

        QueryResult result = database.getProcessor().findEntityData(clazz, primaryKey, fetchLevel);
        if (result == null)
            return null;

        return mapDataToNewEntity(clazz, result.getDataMap(0), "", fetchLevel);
    }

    protected <T extends DatabaseEntity> T findEntity(Class<T> clazz, String conditions, Map<String, Object> parameters, FetchLevel fetchLevel) {
        QueryResult result = database.getProcessor().findEntityData(clazz, conditions, parameters, fetchLevel, 0, 1);
        if (result == null || result.getRowCount() == 0)
            return null;

        FieldScan primaryKeyField = getPrimaryKeyFieldScan(clazz);
        Preconditions.checkNotNull(primaryKeyField);
        Map<String, Object> data = result.getDataMap(0);
        Preconditions.checkState(data.containsKey(primaryKeyField.column().value()));
        Object primaryKeyValue = getTransformedValue(primaryKeyField, data.get(primaryKeyField.column().value()));
        if (entityTable.contains(clazz, primaryKeyValue)) {
            DatabaseEntity entity = entityTable.get(clazz, primaryKeyValue);
            if (entity.fetchLevel.isLowerThan(fetchLevel))
                mapDataToEntity(entity, data, "", fetchLevel);
            return (T) entity;
        }

        return mapDataToNewEntity(clazz, data, "", fetchLevel);
    }

    protected <T extends DatabaseEntity> Set<T> findEntities(Class<T> clazz, String conditions, Map<String, Object> parameters, FetchLevel fetchLevel) {
        QueryResult result = database.getProcessor().findEntityData(clazz, conditions, parameters, fetchLevel, -1, -1);
        if (result == null)
            return null;

        FieldScan primaryKeyField = getPrimaryKeyFieldScan(clazz);
        Preconditions.checkNotNull(primaryKeyField);
        List<T> entities = new ArrayList<>();
        for (Map<String, Object> map : result.getDataMapTable()) {
            Preconditions.checkState(map.containsKey(primaryKeyField.column().value()));
            Object primaryKeyValue = getTransformedValue(primaryKeyField, map.get(primaryKeyField.column().value()));
            if (entityTable.contains(clazz, primaryKeyValue)) {
                DatabaseEntity entity = entityTable.get(clazz, primaryKeyValue);
                if (entity.fetchLevel.isLowerThan(fetchLevel))
                    mapDataToEntity(entity, map, "", fetchLevel);
                entities.add((T) entity);
            } else
                entities.add(mapDataToNewEntity(clazz, map, "", fetchLevel));
        }

        return new HashSet<>(entities);
    }

    protected void loadEntity(DatabaseEntity entity, FetchLevel fetchLevel) {
        if (entity.fetchLevel == fetchLevel || entity.fetchLevel.isHigherThan(fetchLevel))
            return;

        QueryResult result = database.getProcessor().loadEntity(entity, fetchLevel);
        if (result == null)
            return; // TODO: Probably throw an exception
        mapDataToEntity(entity, result.getDataMap(0), "", fetchLevel);
        entity.replicated = true;
        entity.fetchLevel = fetchLevel;
    }

    protected void saveEntity(DatabaseEntity entity) {
        if (entity.replicated) {
            database.getProcessor().saveEntityExisting(entity);
            return;
        }

        Object primaryKey = database.getProcessor().saveEntityNew(entity);
        FieldScan primaryKeyFieldScan = getPrimaryKeyFieldScan(entity.getClass());
        Object primaryKeyValue = getTransformedValue(primaryKeyFieldScan, primaryKey);
        ReflectionUtils.setValue(primaryKeyFieldScan.field(), entity, primaryKeyValue);
        entity.replicated = true;
        entityTable.put(entity.getClass(), primaryKeyValue, entity);
    }
}
