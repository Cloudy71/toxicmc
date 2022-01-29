/*
  User: Cloudy
  Date: 28/01/2022
  Time: 19:32
*/

package cz.cloudy.minecraft.toxicmc.components.protection.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.BlockLocationToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import org.bukkit.block.Block;

/**
 * @author Cloudy
 */
@Table("chest_protection")
public class ChestProtection
        extends DatabaseEntity {

    @Column("owner")
    @ForeignKey
    @Lazy
    protected UserAccount owner;

    @Column("block")
    @Transform(BlockLocationToStringTransformer.class)
    @Index(unique = true)
    @Size(64)
    protected Block block;

    @Column("status")
    @Default("true")
    protected boolean locked;

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
