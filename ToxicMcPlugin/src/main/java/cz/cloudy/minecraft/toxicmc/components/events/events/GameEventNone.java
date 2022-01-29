/*
  User: Cloudy
  Date: 21/01/2022
  Time: 18:50
*/

package cz.cloudy.minecraft.toxicmc.components.events.events;

import org.bukkit.ChatColor;

import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
public class GameEventNone
        extends GameEvent {
    @Override
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public String getName() {
        return "Žádný";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ZonedDateTime getCreatedDate() {
        return null;
    }

    @Override
    public String getAuthor() {
        return null;
    }
}
