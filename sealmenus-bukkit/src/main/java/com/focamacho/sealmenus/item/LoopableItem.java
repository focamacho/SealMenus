package com.focamacho.sealmenus.item;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple item that does an
 * action when clicked.
 *
 * The difference between LoopableItem and
 * ClickableItem, is that Loopable loops
 * through a list of ItemStacks and display
 * a different item after a pre-set amount of
 * time.
 */
public class LoopableItem extends MenuItem {

    @Getter private final List<ItemStack> items;
    private final int totalTicks;

    private int tickCount;

    /**
     * Private constructor. Use the static method LoopableItem#create.
     */
    private LoopableItem(@NonNull List<ItemStack> items, int ticks) {
        super(items.get(0));
        this.items = items;
        this.totalTicks = ticks;
        this.tickCount = ticks;
    }

    /**
     * Creates a loopable item.
     * @param items the item to be displayed in the menu.
     * @param ticks the amount of ticks to wait before changing
     *              the display item to the next one.
     * @return the created ClickableItem.
     */
    public static LoopableItem create(@NonNull List<ItemStack> items, int ticks) {
        return new LoopableItem(items, ticks);
    }

    @Override
    public boolean update() {
        tickCount--;
        if(tickCount <= 0) {
            tickCount = totalTicks;

            int itemIndex = items.indexOf(getItem()) + 1;
            if(itemIndex >= items.size()) setItem(items.get(0));
            else setItem(items.get(itemIndex));

            return true;
        }

        return false;
    }

    @Override
    public MenuItem copy() {
        return create(this.items.stream().map(ItemStack::clone).collect(Collectors.toList()), this.totalTicks)
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
