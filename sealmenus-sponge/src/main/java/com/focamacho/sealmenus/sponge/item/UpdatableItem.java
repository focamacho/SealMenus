package com.focamacho.sealmenus.sponge.item;

import lombok.NonNull;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * An item that can have its icon
 * changed while the menu is open.
 */
public class UpdatableItem extends MenuItem {

    private boolean dirty = false;

    /**
     * Private constructor. Use the static method UpdatableItem#create.
     */
    private UpdatableItem(@NonNull ItemStack item) {
        super(item);
    }

    /**
     * Creates an updatable item.
     * @param item the item to be displayed in the menu.
     * @return the created UpdatableItem.
     */
    public static UpdatableItem create(@NonNull ItemStack item) {
        return new UpdatableItem(item);
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
        return create(this.getItem().copy())
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
