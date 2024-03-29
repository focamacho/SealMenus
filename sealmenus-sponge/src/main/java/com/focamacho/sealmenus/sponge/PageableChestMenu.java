package com.focamacho.sealmenus.sponge;

import com.focamacho.sealmenus.sponge.item.ClickableItem;
import com.focamacho.sealmenus.sponge.item.MenuItem;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tuple;

import java.util.*;
import java.util.function.Consumer;

public class PageableChestMenu extends ChestMenu {

    @Getter private final int[] itemSlots;
    protected List<MenuItem> pageableItems;

    private int page;

    private AbstractMap.SimpleEntry<Integer, MenuItem> nextPageItem = null;
    private AbstractMap.SimpleEntry<Integer, MenuItem> previousPageItem = null;

    // Copies of this menu, allowing multiple players to have it open at the same time,
    // and view different pages.
    private List<PageableChestMenu> mirrorMenus = Lists.newArrayList();
    private PageableChestMenu fatherMenu;

    protected PageableChestMenu(String title, int rows, int[] itemSlots, Object plugin) {
        super(title, rows, plugin);
        this.itemSlots = itemSlots;
        this.page = 0;
        this.pageableItems = Lists.newArrayList();
        this.fatherMenu = null;
    }

    // Constructor for mirror menus
    private PageableChestMenu(PageableChestMenu father) {
        this(father.getTitle(), father.getRows(), father.getItemSlots(), father.plugin);
        this.items = father.items;
        this.pageableItems = father.pageableItems;

        if(father.nextPageItem != null) this.setNextPageItem(father.nextPageItem.getValue().getItem(), father.nextPageItem.getKey());
        if(father.previousPageItem != null) this.setPreviousPageItem(father.previousPageItem.getValue().getItem(), father.previousPageItem.getKey());

        this.mirrorMenus = null;
        this.fatherMenu = father;
        father.mirrorMenus.add(this);
    }

    /**
     * Adds an item that will automatically go
     * to pre-set slots for pageable items.
     *
     * @param item the item to add.
     * @param index the index to put the item in.
     * @return this menu.
     */
    public PageableChestMenu addPageableItem(MenuItem item, int index) {
        pageableItems.add(Math.min(index, pageableItems.size()), item);
        requireUpdate(null);
        return this;
    }

    /**
     * Adds an item that will automatically go
     * to pre-set slots for pageable items.
     *
     * @param item the item to add.
     * @return this menu.
     */
    public PageableChestMenu addPageableItem(MenuItem item) {
        return addPageableItem(item, pageableItems.size());
    }

    /**
     * Remove an item inserted as pageable.
     *
     * @param item the item to remove.
     * @return this menu.
     */
    public PageableChestMenu removePageableItem(MenuItem item) {
        pageableItems.remove(item);
        requireUpdate(null);
        return this;
    }

    /**
     * Returns all pageable items.
     *
     * @return the list containing all
     * pageable items.
     */
    public List<MenuItem> getPageableItems() {
        return Collections.unmodifiableList(pageableItems);
    }

    /**
     * Override all pageable items of this menu.
     *
     * @param items the items to set.
     * @return this menu.
     */
    public PageableChestMenu setPageableItems(List<MenuItem> items) {
        this.pageableItems.clear();
        pageableItems.addAll(items);
        this.page = 0;
        requireUpdate(null);
        return this;
    }

    /**
     * Remove all pageable items of this menu.
     *
     * @return this menu.
     */
    public PageableChestMenu clearPageableItems() {
        this.pageableItems.clear();
        requireUpdate(null);
        return this;
    }

    /**
     * Returns the slot for the inserted pageable item.
     *
     * @return the slot and page of the pageable item, or null if
     * not present in the menu.
     */
    public Map.Entry<Integer, Integer> getPageableItemSlot(MenuItem item) {
        if(!this.pageableItems.contains(item)) return null;

        int indexOf = pageableItems.indexOf(item);
        int page = (int) Math.floor((float) indexOf / itemSlots.length);
        int slot = itemSlots[indexOf - (page * itemSlots.length)];

        return new AbstractMap.SimpleEntry<>(page, slot);
    }

    /**
     * Get the quantity of pages of this menu.
     * The quantity is defined by the amount
     * of items inserted.
     *
     * @return the quantity of pages of this menu.
     */
    public int getPageCount() {
        return (int) Math.max(1, Math.ceil(this.pageableItems.size() / (double) itemSlots.length));
    }

    /**
     * Get the current page the player
     * is in.
     *
     * @param player the player to check the
     *               page.
     * @return the current page index.
     */
    public int getCurrentPage(Player player) {
        if(this.fatherMenu != null && this.fatherMenu.playersViewing.contains(player)) {
            return this.fatherMenu.page;
        } else if(playersViewing.contains(player)) {
            return this.page;
        } else {
            Optional<PageableChestMenu> menu = mirrorMenus.stream().filter(m -> m.playersViewing.contains(player)).findFirst();
            if(menu.isPresent()) return menu.get().page;
        }

        return 0;
    }

