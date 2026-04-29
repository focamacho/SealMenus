package com.focamacho.sealmenus.bukkit;

import com.focamacho.sealmenus.bukkit.item.MenuItem;
import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class SealMenus {

    static final Map<JavaPlugin, ChestMenu.Listener> registeredListeners = Maps.newHashMap();

    /**
     * Creates a chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be greater or equal to 1 and
     *             less or equal to 6.
     * @param plugin the instance of the plugin
     *               creating this menu.
     * @return the created ChestMenu.
     */
    public static ChestMenu createChestMenu(String title, int rows, JavaPlugin plugin) {
        registerListener(plugin);
        return new ChestMenu(title, rows, plugin);
    }

    /**
     * Creates a chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be greater or equal to 1 and
     *             less or equal to 6.
     * @param plugin the instance of the plugin
     *               creating this menu.
     * @return the created ChestMenu.
     */
    public static ChestMenu createChestMenu(Component title, int rows, JavaPlugin plugin) {
        registerListener(plugin);
        return new ChestMenu(title, rows, plugin);
    }

    /**
     * Creates a pageable chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be greater or equals to 1 and
     *             less or equals to 6.
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

    /**
     * Creates a pageable chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be greater or equals to 1 and
     *             less or equals to 6.
     * @param itemSlots the slots where the items will
     *                  be. When all slots are filled, a
     *                  new page is created.
     * @param plugin the instance of the plugin
     *               creating this menu.
     * @return the created ChestMenu.
     */
    public static PageableChestMenu createPageableChestMenu(Component title, int rows, int[] itemSlots, JavaPlugin plugin) {
        registerListener(plugin);
        return new PageableChestMenu(title, rows, itemSlots, plugin);
    }

    /**
     * Creates a lazy pageable chest menu. Items are fetched on demand by the
     * provided function, which receives a page index and returns a CompletableFuture
     * with the items to display for that page.
     *
     * @param title the inventory title.
     * @param rows the amount of rows, needs to be greater or equal to 1 and
     *             less or equal to 6.
     * @param itemSlots the slots reserved for pageable items.
     * @param plugin the instance of the plugin creating this menu.
     * @param pageItemProvider function from page index to a future of items, used to load each page.
     * @param totalItemCount total number of items across all pages, used to compute the page count.
     * @return the created LazyPageableChestMenu.
     */
    public static LazyPageableChestMenu createLazyPageableChestMenu(String title, int rows, int[] itemSlots, JavaPlugin plugin,
                                                                    Function<Integer, CompletableFuture<List<MenuItem>>> pageItemProvider,
                                                                    int totalItemCount) {
        registerListener(plugin);
        return new LazyPageableChestMenu(title, rows, itemSlots, plugin, pageItemProvider, totalItemCount);
    }

    /**
     * Creates a lazy pageable chest menu. See the String-title overload for details.
     */
    public static LazyPageableChestMenu createLazyPageableChestMenu(Component title, int rows, int[] itemSlots, JavaPlugin plugin,
                                                                    Function<Integer, CompletableFuture<List<MenuItem>>> pageItemProvider,
                                                                    int totalItemCount) {
        registerListener(plugin);
        return new LazyPageableChestMenu(title, rows, itemSlots, plugin, pageItemProvider, totalItemCount);
    }

    private static void registerListener(JavaPlugin plugin) {
        if(!registeredListeners.containsKey(plugin)) {
            ChestMenu.Listener listener = new ChestMenu.Listener(plugin);
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            registeredListeners.put(plugin, listener);
        }
    }

}
