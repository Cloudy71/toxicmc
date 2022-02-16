/*
  User: Cloudy
  Date: 10/01/2022
  Time: 04:42
*/

package cz.cloudy.minecraft.messengersystem;

import cz.cloudy.minecraft.core.Constants;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.CheckCondition;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.hashing.Hashing;
import cz.cloudy.minecraft.core.hashing.HashingAlgorithm;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import cz.cloudy.minecraft.messengersystem.types.LoginEventData;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;

import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
@Component
public class MessengerComponent
        implements Listener, IComponent {
    private static final Logger logger = LoggerFactory.getLogger(MessengerComponent.class);

    public static final String hashSalt = "pwsalt12569";

    @Component
    private Database database;

    @Component
    private Hashing hashing;

    @Component
    private ChatComponent messenger;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UserAccount account = database.findEntity(UserAccount.class, event.getPlayer().getUniqueId());
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Vitej " + event.getPlayer().getName() + ".");
        if (account == null) {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Aktuálně na toto jméno není nikdo registrovaný.");
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Prosím, registruj se pomocí příkazu /register <heslo> <hesloznovu>");
        } else {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Aktuálně na toto jméno je někdo registrovaný.");
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Prosím, přihlaš se pomocí příkazu /login <heslo>");
        }
        event.joinMessage(null);
        event.getPlayer().setAllowFlight(true);
        event.getPlayer().setFlying(true);
        event.getPlayer().setInvisible(true);
        event.getPlayer().setSilent(true);
        event.getPlayer().setStarvationRate(Integer.MAX_VALUE);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        event.quitMessage(null);
        if (!event.getPlayer().hasMetadata(MessengerConstants.USER_ACCOUNT))
            return;


        event.getPlayer().removeMetadata(MessengerConstants.USER_ACCOUNT, getPlugin());
        messenger.removePlayerFromLogged(event.getPlayer());
        messenger.sendMessage(ChatColor.YELLOW + "Hráč " + event.getPlayer().getName() + " odešel.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void chatEvent(AsyncChatEvent event) {
        event.setCancelled(true);

        boolean isLogged = event.getPlayer().hasMetadata(MessengerConstants.USER_ACCOUNT);
        if (!isLogged)
            logger.info(ChatColor.GRAY + event.getPlayer().getName() + ": " + ((TextComponent) event.message()).content());
        if (!isLogged)
            return;

        UserAccount account = (UserAccount) event.getPlayer().getMetadata(MessengerConstants.USER_ACCOUNT).get(0).value();

        messenger.sendMessage(
                net.kyori.adventure.text.Component.text()
                                                  .content(
                                                          messenger.resolveColorByByte(account.getChatColor())
                                                          + "<"
                                                          + event.getPlayer().getName()
                                                          + "> "
                                                          + ChatColor.WHITE)
                                                  .append(event.message())
        );
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!messenger.isPlayerLogged(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBlockDestroy(BlockBreakEvent event) {
        if (!messenger.isPlayerLogged(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event) {
        if (!messenger.isPlayerLogged(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (!messenger.isPlayerLogged(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player p && !messenger.isPlayerLogged(p))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player p && !messenger.isPlayerLogged(p))
            event.setCancelled(true);
    }

    public UserAccount getUserAccount(Player player) {
        if (player.hasMetadata(MessengerConstants.USER_ACCOUNT))
            return (UserAccount) player.getMetadata(MessengerConstants.USER_ACCOUNT).get(0).value();
        return getOfflineUserAccount(player);
    }

    public UserAccount getOfflineUserAccount(OfflinePlayer player) {
        return database.findEntity(UserAccount.class, player.getUniqueId());
    }

    private void loginPlayer(Player player, UserAccount userAccount) {
        player.setMetadata(MessengerConstants.USER_ACCOUNT, new FixedMetadataValue(getPlugin(), userAccount));
        messenger.addPlayerToLogged(player);
        messenger.sendMessage(ChatColor.YELLOW + "Hráč " + player.getName() + " se připojil.");
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        player.setInvisible(false);
        player.setSilent(false);
        player.setStarvationRate(80);
        notifyActionListeners("onLogin", new LoginEventData(userAccount, player));
    }

    @CommandListener("register")
    @CheckCondition(CheckCondition.SENDER_IS_PLAYER)
    @CheckCondition(CheckCondition.ARGS_IS_2)
    private boolean onCommandRegister(CommandData data) {
        if (data.getPlayer().hasMetadata(MessengerConstants.USER_ACCOUNT)) {
            return false;
        }

        UserAccount account = database.findEntity(UserAccount.class, data.getPlayer().getUniqueId());
        if (account != null) {
            data.getPlayer().sendMessage(ChatColor.RED + "Toto jméno je již registrované.");
            return false;
        }

        String password = data.arguments()[0];
        String passwordRe = data.arguments()[1];

        if (!password.equals(passwordRe)) {
            data.getPlayer().sendMessage(ChatColor.RED + "Hesla se neshodují.");
            return false;
        } else if (password.length() < 5) {
            data.getPlayer().sendMessage(ChatColor.RED + "Délka textu musí být aspoň 5 znaků.");
            return false;
        }

        account = new UserAccount();
        account.setUuid(data.getPlayer().getUniqueId());
        account.setPassword(hashing.hashStringHex(password, HashingAlgorithm.SHA160, hashSalt));
        account.setPermissions((byte) 0);
        account.setChatColor((byte) 0);
        account.save();

        data.getPlayer().sendMessage(ChatColor.AQUA + "Registrace byla úspěšná.");
        loginPlayer(data.getPlayer(), account);

        return true;
    }

    @CommandListener("login")
    @CheckCondition(CheckCondition.SENDER_IS_PLAYER)
    @CheckCondition(CheckCondition.ARGS_IS_1)
    private boolean onCommandLogin(CommandData data) {
        if (data.getPlayer().hasMetadata(MessengerConstants.USER_ACCOUNT)) {
            return false;
        }

        UserAccount account = database.findEntity(UserAccount.class, data.getPlayer().getUniqueId());
        if (account == null) {
            data.getPlayer().sendMessage(ChatColor.YELLOW + "Na toto jméno není nikdo registrovaný.");
            data.getPlayer().sendMessage(ChatColor.YELLOW + "Prosím, registruj se pomocí příkazu /register <heslo> <hesloznovu>");
            return false;
        }

        String password = hashing.hashStringHex(data.arguments()[0], HashingAlgorithm.SHA160, hashSalt);

        if (!password.equals(account.getPassword())) {
            data.getPlayer().sendMessage(ChatColor.RED + "Hesla se neshodují.");
            return false;
        }

        account.setDateLastOnline(ZonedDateTime.now());
        account.save();
        data.getPlayer().sendMessage(ChatColor.AQUA + "Přihlašení bylo úspěšné.");
        loginPlayer(data.getPlayer(), account);

        return true;
    }

    @CommandListener("changepw")
//    @CheckCondition(CheckCondition.SENDER_IS_PLAYER)
//    @CheckCondition(CheckCondition.ARGS_IS_3)
    private Object onChangePassword(CommandData data) {
        if (data.arguments().length == 3) {
            if (!data.isPlayer() || !data.getPlayer().hasMetadata(MessengerConstants.USER_ACCOUNT)) {
                return false;
            }

            UserAccount account = database.findEntity(UserAccount.class, data.getPlayer().getUniqueId());

            String oldPassword = hashing.hashStringHex(data.arguments()[0], HashingAlgorithm.SHA160, hashSalt);

            if (!oldPassword.equals(account.getPassword()))
                return new ErrorCommandResponse("Staré heslo je špatně.");

            String newPassword = hashing.hashStringHex(data.arguments()[1], HashingAlgorithm.SHA160, hashSalt);
            String newRepeatedPassword = hashing.hashStringHex(data.arguments()[2], HashingAlgorithm.SHA160, hashSalt);

            if (!newPassword.equals(newRepeatedPassword))
                return new ErrorCommandResponse("Nové heslo není stejné s opakovaným heslo.");

            account.setPassword(newPassword);
            account.save();

            return new InfoCommandResponse("Tvé heslo bylo úspěšně změněno.");
        } else if (data.sender().isOp() && data.arguments().length == 2) {
            String playerName = data.arguments()[0];

            Player player = getPlugin().getServer().getPlayer(playerName);
            if (player == null) {
                return new ErrorCommandResponse("Tento hráč neexistuje.");
            }

            UserAccount account = database.findEntity(UserAccount.class, player.getUniqueId());
            String newPassword = hashing.hashStringHex(data.arguments()[1], HashingAlgorithm.SHA160, hashSalt);
            account.setPassword(newPassword);
            account.save();
            return new InfoCommandResponse("Heslo hráče " + player.getName() + " bylo úspěšně změněno.");
        }
        return false;
    }
}
