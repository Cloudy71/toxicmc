/*
  User: Cloudy
  Date: 07/01/2022
  Time: 02:59
*/

package cz.cloudy.minecraft.core.database.processors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.ReflectionUtils;
import cz.cloudy.minecraft.core.data_transforming.DataTransformer;
import cz.cloudy.minecraft.core.data_transforming.interfaces.IDataTransformer;
import cz.cloudy.minecraft.core.database.*;
import cz.cloudy.minecraft.core.database.annotation.Default;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.database.enums.QueryType;
import cz.cloudy.minecraft.core.database.fetch_data.MysqlFetchData;
import cz.cloudy.minecraft.core.database.mappers.MysqlDatabaseMapper;
import cz.cloudy.minecraft.core.database.queries.RawDMLOrDDLQuery;
import cz.cloudy.minecraft.core.database.results.DMLOrDDLQueryResult;
import cz.cloudy.minecraft.core.database.results.DQLQueryResult;
import cz.cloudy.minecraft.core.database.types.ClassScan;
import cz.cloudy.minecraft.core.database.types.DatabaseConnectionData;
import cz.cloudy.minecraft.core.database.types.FieldScan;
import cz.cloudy.minecraft.core.types.Pair;
import org.slf4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cloudy
 */
public class MysqlDatabaseProcessor
        extends DefaultJdbcProcessor<MysqlDatabaseMapper> {
    private static final Logger logger = LoggerFactory.getLogger(MysqlDatabaseProcessor.class);

    private Connection                   connection;
    private DatabaseConnectionData       connectionData;
    private Map<ClassScan, List<String>> constraintList;
    private Map<ClassScan, List<String>> indexList;

    private final Pattern stringPattern;
    private final Pattern paramPattern;
    private final Pattern attributePattern;

    public MysqlDatabaseProcessor() {
        stringPattern = Pattern.compile("('.*?')");
        paramPattern = Pattern.compile(":([a-zA-Z0-9_]+)");
        attributePattern = Pattern.compile("([a-zA-Z0-9_]+)");
    }

    @Override
    public boolean connect(DatabaseConnectionData data) throws SQLException {
        if (isConnected())
            return true;

        connectionData = data;

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("", e);
            return false;
        }
        connection = DriverManager.getConnection(data.url(), data.user(), data.pass());
        return true;
    }

    @Override
    public boolean isConnected() throws SQLException {
        return connection != null && !connection.isClosed();
    }

