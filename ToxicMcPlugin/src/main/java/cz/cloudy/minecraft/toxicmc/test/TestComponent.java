/*
  User: Cloudy
  Date: 19/02/2022
  Time: 15:42
*/

package cz.cloudy.minecraft.toxicmc.test;

import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.Benchmarked;
import cz.cloudy.minecraft.core.componentsystem.annotations.CheckPermission;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.game.BulkWorldBuilder;
import cz.cloudy.minecraft.core.game.annotations.BulkBuildType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.slf4j.Logger;

/**
 * @author Cloudy
 */
@Component
public class TestComponent
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(TestComponent.class);

    @Component
    private BulkWorldBuilder worldBuilder;

    @CommandListener("wset")
    @Benchmarked
    @CheckPermission(CheckPermission.OP)
    private Object onWorldSet(CommandData commandData) {
        if (commandData.arguments().length < 7)
            return new ErrorCommandResponse("Param count");

        int x = Integer.parseInt(commandData.arguments()[0]);
        int y = Integer.parseInt(commandData.arguments()[1]);
        int z = Integer.parseInt(commandData.arguments()[2]);
        int dx = Integer.parseInt(commandData.arguments()[3]);
        int dy = Integer.parseInt(commandData.arguments()[4]);
        int dz = Integer.parseInt(commandData.arguments()[5]);
        Material material = Material.getMaterial(commandData.arguments()[6]);

        commandData.sender().sendMessage(ChatColor.YELLOW + "Start: " + (dx - x) + ", " + (dy - y) + ", " + (dz - z));

        worldBuilder.setBuildType(BulkBuildType.BuildType.Bukkit_Threaded);
        worldBuilder.buildCuboid(
                commandData.getPlayer().getWorld(),
                new Vector(x, y, z),
                new Vector(dx, dy, dz),
                material
        );
        worldBuilder.setBuildType(BulkBuildType.BuildType.Bukkit);

        return new InfoCommandResponse("OK");
    }
}
