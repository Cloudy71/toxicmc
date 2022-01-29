/*
  User: Cloudy
  Date: 22/01/2022
  Time: 21:35
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.Column;
import cz.cloudy.minecraft.core.database.annotation.Size;
import cz.cloudy.minecraft.core.database.annotation.Table;
import cz.cloudy.minecraft.core.database.annotation.Transform;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Table("bank_transaction")
public class BankTransaction
        extends DatabaseEntity {
    @Column("from_uuid")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID fromUuid;

    @Column("to_uuid")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID toUuid;

    @Column("amount")
    protected int amount;

    @Column("message")
    @Size(64)
    protected String message;

    @Column("date_processed")
    protected ZonedDateTime dateTime;

    public UUID getFromUuid() {
        return fromUuid;
    }

    public void setFromUuid(UUID fromUuid) {
        this.fromUuid = fromUuid;
    }

    public UUID getToUuid() {
        return toUuid;
    }

    public void setToUuid(UUID toUuid) {
        this.toUuid = toUuid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }
}