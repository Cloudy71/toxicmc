/*
  User: Cloudy
  Date: 31/01/2022
  Time: 02:44
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;
import cz.cloudy.minecraft.toxicmc.components.economics.transformers.PaymentTypeToByteTransformer;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Table("expense")
public class Expense
        extends DatabaseEntity {

    @Column("uuid")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID uuid;

    @Column("amount")
    protected int amount;

    @Column("expense_type")
    @Transform(PaymentTypeToByteTransformer.class)
    protected PaymentType expenseType;

    @Column("date_created")
    @Default("NOW()")
    protected ZonedDateTime dateCreated;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public PaymentType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(PaymentType expenseType) {
        this.expenseType = expenseType;
    }
}
