package kasperstudios.khgui.core;

import kasperstudios.khgui.editor.CodeDocument;
import org.lwjgl.glfw.GLFW;

/**
 * Handles keyboard input for code editing.
 * Supports basic text editing, cursor movement, and shortcuts.
 */
public class InputHandler {
    private final CodeDocument document;
    private boolean completionRequested = false;
    private boolean saveRequested = false;
    private boolean newRequested = false;
    private boolean runRequested = false;

    public InputHandler(CodeDocument document) {
        this.document = document;
    }

    /**
     * Checks if completion was requested (Ctrl+Space).
     */
    public boolean isCompletionRequested() {
        boolean result = completionRequested;
        completionRequested = false;
        return result;
    }

    /**
     * Checks if save was requested (Ctrl+S).
     */
    public boolean isSaveRequested() {
        boolean result = saveRequested;
        saveRequested = false;
        return result;
    }

    /**
     * Checks if new script was requested (Ctrl+N).
     */
    public boolean isNewRequested() {
        boolean result = newRequested;
        newRequested = false;
        return result;
    }

    /**
     * Checks if run was requested (F5).
     */
    public boolean isRunRequested() {
        boolean result = runRequested;
        runRequested = false;
        return result;
    }

    /**
     * Handles key press events.
     * 
     * @return true if the key was handled
     */
    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (ctrl) {
                    handleCtrlBackspace();
                } else {
                    handleBackspace();
                }
                return true;

            case GLFW.GLFW_KEY_DELETE:
                handleDelete();
                return true;

            case GLFW.GLFW_KEY_ENTER:
                handleEnter();
                return true;

            case GLFW.GLFW_KEY_TAB:
                handleTab();
                return true;

            case GLFW.GLFW_KEY_LEFT:
                handleLeft(ctrl, shift);
                return true;

            case GLFW.GLFW_KEY_RIGHT:
                handleRight(ctrl, shift);
                return true;

            case GLFW.GLFW_KEY_UP:
                if (modifiers == GLFW.GLFW_MOD_ALT) {
                    document.moveLineUp();
                    return true;
                }
                handleUp(shift);
                return true;

            case GLFW.GLFW_KEY_DOWN:
                if (modifiers == GLFW.GLFW_MOD_ALT) {
                    document.moveLineDown();
                    return true;
                }
                handleDown(shift);
                return true;

            case GLFW.GLFW_KEY_HOME:
                handleHome(ctrl, shift);
                return true;

            case GLFW.GLFW_KEY_END:
                handleEnd(ctrl, shift);
                return true;

