/*
  User: Cloudy
  Date: 28/01/2022
  Time: 18:59
*/

package cz.cloudy.minecraft.toxicmc.components.protection;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.CheckCondition;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.data_transforming.DataTransformer;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.messengersystem.MessengerComponent;
import cz.cloudy.minecraft.toxicmc.components.protection.pojo.ChestProtection;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Cloudy
 */
@Component
public class ChestProtectionComponent
        implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(ChestProtectionComponent.class);

    @Component
    private Database database;

    @Component
    private DataTransformer dataTransformer;

    @Component
    private MessengerComponent messenger;

    private Set<ChestProtection> protections;

    @Override
    public void onStart() {
        protections = database.findEntities(
                ChestProtection.class,
                null,
                null,
                FetchLevel.Primitive
        );
        for (ChestProtection protection : protections) {
            protection.getBlock().setMetadata("chestProtection", new FixedMetadataValue(getPlugin(), protection));
        }
        logger.info("Loaded {} chest protections", protections.size());
    }

    private Location getChestLocation(Player player) {
        Block block = player.getTargetBlock(4);
        if (block == null)
            return null;
        if (block.getState() instanceof Chest chest) {
            return chest.getBlockInventory().getHolder().getInventory().getLocation();
//            if (chest.getBlockInventory().getHolder() instanceof Chest) {
//                return chest.getLocation();
//            } else if (chest.getBlockInventory().getHolder() instanceof DoubleChest doubleChest) {
//                return doubleChest.getLocation();
//            }
        }
        return null;
    }

    private Block getChestBlock(Block block) {
        if (!(block.getState() instanceof Chest chest))
            return null;

        return chest.getBlockInventory().getHolder().getInventory().getLocation().getBlock();
    }

    @CommandListener("chest")
    @CheckCondition(CheckCondition.SENDER_IS_PLAYER)
    private Object onChestCommand(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");

        String action = data.arguments()[0];
        CommandData newCommandData = new CommandData(
                data.sender(),
                data.command(),
                Arrays.copyOfRange(data.arguments(), 1, data.arguments().length)
        );

        if (action.equals("lock"))
            return onChestLock(newCommandData);

        return new InfoCommandResponse("Neznámý požadavek.");
    }

    private Object onChestLock(CommandData data) {
        Location chestLocation = getChestLocation(data.getPlayer());
        if (chestLocation == null)
            return new InfoCommandResponse("Nemíříš na žádnou chest.");

        Block block = chestLocation.getBlock();
        if (block.hasMetadata("chestProtection")) {
            ChestProtection chestProtection = (ChestProtection) block.getMetadata("chestProtection").get(0).value();
            Preconditions.checkNotNull(chestProtection, "ChestProtection is null");
            if (chestProtection.getOwner() == null || chestProtection.getOwner().getOfflinePlayer().getUniqueId() != data.getPlayer().getUniqueId())
                return new InfoCommandResponse("Tato chestka je již uzamčena někým jiným.");
            return new InfoCommandResponse("Tato chestka je již uzamčena tebou.");
        }

        ChestProtection chestProtection = new ChestProtection();
        chestProtection.setOwner(messenger.getUserAccount(data.getPlayer()));
        chestProtection.setBlock(block);
        chestProtection.save();
        protections.add(chestProtection);
        block.setMetadata("chestProtection", new FixedMetadataValue(getPlugin(), chestProtection));

        return new InfoCommandResponse("Chest byla úspěšně uzamčena.");
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Chest))
            return;
        Block chestBlock = getChestBlock(e.getBlock());
        Preconditions.checkNotNull(chestBlock);
        if (!chestBlock.hasMetadata("chestProtection"))
            return;

        ChestProtection protection = (ChestProtection) chestBlock.getMetadata("chestProtection").get(0).value();
        Preconditions.checkNotNull(protection, "ChestProtection is null");
        if (protection.getOwner().getUuid() != e.getPlayer().getUniqueId()) {
            e.setCancelled(true);
            return;
        }

        // TODO: ....
        protection.delete();
        protections.remove(protection);
    }
}
