package com.focamacho.sealmenus.bukkit.item;

import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * A simple item that does an
 * action when clicked.
 */
public class ClickableItem extends MenuItem {

    private boolean dirty = false;

    /**
     * Private constructor. Use the static method ClickableItem#create.
     */
    private ClickableItem(@NonNull ItemStack item) {
        super(item);
    }

    /**
     * Creates a clickable item.
     * @param item the item to be displayed in the menu.
     * @return the created ClickableItem.
     */
    public static ClickableItem create(@NonNull ItemStack item) {
        return new ClickableItem(item);
    }

    @Override
    public MenuItem setItem(@NonNull ItemStack item) {
        this.dirty = true;
        return super.setItem(item);
    }

    @Override
    public boolean update() {
        if(this.dirty) {
            this.dirty = false;
            return true;
        }

        return false;
    }

    @Override
    public MenuItem copy() {
        return create(this.getItem().clone())
                .setOnPrimary(this.getOnPrimary())
                .setOnMiddle(this.getOnMiddle())
                .setOnSecondary(this.getOnSecondary())
                .setOnShiftPrimary(this.getOnShiftPrimary())
                .setOnDouble(this.getOnDouble())
                .setOnDrop(this.getOnDrop())
                .setOnNumber(this.getOnNumber())
                .setOnShiftSecondary(this.getOnShiftSecondary())
                .setOnDropAll(this.getOnDropAll());
    }

}
