package com.focamacho.sealmenus.bukkit.item;

import lombok.*;
import lombok.experimental.Accessors;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Base class for menu items.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(chain = true)
public abstract class MenuItem {

    @NonNull @Getter @Setter private ItemStack item;

    //Actions
    @Getter @Setter protected Consumer<InventoryClickEvent> onPrimary = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onMiddle = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onSecondary = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onShiftPrimary = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onDouble = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onDrop = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onShiftSecondary = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onDropAll = (click) -> {};
    @Getter @Setter protected Consumer<InventoryClickEvent> onNumber = (click) -> {};

    /**
     * Performs an update action.
     *
     * This action is performed one time
     * per tick and only happens if at least
     * one player is using the menu.
     *
     * @return if the ItemStack being displayed
     * in the menu should be updated or not.
     */
    public boolean update() { return false; }

    public abstract MenuItem copy();

}
