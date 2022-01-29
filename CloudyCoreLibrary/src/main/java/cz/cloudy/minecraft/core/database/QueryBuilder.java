/*
  User: Cloudy
  Date: 07/01/2022
  Time: 03:42
*/

package cz.cloudy.minecraft.core.database;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.enums.QueryType;
import cz.cloudy.minecraft.core.database.queries.RawDMLOrDDLQuery;
import cz.cloudy.minecraft.core.database.queries.RawDQLQuery;
import cz.cloudy.minecraft.core.database.queries.SelectQuery;
import cz.cloudy.minecraft.core.database.types.FieldScan;

import java.util.List;

/**
 * @author Cloudy
 */
public class QueryBuilder {
    private Query query;

    protected QueryBuilder() {
    }

    private void setQueryType(QueryType queryType) {
        Preconditions.checkState(query == null || query.queryType == queryType, "Query is already defined");

        if (query != null)
            return;

        switch (queryType) {
            case RawDQL -> query = new RawDQLQuery();
            case RawDMLOrDDL -> query = new RawDMLOrDDLQuery();
            case Select -> query = new SelectQuery();
        }
    }

    private String getFieldNames(Class<? extends DatabaseEntity> clazz, String fields) {
        String fieldPrefix = clazz.getSimpleName();
        List<FieldScan> list = ComponentLoader.get(DatabaseEntityMapper.class).getFieldScansForEntityClass(clazz);
        if (fields != null) {
            for (FieldScan fieldScan : list) {
                fields = fields.replaceAll("\\$" + fieldScan.column().value(), fieldPrefix + "." + fieldScan.column().value());
            }
        } else {
            StringBuilder builder = new StringBuilder();
            for (FieldScan fieldScan : list) {
                builder.append(builder.length() > 0 ? "," : "").append(fieldPrefix).append(".").append(fieldScan.column().value());
            }
            fields = builder.toString();
        }

        return fields;
    }

    /**
     * @param clazz
     * @param fields
     * @return
     * @deprecated Not implemented yet
     */
    @Deprecated
    public QueryBuilder select(Class<? extends DatabaseEntity> clazz, String fields) {
        setQueryType(QueryType.Select);
        ComponentLoader.get(DatabaseEntityMapper.class).mapEntityClass(clazz);
        fields = getFieldNames(clazz, fields);
        SelectQuery selectQuery = (SelectQuery) query;
        selectQuery.getSelectBuilder().append(selectQuery.getSelectBuilder().length() > 0 ? "," : "").append(fields);
        return this;
    }

    /**
     * @param clazz
     * @return
     * @deprecated Not implemented yet
     */
    @Deprecated
    public QueryBuilder select(Class<? extends DatabaseEntity> clazz) {
        return select(clazz, null);
    }

    /**
     * @param entityName
     * @param condition
     * @return
     * @deprecated Not implemented yet
     */
    @Deprecated
    public QueryBuilder where(String entityName, String condition) {
        return this;
    }

    /**
     * @param clazz
     * @param condition
     * @return
     * @deprecated Not implemented yet
     */
    @Deprecated
    public QueryBuilder where(Class<? extends DatabaseEntity> clazz, String condition) {
        return where(clazz.getSimpleName(), condition);
    }

    /**
     * @param clazz
     * @param condition
     * @return
     * @deprecated Not implemented yet
     */
    @Deprecated
    public QueryBuilder join(Class<? extends DatabaseEntity> clazz, String condition) {

        return this;
    }

    public QueryBuilder rawDQL(String queryString) {
        setQueryType(QueryType.RawDQL);
        ((RawDQLQuery) query).setQueryString(queryString);
        return this;
    }

    public QueryBuilder rawDMLOrDDL(String queryString) {
        setQueryType(QueryType.RawDMLOrDDL);
        ((RawDMLOrDDLQuery) query).setQueryString(queryString);
        return this;
    }

    public QueryBuilder statementType(int statementType) {
        if (!(query instanceof RawDMLOrDDLQuery rawDMLOrDDLQuery))
            return this;
        rawDMLOrDDLQuery.setStatementType(statementType);
        return this;
    }

    /**
     * @return
     * @deprecated Not implemented yet
     */
    @Deprecated
    public QueryBuilder fullFetch() {
        query.lazyFetch = false;
        return this;
    }

    public Query build() {
        return query;
    }

}
