/*
  User: Cloudy
  Date: 17/01/2022
  Time: 22:59
*/

package cz.cloudy.minecraft.messengersystem;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.CheckPermission;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cloudy
 */
@Component
public class ChatComponent
        implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(ChatComponent.class);

    private final List<Player> loggedPlayers = new ArrayList<>();

    public void addPlayerToLogged(Player player) {
        loggedPlayers.add(player);
    }

    public void removePlayerFromLogged(Player player) {
        loggedPlayers.remove(player);
    }

    public boolean isPlayerLogged(Player player) {
        return loggedPlayers.contains(player);
    }

    public void sendMessage(net.kyori.adventure.text.Component component) {
        if (component instanceof TextComponent textComponent)
            logger.info(((TextComponent) textComponent.compact()).content());
        else
            logger.info(ChatColor.DARK_GRAY + "[" + component.getClass().getSimpleName() + "]");
        for (Player chatListener : loggedPlayers) {
            chatListener.sendMessage(component);
        }
    }

    public void sendMessage(ComponentBuilder<?, ?> builder) {
        sendMessage(builder.build());
    }

    public void sendMessage(String text) {
        sendMessage(net.kyori.adventure.text.Component.text(text));
    }


    /**
     * 0 ~ 12: Normal color
     * 20 ~ 32: Italic color
     * 40 ~ 52: Bold color
     * 60 ~ 72: Italic+Bold color
     *
     * @param colorByte
     * @return
     */
    public String resolveColorByByte(byte colorByte) {
//        String addition = "";
//        if (colorByte >= 60) {
//            addition = ChatColor.ITALIC + ChatColor.BOLD.toString();
//            colorByte -= 60;
//        } else if (colorByte >= 40) {
//            addition = ChatColor.BOLD.toString();
//            colorByte -= 40;
//        } else if (colorByte >= 20) {
//            addition = ChatColor.ITALIC.toString();
//            colorByte -= 20;
//        }
        return switch (colorByte) {
            case 0 -> ChatColor.WHITE.toString();
            case 1 -> ChatColor.RED.toString();
            case 2 -> ChatColor.GREEN.toString();
            case 3 -> ChatColor.BLUE.toString();
            case 4 -> ChatColor.YELLOW.toString();
            case 5 -> ChatColor.AQUA.toString();
            case 6 -> ChatColor.LIGHT_PURPLE.toString();
            case 7 -> ChatColor.DARK_RED.toString();
            case 8 -> ChatColor.DARK_GREEN.toString();
            case 9 -> ChatColor.DARK_BLUE.toString();
            case 10 -> ChatColor.GOLD.toString();
            case 11 -> ChatColor.DARK_AQUA.toString();
            case 12 -> ChatColor.DARK_PURPLE.toString();
            default -> ChatColor.WHITE.toString();
        };
    }

    @CommandListener("gsay")
    @CheckPermission(CheckPermission.OP)
    private void onGlobalSay(CommandData data) {
        String string = String.join(" ", data.arguments());
        for (Player player : getPlugin().getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.DARK_AQUA + string);
        }
        logger.info(ChatColor.DARK_AQUA + string);
    }
}
