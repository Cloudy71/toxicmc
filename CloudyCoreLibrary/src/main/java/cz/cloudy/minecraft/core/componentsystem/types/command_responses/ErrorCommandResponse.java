/*
  User: Cloudy
  Date: 17/01/2022
  Time: 00:47
*/

package cz.cloudy.minecraft.core.componentsystem.types.command_responses;

import cz.cloudy.minecraft.core.componentsystem.interfaces.ICommandResponseResolvable;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

/**
 * @author Cloudy
 */
public class ErrorCommandResponse
        implements ICommandResponseResolvable {
    private final String message;

    public ErrorCommandResponse(String message) {
        this.message = message;
    }

    @Override
    public Component getComponent(CommandData commandData) {
        return Component.text(ChatColor.RED + message);
    }
}
