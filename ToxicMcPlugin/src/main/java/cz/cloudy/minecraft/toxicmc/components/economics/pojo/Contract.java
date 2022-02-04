/*
  User: Cloudy
  Date: 31/01/2022
  Time: 02:54
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Table("contract")
public class Contract
        extends DatabaseEntity {

    @Column("uuid_from")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID uuidFrom;

    @Column("uuid_to")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID uuidTo;

    @Column("amount")
    protected int amount;

    @Column("date_from")
    @Default("NOW()")
    protected ZonedDateTime dateFrom;

    @Column("date_to")
    @Default("NOW()")
    protected ZonedDateTime dateTo;

    public UUID getUuidFrom() {
        return uuidFrom;
    }

    public void setUuidFrom(UUID uuidFrom) {
        this.uuidFrom = uuidFrom;
    }

    public UUID getUuidTo() {
        return uuidTo;
    }

    public void setUuidTo(UUID uuidTo) {
        this.uuidTo = uuidTo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ZonedDateTime getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(ZonedDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    public ZonedDateTime getDateTo() {
        return dateTo;
    }

    public void setDateTo(ZonedDateTime dateTo) {
        this.dateTo = dateTo;
    }
}
