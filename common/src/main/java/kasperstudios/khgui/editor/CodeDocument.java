package kasperstudios.khgui.editor;

import java.util.ArrayList;
import java.util.List;

public class CodeDocument {
    private final List<String> lines = new ArrayList<>();
    public int cursorLine = 0;
    public int cursorColumn = 0;
    public int preferredColumn = -1;

    public int selectionStartLine = 0;
    public int selectionStartColumn = 0;
    public boolean hasSelection = false;

    private final UndoManager undoManager = new UndoManager();

    public CodeDocument() {
        lines.add(""); // Start with one empty line
    }

    public List<String> getLines() {
        return lines;
    }

    public String getText() {
        return String.join("\n", lines);
    }

    public void setText(String text) {
        lines.clear();
        if (text == null || text.isEmpty()) {
            lines.add("");
        } else {
            String sanitized = text.replace("\r", "");
            lines.addAll(java.util.Arrays.asList(sanitized.split("\n", -1)));
        }
        // Fix: Reset cursor to prevent IndexOutOfBounds when switching files
        cursorLine = 0;
        cursorColumn = 0;
        preferredColumn = -1;
        clearSelection();
    }

    private void recordState() {
        undoManager.recordState(getText(), cursorLine, cursorColumn);
    }

    public void undo() {
        UndoManager.EditorState state = undoManager.undo(getText(), cursorLine, cursorColumn);
        if (state != null) {
            setText(state.content);
            cursorLine = state.cursorLine;
            cursorColumn = state.cursorColumn;
        }
    }

    public void redo() {
        UndoManager.EditorState state = undoManager.redo(getText(), cursorLine, cursorColumn);
        if (state != null) {
            setText(state.content);
            cursorLine = state.cursorLine;
            cursorColumn = state.cursorColumn;
        }
    }

    public void insert(String text) {
        if (hasSelection) {
            deleteSelection();
        }
        recordState();
        int safeLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        String currentLine = lines.get(safeLine);
        int safeCol = Math.max(0, Math.min(currentLine.length(), cursorColumn));
        String before = currentLine.substring(0, safeCol);
        String after = currentLine.substring(safeCol);

        String sanitized = text.replace("\r", "");
        // Handle newlines
        if (sanitized.contains("\n")) {
            String[] parts = sanitized.split("\n", -1);
            lines.set(safeLine, before + parts[0]);
            for (int i = 1; i < parts.length; i++) {
                lines.add(safeLine + i, parts[i]);
            }
            // Update cursor
            cursorLine = safeLine + parts.length - 1;
            cursorColumn = parts[parts.length - 1].length();

            // Append the 'after' part to the last inserted line
            String lastLine = lines.get(cursorLine);
            lines.set(cursorLine, lastLine + after);
        } else {
            // Simple insertion
            lines.set(safeLine, before + sanitized + after);
            cursorLine = safeLine;
            cursorColumn = safeCol + sanitized.length();
        }
        preferredColumn = cursorColumn;
    }

    /**
     * Atomically replaces N characters before cursor with the given replacement text.
     * Essential for autocomplete so prefix is replaced without creating multiple undo steps.
     */
    public void replaceBeforeCursor(int length, String text) {
        if (hasSelection) {
            deleteSelection();
        }
        recordState();
        int safeLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        String currentLine = lines.get(safeLine);
        int safeCol = Math.max(0, Math.min(currentLine.length(), cursorColumn));
        int startCol = Math.max(0, safeCol - length);
        String before = currentLine.substring(0, startCol);
        String after = currentLine.substring(safeCol);
        lines.set(safeLine, before + text + after);
        cursorLine = safeLine;
        cursorColumn = startCol + text.length();
        preferredColumn = cursorColumn;
    }

    public void setSelectionStart() {
        if (!hasSelection) {
            selectionStartLine = cursorLine;
            selectionStartColumn = cursorColumn;
            hasSelection = true;
        }
    }

    public void clearSelection() {
        hasSelection = false;
    }

    public String getSelectedText() {
        if (!hasSelection)
            return "";

        int startL = selectionStartLine;
        int startC = selectionStartColumn;
        int endL = cursorLine;
        int endC = cursorColumn;

        if (startL > endL || (startL == endL && startC > endC)) {
            startL = cursorLine;
            startC = cursorColumn;
            endL = selectionStartLine;
            endC = selectionStartColumn;
        }

        if (startL == endL) {
            String line = lines.get(startL);
            return line.substring(Math.min(startC, line.length()), Math.min(endC, line.length()));
        }

        StringBuilder sb = new StringBuilder();
        String firstLine = lines.get(startL);
        sb.append(firstLine.substring(Math.min(startC, firstLine.length()))).append("\n");

        for (int i = startL + 1; i < endL; i++) {
            sb.append(lines.get(i)).append("\n");
        }

        String lastLine = lines.get(endL);
        sb.append(lastLine.substring(0, Math.min(endC, lastLine.length())));

        return sb.toString();
    }

