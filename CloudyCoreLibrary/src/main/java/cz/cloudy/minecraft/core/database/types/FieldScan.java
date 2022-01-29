/*
  User: Cloudy
  Date: 07/01/2022
  Time: 21:57
*/

package cz.cloudy.minecraft.core.database.types;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.DatabaseEntityMapper;
import cz.cloudy.minecraft.core.database.annotation.*;

import java.lang.reflect.Field;

/**
 * @author Cloudy
 */
public record FieldScan(ClassScan classScan, Field field,
                        Column column, PrimaryKey primaryKey,
                        ForeignKey foreignKey, Lazy lazy,
                        Size size, Null nullable,
                        AutoIncrement autoIncrement,
                        Protected protected_, Transform transform,
                        Default default_, Index index,
                        MultiIndex multiIndex) {

    // ==============================================================
    public Class<?> getDatabaseClass() {
        return ComponentLoader.get(DatabaseEntityMapper.class).getFieldScanDatabaseClass(this);
    }

    public Object getDatabaseValue(DatabaseEntity entity) {
        return ComponentLoader.get(DatabaseEntityMapper.class).getFieldScanDatabaseValue(entity, this);
    }
}
