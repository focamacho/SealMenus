package com.focamacho.sealmenus;

import com.focamacho.sealmenus.item.ClickableItem;
import com.focamacho.sealmenus.item.MenuItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Accessors(chain = true)
public class ChestMenu {

    private static final MenuItem dummyItem = ClickableItem.create(ItemStack.empty());

    //Base properties
    @Getter private final String title;
    @Getter private final int rows;
    protected final Object plugin;

    //Global actions
    @Getter @Setter private Consumer<InteractInventoryEvent.Open> onOpen = (interact) -> {};
    @Getter @Setter private Consumer<InteractInventoryEvent.Close> onClose = (interact) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent> onClick = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Primary> onPrimary = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Middle> onMiddle = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Secondary> onSecondary = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Shift.Primary> onShiftPrimary = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Double> onDouble = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Drop.Single> onDrop = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Shift.Secondary> onShiftSecondary = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.Drop.Full> onDropAll = (click) -> {};
    @Getter @Setter private Consumer<ClickInventoryEvent.NumberPress> onNumber = (click) -> {};

    //Items
    protected Map<Integer, MenuItem> items = new HashMap<>();

    //Sponge Inventory
    @Getter protected Inventory inventory;
    protected List<Player> playersViewing = Lists.newArrayList();
    protected final Set<Integer> slotsRequiringUpdate = Sets.newHashSet();
    private Task updateItemsTask = null;

    ChestMenu(String title, int rows, Object plugin) {
        if(rows <= 0 || rows > 6) throw new IllegalArgumentException("The number of rows for a menu must be >= 1 && <= 6.");

        this.title = Objects.requireNonNull(title);
        this.rows = rows;
        this.plugin = Objects.requireNonNull(plugin);
    }

    /**
     * Add an item to this menu.
     * @param item the item to add.
     * @param slot the slot to add the item to.
     * @return this menu.
     */
    public ChestMenu addItem(MenuItem item, int slot) {
        if(slot <= 0 || slot >= this.rows * 9) throw new IllegalArgumentException("The slot can't be less than zero or greater than the inventory size.");

        items.put(slot, item);
        requireUpdate(slot);
        return this;
    }

    /**
     * Remove an item of this menu.
     * @param slot the slot of the item to remove.
     * @return this menu.
     */
    public ChestMenu removeItem(int slot) {
        this.items.remove(slot);
        requireUpdate(slot);
        return this;
    }

    /**
     * Returns the item in the
     * provided slot.
     * @param slot the item slot.
     * @return the item in the
     * slot or null if there is no item.
     */
    public MenuItem getItem(Integer slot) {
        return items.get(slot);
    }

    /**
     * Returns all items of this menu.
     * @return the items of this menu.
     */
    public Map<Integer, MenuItem> getItems() {
        return Collections.unmodifiableMap(this.items);
    }

    /**
     * Returns the ItemStack inside the inventory.
     *
     * This method returns the ItemStack, not the
     * MenuItem.
     *
     * @param slot the slot desired to get the ItemStack from.
     * @return the ItemStack in the inventory, or ItemStack.empty() if the slot is
     * invalid or empty.
     */
    public ItemStack getItemStack(Integer slot) {
        for (Inventory inventorySlot : this.inventory.slots()) {
            Optional<SlotIndex> slotIndex = inventorySlot.getInventoryProperty(SlotIndex.class);
            if(slotIndex.isPresent() && Objects.equals(slotIndex.get().getValue(), slot)) {
                return inventorySlot.peek().orElse(ItemStack.empty());
            }
        }

        return ItemStack.empty();
    }

    /**
     * Returns all items inside the inventory.
     *
     * This method returns the ItemStacks, not the
     * MenuItems.
     *
     * @return all the ItemStacks inside the menu inventory.
     */
    public Map<Integer, ItemStack> getItemStacks() {
        Map<Integer, ItemStack> items = new HashMap<>();

        this.inventory.slots().forEach(slot -> {
            if(slot.peek().isPresent() && !slot.peek().get().isEmpty()) {
                Optional<SlotIndex> slotIndex = slot.getInventoryProperty(SlotIndex.class);
                slotIndex.ifPresent(index -> items.put(index.getValue(), slot.peek().get()));
            }
        });

        return Collections.unmodifiableMap(items);
    }

