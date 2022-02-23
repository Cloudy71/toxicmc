/*
  User: Cloudy
  Date: 22/02/2022
  Time: 21:55
*/

package cz.cloudy.minecraft.toxicmc.components.minigames.games;

import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.WorldOnly;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;

/**
 * @author Cloudy
 */
//@Component // Temporary disabled
@WorldOnly(worldNames = {"minigame_test"})
public class MiniGameTest
        extends MiniGame
        implements Listener {


    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public String getDescription() {
        return "Test game";
    }

    @Override
    public byte getMaxPlayers() {
        return 10;
    }

    @Override
    public World getWorld() {
        return Bukkit.getWorld("minigame_test");
    }
}
