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
import org.bukkit.event.Listener;

/**
 * @author Cloudy
 */
@Component
@WorldOnly(filter = "survival")
public class AreaProtectionComponent
        implements IComponent, Listener {

    @CommandListener("area")
    private Object onArea(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");



        return new InfoCommandResponse("Neznámý požadavek.");
    }
}