    /**
     * Check if a slot contains
     * a menu item.
     *
     * @param slot the slot to check.
     * @return if contains or not an item.
     */
    public boolean containsItem(Integer slot) {
        return slot != null && items.containsKey(slot);
    }

    /**
     * Override all items of this menu.
     *
     * @param items the items to set.
     */
    public ChestMenu setItems(Map<Integer, MenuItem> items) {
        this.items.clear();
        items.forEach((slot, item) -> addItem(item, slot));
        requireUpdate(null);
        return this;
    }

    /**
     * Remove all items of this menu.
     */
    public ChestMenu clearItems() {
        items.clear();
        requireUpdate(null);
        return this;
    }

    /**
     * Updates the inventory with the current
     * items of this menu.
     */
    public void update() {
        if(this.inventory == null) {
            this.inventory = Inventory.builder()
                    .of(InventoryArchetypes.CHEST)
                    .property(InventoryTitle.of(Text.of(this.title)))
                    .property(InventoryDimension.of(this.rows, 9))
                    .listener(ClickInventoryEvent.class, ce -> {
                        if(ce.getSlot().isPresent()) {
                            Integer slot = ce.getSlot().get().getInventoryProperty(SlotIndex.class).get().getValue();
                            if(slot == null) slot = -1;
                            if(slot < 9 * this.rows) {
                                ce.setCancelled(true);

                                this.getOnClick().accept(ce);

                                MenuItem item = getItem(slot);
                                if(item == null) item = dummyItem;

                                if(ce instanceof ClickInventoryEvent.Double) {
                                    this.getOnDouble().accept((ClickInventoryEvent.Double) ce);
                                    item.getOnDouble().accept((ClickInventoryEvent.Double) ce);
                                } else if(ce instanceof ClickInventoryEvent.Shift.Primary) {
                                    this.getOnShiftPrimary().accept((ClickInventoryEvent.Shift.Primary) ce);
                                    item.getOnShiftPrimary().accept((ClickInventoryEvent.Shift.Primary) ce);
                                } else if(ce instanceof ClickInventoryEvent.Shift.Secondary) {
                                    this.getOnShiftSecondary().accept((ClickInventoryEvent.Shift.Secondary) ce);
                                    item.getOnShiftSecondary().accept((ClickInventoryEvent.Shift.Secondary) ce);
                                } else if(ce instanceof ClickInventoryEvent.Primary) {
                                    this.getOnPrimary().accept((ClickInventoryEvent.Primary) ce);
                                    item.getOnPrimary().accept((ClickInventoryEvent.Primary) ce);
                                } else if(ce instanceof ClickInventoryEvent.Middle) {
                                    this.getOnMiddle().accept((ClickInventoryEvent.Middle) ce);
                                    item.getOnMiddle().accept((ClickInventoryEvent.Middle) ce);
                                } else if(ce instanceof ClickInventoryEvent.Secondary) {
                                    this.getOnSecondary().accept((ClickInventoryEvent.Secondary) ce);
                                    item.getOnSecondary().accept((ClickInventoryEvent.Secondary) ce);
                                } else if(ce instanceof ClickInventoryEvent.Drop.Full) {
                                    this.getOnDropAll().accept((ClickInventoryEvent.Drop.Full) ce);
                                    item.getOnDropAll().accept((ClickInventoryEvent.Drop.Full) ce);
                                } else if(ce instanceof ClickInventoryEvent.Drop) {
                                    this.getOnDrop().accept((ClickInventoryEvent.Drop.Single) ce);
                                    item.getOnDrop().accept((ClickInventoryEvent.Drop.Single) ce);
                                } else if(ce instanceof ClickInventoryEvent.NumberPress) {
                                    this.getOnNumber().accept((ClickInventoryEvent.NumberPress) ce);
                                    item.getOnNumber().accept((ClickInventoryEvent.NumberPress) ce);
                                }
                            }
                        }
                    })
                    .listener(InteractInventoryEvent.class, ie -> {
                        if(ie instanceof InteractInventoryEvent.Open) {
                            this.getOnOpen().accept((InteractInventoryEvent.Open) ie);
                            Task.builder().execute(this::handlesUpdateItemsTask).submit(this.plugin);
                        } else if(ie instanceof InteractInventoryEvent.Close) {
                            this.getOnClose().accept((InteractInventoryEvent.Close) ie);
                            if(ie.getSource() instanceof Player) playersViewing.remove((Player) ie.getSource());
                        }
                    })
                    .build(this.plugin);
        }

        for (Inventory slot : this.inventory.slots()) {
            Integer slotIndex = slot.getInventoryProperty(SlotIndex.class).get().getValue();
            ItemStack slotStack = slot.peek().orElse(ItemStack.empty());

            if(containsItem(slotIndex)) {
                ItemStack stack = getItem(slotIndex).getItem();
                if(slotStack != stack) Task.builder().execute(() -> slot.set(stack)).submit(this.plugin);
            } else if(!slotStack.isEmpty()) {
                Task.builder().execute(slot::clear).submit(this.plugin);
            }
        }

        slotsRequiringUpdate.clear();
    }

