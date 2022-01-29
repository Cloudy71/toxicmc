/*
  User: Cloudy
  Date: 07/01/2022
  Time: 14:27
*/

package cz.cloudy.minecraft.core.database.queries;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.database.Query;
import cz.cloudy.minecraft.core.database.enums.QueryType;

/**
 * @author Cloudy
 * @deprecated Not implemented yet
 */
@Deprecated
public class SelectQuery
        extends Query {
    private StringBuilder selectBuilder;
    private StringBuilder fromBuilder;
    private StringBuilder joinAllBuilder;
    private StringBuilder whereBuilder;

    public SelectQuery() {
        super(QueryType.Select);

        selectBuilder = new StringBuilder();
        fromBuilder = new StringBuilder();
        joinAllBuilder = new StringBuilder();
        whereBuilder = new StringBuilder();
    }

    public StringBuilder getSelectBuilder() {
        return selectBuilder;
    }

    public StringBuilder getFromBuilder() {
        return fromBuilder;
    }

    public StringBuilder getJoinAllBuilder() {
        return joinAllBuilder;
    }

    public StringBuilder getWhereBuilder() {
        return whereBuilder;
    }

    @Override
    public String provideQueryString() {
        Preconditions.checkState(selectBuilder.length() == 0);
        Preconditions.checkState(fromBuilder.length() == 0);
        return "SELECT " + getSelectBuilder().toString() + "\n" +
               "FROM " + getFromBuilder().toString() + "\n" +
               getJoinAllBuilder().toString() + "\n" +
               (getWhereBuilder().length() > 0 ? "WHERE " + getWhereBuilder().toString() : "");
    }
}
