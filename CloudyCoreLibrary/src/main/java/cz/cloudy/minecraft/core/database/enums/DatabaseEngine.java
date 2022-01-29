/*
  User: Cloudy
  Date: 07/01/2022
  Time: 21:26
*/

package cz.cloudy.minecraft.core.database.enums;

import cz.cloudy.minecraft.core.database.interfaces.IDatabaseProcessor;
import cz.cloudy.minecraft.core.database.processors.MysqlDatabaseProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cloudy
 */
public class DatabaseEngine {
    private static final Map<String, DatabaseEngine> engineMap = new HashMap<>();

    public static final DatabaseEngine MySQL = new DatabaseEngine("mysql", "jdbc:mysql://%host:%port/%db?autoReconnect=true", MysqlDatabaseProcessor.class);

    private final String                                 name;
    private final String                                 expression;
    private final Class<? extends IDatabaseProcessor<?>> engineClass;

    protected DatabaseEngine(String name, String resolver, Class<? extends IDatabaseProcessor<?>> engineClass) {
        this.name = name;
        this.expression = resolver;
        this.engineClass = engineClass;
        engineMap.put(name, this);
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public Class<? extends IDatabaseProcessor<?>> getEngineClass() {
        return engineClass;
    }

    public String resolveUrl(String host, int port, String db) {
        return getExpression()
                .replaceAll("%host", host)
                .replaceAll("%port", Integer.toString(port))
                .replaceAll("%db", db);
    }

    public static DatabaseEngine resolveEngine(String name) {
        return engineMap.get(name);
    }
}
