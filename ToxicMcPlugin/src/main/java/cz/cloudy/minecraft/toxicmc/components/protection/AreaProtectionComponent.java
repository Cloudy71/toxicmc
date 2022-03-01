/*
  User: Cloudy
  Date: 24/02/2022
  Time: 00:47
*/

package cz.cloudy.minecraft.toxicmc.components.protection;

import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.WorldOnly;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import org.bukkit.event.Listener;

import java.util.Arrays;

/**
 * @author Cloudy
 */
@Component
@WorldOnly(filter = ToxicConstants.WORLDS_SURVIVAL)
public class AreaProtectionComponent
        implements IComponent, Listener {

    @CommandListener("area")
    private Object onArea(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");

        String action = data.arguments()[0];
        CommandData newCommandData = new CommandData(
                data.sender(),
                data.command(),
                Arrays.copyOfRange(data.arguments(), 1, data.arguments().length)
        );
        if (action.equals("set"))
            return onAreaSet(newCommandData);
        if (action.equals("unset"))
            return onAreaUnSet(newCommandData);

        return new InfoCommandResponse("Neznámý požadavek.");
    }

    private Object onAreaSet(CommandData data) {
        return null;
    }

    private Object onAreaUnSet(CommandData data) {
        return null;
    }
}
