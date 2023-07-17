package com.focamacho.sealmenus.sponge.item;

import lombok.*;
import lombok.experimental.Accessors;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Base class for menu items.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(chain = true)
public abstract class MenuItem {

    @NonNull @Getter @Setter private ItemStack item;

    //Actions
    @Getter @Setter protected Consumer<ClickInventoryEvent.Primary> onPrimary = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Middle> onMiddle = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Secondary> onSecondary = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Shift.Primary> onShiftPrimary = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Double> onDouble = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Drop.Single> onDrop = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Shift.Secondary> onShiftSecondary = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.Drop.Full> onDropAll = (click) -> {};
    @Getter @Setter protected Consumer<ClickInventoryEvent.NumberPress> onNumber = (click) -> {};

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
