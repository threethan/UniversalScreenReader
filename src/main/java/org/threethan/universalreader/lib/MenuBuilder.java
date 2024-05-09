// Originally created for Adv. Java Programming on 2/6/24
// Last updated 3/16/24

package org.threethan.universalreader.lib;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Helper class to create context menus, with chainable methods
 * @author Ethan Medeiros
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MenuBuilder {
    private final List<MenuItem> items = new ArrayList<>();
    private String text;
    public MenuBuilder() {}
    public MenuBuilder(String text) {
        setText(text);
    }

    /**
     * Adds an item to the menu
     * @param text Item text
     * @param onAction Item action
     */
    public MenuBuilder addItem(String text, EventHandler<ActionEvent> onAction) {
        return addItem(text, null, null, onAction);
    }
    /**
     * Adds an item to the menu
     * @param text Item text
     * @param keyCombination Key combination string for accelerator
     * @param onAction Item action
     */
    public MenuBuilder addItem(String text, String keyCombination, EventHandler<ActionEvent> onAction) {
        return addItem(text, null, keyCombination, onAction);
    }
    /**
     * Adds an item to the menu
     * @param drawable Node drawable item
     * @param onAction Item action
     */
    public MenuBuilder addItem(Node drawable, EventHandler<ActionEvent> onAction) {
        return addItem(null, drawable, null, onAction);
    }
    /**
     * Adds an item to the menu
     * @param drawable Node drawable item
     * @param keyCombination Key combination string for accelerator
     * @param onAction Item action
     */
    public MenuBuilder addItem(Node drawable, String keyCombination, EventHandler<ActionEvent> onAction) {
        return addItem(null, drawable, keyCombination, onAction);
    }
    /**
     * Adds an item to the menu
     * @param text Item text
     * @param drawable Node drawable item
     * @param onAction Item action
     */
    public MenuBuilder addItem(String text, Node drawable, EventHandler<ActionEvent> onAction) {
        return addItem(text, drawable,null, onAction);
    }
    /**
     * Adds an item to the menu
     * @param text Item text
     * @param drawable Node drawable item
     * @param keyCombination Key combination string for accelerator
     * @param onAction Item action
     */
    public MenuBuilder addItem(String text, Node drawable, String keyCombination, EventHandler<ActionEvent> onAction) {
        MenuItem menuItem = new MenuItem(text, drawable);
        menuItem.setOnAction(onAction);
        if (keyCombination != null)
            menuItem.setAccelerator(KeyCombination.keyCombination(keyCombination));
        items.add(menuItem);
        return this;
    }

    /**
     * Adds a checkMenuItem which can be toggled
     * @param text Item text
     * @param defaultToggled If the item should already be toggled on
     * @param onToggle Action to be called when the item's state changes
     */
    public MenuBuilder addToggle(String text, boolean defaultToggled, Consumer<Boolean> onToggle) {
        return addToggle(text, null, defaultToggled, onToggle);
    }
    /**
     * Adds a checkMenuItem which can be toggled
     * @param text Item text
     * @param keyCombination Key combination string for accelerator
     * @param defaultToggled If the item should already be toggled on
     * @param onToggle Action to be called when the item's state changes
     */
    public MenuBuilder addToggle(String text, String keyCombination, boolean defaultToggled, Consumer<Boolean> onToggle) {
        CheckMenuItem menuItem = new CheckMenuItem(text);
        menuItem.setSelected(defaultToggled);
        menuItem.setOnAction(e -> onToggle.accept(menuItem.isSelected()));
        if (keyCombination != null)
            menuItem.setAccelerator(KeyCombination.keyCombination(keyCombination));
        items.add(menuItem);
        return this;
    }

    /**
     * Adds a set of exclusively-selectable radio items to the menu
     * @param items Collection of items for each button (Maybe string or use toString)
     * @param defaultItem Item to be initially selected
     * @param onSelectionChanged Action to be called when the selected item is changed
     */
    public <T> MenuBuilder addSelectionSet(Collection<T> items, T defaultItem, Consumer<T> onSelectionChanged) {
        ToggleGroup selectionGroup = new ToggleGroup();
        for (T item : items) {
            RadioMenuItem radioMenuItem = new RadioMenuItem(item.toString());
            radioMenuItem.setToggleGroup(selectionGroup);
            radioMenuItem.setOnAction(e -> onSelectionChanged.accept(item));
            if (Objects.equals(item, defaultItem)) radioMenuItem.setSelected(true);
            this.items.add(radioMenuItem);
        }
        return this;
    }
    /**
     * Adds a set of exclusively-selectable radio items as a submenu to the menu
     * @param name String name of the submenu
     * @param items Collection of items for each button (Maybe string or use toString)
     * @param defaultItem Item to be initially selected
     * @param onSelectionChanged Action to be called when the selected item is changed
     */
    public <T> MenuBuilder addSelectionSetSubmenu(String name, Collection<T> items, T defaultItem, Consumer<T> onSelectionChanged) {
        return addSubmenu(new MenuBuilder(name).addSelectionSet(items, defaultItem, onSelectionChanged));
    }

    /**
     * Adds a separator to the menu; if and only if the most recent item is not already a separator.
     * Will be removed if it's the last item in the menu
     */
    public MenuBuilder addSeparator() {
        if (!items.isEmpty() && !(items.get(items.size()-1) instanceof SeparatorMenuItem)) {
            MenuItem menuItem = new SeparatorMenuItem();
            items.add(menuItem);
        }
        return this;
    }

    /** Perform actions to prep for building a menu, such as removing floating separators */
    private void preBuild() {
        if (!items.isEmpty() && (items.get(items.size()-1) instanceof SeparatorMenuItem))
            items.remove(items.size()-1);
    }

    /**
     * Builds as a javafx context menu (terminating operation)
     * Key shortcuts may NOT work in context menus
     * @return Context menu object
     */
    public ContextMenu buildContextMenu() {
        preBuild();
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(items);
        return contextMenu;
    }

    /**
     * Adds the menu as a contextmenu on a node (recommended: pane or scene).
     * Spawns where the mouse is clicked.
     * @param node Node which the menu can be opened by clicking on
     */
    public MenuBuilder addContextMenuToView(Node node) {
        final ContextMenu contextMenu = buildContextMenu();
        node.setOnMouseClicked(e -> contextMenu.show(node, e.getScreenX(), e.getScreenY()));
        return this;
    }

    /**
     * Adds the menu as a contextmenu on a node with a condition for visibility
     * @param node Node which the menu can be opened by clicking on
     */
    public MenuBuilder addContextMenuToView(Node node, Callable<Boolean> shouldOpen) {
        final ContextMenu contextMenu = buildContextMenu();
        node.setOnMouseClicked(e -> {
            try {
                if (shouldOpen.call()) {
                    contextMenu.show(node, e.getScreenX(), e.getScreenY());
                }
            } catch (Exception ex) {
                //Do nothing if visibility can't be calculated
            }
        });
        return this;
    }

    /**
     * Sets the text when used as an item
     * @param text Text for the button to open this menu
     * @return Chainable reference to self
     */
    public MenuBuilder setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Returns a menu of this builder (terminating operation)
     * @return JavaFX Menu of this builder
     */
    public Menu buildMenu() {
        preBuild();
        Menu menu = new Menu();
        menu.getItems().addAll(items);
        menu.setText(text);
        return menu;
    }

    /**
     * Returns a menu bar of this builder (terminating operation)
     * @throws RuntimeException if menu contains any non-menu items
     * @return JavaFX MenuBar of this builder
     */
    public MenuBar buildMenuBar() {
        preBuild();
        MenuBar menuBar = new MenuBar();
        for (MenuItem item : items) {
            if (item instanceof Menu) menuBar.getMenus().add((Menu) item);
            else throw new RuntimeException("Tried creating a menu bar with one or more non-menu items");
        }
        return menuBar;
    }

    /**
     * Adds a submenu
     * @param text Text for the submenu item
     * @param subMenu Submenu builder object
     */
    public MenuBuilder addSubmenu(String text, MenuBuilder subMenu) {
        Menu menu = subMenu.buildMenu();
        menu.setText(text);
        items.add(menu);
        return this;
    }

    /**
     * Adds a submenu
     * @param subMenu Submenu builder object
     */
    public MenuBuilder addSubmenu(MenuBuilder subMenu) {
        Menu menu = subMenu.buildMenu();
        menu.setText(subMenu.text);
        items.add(menu);
        return this;
    }

    /**
     * Adds a submenu
     * @param text Text for the submenu menu item
     * @param subMenu Submenu Menu object
     */
    public MenuBuilder addSubmenu(String text, Menu subMenu) {
        subMenu.setText(text);
        items.add(subMenu);
        return this;
    }

    /**
     * Adds a submenu
     * @param subMenu Submenu Menu object
     */
    public MenuBuilder addSubmenu(Menu subMenu) {
        items.add(subMenu);
        return this;
    }


}
