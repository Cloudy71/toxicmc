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
import cz.cloudy.minecraft.core.game.ChestUtils;
import cz.cloudy.minecraft.messengersystem.MessengerComponent;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.protection.pojo.ChestProtection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Cloudy
 */
@Component
public class ChestProtectionComponent
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(ChestProtectionComponent.class);

    @Component
    private Database database;

    @Component
    private DataTransformer dataTransformer;

    @Component
    private MessengerComponent messenger;

    @Component
    private ChestUtils chestUtils;

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
            protection.getBlock().setMetadata(ToxicConstants.CHEST_PROTECTION, new FixedMetadataValue(getPlugin(), protection));
        }
        logger.info("Loaded {} chest protections", protections.size());
    }

    private Location getChestLocation(Player player) {
        Block block = player.getTargetBlock(4);
        if (block == null)
            return null;
        Block chestBlock = chestUtils.getBaseBlock(block);
        if (chestBlock == null)
            return null;
        return chestBlock.getLocation();
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
            return new InfoCommandResponse("Nemíříš na žádnou truhlu.");

        Block block = chestLocation.getBlock();
        if (block.hasMetadata(ToxicConstants.CHEST_PROTECTION)) {
            ChestProtection chestProtection = (ChestProtection) block.getMetadata(ToxicConstants.CHEST_PROTECTION).get(0).value();
            Preconditions.checkNotNull(chestProtection, "ChestProtection is null");
            if (chestProtection.getOwner() == null || chestProtection.getOwner().getOfflinePlayer().getUniqueId() != data.getPlayer().getUniqueId())
                return new InfoCommandResponse("Tato truhla je již uzamčena někým jiným.");
            return new InfoCommandResponse("Tato truhla je již uzamčena tebou.");
        }

        ChestProtection chestProtection = new ChestProtection();
        chestProtection.setOwner(messenger.getUserAccount(data.getPlayer()));
        chestProtection.setBlock(block);
        chestProtection.save();
        protections.add(chestProtection);
        block.setMetadata(ToxicConstants.CHEST_PROTECTION, new FixedMetadataValue(getPlugin(), chestProtection));

        return new InfoCommandResponse("Truhla byla úspěšně uzamčena.");
    }

    @EventHandler
    private void onBlockBreakEvent(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Chest))
            return;
        Block chestBlock = chestUtils.getBaseBlock(e.getBlock());
        Preconditions.checkNotNull(chestBlock);
        if (!chestBlock.hasMetadata(ToxicConstants.CHEST_PROTECTION))
            return;

        ChestProtection protection = (ChestProtection) chestBlock.getMetadata(ToxicConstants.CHEST_PROTECTION).get(0).value();
        Preconditions.checkNotNull(protection, "ChestProtection is null");
        if (!protection.getOwner().getUuid().equals(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }


        chestBlock.removeMetadata(ToxicConstants.CHEST_PROTECTION, getPlugin());
        protection.delete();
        protections.remove(protection);
        e.getPlayer().sendMessage(ChatColor.AQUA + "Protekce byla odebrána.");
    }

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent e) {
        Block chestBlock;
        if (!e.hasBlock() || e.getClickedBlock() == null || (chestBlock = chestUtils.getBaseBlock(e.getClickedBlock())) == null ||
            !chestBlock.hasMetadata(ToxicConstants.CHEST_PROTECTION))
            return;

        ChestProtection protection = (ChestProtection) chestBlock.getMetadata(ToxicConstants.CHEST_PROTECTION).get(0).value();
        Preconditions.checkNotNull(protection, "ChestProtection is null");
        if (!protection.isLocked() || protection.getOwner().getUuid().equals(e.getPlayer().getUniqueId()))
            return;

        e.setCancelled(true);
        e.getPlayer().sendMessage(ChatColor.AQUA + "Tato truhla je uzamčená.");
    }
}
