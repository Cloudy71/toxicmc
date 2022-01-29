/*
  User: Cloudy
  Date: 26/01/2022
  Time: 23:30
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;

/**
 * @author Cloudy
 */
@Table("player_employee")
public class PlayerEmployee
        extends Employee {
    public static final byte LEVEL_OWNER    = 100;
    public static final byte LEVEL_NEWCOMER = 0;
    public static final byte LEVEL_SUPPLIER = 1;
    public static final byte LEVEL_PICKER   = 2;
    public static final byte LEVEL_BUILDER  = 3;
    public static final byte LEVEL_MANAGER  = 4;

    @Column("employee")
    @ForeignKey
    @Index
    protected UserAccount employee;

    @Column("level")
    @Default("0")
    protected byte level;

    public UserAccount getEmployee() {
        return employee;
    }

    public void setEmployee(UserAccount employee) {
        this.employee = employee;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }
}
