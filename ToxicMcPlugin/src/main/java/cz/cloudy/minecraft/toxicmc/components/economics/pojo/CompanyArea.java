/*
  User: Cloudy
  Date: 09/02/2022
  Time: 03:22
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.Int2ToStringTransform;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.types.Int2;
import org.bukkit.util.Vector;

import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
@Table("company_area")
public class CompanyArea
        extends DatabaseEntity {

    @Column("company")
    @ForeignKey
    @Index
    protected Company company;

    @Column("start")
    @Transform(Int2ToStringTransform.class)
    protected Int2 start;

    @Column("end")
    @Transform(Int2ToStringTransform.class)
    protected Int2 end;

    @Column("date_created")
    @Default("NOW()")
    protected ZonedDateTime dateCreated;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Int2 getStart() {
        return start;
    }

    public void setStart(Int2 start) {
        this.start = start;
    }

    public Int2 getEnd() {
        return end;
    }

    public void setEnd(Int2 end) {
        this.end = end;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    // ======================================================================

    public boolean isVectorInArea(Vector vector) {
        return vector.getX() >= getStart().getX() && vector.getZ() >= getStart().getY() ||
               vector.getX() <= getEnd().getX() || vector.getZ() <= getEnd().getY();
    }
}
