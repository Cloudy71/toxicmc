/*
  User: Cloudy
  Date: 08/01/2022
  Time: 19:38
*/

package cz.cloudy.minecraft.core.database.fetch_data;

import cz.cloudy.minecraft.core.database.interfaces.IFetchData;

import java.util.List;
import java.util.Map;

/**
 * @author Cloudy
 */
public class MysqlFetchData
        implements IFetchData {
    private final String              selectQuery;
    private final String              fromQuery;
    private final List<String>        joinQuery;
    private final Map<String, String> translationMap;

    public MysqlFetchData(String selectQuery, String fromQuery, List<String> joinQuery, Map<String, String> translationMap) {
        this.selectQuery = selectQuery;
        this.fromQuery = fromQuery;
        this.joinQuery = joinQuery;
        this.translationMap = translationMap;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public String getFromQuery() {
        return fromQuery;
    }

    public List<String> getJoinQuery() {
        return joinQuery;
    }

    public Map<String, String> getTranslationMap() {
        return translationMap;
    }
}
