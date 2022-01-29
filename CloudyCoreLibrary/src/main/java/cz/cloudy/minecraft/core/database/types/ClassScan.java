/*
  User: Cloudy
  Date: 07/01/2022
  Time: 21:57
*/

package cz.cloudy.minecraft.core.database.types;

import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.Table;

/**
 * @author Cloudy
 */
public class ClassScan {
    private final Class<? extends DatabaseEntity> clazz;
    private final Table                           table;

    private boolean constructed = false;

    public ClassScan(Class<? extends DatabaseEntity> clazz, Table table) {
        this.clazz = clazz;
        this.table = table;
    }

    public Class<? extends DatabaseEntity> clazz() {
        return clazz;
    }

    public Table table() {
        return table;
    }

    public boolean isConstructed() {
        return constructed;
    }

    public void setConstructed(boolean constructed) {
        this.constructed = constructed;
    }
}
