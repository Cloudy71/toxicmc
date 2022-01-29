/*
  User: Cloudy
  Date: 07/01/2022
  Time: 02:58
*/

package cz.cloudy.minecraft.core.database.interfaces;

import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.Query;
import cz.cloudy.minecraft.core.database.QueryResult;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.types.DatabaseConnectionData;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cloudy
 */
public interface IDatabaseProcessor<T extends IDatabaseMapper> {
    Map<Class<? extends IDatabaseMapper>, IDatabaseMapper> mappers = new HashMap<>();

    /**
     * @param data
     * @return
     * @throws SQLException
     */
    boolean connect(DatabaseConnectionData data) throws SQLException;

    /**
     * @return
     * @throws SQLException
     */
    boolean isConnected() throws SQLException;

//    /**
//     * @return
//     */
//    IDatabaseMapper getMapper();

    default T getMapper() {
        ParameterizedType parameterizedType = null;
        for (Type genericInterface : getClass().getGenericInterfaces()) {
            if (genericInterface != IDatabaseProcessor.class)
                continue;
            parameterizedType = (ParameterizedType) genericInterface;
        }
        if (parameterizedType == null) {
            parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        }
        Class<? extends IDatabaseMapper> type = (Class<? extends IDatabaseMapper>) parameterizedType.getActualTypeArguments()[0];
        if (!mappers.containsKey(type)) {
            mappers.put(type, ReflectionUtils.newInstance(type));
        }
        return (T) mappers.get(type);
    }

    /**
     *
     */
    void buildTableStructure();

    /**
     *
     */
    void buildConstraintStructure();

    /**
     * @param query
     * @param parameters
     * @return
     * @throws SQLException
     */
    QueryResult processQuery(Query query, Map<String, Object> parameters) throws SQLException;

    /**
     * @param clazz
     * @param primaryKey
     * @param fetchLevel
     * @return
     */
    QueryResult findEntityData(Class<? extends DatabaseEntity> clazz, Object primaryKey, FetchLevel fetchLevel);

    /**
     * @param clazz
     * @param conditions
     * @param parameters
     * @param fetchLevel
     * @return
     */
    QueryResult findEntityData(Class<? extends DatabaseEntity> clazz, String conditions, Map<String, Object> parameters, FetchLevel fetchLevel, int from,
                               int limit);

    QueryResult loadEntity(DatabaseEntity entity, FetchLevel fetchLevel);

    /**
     * @param entity
     * @return
     */
    Object saveEntityNew(DatabaseEntity entity);

    /**
     * @param entity
     */
    void saveEntityExisting(DatabaseEntity entity);
}
