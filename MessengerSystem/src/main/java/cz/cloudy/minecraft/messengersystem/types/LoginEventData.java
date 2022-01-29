/*
  User: Cloudy
  Date: 20/01/2022
  Time: 23:51
*/

package cz.cloudy.minecraft.messengersystem.types;

import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import org.bukkit.entity.Player;

/**
 * @author Cloudy
 */
public record LoginEventData(UserAccount account, Player player) {

}