    /**
     * Set the item used to go to the next page.
     * This item is only shown if there is enough items
     * for a next page to exist.
     *
     * @param item the item to display.
     * @param slot the slot for the item.
     * @return this menu.
     */
    public PageableChestMenu setNextPageItem(ItemStack item, int slot) {
        if(slot < 0 || slot >= this.getRows() * 9) throw new IllegalArgumentException("The slot can't be less than zero or greater than the inventory size.");
        for(int slotIndex : itemSlots) {
            if(slot == slotIndex) throw new IllegalArgumentException("You can't add an item in a slot reserved for pageable items.");
        }

        if(fatherMenu == null) mirrorMenus.forEach(menu -> menu.setNextPageItem(item, slot));

        Integer oldSlot = nextPageItem != null ? nextPageItem.getKey() : null;
        nextPageItem = new AbstractMap.SimpleEntry<>(slot, ClickableItem.create(item)
                .setOnPrimary(click -> {
                    if(click.getSource() instanceof Player) {
                        Task.builder().execute(() -> {
                            if(this.page + 1 < getPageCount()) {
                                this.page += 1;
                                update();
                            }
                        }).submit(this.plugin);
                    }
                })
        );

        requireUpdate(slot);
        if(!Objects.equals(oldSlot, slot)) requireUpdate(oldSlot);

        return this;
    }

    /**
     * Set the item used to go to the next page.
     * This item is only shown if the index of
     * the actual page is greater than zero.
     *
     * @param item the item to display.
     * @param slot the slot for the item.
     * @return this menu.
     */
    public PageableChestMenu setPreviousPageItem(ItemStack item, int slot) {
        if(slot < 0 || slot >= this.getRows() * 9) throw new IllegalArgumentException("The slot can't be less than zero or greater than the inventory size.");
        for(int slotIndex : itemSlots) {
            if(slot == slotIndex) throw new IllegalArgumentException("You can't add an item in a slot reserved for pageable items.");
        }

        if(fatherMenu == null) mirrorMenus.forEach(menu -> menu.setPreviousPageItem(item, slot));

        Integer oldSlot = previousPageItem != null ? previousPageItem.getKey() : null;
        previousPageItem = new AbstractMap.SimpleEntry<>(slot, ClickableItem.create(item)
                .setOnPrimary(click -> {
                    if(click.getSource() instanceof Player) {
                        Task.builder().execute(() -> {
                            if(this.page > 0) {
                                this.page -= 1;
                                update();
                            }
                        }).submit(this.plugin);
                    }
                })
        );

        requireUpdate(slot);
        if(!Objects.equals(oldSlot, slot)) requireUpdate(oldSlot);

        return this;
    }

    @Override
    public ChestMenu addItem(MenuItem item, int slot) {
        for(int slotIndex : itemSlots) {
            if(slot == slotIndex) throw new IllegalArgumentException("You can't add an item in a slot reserved for pageable items.");
        }

        return super.addItem(item, slot);
    }

    @Override
    public boolean containsItem(Integer slot) {
        if(nextPageItem != null && Objects.equals(slot, nextPageItem.getKey()) && getPageCount() > this.page + 1) return true;
        else if(previousPageItem != null && Objects.equals(slot, previousPageItem.getKey()) && this.page > 0) return true;

        for (int i = 0; i < itemSlots.length; i++) {
            int itemSlot = itemSlots[i];
            if (slot == itemSlot) {
                if ((this.pageableItems.size() - 1) >= (itemSlots.length * page + i))
                    return true;
                break;
            }
        }

        return super.containsItem(slot);
    }

    @Override
    public void requireUpdate(Integer slot) {
        if(fatherMenu == null) mirrorMenus.forEach(menu -> menu.requireUpdate(slot));

        if(this.inventory != null) {
            if(hasViewers())
                if(slot == null) update();
                else update(slot);
            else this.slotsRequiringUpdate.add(slot);
        }
    }

    @Override
    public MenuItem getItem(Integer slot) {
        if(nextPageItem != null && Objects.equals(slot, nextPageItem.getKey()) && getPageCount() > this.page + 1) return nextPageItem.getValue();
        else if(previousPageItem != null && Objects.equals(slot, previousPageItem.getKey()) && this.page > 0) return previousPageItem.getValue();

        for (int i = 0; i < itemSlots.length; i++) {
            int itemSlot = itemSlots[i];
            if (slot == itemSlot) {
                if ((this.pageableItems.size() - 1) >= (itemSlots.length * page + i))
                    return pageableItems.get(itemSlots.length * page + i);
                break;
            }
        }

        return super.getItem(slot);
    }

