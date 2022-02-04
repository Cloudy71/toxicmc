/*
  User: Cloudy
  Date: 06/01/2022
  Time: 19:01
*/

package cz.cloudy.minecraft.core.database;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.CoreRunnerPlugin;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.database.annotation.AutoIncrement;
import cz.cloudy.minecraft.core.database.annotation.Column;
import cz.cloudy.minecraft.core.database.annotation.PrimaryKey;
import cz.cloudy.minecraft.core.database.annotation.Size;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import org.bukkit.Bukkit;

/**
 * @author Cloudy
 */
public abstract class DatabaseEntity {
    @Column("id")
    @PrimaryKey
    @AutoIncrement
    @Size(Size.IDSize)
    private long id = -1;

    protected boolean    replicated = false;
    protected FetchLevel fetchLevel = FetchLevel.Full;

    protected DatabaseEntity() {
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    /**
     * If current object also exists on database server
     *
     * @return True if current object exists on database server
     */
    public boolean isReplicated() {
        return replicated;
    }

    /**
     * Current object's fetch level
     *
     * @return Full if object has been fully fetched, Primitive only if non-lazy object has been fetched and None if object has not been fetched yet
     */
    public FetchLevel getFetchLevel() {
        return fetchLevel;
    }

    /**
     * If current object is at least primitively fetched.
     * This will always return false if the object is generated only from selected fields instead of all fields
     *
     * @return True if object is at least primitively fetched
     */
    public boolean isFetched() {
        return fetchLevel == FetchLevel.Full || fetchLevel == FetchLevel.Primitive;
    }

    /**
     * If current object is fully fetched.
     * This will always return false if the object is generated only from selected fields instead of all fields
     *
     * @return True if object is fully fetched
     */
    public boolean isFullFetched() {
        return fetchLevel == FetchLevel.Full;
    }

    /**
     * Saves all object data by its primary key.
     * Keep in mind that object must be fully loaded except for foreign objects
     * This method also does not save foreign objects since they are mapped only as ID in the database
     */
    public void save() {
        Preconditions.checkState(isFetched(), "Object must be loaded first");
        ComponentLoader.get(Database.class).saveEntity(this);
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(
                CoreRunnerPlugin.singleton,
                this::save
        );
    }

    /**
     * Saves all data as save does, however this also saves all foreign key entities if they are loaded
     */
    public void fullSave() {
        Preconditions.checkState(isFetched(), "Object must be loaded first");
        ComponentLoader.get(Database.class).fullSaveEntity(this);
    }

    /**
     * This fetches all object data by its primary id
     *
     * @return Fully loaded current entity object
     */
    public DatabaseEntity load() {
        if (fetchLevel == FetchLevel.Full)
            return this;
        ComponentLoader.get(Database.class).loadEntity(this, FetchLevel.Full);
        fetchLevel = FetchLevel.Full;
        return this;
    }

    /**
     * Force reloads current entity object
     *
     * @return Reloaded current entity object
     */
    public DatabaseEntity reload() {
        fetchLevel = FetchLevel.None;
        ComponentLoader.get(Database.class).loadEntity(this, FetchLevel.Full);
        fetchLevel = FetchLevel.Full;
        return this;
    }

    /**
     * Deletes this entity from database
     */
    public void delete() {
        Preconditions.checkState(replicated, "Entity is not replicated");
        ComponentLoader.get(Database.class).deleteEntity(this);
    }

    public void deleteAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(
                CoreRunnerPlugin.singleton,
                this::delete
        );
    }
}
