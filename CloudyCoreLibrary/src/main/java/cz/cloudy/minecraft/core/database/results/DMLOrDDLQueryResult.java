/*
  User: Cloudy
  Date: 08/01/2022
  Time: 02:08
*/

package cz.cloudy.minecraft.core.database.results;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.database.QueryResult;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Cloudy
 */
public class DMLOrDDLQueryResult
        extends QueryResult {

    private final Statement statement;
    private final int       result;

    public DMLOrDDLQueryResult(Statement statement, int result) {
        this.statement = statement;
        this.result = result;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public List<String> getColumns() {
        return ImmutableList.of();
    }

    @Override
    public List<List<Object>> getDataTable() {
        return ImmutableList.of(getData(0));
    }

    @Override
    public List<Object> getData(int rowNumber) {
        if (rowNumber == 0)
            return ImmutableList.of(result);
        return ImmutableList.of();
    }

    @Override
    public Map<String, Object> getDataMap(int rowNumber) {
        if (rowNumber == 0)
            return ImmutableMap.of("result", result);
        return ImmutableMap.of();
    }

    @Override
    public List<Map<String, Object>> getDataMapTable() {
        return ImmutableList.of(getDataMap(0));
    }

    public Statement getStatement() {
        return statement;
    }

    public int getResult() {
        return result;
    }
}
