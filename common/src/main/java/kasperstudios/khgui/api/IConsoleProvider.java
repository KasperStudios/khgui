package kasperstudios.khgui.api;

import java.util.List;

/**
 * Interface for the host mod to provide console text, logs, and problem lists.
 * This powers the VS Code-style bottom panel (Terminal, Output, Problems).
 */
public interface IConsoleProvider {
    
    /**
     * Gets the list of problems (errors, warnings) for the Problems tab.
     * Often populated by a parser or language server.
     */
    List<ProblemEntry> getProblems();

    /**
     * Represents a single problem entry in the Problems tab.
     */
    class ProblemEntry {
        public final String file;
        public final int line;
        public final int column;
        public final String message;
        public final Severity severity;

        public ProblemEntry(String file, int line, int column, String message, Severity severity) {
            this.file = file;
            this.line = line;
            this.column = column;
            this.message = message;
            this.severity = severity;
        }

        public enum Severity {
            ERROR, WARNING, INFO
        }
    }

    /**
     * Gets the text for the Output tab (e.g., standard output, builds).
     */
    List<String> getOutputLines();

    /**
     * Gets the text for the Terminal tab (e.g., script execution prints/logs).
     */
    List<String> getTerminalLines();
    
    /**
     * Optional: submit a command from the terminal input field.
     */
    default void submitTerminalCommand(String command) {
    }

    /**
     * Gets the prompt prefix for the terminal (e.g. "> " or "user@kashub:$ ").
     */
    default String getTerminalPrompt() {
        return "> ";
    }
}
