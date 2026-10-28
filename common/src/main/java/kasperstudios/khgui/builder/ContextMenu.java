package kasperstudios.khgui.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating context menus (right-click menus).
 */
public class ContextMenu {
    private final List<MenuElement> elements = new ArrayList<>();

    /**
     * Adds a simple action item to the menu.
     * 
     * @param label  The text to display
     * @param action The action to run when clicked
     * @return This builder
     */
    public ContextMenu addItem(String label, Runnable action) {
        elements.add(new ItemElement(label, action));
        return this;
    }

    public <T> ContextMenu addItem(String label, T context, Consumer<T> action) {
        elements.add(new ItemElement(label, () -> action.accept(context)));
        return this;
    }

    public ContextMenu addSeparator() {
        elements.add(new SeparatorElement());
        return this;
    }

    public ContextMenu addSubMenu(String label, Consumer<ContextMenu> menuBuilder) {
        ContextMenu subMenu = new ContextMenu();
        menuBuilder.accept(subMenu);
        elements.add(new SubMenuElement(label, subMenu));
        return this;
    }

    public List<MenuElement> getElements() {
        return elements;
    }

    public interface MenuElement {
    }

    public static class ItemElement implements MenuElement {
        public final String label;
        public final Runnable action;

        public ItemElement(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    public static class SeparatorElement implements MenuElement {
    }

    public static class SubMenuElement implements MenuElement {
        public final String label;
        public final ContextMenu subMenu;

        public SubMenuElement(String label, ContextMenu subMenu) {
            this.label = label;
            this.subMenu = subMenu;
        }
    }
}
