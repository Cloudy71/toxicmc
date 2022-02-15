/*
  User: Cloudy
  Date: 09/02/2022
  Time: 03:22
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cached;
import cz.cloudy.minecraft.core.data_transforming.transformers.Int2ToStringTransform;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.particles.ParticleJob;
import cz.cloudy.minecraft.core.particles.Particles;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.AreaType;
import cz.cloudy.minecraft.toxicmc.components.economics.transformers.AreaTypeToByteTransformer;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author Cloudy
 */
// TODO: Implement area types and area creation menu, so there are global areas and under them there are specific areas, like stock or shop
@Table("company_area")
public class CompanyArea
        extends DatabaseEntity {

    @Column("company")
    @ForeignKey
    @Index
    protected Company company;

    @Column("parent")
    @ForeignKey
    @Null
    @Index
    protected CompanyArea parent;

    @Column("start_x")
    protected int startX;

    @Column("start_z")
    protected int startZ;

    @Column("end_x")
    protected int endX;

    @Column("end_z")
    protected int endZ;

    @Column("date_created")
    @Default("NOW()")
    protected ZonedDateTime dateCreated;

    @Column("area_type")
    @Transform(AreaTypeToByteTransformer.class)
    @Index
    protected AreaType areaType;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CompanyArea getParent() {
        return parent;
    }

    public void setParent(CompanyArea parent) {
        this.parent = parent;
    }

    public Int2 getStart() {
        return new Int2(startX, startZ);
    }

    public void setStart(Int2 start) {
        this.startX = start.getX();
        this.startZ = start.getY();
    }

    public Int2 getEnd() {
        return new Int2(endX, endZ);
    }

    public void setEnd(Int2 end) {
        this.endX = end.getX();
        this.endZ = end.getY();
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public AreaType getAreaType() {
        return areaType;
    }

    public void setAreaType(AreaType areaType) {
        this.areaType = areaType;
    }

    // ======================================================================

    public boolean isVectorInArea(Vector vector) {
        return vector.getX() >= getStart().getX() && vector.getZ() >= getStart().getY() ||
                vector.getX() <= getEnd().getX() || vector.getZ() <= getEnd().getY();
    }

    public ParticleJob displayArea(World world, float y, int seconds) {
        Particles particles = ComponentLoader.get(Particles.class);
        Int2 start = getStart();
        Int2 end = getEnd();
        return particles.collection(
                particles.pulseLine(
                        world,
                        new Vector(start.getX(), y, start.getY()),
                        new Vector(end.getX() + 1, y, start.getY()),
                        areaType.getColor(),
                        (seconds * 20) / 5,
                        5
                ),
                particles.pulseLine(
                        world,
                        new Vector(start.getX(), y, start.getY()),
                        new Vector(start.getX(), y, end.getY() + 1),
                        areaType.getColor(),
                        .5f,
                        (seconds * 20) / 5,
                        5
                ),
                particles.pulseLine(
                        world,
                        new Vector(end.getX() + 1, y, start.getY()),
                        new Vector(end.getX() + 1, y, end.getY() + 1),
                        areaType.getColor(),
                        .5f,
                        (seconds * 20) / 5,
                        5
                ),
                particles.pulseLine(
                        world,
                        new Vector(start.getX(), y, end.getY() + 1),
                        new Vector(end.getX() + 1, y, end.getY() + 1),
                        areaType.getColor(),
                        .5f,
                        (seconds * 20) / 5,
                        5
                )
        );
    }

    @Join(table = CompanyArea.class, where = "parent.id = :id")
    public Set<CompanyArea> getSubAreas() {
        return null;
    }
}
