/*
  User: Cloudy
  Date: 22/01/2022
  Time: 17:16
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;

import java.util.UUID;

/**
 * @author Cloudy
 */
@Table("bank_account")
public class BankAccount
        extends DatabaseEntity {
    @Column("uuid")
    @PrimaryKey
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID uuid;

    @Column("balance")
    @Default("0")
    protected long balance;

    @Column("dept")
    @Default("0")
    protected long dept;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getDept() {
        return dept;
    }

    public void setDept(long dept) {
        this.dept = dept;
    }

    // ======================================
    public long getTotalBalance() {
        return balance - dept;
    }
}
