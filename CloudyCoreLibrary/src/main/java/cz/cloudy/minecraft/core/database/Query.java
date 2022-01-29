/*
  User: Cloudy
  Date: 07/01/2022
  Time: 03:38
*/

package cz.cloudy.minecraft.core.database;

import cz.cloudy.minecraft.core.database.enums.QueryType;

/**
 * @author Cloudy
 */
public abstract class Query {
    protected QueryType queryType;
    protected boolean   lazyFetch     = true;

    public static QueryBuilder builder() {
        return new QueryBuilder();
    }

    protected Query(QueryType queryType) {
        this.queryType = queryType;
    }

    public abstract String provideQueryString();

    public QueryType getQueryType() {
        return queryType;
    }

    public boolean isLazyFetch() {
        return lazyFetch;
    }
}
