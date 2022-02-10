/*
  User: Cloudy
  Date: 08/02/2022
  Time: 15:02
*/

package cz.cloudy.minecraft.core.game;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;

/**
 * @author Cloudy
 */
@Component
public class TextUtils {
    public net.kyori.adventure.text.Component getText(String string) {
        return net.kyori.adventure.text.Component.text(string);
    }

    public static net.kyori.adventure.text.Component get(String string) {
        return ComponentLoader.get(TextUtils.class).getText(string);
    }
}
