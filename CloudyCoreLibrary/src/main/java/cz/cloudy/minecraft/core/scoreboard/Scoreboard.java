/*
  User: Cloudy
  Date: 30/01/2022
  Time: 14:29
*/

package cz.cloudy.minecraft.core.scoreboard;

import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cloudy
 */
@Component
public class Scoreboard
        implements IComponent, Listener {
    public static int SCOREBOARD_REFRESH_INTERVAL = 20 * 5;

    protected Map<Player, List<ScoreboardObject>> entries = new HashMap<>();

    private boolean schedulerCreated = false;

    @Override
    public void onStart() {
        if (schedulerCreated)
            return;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(
                getPlugin(),
                () -> {
                    if (entries.isEmpty())
                        return;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        for (ScoreboardObject object : entries.get(player)) {
                            object.update();
                        }
                    }
                },
                0,
                SCOREBOARD_REFRESH_INTERVAL
        );
        schedulerCreated = true;
    }

    @Nullable
    public ScoreboardObject addScoreboard(Player player, ScoreboardObject object) {
        if (object == null)
            return null;
        object.create(player);
        entries.computeIfAbsent(player, p -> new ArrayList<>()).add(object);
        return object;
    }

    @Nullable
    public ScoreboardObject getScoreboard(Player player, String name) {
        if (!entries.containsKey(player))
            return null;

        return entries.get(player).stream()
                      .filter(scoreboardObject -> scoreboardObject.name.equals(name))
                      .findFirst()
                      .orElse(null);
    }

    @Nullable
    public ScoreboardObject getScoreboard(Player player, Class<? extends ScoreboardLogic> logicClass) {
        if (!entries.containsKey(player))
            return null;

        return entries.get(player).stream()
                      .filter(scoreboardObject -> scoreboardObject.logic != null && scoreboardObject.logic.getClass() == logicClass)
                      .findFirst()
                      .orElse(null);
    }

//    @Nullable
//    public <T extends ScoreboardObject> T getScoreboard(Player player, Class<? extends ScoreboardObject> objectClass) {
//
//    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        entries.remove(e.getPlayer());
    }
}
