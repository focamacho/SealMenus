# Seal Menus
API for creating Inventory Menus for Bukkit and Sponge.

## Index
Atalhos para certas seções desse documento.
- [Seal Menus](#Seal-Menus)
    * [First Steps](#First-Steps)
    * [Using the API](#Using-the-API)
        + [Chest Menus](#Chest-Menus)
        + [Pageable Chest Menus](#Pageable-Chest-Menus)

## First Steps
To start using the API, you'll need to setup the dependency in your project. Here is some examples for Gradle and Maven:
<br>
Do not forget to replace *VERSION* with the desired version of the API. Check what is the latest version in the [releases](https://github.com/Seal-Island/SealMenus/releases) tab.

**Maven**
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.seal-island.sealmenus</groupId>
    <artifactId>bukkit</artifactId> <!-- Replace "bukkit" with "sponge" if desired. -->
    <version>VERSION</version>
</dependency>
```

**Gradle**
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.seal-island.sealmenus:bukkit:VERSION' // Replace "bukkit" with "sponge" if desired.
}
```

## Using the API

All you need is creating a new Menu instance through the `SealMenus` class, add the items and that's it.

### Chest Menus

For creating a simple Chest Menu all you need to do is use `SealMenus#createChestMenu`.

Example:
```java
//The package will be com.focamacho.sealmenus.sponge for Sponge.
import com.focamacho.sealmenus.bukkit.*;
import com.focamacho.sealmenus.bukkit.item.*;

public class ExamplePlugin {

    {
        //Creates the menu - Parameters: Title (String), Rows (int), Plugin Instance (JavaPlugin)
        ChestMenu menu = SealMenus.createChestMenu("Cool Menu", 6, pluginObject);
    
        //Creates a simple clickable item - Parameters: Item (ItemStack) 
        MenuItem clickableItem = ClickableItem.create(new ItemStack(Material.SPONGE))
                .setOnPrimary(click -> {
                    click.getWhoClicked().sendMessage("You clicked this item.");
                });
        
        //Creates an item that loops between a List of ItemStacks, displaying a
        //different item after an amount of time. Parameters - Items (List<ItemStack), Ticks (int)
        LoopableItem loopableItem = LoopableItem.create(Arrays.asList(new ItemStack(Material.DIAMOND), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GOLD_INGOT)), 20);
        
        //Add the items to the menu. Parameters - Item (MenuItem), Slot (int)
        menu.addItem(clickableItem, 22);
        menu.addItem(loopableItem, 31);
        
        //Opens the menu for a player
        menu.open(player);
    }
    
}
```

### Pageable Chest Menus

You can also create a menu that auto-creates new pages based on the amount of items inserted.
<br>
This type of menu has two methos for inserting items. One for the base items, available in all pages of the menu, and other for the items that will go into specific slots, and when all slots get filled it auto-creates a new page.

Example:
```java
//The package will be com.focamacho.sealmenus.sponge for Sponge.
import com.focamacho.sealmenus.bukkit.*;
import com.focamacho.sealmenus.bukkit.item.*;

public class ExamplePlugin {

    {
        //Creates the menu - Parameters: Title (String), Rows (int), Items Slots (int[]), Plugin Instance (JavaPlugin)
        PageableChestMenu menu = SealMenus.createPageableChestMenu("Cool Menu", 6, new int[]{20, 21, 22, 23, 24, 29, 30, 31, 32, 33}, pluginObject);

        //Creates an item that loops between a List of ItemStacks, displaying a
        //different item after an amount of time. Parameters - Items (List<ItemStack), Ticks (int)
        LoopableItem loopableItem = LoopableItem.create(Arrays.asList(new ItemStack(Material.DIAMOND), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.GOLD_INGOT)), 20);

        //Add the item to the menu. Parameters - Item (MenuItem), Slot (int)
        menu.addItem(loopableItem, 4);
                
        //Add items that will go into the pre-set slots in the menu, when all slots get filled, a new page is automatically created.
        for(int i = 1; i <= 30; i++) {
            ItemStack item = new ItemStack(Material.DRAGON_EGG, i);
            //Parameters - Item (MenuItem)
            menu.addPageableItem(ClickableItem.create(item));
        }
        
        //Sets the item used for go to the next and previous page. Parameters - Item (ItemStack), Slot (int)
        menu.setPreviousPageItem(new ItemStack(Material.ARROW), 36);
        menu.setNextPageItem(new ItemStack(Material.ARROW), 44);

        //Opens the menu for a player
        menu.open(player);
    }
    
}
```