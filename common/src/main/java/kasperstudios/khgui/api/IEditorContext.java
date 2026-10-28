package kasperstudios.khgui.api;

import java.util.List;

/**
 * Context interface for editor screen.
 * KasHub implements this to provide data to KHGui.
 */
public interface IEditorContext {
    /**
     * Get the title to display in the title bar.
     * 
     * @return Title text (e.g., "KasHub - script.lua")
     */
    String getTitleText();

    /**
     * Get the list of open tabs.
     * 
     * @return List of tab names
     */
    List<String> getOpenTabs();

    /**
     * Get the currently active tab index.
     * 
     * @return Active tab index
     */
    int getActiveTabIndex();

    /**
     * Get the sidebar title.
     * 
     * @return Sidebar title (e.g., "EXPLORER", "SCRIPTS")
     */
    String getSidebarTitle();

    /**
     * Get the status bar text.
     * 
     * @return Status bar text (e.g., "Ln 1, Col 1 UTF-8 Lua")
     */
    String getStatusBarText();

    /**
     * Get the current file language/type.
     * 
     * @return Language name (e.g., "Java", "Lua", "JavaScript")
     */
    String getLanguage();

    /**
     * Gets the current file content.
     */
    String getContent();

    /**
     * Updates in-memory content of the active tab without saving to disk.
     * Called when switching tabs or when editor content changes.
     */
    default void updateContent(String content) {
    }

    /**
     * Updates in-memory content of a specific tab without saving to disk.
     */
    default void updateContent(int tabIndex, String content) {
        updateContent(content);
    }

    /**
     * Checks if the tab at the given index has unsaved modifications.
     */
    default boolean isTabDirty(int index) {
        return false;
    }

    /**
     * Save the current file content.
     * 
     * @param content The text to save
     */
    void save(String content);

    /**
     * Called when a tab is requested to close.
     * 
     * @param index Tab index
     */
    void closeTab(int index);

    /**
     * Called to switch to a specific tab.
     * 
     * @param index Tab index
     */
    void switchTab(int index);

    /**
     * Called to open a file.
     * 
     * @param fileName File name/path
     */
    void openFile(String fileName);

    /**
     * Called when the editor screen is closed.
     */
    default void onClose() {
    }

    /**
     * Gets the completion provider for autocomplete.
     * Return null to disable autocomplete.
     */
    default ICompletionProvider getCompletionProvider() {
        return null;
    }

    /**
     * Gets the hover provider for tooltips.
     * Return null to disable hover tooltips.
     */
    default IHoverProvider getHoverProvider() {
        return null;
    }

    /**
     * Get custom title bar icon/logo text.
     * 
     * @return Icon text (e.g., "⚡ KHGUI", "🔥 KASHUB") or null for default
     */
    default String getTitleIcon() {
        return null;
    }

    /**
     * Gets the logo texture to display in the title bar as "namespace:path" string.
     * If provided, this overrides {@link #getTitleIcon()}.
     * Example: "kashub:textures/gui/logo.png"
     */
    default String getTitleLogoTexture() {
        return null;
    }

    /**
     * Gets the width of the logo texture (full PNG size in pixels).
     */
    default int getTitleLogoWidth() {
        return 100;
    }

    /**
     * Gets the height of the logo texture (full PNG size in pixels).
     */
    default int getTitleLogoHeight() {
        return 20;
    }

    /**
     * Gets a map of panel icon textures as "namespace:path" strings, keyed by panel ID.
     * Example: Map.of("explorer", "kashub:textures/icons/explorer.png")
     */
    default java.util.Map<String, String> getCustomPanelIconTextures() {
        return java.util.Collections.emptyMap();
    }

    /**
     * Gets a list of custom context-provided side panels (e.g. Marketplace).
     * By default, FileTreePanel is registered by KHGui automatically.
     */
    default List<IEditorPanel> getCustomSidePanels() {
        return java.util.Collections.emptyList();
    }

    /**
     * Gets a list of custom context-provided toolbar buttons.
     */
    default List<IToolbarButton> getCustomToolbarButtons() {
        return java.util.Collections.emptyList();
    }

    /**
     * Gets a map of script environment variables.
     */
    default java.util.Map<String, String> getEnvironmentVariables() {
        return java.util.Collections.emptyMap();
    }

    /**
     * Gets a list of user-defined script variables.
     */
    default List<String> getUserVariables() {
        return java.util.Collections.emptyList();
    }

    /**
     * Gets members of a specific object for code completion.
     */
    default List<String> getObjectMembers(String objectName) {
        return java.util.Collections.emptyList();
    }

    /**
     * Gets the current execution line (1-indexed).
     * 
     * @return Execution line or -1 if none
     */
    default int getExecutionLine() {
        return -1;
    }

    /**
     * Gets set of line numbers containing errors (1-indexed).
     */
    default java.util.Set<Integer> getErrorLines() {
        return java.util.Collections.emptySet();
    }

    /**
     * Gets set of line numbers containing breakpoints (1-indexed).
     */
    default java.util.Set<Integer> getBreakpoints() {
        return java.util.Collections.emptySet();
    }

    /**
     * Gets the file provider for the editor.
     * Return null to disable file explorer functionality.
     */
    default IFileProvider getFileProvider() {
        return null;
    }

    /**
     * Gets the list of menus to display in the title bar (e.g. File, Edit).
     */
    default List<IEditorMenu> getTitleMenus() {
        return java.util.Collections.emptyList();
    }

    /**
     * Gets direct actions for the title bar (immediate buttons).
     */
    default List<ITitleAction> getTitleActions() {
        return java.util.Collections.emptyList();
    }

    /**
     * Creates a new untitled script buffer.
     */
    default void createNewUntitled() {}

    /**
     * Returns true if the screen should refresh its menus, actions, and panels.
     */
    default boolean shouldRefreshLayout() {
        return false;
    }
}
