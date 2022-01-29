/*
  User: Cloudy
  Date: 08/01/2022
  Time: 01:49
*/

package cz.cloudy.minecraft.core.database.queries;

import cz.cloudy.minecraft.core.database.Query;
import cz.cloudy.minecraft.core.database.enums.QueryType;

import java.sql.Statement;

/**
 * @author Cloudy
 */
public class RawDMLOrDDLQuery
        extends Query {
    private   String queryString;
    protected int    statementType = Statement.NO_GENERATED_KEYS;

    public RawDMLOrDDLQuery() {
        super(QueryType.RawDMLOrDDL);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public int getStatementType() {
        return statementType;
    }

    public void setStatementType(int statementType) {
        this.statementType = statementType;
    }

    @Override
    public String provideQueryString() {
        return queryString;
    }
}
