package kasperstudios.khgui.editor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo/Redo manager for the code editor.
 *
 * Design:
 *  - Normal typing within MERGE_THRESHOLD ms is merged into one undo step.
 *  - Paste, delete-selection, and multi-char inserts always create their own step.
 *  - History capped at MAX_HISTORY_SIZE to limit memory.
 */
public class UndoManager {

    private static final int  MAX_HISTORY_SIZE = 200;
    /** Rapid keystrokes within this window get merged into one undo step (ms). */
    private static final long MERGE_THRESHOLD  = 600;

    private final Deque<EditorState> undoStack = new ArrayDeque<>();
    private final Deque<EditorState> redoStack = new ArrayDeque<>();

    private String lastSavedContent = "";
    private long   lastChangeTime   = 0;

    /**
     * Records a new undo state. Normal typing within MERGE_THRESHOLD is merged.
     */
    public void recordState(String content, int cursorLine, int cursorColumn) {
        recordState(content, cursorLine, cursorColumn, false);
    }

    /**
     * Records state. Set forceNewStep=true for paste / delete-all / replace actions.
     */
    public void recordState(String content, int cursorLine, int cursorColumn, boolean forceNewStep) {
        long now = System.currentTimeMillis();

        if (!forceNewStep && !undoStack.isEmpty() && (now - lastChangeTime) < MERGE_THRESHOLD) {
            EditorState last = undoStack.peek();
            if (last != null && isSingleCharChange(last.content, content)) {
                // Merge rapid single-character input into the existing top state
                lastChangeTime = now;
                redoStack.clear();
                return;
            }
        }

        undoStack.push(new EditorState(content, cursorLine, cursorColumn, now));
        redoStack.clear();

        // Trim oldest history if over limit
        while (undoStack.size() > MAX_HISTORY_SIZE) {
            ((ArrayDeque<EditorState>) undoStack).removeLast();
        }

        lastChangeTime = now;
    }

    public EditorState undo(String currentContent, int currentLine, int currentColumn) {
        if (undoStack.isEmpty()) return null;
        redoStack.push(new EditorState(currentContent, currentLine, currentColumn, System.currentTimeMillis()));
        return undoStack.pop();
    }

    public EditorState redo(String currentContent, int currentLine, int currentColumn) {
        if (redoStack.isEmpty()) return null;
        undoStack.push(new EditorState(currentContent, currentLine, currentColumn, System.currentTimeMillis()));
        return redoStack.pop();
    }

    public boolean canUndo()  { return !undoStack.isEmpty(); }
    public boolean canRedo()  { return !redoStack.isEmpty(); }
    public int     undoDepth() { return undoStack.size(); }
    public int     redoDepth() { return redoStack.size(); }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        lastSavedContent = "";
        lastChangeTime   = 0;
    }

    public void markSaved(String content) {
        lastSavedContent = content;
    }

    public boolean hasUnsavedChanges(String currentContent) {
        return !currentContent.equals(lastSavedContent);
    }

    /**
     * Returns true only when exactly 1 character was added or removed — i.e. normal
     * typing. Paste, whole-word deletions and delete-selection all return false and
     * always get their own undo step.
     */
    private boolean isSingleCharChange(String oldText, String newText) {
        int diff = newText.length() - oldText.length();
        return diff == 1 || diff == -1;
    }

    // ── State record ──────────────────────────────────────────────────────────

    public static class EditorState {
        public final String content;
        public final int    cursorLine;
        public final int    cursorColumn;
        public final long   timestamp;

        public EditorState(String content, int cursorLine, int cursorColumn, long timestamp) {
            this.content      = content;
            this.cursorLine   = cursorLine;
            this.cursorColumn = cursorColumn;
            this.timestamp    = timestamp;
        }
    }
}