    /**
     * Updates a specific slot of
     * this menu.
     */
    public void update(int slot) {
        if(this.inventory == null) update();

        for (Inventory inventorySlot : this.inventory.slots()) {
            Integer slotIndex = inventorySlot.getInventoryProperty(SlotIndex.class).get().getValue();
            if(Objects.equals(slot, slotIndex)) {
                if(containsItem(slot)) {
                    ItemStack stack = getItem(slot).getItem();
                    if (inventorySlot.peek().orElse(ItemStack.empty()) != stack)
                        Task.builder().execute(() -> inventorySlot.set(stack)).submit(this.plugin);
                } else Task.builder().execute(inventorySlot::clear).submit(this.plugin);
                break;
            }
        }

        slotsRequiringUpdate.remove(slot);
    }

    /**
     * Mark a slot as required update.
     * Slots are only updated if there is
     * a player using the inventory, otherwise
     * it will only be updated the next time a player
     * opens it.
     *
     * @param slot the slot to mark. Can be null
     *             to require an update for the
     *             entire inventory.
     */
    public void requireUpdate(Integer slot) {
        if(this.inventory != null) {
            if(hasViewers())
                if(slot == null) update();
                else update(slot);
            else this.slotsRequiringUpdate.add(slot);
        }
    }

    /**
     * Open this menu for a player.
     */
    public void open(Player player) {
        if(this.inventory == null) update();

        Task.builder().execute(() -> {
            if(slotsRequiringUpdate.size() > 0)
                if(slotsRequiringUpdate.contains(null)) update();
                else slotsRequiringUpdate.forEach(this::update);
            player.closeInventory();
            player.openInventory(this.inventory).ifPresent(container -> playersViewing.add(player));
        }).submit(this.plugin);
    }

    /**
     * Returns if someone is viewing this menu.
     *
     * @return true if there is a player using this
     * menu.
     */
    public boolean hasViewers() {
        return playersViewing.size() > 0;
    }

    /**
     * Creates a copy of this menu.
     * @return the copy of this menu.
     */
    public ChestMenu copy() {
        ChestMenu copy = new ChestMenu(this.title, this.rows, this.plugin);

        copy.setOnOpen(this.getOnOpen());
        copy.setOnClose(this.getOnClose());

        copy.setOnClick(this.getOnClick());
        copy.setOnPrimary(this.getOnPrimary());
        copy.setOnSecondary(this.getOnSecondary());
        copy.setOnDrop(this.getOnDrop());
        copy.setOnDropAll(this.getOnDropAll());
        copy.setOnMiddle(this.getOnMiddle());
        copy.setOnNumber(this.getOnNumber());
        copy.setOnShiftPrimary(this.getOnShiftPrimary());
        copy.setOnShiftSecondary(this.getOnShiftSecondary());
        copy.setOnDouble(this.getOnDouble());

        getItems().forEach((slot, item) -> copy.addItem(item.copy(), slot));
        return copy;
    }

    protected void handlesUpdateItemsTask() {
        if(this.updateItemsTask == null && hasViewers())
            updateItemsTask = Task.builder().intervalTicks(1).execute((task) -> {
                items.forEach((slot, item) -> {
                    if(item.update()) requireUpdate(slot);
                });

                if(!hasViewers()) {
                    task.cancel();
                    this.updateItemsTask = null;
                }
            }).submit(this.plugin);
    }

}