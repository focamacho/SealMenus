package com.focamacho.sealmenus;

import com.focamacho.sealmenus.listener.InventoryListener;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class SealMenus {

    static final Map<Object, InventoryListener> registeredListeners = Maps.newHashMap();

    /**
     * Creates a chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be >= 1 && <= 6.
     * @param plugin the instance of the plugin
     *               creating this menu.
     * @return the created ChestMenu.
     */
    public static ChestMenu createChestMenu(String title, int rows, JavaPlugin plugin) {
        registerListener(plugin);
        return new ChestMenu(title, rows, plugin);
    }

    /**
     * Creates a pageable chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be >= 1 && <= 6.
     * @param itemSlots the slots where the items will
     *                  be. When all slots are filled, a
     *                  new page is created.
     * @param plugin the instance of the plugin
     *               creating this menu.
     * @return the created ChestMenu.
     */
    public static PageableChestMenu createPageableChestMenu(String title, int rows, int[] itemSlots, JavaPlugin plugin) {
        registerListener(plugin);
        return new PageableChestMenu(title, rows, itemSlots, plugin);
    }

    private static void registerListener(JavaPlugin plugin) {
        if(!registeredListeners.containsKey(plugin)) {
            InventoryListener listener = new InventoryListener(plugin);
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            registeredListeners.put(plugin, listener);
        }
    }

}
