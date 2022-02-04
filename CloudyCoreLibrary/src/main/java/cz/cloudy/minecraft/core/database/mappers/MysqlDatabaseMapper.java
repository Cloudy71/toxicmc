/*
  User: Cloudy
  Date: 07/01/2022
  Time: 22:12
*/

package cz.cloudy.minecraft.core.database.mappers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.DatabaseEntityMapper;
import cz.cloudy.minecraft.core.database.annotation.Size;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.fetch_data.MysqlFetchData;
import cz.cloudy.minecraft.core.database.interfaces.IFetchData;
import cz.cloudy.minecraft.core.database.types.ClassScan;
import cz.cloudy.minecraft.core.database.types.FieldScan;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Cloudy
 */
public class MysqlDatabaseMapper
        extends DefaultJdbcMapper {
    @Override
    public String mapFieldScanToDatabaseType(FieldScan fieldScan) {
        Class<?> clazz = fieldScan.getDatabaseClass();

        if (String.class.isAssignableFrom(clazz)) {
            int size = Size.DefaultSize;
            if (fieldScan.size() != null)
                size = fieldScan.size().value();
            return "VARCHAR(" + size + ")";
        }
        if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz)) {
            return "TINYINT" + (fieldScan.size() != null ? "(" + fieldScan.size().value() + ")" : "");
        }
        if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)) {
            return "SMALLINT" + (fieldScan.size() != null ? "(" + fieldScan.size().value() + ")" : "");
        }
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return "INT" + (fieldScan.size() != null ? "(" + fieldScan.size().value() + ")" : "");
        }
        if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
            return "BIGINT" + (fieldScan.size() != null ? "(" + fieldScan.size().value() + ")" : "");
        }
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
            return "BOOL";
        }
        if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
            return "FLOAT";
        }
        if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return "DOUBLE";
        }
        if (ZonedDateTime.class.isAssignableFrom(clazz)) {
            return "DATETIME";
        }
        if (DatabaseEntity.class.isAssignableFrom(clazz)) {
            FieldScan primaryKey = ComponentLoader.get(DatabaseEntityMapper.class).getPrimaryKeyFieldScan((Class<? extends DatabaseEntity>) clazz);
            return mapFieldScanToDatabaseType(primaryKey);
        }

        return null;
    }

    @Override
    public String fieldScanToFieldDefinition(FieldScan fieldScan) {
        return fieldScan.column().value() +
               " " +
               mapFieldScanToDatabaseType(fieldScan) +
               " " +
               (fieldScan.nullable() == null ? "NOT NULL" : "") +
               " " +
               (fieldScan.autoIncrement() != null ? "AUTO_INCREMENT" : "") +
               " " +
               (fieldScan.default_() != null ? "DEFAULT " + fieldScan.default_().value() : "");
    }

    @Override
    public String fieldScanToConstraint(FieldScan fieldScan) {
        if (fieldScan.primaryKey() != null) {
            return "CONSTRAINT PK_" +
                   fieldScan.classScan().table().value() +
                   "_" + fieldScan.column().value() +
                   " PRIMARY KEY (" +
                   fieldScan.column().value() +
                   ")";
        }
        if (fieldScan.foreignKey() != null) {
            FieldScan ref = ComponentLoader.get(DatabaseEntityMapper.class)
                                           .getPrimaryKeyFieldScan((Class<? extends DatabaseEntity>) fieldScan.field().getType());
            return "CONSTRAINT FK_" +
                   fieldScan.classScan().table().value() +
                   "_" +
                   fieldScan.column().value() +
                   " " +
                   "FOREIGN KEY (" +
                   fieldScan.column().value() +
                   ") REFERENCES " +
                   ref.classScan().table().value() +
                   "(" +
                   ref.column().value() + ")";

        }
        // TODO: Constraint types depends on Protected annotation

        return null;
    }

    @Override
    public List<String> fieldScansToIndexes(Set<FieldScan> fieldScanSet) {
        List<String> indexList = new ArrayList<>();
        Map<Byte, List<String>> multiIndexMap = new HashMap<>();
        for (FieldScan fieldScan : fieldScanSet) {
            if (fieldScan.index() != null) {
                indexList.add(
                        "CREATE " +
                        (fieldScan.index().unique() ? "UNIQUE " : "") +
                        "INDEX IDX_" +
                        fieldScan.classScan().table().value() +
                        "_" +
                        fieldScan.column().value() +
                        " ON " +
                        fieldScan.classScan().table().value() +
                        " (" +
                        fieldScan.column().value() +
                        ")"
                );
            }
            if (fieldScan.multiIndex() != null) {
                multiIndexMap.computeIfAbsent(fieldScan.multiIndex().value(), aByte -> new ArrayList<>(List.of(fieldScan.classScan().table().value())))
                             .add(fieldScan.column().value());
            }
        }

        for (Map.Entry<Byte, List<String>> entry : multiIndexMap.entrySet()) {
            String tableName = entry.getValue().get(0);
            entry.getValue().remove(0);
            String idxString = "CREATE INDEX MIDX_" +
                               entry.getKey() +
                               " ON " +
                               tableName +
                               " (";
            StringBuilder fields = new StringBuilder();
            for (String s : entry.getValue()) {
                fields.append(fields.length() > 0 ? "," : "")
                      .append(s);
            }
            idxString += fields +
                         ")";
            indexList.add(idxString);

        }
        return indexList;
    }

    private void writeFetchDataForEntityType(Class<? extends DatabaseEntity> clazz, String prefix, String conditions, FetchLevel fetchLevel,
                                             StringBuilder selectQuery, List<String> joinQuery, Map<String, String> translationMap) {
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        List<FieldScan> fields = entityMapper.getFieldScansForEntityClass(clazz);
        ClassScan classScan = entityMapper.getClassScanForEntityClass(clazz);
        int tableId = joinQuery.size();
        String tableLabel = "t" + tableId;
        if (conditions != null)
            conditions = conditions.replaceAll("%", tableLabel);
        joinQuery.add(classScan.table().value() + " " + tableLabel + (conditions != null ? " ON " + conditions : ""));

        for (FieldScan field : fields) {
            if (field.primaryKey() == null && fetchLevel == FetchLevel.None)
                continue;

            if (field.foreignKey() != null) {
                Class<? extends DatabaseEntity> foreignClass = (Class<? extends DatabaseEntity>) field.field().getType();
                FieldScan foreignPrimaryKeyField = entityMapper.getPrimaryKeyFieldScan(foreignClass);
                if (field.lazy() == null || fetchLevel == FetchLevel.Full) {
                    writeFetchDataForEntityType(
                            foreignClass,
                            prefix + field.column().value() + "__",
                            "%." + foreignPrimaryKeyField.column().value() + "=" + tableLabel + "." + field.column().value(),
                            field.lazy() == null ? fetchLevel : FetchLevel.Primitive,
                            selectQuery,
                            joinQuery,
                            translationMap
                    );
                } else {
                    selectQuery.append(!selectQuery.isEmpty() ? "," : "")
                               .append(tableLabel)
                               .append(".")
                               .append(field.column().value())
                               .append(" AS ")
                               .append(prefix)
                               .append(field.column().value())
                               .append("__")
                               .append(foreignPrimaryKeyField.column().value());
                    translationMap.put(
                            prefix + field.column().value() + "__" + foreignPrimaryKeyField.column().value(),
                            tableLabel + "." + field.column().value()
                    );
                }
            } else if (field.lazy() == null || fetchLevel == FetchLevel.Full) {
                selectQuery.append(!selectQuery.isEmpty() ? "," : "")
                           .append(tableLabel)
                           .append(".")
                           .append(field.column().value())
                           .append(" AS ")
                           .append(prefix)
                           .append(field.column().value());
                translationMap.put(
                        prefix + field.column().value(),
                        tableLabel + "." + field.column().value()
                );
            }
        }
    }

    @Override
    public IFetchData buildFetchDataForEntityType(Class<? extends DatabaseEntity> clazz, FetchLevel fetchLevel) {
        StringBuilder select = new StringBuilder();
        List<String> joins = new ArrayList<>();

        Map<String, String> translationMap = new HashMap<>();
        writeFetchDataForEntityType(
                clazz,
                "",
                null,
                fetchLevel,
                select,
                joins,
                translationMap
        );

        Preconditions.checkState(!joins.isEmpty());
        String from = joins.get(0);
        joins.remove(0);

        return new MysqlFetchData(
                select.toString(),
                from,
                ImmutableList.copyOf(joins),
                ImmutableMap.copyOf(translationMap)
        );
    }
}
