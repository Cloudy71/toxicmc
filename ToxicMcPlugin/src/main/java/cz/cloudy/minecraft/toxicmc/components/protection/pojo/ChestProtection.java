/*
  User: Cloudy
  Date: 28/01/2022
  Time: 19:32
*/

package cz.cloudy.minecraft.toxicmc.components.protection.pojo;

import com.cronutils.utils.Preconditions;
import cz.cloudy.minecraft.core.data_transforming.transformers.BlockLocationToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Cloudy
 */
@Table("chest_protection")
public class ChestProtection
        extends DatabaseEntity {

    @Column("owner")
    @ForeignKey
    @Lazy
    @Null
    protected UserAccount owner;

    @Column("block")
    @Transform(BlockLocationToStringTransformer.class)
    @Index(unique = true)
    @Size(64)
    protected Block block;

    @Column("status")
    @Default("1")
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

    @Join(table = ChestShare.class, where = "chest_protection.id = :id")
    public Set<ChestShare> getShares() {
        return null;
    }

    public boolean isSharedWith(Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        for (ChestShare share : getShares()) {
            if (player.getUniqueId().equals(share.getUser().getUuid()))
                return true;
        }

        return false;
    }
}
