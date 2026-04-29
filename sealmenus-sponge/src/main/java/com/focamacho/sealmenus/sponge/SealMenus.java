package com.focamacho.sealmenus.sponge;

import com.focamacho.sealmenus.sponge.item.MenuItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class SealMenus {

    /**
     * Creates a chest menu.
     * @param title the inventory title.
     * @param rows the amount of rows, needs to
     *             be greater or equals to 1 and
     *             less or equals to 6.
     * @param plugin the instance of the plugin
     *               creating this menu.
     * @return the created ChestMenu.
     */
    public static ChestMenu createChestMenu(String title, int rows, Object plugin) {
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
    public static PageableChestMenu createPageableChestMenu(String title, int rows, int[] itemSlots, Object plugin) {
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
    public static LazyPageableChestMenu createLazyPageableChestMenu(String title, int rows, int[] itemSlots, Object plugin,
                                                                    Function<Integer, CompletableFuture<List<MenuItem>>> pageItemProvider,
                                                                    int totalItemCount) {
        return new LazyPageableChestMenu(title, rows, itemSlots, plugin, pageItemProvider, totalItemCount);
    }

}