    @Override
    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        if(this.inventory == null || !super.hasViewers()) {
            this.page = Math.min(getPageCount() - 1, page);
            requireUpdate(null);
            super.open(player);
        } else new PageableChestMenu(this).open(player, page);
    }

    @Override
    public boolean hasViewers() {
        return super.hasViewers() || (this.fatherMenu == null && mirrorMenus.stream().anyMatch(PageableChestMenu::hasViewers));
    }

    @Override
    public ChestMenu copy() {
        PageableChestMenu copy = new PageableChestMenu(this.getTitle(), this.getRows(), this.getItemSlots(), this.plugin);

        copy.setOnOpen(getOnOpen());
        copy.setOnClose(getOnClose());

        copy.setOnPrimary(getOnPrimary());
        copy.setOnSecondary(getOnSecondary());
        copy.setOnDrop(getOnDrop());
        copy.setOnDropAll(getOnDropAll());
        copy.setOnMiddle(getOnMiddle());
        copy.setOnNumber(getOnNumber());
        copy.setOnShiftPrimary(getOnShiftPrimary());
        copy.setOnShiftSecondary(getOnShiftSecondary());
        copy.setOnDouble(getOnDouble());

        if(nextPageItem != null) copy.setNextPageItem(nextPageItem.getValue().getItem(), nextPageItem.getKey());
        if(previousPageItem != null) copy.setPreviousPageItem(previousPageItem.getValue().getItem(), previousPageItem.getKey());

        getItems().forEach((slot, item) -> copy.addItem(item.copy(), slot));
        copy.setPageableItems(getPageableItems());
        return copy;
    }

    @Override
    protected Task getUpdateItemsTask() {
        if(fatherMenu == null) return super.getUpdateItemsTask();
        else return fatherMenu.getUpdateItemsTask();
    }

    @Override
    protected ChestMenu setUpdateItemsTask(Task updateItemsTask) {
        if(fatherMenu == null) return super.setUpdateItemsTask(updateItemsTask);
        else return fatherMenu.setUpdateItemsTask(updateItemsTask);
    }

    @Override
    protected void handlesUpdateItemsTask() {
        if(fatherMenu == null) super.handlesUpdateItemsTask();
        else fatherMenu.handlesUpdateItemsTask();
    }

    @Override
    protected void handleUpdateItems() {
        super.handleUpdateItems();

        getPageableItems().forEach(item -> {
            if(item.update()) {
                requireUpdate(getPageableItemSlot(item).getValue());
            }
        });
    }

    //Override global actions for mirrored menus
    @Override
    public Consumer<InteractInventoryEvent.Open> getOnOpen() {
        return this.fatherMenu == null ? super.getOnOpen() : this.fatherMenu.getOnOpen();
    }

    @Override
    public Consumer<InteractInventoryEvent.Close> getOnClose() {
        return this.fatherMenu == null ? super.getOnClose() : this.fatherMenu.getOnClose();
    }

    @Override
    public Consumer<ClickInventoryEvent> getOnClick() {
        return this.fatherMenu == null ? super.getOnClick() : this.fatherMenu.getOnClick();
    }

    @Override
    public Consumer<ClickInventoryEvent.Primary> getOnPrimary() {
        return this.fatherMenu == null ? super.getOnPrimary() : this.fatherMenu.getOnPrimary();
    }

    @Override
    public Consumer<ClickInventoryEvent.Middle> getOnMiddle() {
        return this.fatherMenu == null ? super.getOnMiddle() : this.fatherMenu.getOnMiddle();
    }

    @Override
    public Consumer<ClickInventoryEvent.Secondary> getOnSecondary() {
        return this.fatherMenu == null ? super.getOnSecondary() : this.fatherMenu.getOnSecondary();
    }

    @Override
    public Consumer<ClickInventoryEvent.Shift.Primary> getOnShiftPrimary() {
        return this.fatherMenu == null ? super.getOnShiftPrimary() : this.fatherMenu.getOnShiftPrimary();
    }

    @Override
    public Consumer<ClickInventoryEvent.Double> getOnDouble() {
        return this.fatherMenu == null ? super.getOnDouble() : this.fatherMenu.getOnDouble();
    }

    @Override
    public Consumer<ClickInventoryEvent.Drop.Single> getOnDrop() {
        return this.fatherMenu == null ? super.getOnDrop() : this.fatherMenu.getOnDrop();
    }

    @Override
    public Consumer<ClickInventoryEvent.Shift.Secondary> getOnShiftSecondary() {
        return this.fatherMenu == null ? super.getOnShiftSecondary() : this.fatherMenu.getOnShiftSecondary();
    }

    @Override
    public Consumer<ClickInventoryEvent.Drop.Full> getOnDropAll() {
        return this.fatherMenu == null ? super.getOnDropAll() : this.fatherMenu.getOnDropAll();
    }

    @Override
    public Consumer<ClickInventoryEvent.NumberPress> getOnNumber() {
        return this.fatherMenu == null ? super.getOnNumber() : this.fatherMenu.getOnNumber();
    }

}
