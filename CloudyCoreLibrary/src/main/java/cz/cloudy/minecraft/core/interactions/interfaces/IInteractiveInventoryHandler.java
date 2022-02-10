/*
  User: Cloudy
  Date: 08/02/2022
  Time: 03:54
*/

package cz.cloudy.minecraft.core.interactions.interfaces;

import org.bukkit.entity.Player;

/**
 * @author Cloudy
 */
public interface IInteractiveInventoryHandler {
    void onClick(Player player);

    void onTake(Player player);
}
