/*
  User: Cloudy
  Date: 21/01/2022
  Time: 18:45
*/

package cz.cloudy.minecraft.toxicmc.components.events.events;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

import java.time.ZonedDateTime;

/**
 * @author Cloudy
 */
public abstract class GameEvent
        implements Listener {
    private Plugin plugin;

    public abstract ChatColor getColor();

    public abstract String getName();

    public abstract String getDescription();

    public abstract ZonedDateTime getCreatedDate();

    public abstract String getAuthor();

    public String getMessageString() {
        return getColor() + getName();
    }

    public String getMessagePrint() {
        if (getDescription() == null || getDescription().isEmpty())
            return null;

        int width = 48;
        String name = getName();
        String description = getDescription();
        StringBuilder builder = new StringBuilder();
        String nameSpace = " ".repeat(width / 2 - name.length() / 2);
        boolean nameIsEven = name.length() % 2 == 1;
        builder.append(ChatColor.WHITE)
                .append("-".repeat(width))
                .append('\n')
                .append(nameSpace)
                .append(getColor())
                .append(name)
                .append(ChatColor.WHITE)
                .append(nameIsEven ? " " : "")
                .append('\n')
                .append("-".repeat(width))
                .append('\n');
        for (String s : description.split("\n")) {
            String wrapped = WordUtils.wrap(s, width, "\n", false);
            for (String sw : wrapped.split("\n")) {
                builder.append(" ")
                        .append(ChatColor.ITALIC)
                        .append(sw)
                        .append(ChatColor.RESET)
                        .append('\n');
            }
        }
        builder.append("-".repeat(width));
        return builder.toString();
    }

    public boolean setup(Plugin plugin, String[] arguments) {
        this.plugin = plugin;
        return true;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void onStart() {
    }

    public void onEnd() {
    }

    public void onEntityDamageEvent(EntityDamageEvent e) {
    }

    public void onEntityDeathEvent(EntityDeathEvent e) {
    }

    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
    }

    public void onEntityShootBowEvent(EntityShootBowEvent e) {
    }

    public void onEntityExplodeEvent(EntityExplodeEvent e) {
    }

    public void onPlayerMoveEvent(PlayerMoveEvent e) {
    }

    public void onPlayerQuitEvent(PlayerQuitEvent e) {
    }
}