    public void deleteSelection() {
        if (!hasSelection)
            return;
        recordState();

        int startL = selectionStartLine;
        int startC = selectionStartColumn;
        int endL = cursorLine;
        int endC = cursorColumn;

        if (startL > endL || (startL == endL && startC > endC)) {
            startL = cursorLine;
            startC = cursorColumn;
            endL = selectionStartLine;
            endC = selectionStartColumn;
        }

        String firstLine = lines.get(startL);
        String before = firstLine.substring(0, Math.min(startC, firstLine.length()));

        String lastLine = lines.get(endL);
        String after = lastLine.substring(Math.min(endC, lastLine.length()));

        lines.set(startL, before + after);

        for (int i = endL; i > startL; i--) {
            lines.remove(i);
        }

        cursorLine = startL;
        cursorColumn = startC;
        hasSelection = false;
    }

    public void backspace() {
        if (hasSelection) {
            deleteSelection();
            return;
        }
        recordState();
        int safeLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        cursorLine = safeLine;
        if (cursorColumn > 0) {
            String line = lines.get(cursorLine);
            int safeCol = Math.max(0, Math.min(line.length(), cursorColumn));
            
            int charsToRemove = 1;
            boolean allSpaces = true;
            for (int i = 0; i < safeCol; i++) {
                if (line.charAt(i) != ' ') {
                    allSpaces = false;
                    break;
                }
            }
            
            if (allSpaces && safeCol > 0) {
                charsToRemove = safeCol % 4;
                if (charsToRemove == 0) charsToRemove = 4;
            }
            
            String before = line.substring(0, Math.max(0, safeCol - charsToRemove));
            String after = line.substring(safeCol);
            lines.set(cursorLine, before + after);
            cursorColumn = Math.max(0, safeCol - charsToRemove);
        } else if (cursorLine > 0) {
            // Merge with previous line
            String currentLine = lines.remove(cursorLine);
            cursorLine--;
            String prevLine = lines.get(cursorLine);
            cursorColumn = prevLine.length();
            lines.set(cursorLine, prevLine + currentLine);
        }
        preferredColumn = cursorColumn;
    }

    public void delete() {
        if (hasSelection) {
            deleteSelection();
            return;
        }
        recordState();
        int safeLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        cursorLine = safeLine;
        String line = lines.get(cursorLine);
        int safeCol = Math.max(0, Math.min(line.length(), cursorColumn));
        if (safeCol < line.length()) {
            String newLine = line.substring(0, safeCol) + line.substring(safeCol + 1);
            lines.set(cursorLine, newLine);
        } else if (cursorLine < lines.size() - 1) {
            // Merge with next line
            String nextLine = lines.remove(cursorLine + 1);
            lines.set(cursorLine, line + nextLine);
        }
        preferredColumn = cursorColumn;
    }

    public void enter() {
        recordState();
        int safeLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        cursorLine = safeLine;
        String line = lines.get(cursorLine);
        int safeCol = Math.max(0, Math.min(line.length(), cursorColumn));
        String before = line.substring(0, safeCol);
        String after = line.substring(safeCol);

        // Calculate indentation
        int indent = 0;
        for (char c : before.toCharArray()) {
            if (c == ' ' || c == '\t')
                indent++;
            else
                break;
        }

        lines.set(cursorLine, before);
        cursorLine++;
        lines.add(cursorLine, " ".repeat(indent) + after);
        cursorColumn = indent;
        preferredColumn = cursorColumn;
    }

    // Simple navigation
    public void moveCursor(int dLine, int dCol) {
        cursorLine = Math.max(0, Math.min(lines.size() - 1, cursorLine + dLine));
        String line = lines.get(cursorLine);
        cursorColumn = Math.max(0, Math.min(line.length(), cursorColumn + dCol));
        preferredColumn = cursorColumn;
    }

    /**
     * Inserts a single character at the cursor position.
     */
    public void insertChar(char c) {
        if (hasSelection) {
            deleteSelection();
        }
        recordState();
        int safeLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        cursorLine = safeLine;
        String line = lines.get(cursorLine);
        int safeCol = Math.max(0, Math.min(line.length(), cursorColumn));
        String before = line.substring(0, safeCol);
        String after = line.substring(safeCol);
        lines.set(cursorLine, before + c + after);
        cursorColumn = safeCol + 1;
        preferredColumn = cursorColumn;
    }

    public void duplicateLine() {
        recordState();
        String line = lines.get(cursorLine);
        lines.add(cursorLine + 1, line);
        cursorLine++;
    }

    public void deleteLine() {
        if (lines.size() <= 1) {
            recordState();
            lines.set(0, "");
            cursorLine = 0;
            cursorColumn = 0;
            return;
        }
        recordState();
        lines.remove(cursorLine);
        cursorLine = Math.max(0, Math.min(lines.size() - 1, cursorLine));
        cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
    }

    public void moveLineUp() {
        if (cursorLine <= 0)
            return;
        recordState();
        String current = lines.remove(cursorLine);
        lines.add(cursorLine - 1, current);
        cursorLine--;
    }

    public void moveLineDown() {
        if (cursorLine >= lines.size() - 1)
            return;
        recordState();
        String current = lines.remove(cursorLine);
        lines.add(cursorLine + 1, current);
        cursorLine++;
    }
}
