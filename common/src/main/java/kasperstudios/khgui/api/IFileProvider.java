package kasperstudios.khgui.api;

import java.util.List;
import java.util.function.Consumer;

/**
 * Provider interface for file system access.
 * KasHub (or any mod) implements this to supply file listings.
 */
public interface IFileProvider {

    /**
     * File category for organizing the file tree.
     */
    record FileCategory(String id, String displayName, String icon, List<String> files) {
    }

    /**
     * Returns all file categories to display.
     * Each category has an ID, display name, icon, and list of file names.
     */
    List<FileCategory> getCategories();

    /**
     * Called when a file is selected.
     */
    void onFileSelected(String fileName, String categoryId);

    /**
     * Called when user requests creating a new file.
     * 
     * @param fileName   the name of the file to create
     * @param categoryId the category to create it in (nullable for default)
     * @param callback   called with true on success, false on failure
     */
    default void createFile(String fileName, String categoryId, Consumer<Boolean> callback) {
        callback.accept(false);
    }

    /**
     * Returns whether the user can create files in a given category.
     */
    default boolean canCreateFiles(String categoryId) {
        return false;
    }

    /**
     * Returns the list of categories where files can be created.
     */
    default List<String> getCreatableCategories() {
        return List.of();
    }

    /**
     * Called when user requests deleting a file.
     * 
     * @param fileName   the name of the file to delete
     * @param categoryId the category the file belongs to
     * @param callback   called with true on success, false on failure
     */
    default void deleteFile(String fileName, String categoryId, Consumer<Boolean> callback) {
        callback.accept(false);
    }

    /**
     * Returns a custom icon for a specific file (emoji or texture path).
     */
    default String getFileIcon(String fileName, String categoryId) {
        return null; // Use default based on extension
    }
}
