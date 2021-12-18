package com.focamacho.sealmenus.listener;

import com.focamacho.sealmenus.ChestMenu;
import com.focamacho.sealmenus.item.ClickableItem;
import com.focamacho.sealmenus.item.MenuItem;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class InventoryListener implements Listener {

    private static final MenuItem dummyItem = ClickableItem.create(new ItemStack(Material.AIR));

    private final JavaPlugin plugin;
    public Map<ChestMenu, BukkitTask> chestMenus = Maps.newHashMap();

    @EventHandler
    public void onClick(InventoryClickEvent ce) {
        for (ChestMenu chestMenu : chestMenus.keySet()) {
            if(chestMenu.getInventory() == ce.getInventory()) {
                int slot = ce.getSlot();
                if (slot < 9 * chestMenu.getRows()) {
                    ce.setCancelled(true);

                    chestMenu.getOnClick().accept(ce);

                    MenuItem item = chestMenu.getItem(slot);
                    if (item == null) item = dummyItem;

                    switch (ce.getClick()) {
                        case DOUBLE_CLICK:
                            chestMenu.getOnDouble().accept(ce);
                            item.getOnDouble().accept(ce);
                            break;
                        case SHIFT_LEFT:
                            chestMenu.getOnShiftPrimary().accept(ce);
                            item.getOnShiftPrimary().accept(ce);
                            break;
                        case SHIFT_RIGHT:
                            chestMenu.getOnShiftSecondary().accept(ce);
                            item.getOnShiftSecondary().accept(ce);
                            break;
                        case LEFT:
                            chestMenu.getOnPrimary().accept(ce);
                            item.getOnPrimary().accept(ce);
                            break;
                        case MIDDLE:
                            chestMenu.getOnMiddle().accept(ce);
                            item.getOnMiddle().accept(ce);
                            break;
                        case RIGHT:
                            chestMenu.getOnSecondary().accept(ce);
                            item.getOnSecondary().accept(ce);
                            break;
                        case CONTROL_DROP:
                            chestMenu.getOnDropAll().accept(ce);
                            item.getOnDropAll().accept(ce);
                            break;
                        case DROP:
                            chestMenu.getOnDrop().accept(ce);
                            item.getOnDrop().accept(ce);
                            break;
                        case NUMBER_KEY:
                            chestMenu.getOnNumber().accept(ce);
                            item.getOnNumber().accept(ce);
                    }

                    break;
                }
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent ie) {
        for (Map.Entry<ChestMenu, BukkitTask> entry : chestMenus.entrySet()) {
            ChestMenu menu = entry.getKey();
            if (menu.getInventory() == ie.getInventory()) {
                if(entry.getValue() == null) entry.setValue(Bukkit.getScheduler().runTaskTimer(this.plugin, () -> menu.getItems().forEach((slot, item) -> {
                    if (item.update()) menu.update(slot);
                }), 1, 1));

                menu.getOnOpen().accept(ie);
                break;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ie) {
        Iterator<Map.Entry<ChestMenu, BukkitTask>> iterator = chestMenus.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<ChestMenu, BukkitTask> entry = iterator.next();
            ChestMenu menu = entry.getKey();
            if(ie.getInventory() == menu.getInventory()) {
                if(ie.getInventory().getViewers().size() <= 0 || (ie.getInventory().getViewers().size() == 1 && ie.getInventory().getViewers().get(0) == ie.getPlayer())) {
                    BukkitTask task = entry.getValue();
                    task.cancel();

                    iterator.remove();
                }

                menu.getOnClose().accept(ie);
            }
        }
    }

}
