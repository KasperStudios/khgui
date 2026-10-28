package kasperstudios.khgui.core;

/**
 * Manages the dynamic layout state of the VS Code-like GUI.
 * Supports resizable sidebar and responsive layout.
 */
public class GuiLayout {

    // --- State ---
    private boolean isSidebarVisible = true;
    private float sidebarWidthPercent = 0.15f; // 15% from screen width
    private boolean isResizing = false;
    
    private boolean isConsoleVisible = false;
    private float consoleHeightPercent = 0.35f; // 35% from content height
    private boolean isResizingConsole = false;

    // --- Constants ---
    public static final int TITLE_BAR_HEIGHT = 30;
    public static final int ACTIVITY_BAR_WIDTH = 40; // Fixed 40px
    public static final float SIDEBAR_MIN_PERCENT = 0.10f; // Min 10%
    public static final float SIDEBAR_MAX_PERCENT = 0.30f; // Max 30%
    
    public static final float CONSOLE_MIN_PERCENT = 0.10f;
    public static final float CONSOLE_MAX_PERCENT = 0.80f;
    
    public static final int STATUS_BAR_HEIGHT = 22;
    public static final int TAB_HEIGHT = 35;
    public static final int RESIZE_HANDLE_WIDTH = 4;
    // --- Calculated Bounds ---
    public final LayoutRect titleBar = new LayoutRect();
    public final LayoutRect activityBar = new LayoutRect();
    public final LayoutRect sidebar = new LayoutRect();
    public final LayoutRect editorArea = new LayoutRect();
    public final LayoutRect consoleArea = new LayoutRect();
    public final LayoutRect statusBar = new LayoutRect();

    // --- Helper Class ---
    public static class LayoutRect {
        public int x, y, w, h;

        public void set(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    public void deploy(int screenWidth, int screenHeight) {
        titleBar.set(0, 0, screenWidth, TITLE_BAR_HEIGHT);
        statusBar.set(0, screenHeight - STATUS_BAR_HEIGHT, screenWidth, STATUS_BAR_HEIGHT);
        
        int contentHeight = screenHeight - TITLE_BAR_HEIGHT - STATUS_BAR_HEIGHT;
        activityBar.set(0, TITLE_BAR_HEIGHT, ACTIVITY_BAR_WIDTH, contentHeight);

        int calculatedSidebarWidth = (int)(screenWidth * sidebarWidthPercent);
        int currentSidebarWidth = isSidebarVisible ? calculatedSidebarWidth : 0;
        sidebar.set(ACTIVITY_BAR_WIDTH, TITLE_BAR_HEIGHT, currentSidebarWidth, contentHeight);

        // Editor and Console share the remaining space horizontally
        int mainX = ACTIVITY_BAR_WIDTH + currentSidebarWidth;
        int mainW = screenWidth - mainX;

        int calculatedConsoleHeight = (int)(contentHeight * consoleHeightPercent);
        int currentConsoleHeight = isConsoleVisible ? calculatedConsoleHeight : 0;
        
        // Editor takes the top portion, console takes the bottom
        int editorH = contentHeight - currentConsoleHeight;
        editorArea.set(mainX, TITLE_BAR_HEIGHT, mainW, editorH);
        
        if (isConsoleVisible) {
            consoleArea.set(mainX, TITLE_BAR_HEIGHT + editorH, mainW, currentConsoleHeight);
        } else {
            consoleArea.set(0, 0, 0, 0);
        }
    }

    public void toggleSidebar() {
        this.isSidebarVisible = !this.isSidebarVisible;
    }

    public boolean isSidebarVisible() {
        return isSidebarVisible;
    }
    
    public void setSidebarWidthPercent(float percent) {
        this.sidebarWidthPercent = Math.max(SIDEBAR_MIN_PERCENT, Math.min(SIDEBAR_MAX_PERCENT, percent));
    }
    
    public float getSidebarWidthPercent() {
        return sidebarWidthPercent;
    }
    
    public int getSidebarWidth(int screenWidth) {
        return (int)(screenWidth * sidebarWidthPercent);
    }
    
    public void startResize() {
        isResizing = true;
    }
    
    public void stopResize() {
        isResizing = false;
    }
    
    public boolean isResizing() {
        return isResizing;
    }

    public LayoutRect getSidebarResizeHandle(int screenWidth) {
        if (!isSidebarVisible) return null;
        LayoutRect handle = new LayoutRect();
        int sidebarWidth = getSidebarWidth(screenWidth);
        int handleX = ACTIVITY_BAR_WIDTH + sidebarWidth - RESIZE_HANDLE_WIDTH / 2;
        handle.set(handleX, sidebar.y, RESIZE_HANDLE_WIDTH, sidebar.h);
        return handle;
    }

    // --- Console API ---

    public void toggleConsole() {
        this.isConsoleVisible = !this.isConsoleVisible;
    }

    public boolean isConsoleVisible() {
        return isConsoleVisible;
    }

    public void setConsoleHeightPercent(float percent) {
        this.consoleHeightPercent = Math.max(CONSOLE_MIN_PERCENT, Math.min(CONSOLE_MAX_PERCENT, percent));
    }

    public float getConsoleHeightPercent() {
        return consoleHeightPercent;
    }

    public void startConsoleResize() {
        isResizingConsole = true;
    }

    public void stopConsoleResize() {
        isResizingConsole = false;
    }

    public boolean isResizingConsole() {
        return isResizingConsole;
    }

    public LayoutRect getConsoleResizeHandle(int screenHeight) {
        if (!isConsoleVisible) return null;
        LayoutRect handle = new LayoutRect();
        int handleY = consoleArea.y - RESIZE_HANDLE_WIDTH / 2;
        handle.set(consoleArea.x, handleY, consoleArea.w, RESIZE_HANDLE_WIDTH);
        return handle;
    }
}
