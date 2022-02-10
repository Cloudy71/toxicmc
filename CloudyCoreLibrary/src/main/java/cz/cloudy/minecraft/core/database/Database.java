/*
  User: Cloudy
  Date: 07/01/2022
  Time: 03:59
*/

package cz.cloudy.minecraft.core.database;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.database.enums.DatabaseEngine;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.interfaces.IDatabaseProcessor;
import cz.cloudy.minecraft.core.database.types.DatabaseConnectionData;
import cz.cloudy.minecraft.core.database.types.FieldScan;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cloudy
 */
/* TODO: Every plugin replaces processor object. Old processor objects keep connection to DB.
 *       To keep support for different dbs for each plugin, this component must be plugin singleton based.
 */
@Component
public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    @Component
    private DatabaseEntityMapper databaseEntityMapper;

    private IDatabaseProcessor<?> processor = null;

    /**
     * @param data
     * @return
     */
    public IDatabaseProcessor<?> initializeProcessor(DatabaseConnectionData data) {
        processor = ReflectionUtils.newInstance(data.engine().getEngineClass());
        Preconditions.checkNotNull(processor);

        try {
            logger.info("Connecting to the database");
            boolean connected = processor.connect(data);
            if (connected)
                logger.info("Connected!");
        } catch (SQLException e) {
            logger.error("", e);
        }

        return processor;
    }

    /**
     * @return
     */
    public IDatabaseProcessor<?> getProcessor() {
        return processor;
    }

    /**
     * @param engine
     * @param host
     * @param port
     * @param user
     * @param pass
     * @param db
     * @return
     */
    public DatabaseConnectionData generateDatabaseConnectionDataFromAttributes(DatabaseEngine engine, String host, int port, String user, String pass,
                                                                               String db) {
        String url = engine.resolveUrl(host, port, db);
        //"mysql://" + host + ":" + port + "/" + db
        return new DatabaseConnectionData(
                engine,
                url,
                user,
                pass
        );
    }

    /**
     * @param query
     * @param parameters
     * @return
     */
    public QueryResult processQuery(Query query, Map<String, Object> parameters) {
        try {
            return getProcessor().processQuery(query, parameters);
        } catch (SQLException e) {
            logger.error("Failed to process query", e);
        }
        return null;
    }

    /**
     * @param query
     * @return
     */
    public QueryResult processQuery(Query query) {
        return processQuery(query, null);
    }

    /**
     * @param clazz
     * @param primaryKey
     * @param fetchLevel
     * @param <T>
     * @return
     */
    public <T extends DatabaseEntity> T findEntity(Class<T> clazz, Object primaryKey, FetchLevel fetchLevel) {
        return databaseEntityMapper.findEntity(clazz, primaryKey, fetchLevel);
    }

    /**
     * @param clazz
     * @param primaryKey
     * @param <T>
     * @return
     */
    public <T extends DatabaseEntity> T findEntity(Class<T> clazz, Object primaryKey) {
        return findEntity(clazz, primaryKey, FetchLevel.Primitive);
    }

    /**
     * @param clazz
     * @param conditions
     * @param parameters
     * @param fetchLevel
     * @param <T>
     * @return
     */
    public <T extends DatabaseEntity> T findEntity(Class<T> clazz, String conditions, Map<String, Object> parameters, FetchLevel fetchLevel) {
        return databaseEntityMapper.findEntity(clazz, conditions, parameters, fetchLevel);
    }

    public <T extends DatabaseEntity> Set<T> findEntities(Class<T> clazz, String conditions, Map<String, Object> parameters, FetchLevel fetchLevel) {
        return databaseEntityMapper.findEntities(clazz, conditions, parameters, fetchLevel);
    }

    public <T extends DatabaseEntity> Set<T> findEntities(Class<T> clazz, FetchLevel fetchLevel) {
        return findEntities(clazz, null, null, fetchLevel);
    }

    public <T extends DatabaseEntity> Set<T> findEntities(Class<T> clazz) {
        return findEntities(clazz, FetchLevel.Primitive);
    }

    /**
     * @param entity
     */
    protected void loadEntity(DatabaseEntity entity, FetchLevel fetchLevel) {
        databaseEntityMapper.loadEntity(entity, fetchLevel);
    }

    /**
     * @param entity
     */
    protected void saveEntity(DatabaseEntity entity) {
        databaseEntityMapper.saveEntity(entity);
    }

    /**
     * @param entity
     */
    protected void fullSaveEntity(DatabaseEntity entity) {
        databaseEntityMapper.saveEntity(entity);
        List<FieldScan> fields = databaseEntityMapper.getFieldScansForEntityClass(entity.getClass());
        for (FieldScan field : fields) {
            if (field.foreignKey() == null)
                continue;
            DatabaseEntity obj = (DatabaseEntity) ReflectionUtils.getValueOpt(field.field(), entity).orElse(null);
            if (obj == null)
                return;

            fullSaveEntity(obj);
        }
    }

    protected void deleteEntity(DatabaseEntity entity) {
        databaseEntityMapper.deleteEntity(entity);
    }
}
