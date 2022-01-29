/*
  User: Cloudy
  Date: 17/01/2022
  Time: 23:59
*/

package cz.cloudy.minecraft.core.componentsystem.types.command_responses;

import cz.cloudy.minecraft.core.componentsystem.interfaces.ICommandResponseResolvable;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

/**
 * @author Cloudy
 */
public class InfoCommandResponse
        implements ICommandResponseResolvable {
    private final String message;

    public InfoCommandResponse(String message) {
        this.message = message;
    }

    @Override
    public Component getComponent(CommandData commandData) {
        return Component.text(ChatColor.AQUA + message);
    }
}
