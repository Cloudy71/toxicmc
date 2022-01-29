/*
  User: Cloudy
  Date: 06/01/2022
  Time: 18:39
*/

package cz.cloudy.minecraft.core;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.DatabaseEngine;
import cz.cloudy.minecraft.core.database.types.DatabaseConnectionData;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Cloudy
 */
public abstract class CorePlugin
        extends JavaPlugin {
    private ComponentLoader componentLoader;

    @Override
    public final void onEnable() {
        super.onEnable();

        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

        componentLoader = new ComponentLoader();
        componentLoader.readComponentScansFromClass(getClass());
        onStart();
        componentLoader.loadAllComponents(this);
        onLoaded();

        doPostProcess();
    }

    @Override
    public final void onDisable() {
        super.onDisable();
        onUnloaded();
    }

    @Override
    public final void onLoad() {
        super.onLoad();
    }

    public abstract void onStart();

    public abstract void onLoaded();

    public abstract void onUnloaded();

    public void doPostProcess() {
        Database database = ComponentLoader.get(Database.class);
        if (database.getProcessor() == null) {
            boolean coreDatabase = getCoreDatabaseAttribute();
            if (coreDatabase) {
                database.initializeProcessor(getDatabaseConnectionData());
            }
        }
        if (database.getProcessor() != null) {
            database.getProcessor().buildTableStructure();
            database.getProcessor().buildConstraintStructure();
        }
        componentLoader.startComponents(this);
    }

    protected ComponentLoader getComponentLoader() {
        return componentLoader;
    }

    public boolean getCoreDatabaseAttribute() {
        return getConfig().getBoolean("core.database", false);
    }

    /**
     * @return
     * @deprecated Move somewhere else
     */
    @Deprecated(forRemoval = true)
    public DatabaseConnectionData getDatabaseConnectionData() {
        String engineString = getConfig().getString("database.engine", "mysql");
        DatabaseEngine engine = DatabaseEngine.resolveEngine(engineString);

        if (engine == null)
            return null;

        String host = getConfig().getString("database.host", null);
        int port = getConfig().getInt("database.port", 0);
        String user = getConfig().getString("database.user", null);
        String pass = getConfig().getString("database.pass", null);
        String db = getConfig().getString("database.db", null);

        return ComponentLoader.get(Database.class).generateDatabaseConnectionDataFromAttributes(
                engine,
                host,
                port,
                user,
                pass,
                db
        );
    }
}