            case GLFW.GLFW_KEY_D:
                if (ctrl) {
                    document.duplicateLine();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_K:
                if (ctrl && shift) {
                    document.deleteLine();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_A:
                if (ctrl) {
                    handleSelectAll();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_C:
                if (ctrl) {
                    handleCopy();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_V:
                if (ctrl) {
                    handlePaste();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_X:
                if (ctrl) {
                    handleCut();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_Z:
                if (ctrl) {
                    handleUndo();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_Y:
                if (ctrl) {
                    handleRedo();
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_S:
                if (ctrl) {
                    saveRequested = true;
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_N:
                if (ctrl) {
                    newRequested = true;
                    return true;
                }
                break;

            case GLFW.GLFW_KEY_F5:
                runRequested = true;
                return true;

            case GLFW.GLFW_KEY_SPACE:
                if (ctrl) {
                    completionRequested = true;
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * Handles character input (typing).
     */
    public boolean handleCharTyped(char character, int modifiers) {
        // If Ctrl or Alt is pressed, don't insert the character (e.g. Ctrl+Space)
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_ALT) != 0) {
            return false;
        }

        if (Character.isISOControl(character)) {
            return false;
        }

        document.insertChar(character);
        return true;
    }

    private void handleBackspace() {
        document.backspace();
    }

    private void handleDelete() {
        // To implement delete efficiently with the new state tracking, we can simulate
        // moving right then backspacing.
        // It's cleaner to just add a delete() method to CodeDocument, but for now we
        // manually record state.
        // Actually, since we need to record state, we should just call
        // document.delete()
        document.delete();
    }

    private void handleEnter() {
        document.enter();
    }

    private void handleTab() {
        document.insert("    "); // 4 spaces
    }

    private void ensureSelectionState(boolean shift) {
        if (shift) {
            document.setSelectionStart();
        } else {
            document.clearSelection();
        }
    }

    private void handleCtrlBackspace() {
        if (document.hasSelection) {
            document.deleteSelection();
            return;
        }
        int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
        String line = document.getLines().get(safeLine);
        int col = Math.max(0, Math.min(line.length(), document.cursorColumn));
        if (col == 0) {
            handleBackspace();
            return;
        }
        int start = col;
        while (start > 0 && Character.isWhitespace(line.charAt(start - 1))) {
            start--;
        }
        while (start > 0 && (Character.isLetterOrDigit(line.charAt(start - 1)) || line.charAt(start - 1) == '_')) {
            start--;
        }
        if (start == col && start > 0) {
            start--;
        }
        int toDelete = col - start;
        document.replaceBeforeCursor(toDelete, "");
    }

    private void handleLeft(boolean ctrl, boolean shift) {
        ensureSelectionState(shift);
        int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
        String line = document.getLines().get(safeLine);
        int col = Math.max(0, Math.min(line.length(), document.cursorColumn));

        if (ctrl) {
            if (col == 0) {
                if (document.cursorLine > 0) {
                    document.cursorLine--;
                    document.cursorColumn = document.getLines().get(document.cursorLine).length();
                }
            } else {
                while (col > 0 && Character.isWhitespace(line.charAt(col - 1))) {
                    col--;
                }
                while (col > 0 && (Character.isLetterOrDigit(line.charAt(col - 1)) || line.charAt(col - 1) == '_')) {
                    col--;
                }
                if (col == document.cursorColumn && col > 0) {
                    col--;
                }
                document.cursorColumn = col;
            }
            document.preferredColumn = document.cursorColumn;
            return;
        }

        if (document.cursorColumn > 0) {
            document.cursorColumn--;
        } else if (document.cursorLine > 0) {
            document.cursorLine--;
            document.cursorColumn = document.getLines().get(document.cursorLine).length();
        }
        document.preferredColumn = document.cursorColumn;
    }

    private void handleRight(boolean ctrl, boolean shift) {
        ensureSelectionState(shift);
        int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
        String line = document.getLines().get(safeLine);
        int col = Math.max(0, Math.min(line.length(), document.cursorColumn));

        if (ctrl) {
            if (col >= line.length()) {
                if (document.cursorLine < document.getLines().size() - 1) {
                    document.cursorLine++;
                    document.cursorColumn = 0;
                }
            } else {
                while (col < line.length() && (Character.isLetterOrDigit(line.charAt(col)) || line.charAt(col) == '_')) {
                    col++;
                }
                while (col < line.length() && Character.isWhitespace(line.charAt(col))) {
                    col++;
                }
                if (col == document.cursorColumn && col < line.length()) {
                    col++;
                }
                document.cursorColumn = col;
            }
            document.preferredColumn = document.cursorColumn;
            return;
        }

        if (document.cursorColumn < line.length()) {
            document.cursorColumn++;
        } else if (document.cursorLine < document.getLines().size() - 1) {
            document.cursorLine++;
            document.cursorColumn = 0;
        }
        document.preferredColumn = document.cursorColumn;
    }

    private void handleUp(boolean shift) {
        ensureSelectionState(shift);
        if (document.cursorLine > 0) {
            if (document.preferredColumn < 0) {
                document.preferredColumn = document.cursorColumn;
            }
            document.cursorLine--;
            String line = document.getLines().get(document.cursorLine);
            document.cursorColumn = Math.min(document.preferredColumn, line.length());
        }
    }

    private void handleDown(boolean shift) {
        ensureSelectionState(shift);
        if (document.cursorLine < document.getLines().size() - 1) {
            if (document.preferredColumn < 0) {
                document.preferredColumn = document.cursorColumn;
            }
            document.cursorLine++;
            String line = document.getLines().get(document.cursorLine);
            document.cursorColumn = Math.min(document.preferredColumn, line.length());
        }
    }

    private void handleHome(boolean ctrl, boolean shift) {
        ensureSelectionState(shift);
        if (ctrl) {
            document.cursorLine = 0;
        }
        document.cursorColumn = 0;
        document.preferredColumn = document.cursorColumn;
    }

    private void handleEnd(boolean ctrl, boolean shift) {
        ensureSelectionState(shift);
        if (ctrl) {
            document.cursorLine = document.getLines().size() - 1;
        }
        document.cursorColumn = document.getLines().get(document.cursorLine).length();
        document.preferredColumn = document.cursorColumn;
    }

    private void handleSelectAll() {
        if (document.getLines().isEmpty())
            return;

        document.selectionStartLine = 0;
        document.selectionStartColumn = 0;
        document.hasSelection = true;

        document.cursorLine = document.getLines().size() - 1;
        document.cursorColumn = document.getLines().get(document.cursorLine).length();
    }

    private void handleCopy() {
        if (!document.hasSelection)
            return;
        String selected = document.getSelectedText();
        if (selected != null && !selected.isEmpty()) {
            net.minecraft.client.Minecraft.getInstance().keyboardHandler.setClipboard(selected);
        }
    }

    private void handlePaste() {
        String clipboard = net.minecraft.client.Minecraft.getInstance().keyboardHandler.getClipboard();
        if (clipboard != null && !clipboard.isEmpty()) {
            document.insert(clipboard);
        }
    }

    private void handleCut() {
        if (!document.hasSelection)
            return;
        handleCopy();
        document.deleteSelection();
    }

    private void handleUndo() {
        document.undo();
    }

    private void handleRedo() {
        document.redo();
    }
}
