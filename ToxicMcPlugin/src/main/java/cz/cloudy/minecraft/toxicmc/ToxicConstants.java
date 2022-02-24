/*
  User: Cloudy
  Date: 30/01/2022
  Time: 15:13
*/

package cz.cloudy.minecraft.toxicmc;

import cz.cloudy.minecraft.core.componentsystem.annotations.WorldFilter;
import cz.cloudy.minecraft.messengersystem.MessengerConstants;
import org.bukkit.ChatColor;

/**
 * @author Cloudy
 */
public abstract class ToxicConstants
        extends MessengerConstants {
    @WorldFilter(name = "survival")
    public static final String[] SURVIVAL_WORLDS = {"world", "world_nether", "world_the_end"};

    public static final String CHEST_PROTECTION       = "chestProtection";
    public static final String CHEST_SHARE            = "chestShare";
    public static final String PLAYER_EMPLOYEE        = "playerEmployee";
    public static final String BANNER_ITEM_FRAMES     = "bannerItemFrames";
    public static final String ITEM_COMPANY_TOOL_NAME = ChatColor.DARK_AQUA + "Firemní nástroj";
    public static final String ITEM_LORE_USAGE_AREA   = ChatColor.GOLD + "Tvoření areálu";
    public static final String ITEM_LORE_USAGE_STOCK  = ChatColor.GOLD + "Tvoření skladu";
    public static final String ITEM_LORE_QUALITY = ChatColor.GREEN + "Kvalita: ";
}
