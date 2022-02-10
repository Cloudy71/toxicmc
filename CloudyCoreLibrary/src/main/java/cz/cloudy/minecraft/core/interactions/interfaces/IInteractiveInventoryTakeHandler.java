/*
  User: Cloudy
  Date: 08/02/2022
  Time: 04:49
*/

package cz.cloudy.minecraft.core.interactions.interfaces;

import org.bukkit.entity.Player;

/**
 * @author Cloudy
 */
public interface IInteractiveInventoryTakeHandler
        extends IInteractiveInventoryHandler {
    @Override
    default void onClick(Player player) {
    }
}
