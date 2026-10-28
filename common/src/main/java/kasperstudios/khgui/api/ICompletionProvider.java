package kasperstudios.khgui.api;

import java.util.List;

/**
 * Provides code completion suggestions.
 * Implement this to provide autocomplete for your scripting language.
 */
public interface ICompletionProvider {
    
    /**
     * Gets completion suggestions for the current cursor position.
     * 
     * @param line Current line text
     * @param column Cursor column position
     * @param prefix Text before cursor (for filtering)
     * @return List of completion items
     */
    List<CompletionItem> getCompletions(String line, int column, String prefix);
    
    /**
     * Represents a single completion suggestion.
     */
    class CompletionItem {
        public final String label;           // Display text
        public final String insertText;      // Text to insert
        public final CompletionKind kind;    // Type of completion
        public final String detail;          // Additional info
        public final String documentation;   // Full documentation
        
        public CompletionItem(String label, String insertText, CompletionKind kind, String detail, String documentation) {
            this.label = label;
            this.insertText = insertText != null ? insertText : label;
            this.kind = kind;
            this.detail = detail;
            this.documentation = documentation;
        }
        
        public CompletionItem(String label, CompletionKind kind) {
            this(label, label, kind, null, null);
        }
    }
    
    /**
     * Type of completion item.
     */
    enum CompletionKind {
        METHOD,      // Function/method
        FUNCTION,    // Function
        VARIABLE,    // Variable
        FIELD,       // Object field
        CLASS,       // Class
        KEYWORD,     // Language keyword
        SNIPPET,     // Code snippet
        PROPERTY,    // Property
        CONSTANT,    // Constant
        ENUM,        // Enum value
        MODULE       // Module/namespace
    }
}
