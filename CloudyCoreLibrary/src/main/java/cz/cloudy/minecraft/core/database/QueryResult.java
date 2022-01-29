/*
  User: Cloudy
  Date: 08/01/2022
  Time: 01:05
*/

package cz.cloudy.minecraft.core.database;

import java.util.List;
import java.util.Map;

/**
 * @author Cloudy
 */
public abstract class QueryResult {
    public abstract int getRowCount();

    public abstract List<String> getColumns();

    public abstract List<List<Object>> getDataTable();

    public abstract List<Object> getData(int rowNumber);

    public abstract Map<String, Object> getDataMap(int rowNumber);

    public abstract List<Map<String, Object>> getDataMapTable();
}
