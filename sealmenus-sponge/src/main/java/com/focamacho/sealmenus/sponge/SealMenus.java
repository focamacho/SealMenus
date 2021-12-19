package com.focamacho.sealmenus.sponge;

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

}
