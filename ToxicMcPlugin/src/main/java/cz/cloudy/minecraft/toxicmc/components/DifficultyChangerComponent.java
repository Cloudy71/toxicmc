/*
  User: Cloudy
  Date: 20/01/2022
  Time: 23:42
*/

package cz.cloudy.minecraft.toxicmc.components;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.componentsystem.annotations.ActionListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cron;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.messengersystem.ChatComponent;
import cz.cloudy.minecraft.messengersystem.types.LoginEventData;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.World;

import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
@Component
public class DifficultyChangerComponent
        implements IComponent {

    @Component
    private ChatComponent messenger;

    @Override
    public void onLoad() {
        difficultyChange();
    }

    @Cron("0 0 6,20,22 * * *")
    private void difficultyChange() {
        ZonedDateTime now = ZonedDateTime.now();
        Difficulty difficulty =
                now.getHour() >= 6 && now.getHour() < 20 ? Difficulty.EASY
                        : (now.getHour() >= 20 && now.getHour() < 22 ? Difficulty.NORMAL : Difficulty.HARD);
        World world = getPlugin().getServer().getWorld("world");
        Preconditions.checkNotNull(world);
        world.setDifficulty(difficulty);
        world = getPlugin().getServer().getWorld("world_nether");
        Preconditions.checkNotNull(world);
        world.setDifficulty(difficulty);
        world = getPlugin().getServer().getWorld("world_the_end");
        Preconditions.checkNotNull(world);
        world.setDifficulty(difficulty);
        messenger.sendMessage(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Byla změněna obtížnost světa na: " +
                (difficulty == Difficulty.EASY ? ChatColor.GREEN : (difficulty == Difficulty.NORMAL ? ChatColor.GOLD : ChatColor.RED)) + difficulty);
    }

    @ActionListener(value = "MessengerSystem.onLogin", priority = 20)
    private void onLogin(LoginEventData data) {
        Difficulty difficulty = data.player().getWorld().getDifficulty();
        data.player().sendMessage(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktuální obtížnost: " +
                (difficulty == Difficulty.EASY ? ChatColor.GREEN : (difficulty == Difficulty.NORMAL ? ChatColor.GOLD : ChatColor.RED)) +
                difficulty);
    }
}
