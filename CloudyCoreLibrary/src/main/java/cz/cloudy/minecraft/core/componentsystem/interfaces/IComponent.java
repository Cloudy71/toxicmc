/*
  User: Cloudy
  Date: 10/01/2022
  Time: 02:21
*/

package cz.cloudy.minecraft.core.componentsystem.interfaces;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import org.bukkit.plugin.Plugin;

/**
 * @author Cloudy
 */
public interface IComponent {
    default void onLoad() {
    }

    default void onClassScan(Class<?>[] classes) {
    }

    /**
     * After server start
     * Is called after all components are loaded and database connection was made
     */
    default void onStart() {

    }

    default Plugin getPlugin() {
        return ComponentLoader.getComponentOwner(this);
    }

    default void notifyActionListeners(String name, Object... data) {
        ComponentLoader.notifyActionListeners(getClass(), name, data);
    }
}
