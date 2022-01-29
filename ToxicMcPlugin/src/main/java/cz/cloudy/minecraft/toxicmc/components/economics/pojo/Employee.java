/*
  User: Cloudy
  Date: 22/01/2022
  Time: 22:11
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.toxicmc.components.economics.NpcLogic;
import cz.cloudy.minecraft.toxicmc.components.economics.NpcLogicToByteTransformer;

import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
@Table("employee")
public class Employee
        extends DatabaseEntity {

    @Column("company")
    @ForeignKey
    @Lazy
    protected Company company;

    @Column("name")
    @Size(32)
    @Null
    protected String name;

    @Column("npc_logic")
    @Transform(NpcLogicToByteTransformer.class)
    @Null
    protected Class<? extends NpcLogic> npcLogic;

    @Column("salary")
    @Default("0")
    protected int salary;

    @Column("date_employed")
    @Default("NOW()")
    protected ZonedDateTime dateEmployed;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends NpcLogic> getNpcLogic() {
        return npcLogic;
    }

    public void setNpcLogic(Class<? extends NpcLogic> npcLogic) {
        this.npcLogic = npcLogic;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public ZonedDateTime getDateEmployed() {
        return dateEmployed;
    }

    public void setDateEmployed(ZonedDateTime dateEmployed) {
        this.dateEmployed = dateEmployed;
    }
}
