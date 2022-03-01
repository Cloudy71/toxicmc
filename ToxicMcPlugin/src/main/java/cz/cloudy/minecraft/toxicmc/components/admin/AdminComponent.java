package cz.cloudy.minecraft.toxicmc.components.admin;

import cz.cloudy.minecraft.core.componentsystem.annotations.CheckPermission;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import org.bukkit.*;
import org.bukkit.event.Listener;

import java.util.Arrays;

@Component
public class AdminComponent implements IComponent, Listener {

    @CommandListener("world")
    @CheckPermission(CheckPermission.OP)
    private Object onWorld(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");

        String action = data.arguments()[0];
        CommandData newCommandData = new CommandData(
                data.sender(),
                data.command(),
                Arrays.copyOfRange(data.arguments(), 1, data.arguments().length)
        );
        if (action.equals("create"))
            return onWorldCreate(newCommandData);
        if (action.equals("teleport"))
            return onWorldTeleport(newCommandData);

        return new InfoCommandResponse("Neznámý požadavek.");
    }

    private Object onWorldCreate(CommandData data) {
        if (data.arguments().length < 3)
            return new InfoCommandResponse("Nedostatek parametrů.");

        String name = data.arguments()[0];
        long seed = Long.parseLong(data.arguments()[1]);
        String type = data.arguments()[2].toUpperCase();

        World world = Bukkit.getWorld(name);
        if (world != null)
            return new InfoCommandResponse("Svět s jménem \"" + name + "\" již existuje.");

        WorldType worldType = WorldType.getByName(type);
        if (worldType == null)
            return new InfoCommandResponse("Typ světa \"" + type + "\" neexistuje.");
        WorldCreator worldCreator = new WorldCreator(name)
                .type(worldType)
                .seed(seed)
                .generateStructures(true);
        Bukkit.createWorld(worldCreator);

        return new InfoCommandResponse("Svět byl úspěšně vytvořen.");
    }

    private Object onWorldTeleport(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Nedostatek parametrů.");

        String name = data.arguments()[0];

        World world = Bukkit.getWorld(name);
        if (world == null)
            return new InfoCommandResponse("Svět s jménem \"" + name + "\" neexistuje.");

        data.getPlayer().teleport(world.getSpawnLocation());
        return new InfoCommandResponse("Ok.");
    }
}
