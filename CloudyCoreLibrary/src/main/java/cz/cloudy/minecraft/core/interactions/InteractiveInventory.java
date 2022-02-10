/*
  User: Cloudy
  Date: 08/02/2022
  Time: 03:34
*/

package cz.cloudy.minecraft.core.interactions;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Component
public class InteractiveInventory
        implements Listener {
    private static int idGenerator = 0;

    protected final Map<String, InteractiveInventoryObject>            globalInventoryMapByTitle    = new HashMap<>();
    protected final Map<Integer, InteractiveInventoryObject>           globalInventoryMapById       = new HashMap<>();
    protected final Map<String, Map<UUID, InteractiveInventoryObject>> temporaryInventoryMapByTitle = new HashMap<>();

    public InteractiveInventoryObject createGlobalInventory(int size, String title) {
        Preconditions.checkState(!globalInventoryMapByTitle.containsKey(title), "Global interactive inventory with such name exists");
        Inventory inventory = Bukkit.createInventory(null, size, net.kyori.adventure.text.Component.text(title));

        InteractiveInventoryObject interactiveInventory = new InteractiveInventoryObject(idGenerator++, title, inventory);
        globalInventoryMapByTitle.put(title, interactiveInventory);
        globalInventoryMapById.put(interactiveInventory.id, interactiveInventory);
        return interactiveInventory;
    }

    public InteractiveInventoryObject openOnetimeInventory(int size, String title, Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, net.kyori.adventure.text.Component.text(title));
        InteractiveInventoryObject interactiveInventory = new InteractiveInventoryObject(-1, title, inventory);
        temporaryInventoryMapByTitle.computeIfAbsent(title, s -> new HashMap<>()).put(player.getUniqueId(), interactiveInventory);
        interactiveInventory.players.add(player.getUniqueId());
        player.openInventory(inventory);
        return interactiveInventory;
    }

    public void openGlobalInventory(Player player, int id) {
        Preconditions.checkState(globalInventoryMapById.containsKey(id), "No interactive inventory with id " + id + " exists");

        InteractiveInventoryObject interactiveInventoryObject = globalInventoryMapById.get(id);
        interactiveInventoryObject.players.add(player.getUniqueId());
        player.openInventory(interactiveInventoryObject.inventory);
    }

    public void destroyGlobalInventory(int id) {
        Preconditions.checkState(globalInventoryMapById.containsKey(id), "No interactive inventory with id " + id);
        InteractiveInventoryObject interactiveInventoryObject = globalInventoryMapById.get(id);
        globalInventoryMapByTitle.remove(interactiveInventoryObject.title);
        globalInventoryMapById.remove(id);
        interactiveInventoryObject.inventory.close();
    }

    public void destroyGlobalInventory(String title) {
        Preconditions.checkState(globalInventoryMapByTitle.containsKey(title), "No interactive inventory with title \"" + title + "\"");
        destroyGlobalInventory(globalInventoryMapByTitle.get(title).id);
    }

    public void destroyOnetimeInventory(String title, Player player) {
        Preconditions.checkState(temporaryInventoryMapByTitle.containsKey(title), "No interactive inventory with title \"" + title + "\"");
        Preconditions.checkState(temporaryInventoryMapByTitle.get(title).containsKey(player.getUniqueId()),
                                 "No interactive inventory with title \"" + title + "\" with player \"" + player.getName() + "\"");
        InteractiveInventoryObject interactiveInventoryObject = temporaryInventoryMapByTitle.get(title).get(player.getUniqueId());
        temporaryInventoryMapByTitle.get(title).remove(player.getUniqueId());
        interactiveInventoryObject.inventory.close();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClickEvent(InventoryClickEvent e) {
        InventoryView inventoryView = e.getView();
        InteractiveInventoryObject interactiveInventory;
        String title = ((TextComponent) inventoryView.title().compact()).content();
        if (!e.isLeftClick() || e.getClickedInventory() != inventoryView.getTopInventory() ||
            (!globalInventoryMapByTitle.containsKey(title) &&
             (!temporaryInventoryMapByTitle.containsKey(title) || !temporaryInventoryMapByTitle.get(title).containsKey(e.getWhoClicked().getUniqueId()))))
            return;

        interactiveInventory = globalInventoryMapByTitle.get(title);
        if (interactiveInventory == null)
            interactiveInventory = temporaryInventoryMapByTitle.get(title).get(e.getWhoClicked().getUniqueId());

        if (interactiveInventory.buttonMap.containsKey(e.getSlot()))
            interactiveInventory.buttonMap.get(e.getSlot()).onClick((Player) e.getWhoClicked());
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryCloseEvent(InventoryCloseEvent e) {
        InventoryView inventoryView = e.getView();
        if (!(inventoryView.title().compact() instanceof TextComponent))
            return;

        InteractiveInventoryObject interactiveInventory;
        String title = ((TextComponent) inventoryView.title().compact()).content();
        if ((!globalInventoryMapByTitle.containsKey(title) &&
             (!temporaryInventoryMapByTitle.containsKey(title) || !temporaryInventoryMapByTitle.get(title).containsKey(e.getPlayer().getUniqueId()))))
            return;

        interactiveInventory = globalInventoryMapByTitle.get(title);
        if (interactiveInventory == null)
            interactiveInventory = temporaryInventoryMapByTitle.get(title).get(e.getPlayer().getUniqueId());

        interactiveInventory.players.remove(e.getPlayer().getUniqueId());
        if (interactiveInventory.id == -1)
            destroyOnetimeInventory(title, (Player) e.getPlayer());
    }
}
