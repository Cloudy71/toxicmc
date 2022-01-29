/*
  User: Cloudy
  Date: 07/01/2022
  Time: 22:12
*/

package cz.cloudy.minecraft.core.database.interfaces;

import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.types.FieldScan;

import java.util.List;
import java.util.Set;

/**
 * @author Cloudy
 */
public interface IDatabaseMapper {
    String mapFieldScanToDatabaseType(FieldScan fieldScan);

    String fieldScanToFieldDefinition(FieldScan fieldScan);

    String fieldScanToConstraint(FieldScan fieldScan);

    List<String> fieldScansToIndexes(Set<FieldScan> fieldScanSet);

    //    String buildSelectStringForEntityType(Class<? extends DatabaseEntity> clazz, String prefix, FetchLevel fetchLevel);
    IFetchData buildFetchDataForEntityType(Class<? extends DatabaseEntity> clazz, FetchLevel fetchLevel);
}
