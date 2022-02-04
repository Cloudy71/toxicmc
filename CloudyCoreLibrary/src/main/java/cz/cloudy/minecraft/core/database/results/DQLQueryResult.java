/*
  User: Cloudy
  Date: 08/01/2022
  Time: 02:08
*/

package cz.cloudy.minecraft.core.database.results;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.database.QueryResult;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cloudy
 */
public class DQLQueryResult
        extends QueryResult {
    private static final Logger logger = LoggerFactory.getLogger(DQLQueryResult.class);

    private final ResultSet resultSet;

    private final List<String>       columns;
    private final List<List<Object>> dataTable;

    private List<Map<String, Object>> processedDataMap;

    public DQLQueryResult(ResultSet resultSet) {
        this.resultSet = resultSet;
        this.columns = new ArrayList<>();
        this.dataTable = new ArrayList<>();
        try {
            processResultSet();
        } catch (SQLException e) {
            logger.error("An error occurred during processing result set", e);
        }
    }

    private void processResultSet() throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            columns.add(metaData.getColumnLabel(i + 1));
        }

        while (resultSet.next()) {
            List<Object> data = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                data.add(resultSet.getObject(i + 1));
            }
            dataTable.add(data);
        }
    }

    @Override
    public int getRowCount() {
        return dataTable.size();
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public List<String> getColumns() {
        return columns;
    }

    @Override
    public List<List<Object>> getDataTable() {
        return dataTable;
    }

    @Override
    public List<Object> getData(int rowNumber) {
        Preconditions.checkState(dataTable.size() > rowNumber);
        return dataTable.get(rowNumber);
    }

    @Override
    public Map<String, Object> getDataMap(int rowNumber) {
        Preconditions.checkState(dataTable.size() > rowNumber);
        Map<String, Object> map = new HashMap<>();
        List<Object> data = getData(rowNumber);
        int size = data.size();
        for (int i = 0; i < size; i++) {
            map.put(columns.get(i), data.get(i));
        }
        return map;
    }

    public List<Map<String, Object>> getDataMapTable() {
        if (processedDataMap != null)
            return processedDataMap;
        List<Map<String, Object>> data = new ArrayList<>();
        for (List<Object> dataList : dataTable) {
            Map<String, Object> map = new HashMap<>();
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                map.put(columns.get(i), dataList.get(i));
            }
            data.add(map);
        }
        processedDataMap = data;
        return data;
    }
}
