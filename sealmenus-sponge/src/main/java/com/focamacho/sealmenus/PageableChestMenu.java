package com.focamacho.sealmenus;

import com.focamacho.sealmenus.item.ClickableItem;
import com.focamacho.sealmenus.item.MenuItem;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PageableChestMenu extends ChestMenu {

    @Getter private final int[] itemSlots;
    private final List<MenuItem> pageableItems;

    private int page;

    private AbstractMap.SimpleEntry<Integer, MenuItem> nextPageItem = null;
    private AbstractMap.SimpleEntry<Integer, MenuItem> previousPageItem = null;

    PageableChestMenu(String title, int rows, int[] itemSlots, Object plugin) {
        super(title, rows, plugin);
        this.pageableItems = Lists.newArrayList();
        this.itemSlots = itemSlots;
        this.page = 0;
    }

    /**
     * Adds an item that will automatically go
     * to pre-set slots for pageable items.
     *
     * @param item the item to add.
     * @return this menu.
     */
    public PageableChestMenu addPageableItem(MenuItem item) {
        pageableItems.add(item);
        requireUpdate(null);
        return this;
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
     */
    public PageableChestMenu setPageableItems(List<MenuItem> items) {
        this.pageableItems.clear();
        pageableItems.addAll(items);
        requireUpdate(null);
        return this;
    }

    /**
     * Get the quantity of pages of this menu.
     * The quantity is defined by the amount
     * of items inserted.
     * @return the quantity of pages of this menu.
     */
    public int getPageCount() {
        return (int) Math.max(1, Math.ceil(this.pageableItems.size() / (double) itemSlots.length));
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
        if(slot <= 0 || slot >= this.getRows() * 9) throw new IllegalArgumentException("The slot can't be less than zero or greater than the inventory size.");
        for(int slotIndex : itemSlots) {
            if(slot == slotIndex) throw new IllegalArgumentException("You can't add an item in a slot reserved for pageable items. Use PageableChestMenu#addPageableItem instead.");
        }

        Integer oldSlot = nextPageItem != null ? nextPageItem.getKey() : null;
        nextPageItem = new AbstractMap.SimpleEntry<>(slot, ClickableItem.create(item)
                .setOnPrimary(click -> {
                    if(click.getSource() instanceof Player) {
                        Task.builder().execute(() -> {
                            this.page += 1;
                            requireUpdate(slot);
                            if(!Objects.equals(oldSlot, slot)) requireUpdate(oldSlot);
                        }).submit(this.plugin);
                    }
                })
        );

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
        if(slot <= 0 || slot >= this.getRows() * 9) throw new IllegalArgumentException("The slot can't be less than zero or greater than the inventory size.");
        for(int slotIndex : itemSlots) {
            if(slot == slotIndex) throw new IllegalArgumentException("You can't add an item in a slot reserved for pageable items. Use PageableChestMenu#addPageableItem instead.");
        }

        Integer oldSlot = previousPageItem != null ? previousPageItem.getKey() : null;
        previousPageItem = new AbstractMap.SimpleEntry<>(slot, ClickableItem.create(item)
                .setOnPrimary(click -> {
                    if(click.getSource() instanceof Player) {
                        Task.builder().execute(() -> {
                            this.page -= 1;
                            requireUpdate(slot);
                            if(!Objects.equals(oldSlot, slot)) requireUpdate(oldSlot);
                        }).submit(this.plugin);
                    }
                })
        );

        return this;
    }

    @Override
    public ChestMenu addItem(MenuItem item, int slot) {
        for(int slotIndex : itemSlots) {
            if(slot == slotIndex) throw new IllegalArgumentException("You can't add an item in a slot reserved for pageable items. Use PageableChestMenu#addPageableItem instead.");
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

}
