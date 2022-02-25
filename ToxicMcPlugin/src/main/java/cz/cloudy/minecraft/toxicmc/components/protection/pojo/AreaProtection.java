/*
  User: Cloudy
  Date: 24/02/2022
  Time: 00:48
*/

package cz.cloudy.minecraft.toxicmc.components.protection.pojo;

import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;

/**
 * @author Cloudy
 */
@Table("area_protection")
public class AreaProtection
        extends DatabaseEntity {

    @Column("owner")
    @ForeignKey
    @Lazy
    @Null
    protected UserAccount owner;

    @Column("start_x")
    protected int startX;

    @Column("start_z")
    protected int startZ;

    @Column("end_x")
    protected int endX;

    @Column("end_z")
    protected int endZ;

    @Column("status")
    @Default("1")
    protected boolean locked;

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public void setStart(Int2 start) {
        this.startX = start.getX();
        this.startZ = start.getY();
    }

    public Int2 getStart() {
        return new Int2(startX, startZ);
    }

    public void setEnd(Int2 end) {
        this.endX = end.getX();
        this.endZ = end.getY();
    }

    public Int2 getEnd() {
        return new Int2(endX, endZ);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
