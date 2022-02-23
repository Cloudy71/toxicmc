/*
  User: Cloudy
  Date: 22/02/2022
  Time: 21:55
*/

package cz.cloudy.minecraft.toxicmc.components.minigames.games;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.game.BulkWorldBuilder;
import cz.cloudy.minecraft.toxicmc.components.minigames.enums.MiniGameStatus;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * @author Cloudy
 */
public abstract class MiniGame {
    public abstract String getName();

    public abstract String getDescription();

    public abstract byte getMaxPlayers();

    public abstract World getWorld();

    private MiniGameStatus status = MiniGameStatus.Opened;

    public MiniGameStatus getStatus() {
        return status;
    }

    protected void setStatus(MiniGameStatus status) {
        this.status = status;
    }

    public Vector worldCopyPosition() {
        return new Vector(50000, 0, 50000);
    }

    public Vector worldCopySize() {
        return new Vector(256, 64, 256);
    }

    public void rebuildMap() {
        ComponentLoader.get(BulkWorldBuilder.class).copyPaste(
                getWorld(),
                new BoundingBox(
                        worldCopyPosition().getX(),
                        worldCopyPosition().getY(),
                        worldCopyPosition().getZ(),
                        worldCopySize().getX(),
                        worldCopySize().getY(),
                        worldCopySize().getZ()
                ), // TODO: Create bounding box
                getWorld(),
                new Vector(0, 0, 0)
        );
    }
}
