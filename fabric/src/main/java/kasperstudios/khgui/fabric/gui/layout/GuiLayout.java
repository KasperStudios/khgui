package kasperstudios.khgui.fabric.gui.layout;

/**
 * Manages the dynamic layout state of the VS Code-like GUI.
 * All sizes are relative to screen dimensions.
 */
public class GuiLayout {

    // --- State ---
    private boolean isSidebarVisible = true;
    private float sidebarWidthPercent = 0.15f; // 15% от ширины экрана
    private boolean isResizing = false;

    // --- Constants ---
    public static final int TITLE_BAR_HEIGHT = 30;
    public static final int ACTIVITY_BAR_WIDTH = 40; // Фиксированно 40px
    public static final float SIDEBAR_MIN_PERCENT = 0.10f; // Минимум 10%
    public static final float SIDEBAR_MAX_PERCENT = 0.30f; // Максимум 30%
    public static final int STATUS_BAR_HEIGHT = 22;
    public static final int TAB_HEIGHT = 25;
    public static final int RESIZE_HANDLE_WIDTH = 4;

    // --- Calculated Bounds ---
    public final LayoutRect titleBar = new LayoutRect();
    public final LayoutRect activityBar = new LayoutRect();
    public final LayoutRect sidebar = new LayoutRect();
    public final LayoutRect editorArea = new LayoutRect();
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
        // Все размеры относительные от размера экрана
        titleBar.set(0, 0, screenWidth, TITLE_BAR_HEIGHT);
        statusBar.set(0, screenHeight - STATUS_BAR_HEIGHT, screenWidth, STATUS_BAR_HEIGHT);

        int contentHeight = screenHeight - TITLE_BAR_HEIGHT - STATUS_BAR_HEIGHT;
        activityBar.set(0, TITLE_BAR_HEIGHT, ACTIVITY_BAR_WIDTH, contentHeight);

        // Sidebar width = процент от ширины экрана
        int calculatedSidebarWidth = (int) (screenWidth * sidebarWidthPercent);
        int currentSidebarWidth = isSidebarVisible ? calculatedSidebarWidth : 0;
        sidebar.set(ACTIVITY_BAR_WIDTH, TITLE_BAR_HEIGHT, currentSidebarWidth, contentHeight);

        // Editor занимает всё оставшееся пространство
        int editorX = ACTIVITY_BAR_WIDTH + currentSidebarWidth;
        int editorW = screenWidth - editorX;
        editorArea.set(editorX, TITLE_BAR_HEIGHT, editorW, contentHeight);
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
        return (int) (screenWidth * sidebarWidthPercent);
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

    public LayoutRect getResizeHandle(int screenWidth) {
        if (!isSidebarVisible)
            return null;
        LayoutRect handle = new LayoutRect();
        int sidebarWidth = getSidebarWidth(screenWidth);
        int handleX = ACTIVITY_BAR_WIDTH + sidebarWidth - RESIZE_HANDLE_WIDTH / 2;
        handle.set(handleX, sidebar.y, RESIZE_HANDLE_WIDTH, sidebar.h);
        return handle;
    }
}
