/*
  User: Cloudy
  Date: 16/01/2022
  Time: 02:03
*/

package cz.cloudy.minecraft.core.componentsystem.types;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Cloudy
 */
public record CommandData(CommandSender sender, Command command, String[] arguments) {
    // ==================================================
    public boolean isPlayer() {
        return sender instanceof Player;
    }
    public Player getPlayer() {
        return (Player) sender;
    }
}
