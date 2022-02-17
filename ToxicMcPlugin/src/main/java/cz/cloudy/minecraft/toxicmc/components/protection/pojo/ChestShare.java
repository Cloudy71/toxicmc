package cz.cloudy.minecraft.toxicmc.components.protection.pojo;

import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.Column;
import cz.cloudy.minecraft.core.database.annotation.ForeignKey;
import cz.cloudy.minecraft.core.database.annotation.Lazy;
import cz.cloudy.minecraft.core.database.annotation.Table;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;

/**
 * @author Háně
 */
@Table("chest_share")
public class ChestShare extends DatabaseEntity {

    @Column("chest_protection")
    @ForeignKey
    protected ChestProtection chestProtection;

    @Column("user")
    @ForeignKey
    @Lazy
    protected UserAccount user;

    public ChestProtection getChestProtection() {
        return chestProtection;
    }

    public void setChestProtection(ChestProtection chestProtection) {
        this.chestProtection = chestProtection;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }
}
