/*
  User: Cloudy
  Date: 08/02/2022
  Time: 03:52
*/

package cz.cloudy.minecraft.core.interactions;

import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.interactions.interfaces.IInteractiveInventoryHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Cloudy
 */
public class InteractiveInventoryObject {
    protected final int       id;
    protected final String    title;
    protected final Inventory inventory;

    protected final Map<Integer, IInteractiveInventoryHandler> buttonMap = new HashMap<>();
    protected final List<UUID>                                 players   = new ArrayList<>();

    protected InteractiveInventoryObject(int id, String title, Inventory inventory) {
        this.id = id;
        this.title = title;
        this.inventory = inventory;
    }

    public int getId() {
        return id;
    }

    public InteractiveInventoryObject addButton(ItemStack itemStack, int position, IInteractiveInventoryHandler handler) {
        inventory.setItem(position, itemStack);
        if (handler != null)
            buttonMap.put(position, handler);
        return this;
    }

    public void open(Player player) {
        if (id == -1)
            return;
        ComponentLoader.get(InteractiveInventory.class).openGlobalInventory(player, id);
    }

    public void destroy() {
        if (id != -1)
            ComponentLoader.get(InteractiveInventory.class).destroyGlobalInventory(id);
        else
            for (UUID player : players)
                ComponentLoader.get(InteractiveInventory.class).destroyOnetimeInventory(title, Bukkit.getPlayer(player));
    }
}
