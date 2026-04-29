package com.focamacho.sealmenus.bukkit;

import com.focamacho.sealmenus.bukkit.item.MenuItem;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.logging.Level;

/**
 * A pageable chest menu whose page items are loaded lazily via a
 * {@link CompletableFuture}-returning provider. The provider receives a
 * page index and returns the items for that page; the menu calls it
 * on demand whenever the player navigates or whenever {@link #refresh()}
 * is invoked.
 *
 * <p>The total item count must be supplied up front (and may be updated
 * with {@link #setTotalItemCount(int)}) so the menu knows how many pages
 * exist without enumerating them. If the count shrinks below the
 * currently-viewed page, the menu redirects to the last available page
 * and re-fetches its items.
 *
 * <p>All state mutations happen on the server main thread; the provider's
 * future may complete on any thread. A monotonic load-generation token
 * discards the result of any load that has been superseded.
 *
 * <p><b>Limitation:</b> only one viewer at a time. The mirror-menu
 * pattern used by {@link PageableChestMenu} is not supported here, and
 * attempting to open a second concurrent viewer throws
 * {@link UnsupportedOperationException}.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@Accessors(chain = true)
public class LazyPageableChestMenu extends PageableChestMenu {

    @Getter private Function<Integer, CompletableFuture<List<MenuItem>>> pageItemProvider;
    @Getter private int totalItemCount;

    // Items currently loaded for the page this instance is showing.
    private List<MenuItem> currentPageItems = Lists.newArrayList();

    // Monotonic token; incremented on every loadPage call. Callbacks discard results for stale loads.
    private int loadGeneration = 0;

    protected LazyPageableChestMenu(Component title, int rows, int[] itemSlots, JavaPlugin plugin,
                                    Function<Integer, CompletableFuture<List<MenuItem>>> pageItemProvider,
                                    int totalItemCount) {
        super(title, rows, itemSlots, plugin);
        this.pageItemProvider = Objects.requireNonNull(pageItemProvider, "pageItemProvider");
        if(totalItemCount < 0) throw new IllegalArgumentException("totalItemCount must be >= 0.");
        this.totalItemCount = totalItemCount;
    }

    protected LazyPageableChestMenu(String title, int rows, int[] itemSlots, JavaPlugin plugin,
                                    Function<Integer, CompletableFuture<List<MenuItem>>> pageItemProvider,
                                    int totalItemCount) {
        this(Component.text(title), rows, itemSlots, plugin, pageItemProvider, totalItemCount);
    }

    /**
     * Update the total item count and recompute the page count.
     * If the player is currently viewing a page that no longer exists
     * (e.g. they were on page 5 and the new total only allows 3 pages),
     * the menu redirects to the last available page and re-fetches its items.
     *
     * @param totalItemCount the new total item count.
     * @return this menu.
     */
    public LazyPageableChestMenu setTotalItemCount(int totalItemCount) {
        if(totalItemCount < 0) throw new IllegalArgumentException("totalItemCount must be >= 0.");
        this.totalItemCount = totalItemCount;
        int maxPage = Math.max(0, getPageCount() - 1);
        int currentPage = getCurrentPageIndex();
        if(currentPage > maxPage) {
            // Page shrunk away under us — reload items for the clamped page, then refresh the inventory.
            loadPage(maxPage, page -> {
                setCurrentPageIndex(page);
                update();
            });
        } else {
            // Page count may still have changed (next-button visibility, partially-filled last page) — refresh.
            requireUpdate(null);
        }
        return this;
    }

    /**
     * Replace the page-item provider.
     *
     * @param provider the new provider.
     * @return this menu.
     */
    public LazyPageableChestMenu setPageItemProvider(Function<Integer, CompletableFuture<List<MenuItem>>> provider) {
        this.pageItemProvider = Objects.requireNonNull(provider, "pageItemProvider");
        return this;
    }

    /**
     * Re-fetches the items for the page currently being viewed.
     *
     * @return this menu.
     */
    public LazyPageableChestMenu refresh() {
        loadPage(getCurrentPageIndex(), page -> update());
        return this;
    }

    @Override
    public int getPageCount() {
        return Math.max(1, (int) Math.ceil(totalItemCount / (double) getItemSlots().length));
    }

    @Override
    public boolean containsItem(Integer slot) {
        int index = pageableSlotIndex(slot);
        if(index >= 0) return index < currentPageItems.size();
        return super.containsItem(slot);
    }

    @Override
    public MenuItem getItem(Integer slot) {
        int index = pageableSlotIndex(slot);
        if(index >= 0) return index < currentPageItems.size() ? currentPageItems.get(index) : null;
        return super.getItem(slot);
    }

    @Override
    public Map.Entry<Integer, Integer> getPageableItemSlot(MenuItem item) {
        int idx = currentPageItems.indexOf(item);
        if(idx < 0) return null;
        return new AbstractMap.SimpleEntry<>(getCurrentPageIndex(), getItemSlots()[idx]);
    }

    @Override
    public List<MenuItem> getPageableItems() {
        return Collections.unmodifiableList(currentPageItems);
    }

    @Override
    public PageableChestMenu addPageableItem(MenuItem item, int index) {
        throw new UnsupportedOperationException("LazyPageableChestMenu items are provided by the page item provider.");
    }

    @Override
    public PageableChestMenu addPageableItem(MenuItem item) {
        throw new UnsupportedOperationException("LazyPageableChestMenu items are provided by the page item provider.");
    }

    @Override
    public PageableChestMenu removePageableItem(MenuItem item) {
        throw new UnsupportedOperationException("LazyPageableChestMenu items are provided by the page item provider.");
    }

    @Override
    public PageableChestMenu setPageableItems(List<MenuItem> items) {
        throw new UnsupportedOperationException("LazyPageableChestMenu items are provided by the page item provider.");
    }

    @Override
    public PageableChestMenu clearPageableItems() {
        throw new UnsupportedOperationException("LazyPageableChestMenu items are provided by the page item provider.");
    }

    @Override
    protected void goToNextPage() {
        if(isPageLocked()) return;
        setPageLocked(true);
        int next = getCurrentPageIndex() + 1;
        if(next >= getPageCount()) {
            scheduleCooldownUnlock();
            return;
        }
        loadPage(next, page -> {
            setCurrentPageIndex(page);
            update();
            scheduleCooldownUnlock();
        });
    }

    @Override
    protected void goToPreviousPage() {
        if(isPageLocked()) return;
        setPageLocked(true);
        int prev = getCurrentPageIndex() - 1;
        if(prev < 0) {
            scheduleCooldownUnlock();
            return;
        }
        loadPage(prev, page -> {
            setCurrentPageIndex(page);
            update();
            scheduleCooldownUnlock();
        });
    }

    @Override
    public void open(Player player, int page) {
        if(hasViewers() && (this.inventory == null || !this.inventory.getViewers().contains(player))) {
            throw new UnsupportedOperationException("LazyPageableChestMenu does not support multiple concurrent viewers.");
        }
        int target = Math.min(Math.max(0, page), Math.max(0, getPageCount() - 1));
        loadPage(target, p -> {
            setCurrentPageIndex(p);
            super.open(player, p);
        });
    }

    @Override
    protected void handleUpdateItems() {
        // Skip parent's iteration over the (unused) pageableItems list; iterate currentPageItems instead.
        getItems().forEach((slot, item) -> {
            if(item.update()) requireUpdate(slot);
        });
        for(int i = 0; i < currentPageItems.size(); i++) {
            MenuItem item = currentPageItems.get(i);
            if(item.update()) requireUpdate(getItemSlots()[i]);
        }
    }

    private void loadPage(int page, IntConsumer onComplete) {
        final int generation = ++loadGeneration;
        CompletableFuture<List<MenuItem>> future;
        try {
            future = pageItemProvider.apply(page);
        } catch(Throwable t) {
            this.plugin.getLogger().log(Level.WARNING, "LazyPageableChestMenu provider threw for page " + page, t);
            setPageLocked(false);
            return;
        }
        if(future == null) {
            this.plugin.getLogger().warning("LazyPageableChestMenu provider returned null for page " + page);
            setPageLocked(false);
            return;
        }
        future.whenComplete((items, error) -> {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if(generation != loadGeneration) return; // superseded by a newer load
                if(error != null || items == null) {
                    this.plugin.getLogger().log(Level.WARNING,
                            "LazyPageableChestMenu failed to load page " + page, error);
                    setPageLocked(false);
                    return;
                }
                int slotCount = getItemSlots().length;
                currentPageItems = items.size() > slotCount
                        ? Lists.newArrayList(items.subList(0, slotCount))
                        : Lists.newArrayList(items);
                onComplete.accept(page);
            });
        });
    }

    /**
     * @return the index into {@link #currentPageItems} for this slot, or -1 if
     *         the slot is null or not one of the pageable slots.
     */
    private int pageableSlotIndex(Integer slot) {
        if(slot == null) return -1;
        int[] slots = getItemSlots();
        for(int i = 0; i < slots.length; i++) if(slots[i] == slot) return i;
        return -1;
    }
}
