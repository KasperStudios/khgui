package kasperstudios.khgui.api;

/**
 * Provides hover information (tooltips) for code elements.
 */
public interface IHoverProvider {
    
    /**
     * Gets hover information for the word at cursor position.
     * 
     * @param line Current line text
     * @param column Cursor column
     * @param word Word under cursor
     * @return Hover info or null if none
     */
    HoverInfo getHoverInfo(String line, int column, String word);
    
    /**
     * Hover information to display.
     */
    class HoverInfo {
        public final String title;          // Title (e.g., function signature)
        public final String description;    // Description
        public final String documentation;  // Full documentation
        
        public HoverInfo(String title, String description, String documentation) {
            this.title = title;
            this.description = description;
            this.documentation = documentation;
        }
        
        public HoverInfo(String title, String description) {
            this(title, description, null);
        }
    }
}