//    @Override
//    public IDatabaseMapper getMapper() {
//        if (mapper == null)
//            mapper = new MysqlDatabaseMapper();
//        return mapper;
//    }

    // TODO: Alter table columns in case columns are missing or are deleted
    @Override
    public void buildTableStructure() {
        Set<Pair<ClassScan, Set<FieldScan>>> scans = ComponentLoader.get(DatabaseEntityMapper.class).getMappedUnConstructedDatabaseEntityClasses();
        constraintList = new HashMap<>();
        indexList = new HashMap<>();

        DQLQueryResult result;
        try {
            result = (DQLQueryResult) processQuery(
                    Query.builder()
                         .rawDQL(
                                 "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, COLUMN_TYPE, COLUMN_KEY\n" +
                                 "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                                 "WHERE TABLE_SCHEMA = :schema"
                         )
                         .build(),
                    ImmutableMap.of("schema", connection.getCatalog())
            );
        } catch (SQLException e) {
            logger.error("Failed to fetch tables: ", e);
            return;
        }

        for (Pair<ClassScan, Set<FieldScan>> scan : scans) {
            ClassScan classScan = scan.getKey();
            Set<FieldScan> fieldScans = scan.getValue();

            boolean exists = false;
            for (Map<String, Object> tableData : result.getDataMapTable()) {
                if (!tableData.get("TABLE_NAME").equals(classScan.table().value()))
                    continue;

                exists = true;
                break;
            }

            if (exists) {
                List<String> constraints = new ArrayList<>();
                for (FieldScan fieldScan : fieldScans) {
                    boolean columnExists = false;
                    for (Map<String, Object> tableData : result.getDataMapTable()) {
                        if (!tableData.get("TABLE_NAME").equals(classScan.table().value()) || !tableData.get("COLUMN_NAME").equals(fieldScan.column().value()))
                            continue;

                        columnExists = true;
                        break;
                    }
                    if (columnExists)
                        continue;

                    logger.info("Appending column {} to the {} table.", fieldScan.column().value(), classScan.table().value());
                    StringBuilder query = new StringBuilder()
                            .append("ALTER TABLE ")
                            .append(classScan.table().value())
                            .append(" ADD ")
                            .append(getMapper().fieldScanToFieldDefinition(fieldScan));

                    String constraint = getMapper().fieldScanToConstraint(fieldScan);
                    try {
                        processQuery(
                                Query.builder()
                                     .rawDMLOrDDL(query.toString())
                                     .build(),
                                null
                        );
                        if (constraint != null)
                            constraints.add(constraint);
                    } catch (SQLException e) {
                        logger.error("Failed to alter table structure: ", e);
                    }
                }
            } else {

                StringBuilder query = new StringBuilder()
                        .append("CREATE TABLE ")
                        .append(classScan.table().value())
                        .append(" (");
                List<String> constraints = new ArrayList<>();
                String primaryConstraint = "";
                boolean firstScan = true;
                for (FieldScan fieldScan : fieldScans) {
                    query.append(!firstScan ? "," : "")
                         .append(getMapper().fieldScanToFieldDefinition(fieldScan));
                    firstScan = false;

                    String constraint = getMapper().fieldScanToConstraint(fieldScan);
                    if (constraint != null) {
                        if (fieldScan.primaryKey() != null)
                            primaryConstraint = constraint;
                        else
                            constraints.add(constraint);
                    }
                }
                List<String> indexes = getMapper().fieldScansToIndexes(fieldScans);

                query.append(",")
                     .append(primaryConstraint)
                     .append(")");

                try {
                    processQuery(
                            Query.builder()
                                 .rawDMLOrDDL(query.toString())
                                 .build(),
                            null
                    );
                    if (!constraints.isEmpty())
                        constraintList.put(classScan, constraints);
                    if (!indexes.isEmpty())
                        indexList.put(classScan, indexes);
                } catch (SQLException e) {
                    // If table exists, an exception is thrown, so no need to log
                    logger.error("Failed to build table structure: ", e);
                }
            }
            classScan.setConstructed(true);
        }
    }

    @Override
    public void buildConstraintStructure() {
        for (Map.Entry<ClassScan, List<String>> entry : constraintList.entrySet()) {
            ClassScan classScan = entry.getKey();
            List<String> constraints = entry.getValue();
            StringBuilder query = new StringBuilder();
            query.append("ALTER TABLE ")
                 .append(classScan.table().value());
            boolean firstConstraint = true;
            for (String constraint : constraints) {
                query.append(!firstConstraint ? "," : "")
                     .append(" ADD ")
                     .append(constraint);
                firstConstraint = false;
            }
            try {
                processQuery(
                        Query.builder()
                             .rawDMLOrDDL(query.toString())
                             .build(),
                        null
                );
            } catch (SQLException e) {
                logger.error("Failed to build structure: ", e);
            }
        }

        constraintList.clear();
        constraintList = null;

        for (Map.Entry<ClassScan, List<String>> entry : indexList.entrySet()) {
            try {
                for (String s : entry.getValue()) {
                    processQuery(
                            Query.builder()
                                 .rawDMLOrDDL(s)
                                 .build(),
                            null
                    );
                }
            } catch (SQLException e) {
                logger.error("Failed to build indexes: ", e);
            }
        }

        indexList.clear();
        indexList = null;
    }

    @Override
    public QueryResult processQuery(Query query, Map<String, Object> parameters) throws SQLException {
        connect(connectionData);
        String queryString = query.provideQueryString();
        List<Object> paramList = null;
        if (parameters != null && !parameters.isEmpty()) {
            String stringHiddenQuery = queryString;
            Matcher matcher = stringPattern.matcher(stringHiddenQuery);
            while (matcher.find()) {
                stringHiddenQuery = stringHiddenQuery.substring(0, matcher.start())
                                    + " ".repeat(matcher.end() - matcher.start())
                                    + stringHiddenQuery.substring(matcher.end());
            }
            List<Pair<Integer, Integer>> matchList = new ArrayList<>();
            matcher = paramPattern.matcher(stringHiddenQuery);
            paramList = new ArrayList<>();
            while (matcher.find()) {
                String paramName = stringHiddenQuery.substring(matcher.start() + 1, matcher.end());
                Preconditions.checkState(parameters.containsKey(paramName), "Parameter \"" + paramName + "\" not found in parameter map");
                stringHiddenQuery = stringHiddenQuery.substring(0, matcher.start()) + "?" + queryString.substring(matcher.end());
                queryString = queryString.substring(0, matcher.start()) + "?" + queryString.substring(matcher.end());
                paramList.add(parameters.get(paramName));
                matcher.reset(stringHiddenQuery);
//                matchList.add(new Pair<>(matcher.start(), matcher.end()));
            }
//            paramArray = new Object[matchList.size()];
//            for (int i = matchList.size() - 1; i >= 0; --i) {
//                Pair<Integer, Integer> positions = matchList.get(i);
//                String paramName = stringHiddenQuery.substring(positions.getKey() + 1, positions.getValue());
//                Preconditions.checkState(parameters.containsKey(paramName), "Parameter \"" + paramName + "\" not found in parameter map");
//                queryString = queryString.substring(0, positions.getKey()) + "?" + queryString.substring(positions.getValue());
//                paramArray[i] = parameters.get(paramName);
//
//            }
        }

        PreparedStatement statement = null;
        if (query instanceof RawDMLOrDDLQuery rawDMLOrDDLQuery) {
            if (rawDMLOrDDLQuery.getStatementType() == Statement.RETURN_GENERATED_KEYS)
                statement = connection.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
        }
        if (statement == null)
            statement = connection.prepareStatement(queryString);

        if (paramList != null) {
            int size = paramList.size();
            for (int i = 0; i < size; i++) {
                statement.setObject(i + 1, paramList.get(i));
            }
        }

        logger.info(queryString);
        if (query.getQueryType() == QueryType.RawDQL) {
            ResultSet resultSet = statement.executeQuery();
            return new DQLQueryResult(resultSet);
        } else {
            int resultNum = statement.executeUpdate();
            return new DMLOrDDLQueryResult(statement, resultNum);
        }

    }

    @Override
    public QueryResult findEntityData(Class<? extends DatabaseEntity> clazz, Object primaryKey, FetchLevel fetchLevel) {
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        FieldScan primaryKeyField = entityMapper.getPrimaryKeyFieldScan(clazz);
        Preconditions.checkNotNull(primaryKeyField);
        if (primaryKeyField.transform() != null) {
            IDataTransformer transformer = ComponentLoader.get(DataTransformer.class)
                                                          .getUnknownDataTransformer(primaryKeyField.transform().value());
            if (primaryKey.getClass() != transformer.getTypes().getValue())
                primaryKey = transformer.transform0to1(primaryKey);
        }
        return findEntityData(
                clazz,
                primaryKeyField.column().value() + " = :pk",
                ImmutableMap.of("pk", primaryKey),
                fetchLevel,
                0,
                1
        );
    }

    private String buildQueryForEntityData(Class<? extends DatabaseEntity> clazz, String conditions, FetchLevel fetchLevel, int from, int limit) {
        StringBuilder query = new StringBuilder();
        MysqlFetchData fetchData = (MysqlFetchData) getMapper().buildFetchDataForEntityType(clazz, fetchLevel);
        if (conditions != null && !conditions.isEmpty()) {
            conditions = conditions.replaceAll("\\.", "__"); // TODO: Could replace dots in strings
            Matcher matcher = attributePattern.matcher(conditions);
            int start = 0;
            while (matcher.find(start)) {
                if (matcher.start() > 0 && conditions.charAt(matcher.start() - 1) == ':') {
                    start = matcher.end();
                    continue;
                }

                String attributeName = conditions.substring(matcher.start(), matcher.end());
                if (!fetchData.getTranslationMap().containsKey(attributeName)) {
                    start = matcher.end();
                    continue;
                }
                String mappedValue = fetchData.getTranslationMap().get(attributeName);
                conditions = conditions.substring(0, matcher.start()) + mappedValue + conditions.substring(matcher.end());
                start = matcher.start() + mappedValue.length();
                matcher.reset(conditions);
            }
        }

        query.append("SELECT ")
             .append(fetchData.getSelectQuery())
             .append(" FROM ")
             .append(fetchData.getFromQuery());
        for (int i = 0; i < fetchData.getJoinQuery().size(); i++) {
            query.append(" JOIN ")
                 .append(fetchData.getJoinQuery().get(i));
        }
        query.append(conditions != null ? (" WHERE " + conditions + " ") : "")
             .append(limit != -1 ? (" LIMIT " + limit) : "")
             .append(from != -1 ? (" OFFSET " + from) : "");

        return query.toString();
    }

    @Override
    public QueryResult findEntityData(Class<? extends DatabaseEntity> clazz, String conditions, Map<String, Object> parameters, FetchLevel fetchLevel, int from,
                                      int limit) {
        // TODO: Parameters could contain untransformed data.
        String query = buildQueryForEntityData(clazz, conditions, fetchLevel, from, limit);
        DQLQueryResult result = (DQLQueryResult) ComponentLoader.get(Database.class)
                                                                .processQuery(Query.builder()
                                                                                   .rawDQL(query)
                                                                                   .build(),
                                                                              parameters);
        return result;
    }

    @Override
    public QueryResult loadEntity(DatabaseEntity entity, FetchLevel fetchLevel) {
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        FieldScan primaryKeyField = entityMapper.getPrimaryKeyFieldScan(entity.getClass());
        Map<String, Object> parameters = ImmutableMap.of("pk", entityMapper.getFieldScanDatabaseValue(entity, primaryKeyField));
        String query = buildQueryForEntityData(
                entity.getClass(),
                primaryKeyField.column().value() + " = :pk",
                fetchLevel,
                0,
                1
        );
        DQLQueryResult result = (DQLQueryResult) ComponentLoader.get(Database.class)
                                                                .processQuery(Query.builder()
                                                                                   .rawDQL(query)
                                                                                   .build(),
                                                                              parameters);
        if (result.getRowCount() != 1)
            return null;
        return result;
    }

    @Override
    public Object saveEntityNew(DatabaseEntity entity) {
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        StringBuilder query = new StringBuilder();
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<FieldScan> fieldScans = entityMapper.getFieldScansForEntityClass(entity.getClass());
        Map<String, Object> parameters = new HashMap<>();
        int v = 0;
        boolean autoIncrement = false;
        for (FieldScan fieldScan : fieldScans) {
            if (fieldScan.primaryKey() != null && fieldScan.autoIncrement() != null) {
                autoIncrement = true;
                continue;
            }

            Object value = null;
            if (fieldScan.foreignKey() != null) {
                DatabaseEntity foreignEntity = (DatabaseEntity) ReflectionUtils.getValueOpt(fieldScan.field(), entity).orElse(null);
                if (foreignEntity != null) {
                    FieldScan foreignPrimaryKeyFieldScan = entityMapper.getPrimaryKeyFieldScan(foreignEntity.getClass());
                    value = foreignPrimaryKeyFieldScan.getDatabaseValue(foreignEntity);
                }
            } else {
                value = fieldScan.getDatabaseValue(entity);
            }

            if (fieldScan.nullable() == null && value == null) {
                if (fieldScan.default_() == null)
                    continue;
                value = fieldScan.default_();
            }

            fields.append(fields.length() > 0 ? "," : "")
                  .append(fieldScan.column().value());
            values.append(values.length() > 0 ? "," : "");
            if (value instanceof Default default_) {
                values.append(default_.value());
            } else {
                values.append(":v")
                      .append(v);
                parameters.put("v" + (v++), value);
            }
        }

        query.append("INSERT INTO ")
             .append(entityMapper.getClassScanForEntityClass(entity.getClass()).table().value())
             .append(" (")
             .append(fields)
             .append(") VALUES (")
             .append(values)
             .append(")");

        DMLOrDDLQueryResult result = (DMLOrDDLQueryResult) ComponentLoader.get(Database.class)
                                                                          .processQuery(Query.builder()
                                                                                             .rawDMLOrDDL(query.toString())
                                                                                             .statementType(autoIncrement
                                                                                                                    ? Statement.RETURN_GENERATED_KEYS
                                                                                                                    : Statement.NO_GENERATED_KEYS)
                                                                                             .build(),
                                                                                        parameters);

        if (result.getResult() == 0) {
            return null;
        }

        if (autoIncrement) {
            try {
                ResultSet generatedKeys = result.getStatement().getGeneratedKeys();
                generatedKeys.next();
                return generatedKeys.getLong(1);
            } catch (SQLException e) {
                logger.error("Failed to fetch generated keys", e);
                return null;
            }
        }
        FieldScan primaryKeyFieldScan = entityMapper.getPrimaryKeyFieldScan(entity.getClass());
        return ReflectionUtils.getValueOpt(primaryKeyFieldScan.field(), entity).orElse(null);
    }

    @Override
    public void saveEntityExisting(DatabaseEntity entity) {
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        StringBuilder query = new StringBuilder();
        StringBuilder fields = new StringBuilder();
        List<FieldScan> fieldScans = entityMapper.getFieldScansForEntityClass(entity.getClass());
        Map<String, Object> parameters = new HashMap<>();
        int v = 0;
        for (FieldScan fieldScan : fieldScans) {
            if (fieldScan.primaryKey() != null) // It is currently unable to change entity's primary key value
                continue;

            Object value = null;
            if (fieldScan.foreignKey() != null) {
                DatabaseEntity foreignEntity = (DatabaseEntity) ReflectionUtils.getValueOpt(fieldScan.field(), entity).orElse(null);
                if (foreignEntity != null) {
                    FieldScan foreignPrimaryKeyFieldScan = entityMapper.getPrimaryKeyFieldScan(foreignEntity.getClass());
                    value = foreignPrimaryKeyFieldScan.getDatabaseValue(foreignEntity);
                }
            } else {
                value = fieldScan.getDatabaseValue(entity);
            }

            if (fieldScan.nullable() == null && value == null) {
                if (fieldScan.default_() == null)
                    continue;
                value = fieldScan.default_();
            }

            fields.append(fields.length() > 0 ? "," : "")
                  .append(fieldScan.column().value())
                  .append("=");
            if (value instanceof Default default_) {
                fields.append(default_.value());
            } else {
                fields.append(":v")
                      .append(v);
                parameters.put("v" + (v++), value);
            }
        }
        FieldScan primaryKeyFieldScan = entityMapper.getPrimaryKeyFieldScan(entity.getClass());

        query.append("UPDATE ")
             .append(entityMapper.getClassScanForEntityClass(entity.getClass()).table().value())
             .append(" SET ")
             .append(fields)
             .append(" WHERE ")
             .append(primaryKeyFieldScan.column().value())
             .append("=:pk");
        parameters.put("pk", entityMapper.getFieldScanDatabaseValue(entity, primaryKeyFieldScan));

        ComponentLoader.get(Database.class)
                       .processQuery(Query.builder()
                                          .rawDMLOrDDL(query.toString())
                                          .build(),
                                     parameters);
    }

    @Override
    public void deleteEntity(DatabaseEntity entity) {
        DatabaseEntityMapper entityMapper = ComponentLoader.get(DatabaseEntityMapper.class);
        FieldScan primaryKeyFieldScan = entityMapper.getPrimaryKeyFieldScan(entity.getClass());
        Object databasePrimaryKey = primaryKeyFieldScan.getDatabaseValue(entity);

        String query = "DELETE FROM " +
                       entityMapper.getClassScanForEntityClass(entity.getClass()).table().value() +
                       " WHERE " +
                       primaryKeyFieldScan.column().value() +
                       "=:pk";

        ComponentLoader.get(Database.class)
                       .processQuery(Query.builder()
                                          .rawDMLOrDDL(query)
                                          .build(),
                                     ImmutableMap.of("pk", databasePrimaryKey));
    }
}
