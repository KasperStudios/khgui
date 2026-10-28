package kasperstudios.khgui.panels;

import kasperstudios.khgui.api.IEditorPanel;
import kasperstudios.khgui.api.IFileProvider;
import kasperstudios.khgui.api.ITheme;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

/**
 * Hierarchical file explorer panel with search, folders, categories, selection, and create dialog.
 * Uses IFileProvider for data, so any mod can supply its own file listings.
 */
public class FileTreePanel implements IEditorPanel {

    private IFileProvider fileProvider;
    private Consumer<String> onFileSelect;

    // Cached flat list for rendering
    private final List<FileEntry> entries = new ArrayList<>();
    private final List<FileEntry> filteredEntries = new ArrayList<>();
    private final Set<String> collapsedDirs = new HashSet<>();

    private String searchQuery = "";
    private boolean searchFocused = false;
    private int hoveredIndex = -1;
    private int selectedIndex = -1;
    private int scrollY = 0;

    // Create dialog state
    private boolean showCreateDialog = false;
    private String newFileName = "";
    private boolean createDialogFocused = false;
    private String createCategory = null;

    private static final int SEARCH_HEIGHT = 28;
    private static final int CREATE_BUTTON_HEIGHT = 32;
    private static final int HEADER_HEIGHT = 24;
    private static final int ITEM_HEIGHT = 22;

    public static class FileEntry {
        public final String fullPath;
        public final String displayName;
        public final String categoryId;
        public final String categoryIcon;
        public final boolean isHeader;
        public final boolean isDirectory;
        public final int depth;
        public final String dirKey;

        public FileEntry(String fullPath, String displayName, String categoryId, String categoryIcon,
                         boolean isHeader, boolean isDirectory, int depth, String dirKey) {
            this.fullPath = fullPath;
            this.displayName = displayName;
            this.categoryId = categoryId;
            this.categoryIcon = categoryIcon;
            this.isHeader = isHeader;
            this.isDirectory = isDirectory;
            this.depth = depth;
            this.dirKey = dirKey;
        }
    }

    private static class TreeNode {
        final String name;
        final String fullPath;
        final boolean isDirectory;
        final Map<String, TreeNode> children = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        TreeNode(String name, String fullPath, boolean isDirectory) {
            this.name = name;
            this.fullPath = fullPath;
            this.isDirectory = isDirectory;
        }
    }

    public FileTreePanel() {
    }

    /**
     * Sets the file provider that supplies file data.
     */
    public void setFileProvider(IFileProvider provider) {
        this.fileProvider = provider;
        refreshFiles();
    }

    /**
     * Sets the callback for file selection events.
     */
    public void setOnFileSelect(Consumer<String> onFileSelect) {
        this.onFileSelect = onFileSelect;
    }

    /**
     * Refreshes the file list from the provider.
     */
    public void refreshFiles() {
        entries.clear();
        if (fileProvider != null) {
            for (IFileProvider.FileCategory cat : fileProvider.getCategories()) {
                // Add category header
                entries.add(new FileEntry(cat.displayName(), cat.displayName(), cat.id(), cat.icon(), true, false, 0, null));

                // Build folder tree for category files
                TreeNode root = new TreeNode("", "", true);
                for (String file : cat.files()) {
                    addFileToTree(root, file);
                }

                flattenTree(root, 0, cat.id(), cat.icon(), !searchQuery.isEmpty(), entries);
            }
        }
        applyFilter();
    }

    private void addFileToTree(TreeNode root, String relativePath) {
        String clean = relativePath.replace("\\", "/").replaceAll("^/+", "");
        String[] parts = clean.split("/");
        TreeNode current = root;
        StringBuilder currentPath = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (currentPath.length() > 0) currentPath.append("/");
            currentPath.append(part);

            boolean isDir = (i < parts.length - 1);
            String fullPath = currentPath.toString();
            current = current.children.computeIfAbsent(part, k -> new TreeNode(part, fullPath, isDir));
        }
    }

    private void flattenTree(TreeNode node, int depth, String categoryId, String categoryIcon, boolean searchActive, List<FileEntry> target) {
        List<TreeNode> sorted = new ArrayList<>(node.children.values());
        sorted.sort((a, b) -> {
            if (a.isDirectory != b.isDirectory) {
                return a.isDirectory ? -1 : 1; // Directories first
            }
            return a.name.compareToIgnoreCase(b.name);
        });

        for (TreeNode child : sorted) {
            if (child.isDirectory) {
                String dirKey = categoryId + ":" + child.fullPath;
                boolean isCollapsed = !searchActive && collapsedDirs.contains(dirKey);
                target.add(new FileEntry(child.fullPath, child.name, categoryId, categoryIcon, false, true, depth, dirKey));
                if (!isCollapsed) {
                    flattenTree(child, depth + 1, categoryId, categoryIcon, searchActive, target);
                }
            } else {
                target.add(new FileEntry(child.fullPath, child.name, categoryId, categoryIcon, false, false, depth, null));
            }
        }
    }

    private void applyFilter() {
        filteredEntries.clear();
        if (searchQuery.isEmpty()) {
            filteredEntries.addAll(entries);
            return;
        }

        String query = searchQuery.toLowerCase();
        String currentCatId = null;
        boolean catHeaderAdded = false;

        for (FileEntry entry : entries) {
            if (entry.isHeader) {
                currentCatId = entry.categoryId;
                catHeaderAdded = false;
            } else if (!entry.isDirectory) {
                if (entry.displayName.toLowerCase().contains(query) || entry.fullPath.toLowerCase().contains(query)) {
                    if (!catHeaderAdded) {
                        for (FileEntry h : entries) {
                            if (h.isHeader && h.categoryId.equals(currentCatId)) {
                                filteredEntries.add(h);
                                catHeaderAdded = true;
                                break;
                            }
                        }
                    }
                    filteredEntries.add(entry);
                }
            }
        }
    }

    @Override
    public String getId() {
        return "explorer";
    }

    @Override
    public String getDisplayName() {
        return "EXPLORER";
    }

    @Override
    public String getIcon() {
        return "";
    }

    @Override
    public void onOpen() {
        refreshFiles();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int x, int y, int width,
            int height, ITheme theme) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        // Search bar
        renderSearchBar(graphics, font, mouseX, mouseY, x, y, width, theme);

        // File list
        int listY = y + SEARCH_HEIGHT;
        int listHeight = height - SEARCH_HEIGHT - CREATE_BUTTON_HEIGHT;
        renderFileList(graphics, font, mouseX, mouseY, x, listY, width, listHeight, theme);

        // Create button
        renderCreateButton(graphics, font, mouseX, mouseY, x, y + height - CREATE_BUTTON_HEIGHT, width, theme);
    }

    private void renderSearchBar(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int y, int width,
            ITheme theme) {
        int barBg = theme.getSidebarBackground();
        graphics.fill(x, y, x + width, y + SEARCH_HEIGHT, barBg);

        int inputX = x + 8;
        int inputY = y + 4;
        int inputWidth = width - 16;
        int inputHeight = SEARCH_HEIGHT - 8;
        
        int bgColor = searchFocused ? adjustBrightness(theme.getEditorBackground(), 10) : adjustBrightness(theme.getEditorBackground(), 5);
        graphics.fill(inputX, inputY, inputX + inputWidth, inputY + inputHeight, bgColor);

        graphics.drawString(font, "🔍", inputX + 6, inputY + 5, theme.getMutedText(), false);

        if (searchFocused) {
            int accent = theme.getPrimaryAccent();
            graphics.fill(inputX, inputY + inputHeight - 1, inputX + inputWidth, inputY + inputHeight, accent);
        }

        String displayText = searchQuery.isEmpty() ? "Search files..." : searchQuery;
        int textColor = searchQuery.isEmpty() ? theme.getMutedText() : theme.getPrimaryText();
        graphics.drawString(font, displayText, inputX + 22, inputY + 5, textColor, false);

        // Cursor blink
        if (searchFocused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = inputX + 22 + font.width(searchQuery);
            graphics.fill(cursorX, inputY + 4, cursorX + 1, inputY + inputHeight - 4, theme.getPrimaryText());
        }

        // Clear button
        if (!searchQuery.isEmpty()) {
            int clearX = inputX + inputWidth - 14;
            boolean clearHovered = mouseX >= clearX && mouseX < clearX + 10 && mouseY >= inputY && mouseY < inputY + inputHeight;
            graphics.drawString(font, "×", clearX, inputY + 4, clearHovered ? 0xFFFF5555 : theme.getMutedText(), false);
        }
    }

    private void renderFileList(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int listY, int width,
            int listHeight, ITheme theme) {
        hoveredIndex = -1;

        int totalHeight = filteredEntries.size() * ITEM_HEIGHT;
        int maxScroll = Math.max(0, totalHeight - listHeight);
        scrollY = Math.max(0, Math.min(maxScroll, scrollY));

        int currentY = listY - scrollY;

        Minecraft mc2 = Minecraft.getInstance();
        double guiScale = mc2.getWindow().getGuiScale();
        int sx = (int) (x * guiScale);
        int sy = (int) (mc2.getWindow().getHeight() - (listY + listHeight) * guiScale);
        int sw = (int) (width * guiScale);
        int sh = (int) (listHeight * guiScale);
        RenderSystem.enableScissor(sx, sy, sw, sh);
        try {
            for (int i = 0; i < filteredEntries.size(); i++) {
                FileEntry entry = filteredEntries.get(i);
                int itemY = currentY + i * ITEM_HEIGHT;

                if (itemY + ITEM_HEIGHT < listY || itemY > listY + listHeight)
                    continue;

                if (entry.isHeader) {
                    graphics.fill(x, Math.max(listY, itemY), x + width,
                            Math.min(listY + listHeight, itemY + ITEM_HEIGHT),
                            adjustBrightness(theme.getSidebarBackground(), 8));

                    String label = entry.displayName;
                    int labelX = x + 8;
                    if (entry.categoryIcon != null && !entry.categoryIcon.isEmpty()) {
                        graphics.drawString(font, entry.categoryIcon, labelX, itemY + 6, theme.getPrimaryAccent(), true);
                        labelX += font.width(entry.categoryIcon) + 4;
                    }

                    int maxLabelWidth = (x + width) - labelX - 10;
                    if (font.width(label) > maxLabelWidth) {
                        label = font.plainSubstrByWidth(label, maxLabelWidth - 10) + "...";
                    }
                    graphics.drawString(font, label, labelX, itemY + 6, theme.getPrimaryAccent(), true);
                } else if (entry.isDirectory) {
                    // Directory entry with expand/collapse arrow
                    int indent = 10 + entry.depth * 10;
                    boolean isHovered = mouseX >= x && mouseX < x + width && mouseY >= itemY
                            && mouseY < itemY + ITEM_HEIGHT && mouseY >= listY && mouseY < listY + listHeight;

                    if (isHovered) {
                        hoveredIndex = i;
                        graphics.fill(x + 4, itemY + 1, x + width - 4, itemY + ITEM_HEIGHT - 1, theme.getHover());
                    }

                    boolean isCollapsed = collapsedDirs.contains(entry.dirKey);
                    String arrow = isCollapsed ? "▶ " : "▼ ";
                    String icon = isCollapsed ? "📁 " : "📂 ";
                    
                    graphics.drawString(font, arrow + icon + entry.displayName, x + indent, itemY + 6, theme.getPrimaryAccent(), false);
                } else {
                    // File entry
                    int indent = 12 + entry.depth * 10;
                    boolean isHovered = mouseX >= x && mouseX < x + width && mouseY >= itemY
                            && mouseY < itemY + ITEM_HEIGHT && mouseY >= listY && mouseY < listY + listHeight;
                    boolean isSelected = i == selectedIndex;

                    if (isHovered) {
                        hoveredIndex = i;
                        graphics.fill(x + 4, itemY + 1, x + width - 4, itemY + ITEM_HEIGHT - 1, theme.getHover());
                    }
                    if (isSelected) {
                        graphics.fill(x + 4, itemY + 1, x + width - 4, itemY + ITEM_HEIGHT - 1, theme.getSelection());
                    }

                    // Custom texture icon or fallback symbol
                    String customIcon = fileProvider != null ? fileProvider.getFileIcon(entry.fullPath, entry.categoryId) : null;
                    boolean isTextureIcon = customIcon != null && customIcon.contains(":");

                    if (isTextureIcon) {
                        try {
                            ResourceLocation loc = ResourceLocation.parse(customIcon);
                            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
                            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                            RenderSystem.enableBlend();
                            RenderSystem.defaultBlendFunc();
                            graphics.blit(loc, x + indent, itemY + 4, 14, 14, 0, 0, 16, 16, 16, 16);
                            RenderSystem.disableBlend();
                        } catch (Exception e) {
                            graphics.drawString(font, "📄", x + indent, itemY + 6, theme.getMutedText(), false);
                        }
                    } else {
                        String icon = "📄";
                        graphics.drawString(font, icon, x + indent, itemY + 6, theme.getMutedText(), false);
                    }

                    // File name
                    String name = entry.displayName;
                    int maxNameWidth = width - 45 - indent;
                    if (font.width(name) > maxNameWidth) {
                        name = font.plainSubstrByWidth(name, maxNameWidth - 8) + "...";
                    }
                    int nameColor = isSelected ? theme.getPrimaryAccent()
                            : (isHovered ? theme.getPrimaryText() : theme.getSecondaryText());
                    graphics.drawString(font, name, x + indent + 16, itemY + 6, nameColor, isSelected);

                    // Delete button
                    if (isHovered && !entry.isHeader && !entry.isDirectory) {
                        int delX = x + width - 18;
                        boolean delHovered = mouseX >= delX && mouseX < delX + 12 && mouseY >= itemY + 4 && mouseY < itemY + 18;
                        graphics.drawString(font, "×", delX, itemY + 6, delHovered ? 0xFFFF5555 : theme.getMutedText(), false);
                    }
                }
            }
        } finally {
            RenderSystem.disableScissor();
        }

        // Empty state
        if (filteredEntries.isEmpty()) {
            String emptyText = searchQuery.isEmpty() ? "No files found" : "No matches for \"" + searchQuery + "\"";
            int textWidth = font.width(emptyText);
            graphics.drawString(font, emptyText, x + (width - textWidth) / 2, listY + 40, theme.getMutedText(), false);
        }

        // Scrollbar
        if (totalHeight > listHeight) {
            int scrollbarHeight = Math.max(20, (listHeight * listHeight) / totalHeight);
            int scrollbarY = listY + (scrollY * (listHeight - scrollbarHeight)) / Math.max(1, maxScroll);

            graphics.fill(x + width - 4, listY, x + width, listY + listHeight, 0x22FFFFFF);
            graphics.fill(x + width - 4, scrollbarY, x + width, scrollbarY + scrollbarHeight, theme.getPrimaryAccent());
        }
    }

    private void renderCreateButton(GuiGraphics graphics, Font font, int mouseX, int mouseY, int x, int btnY,
            int width, ITheme theme) {
        if (fileProvider == null)
            return;

        List<String> creatableCategories = fileProvider.getCreatableCategories();
        if (creatableCategories.isEmpty())
            return;

        int btnX = x + 10;
        int btnWidth = width - 20;
        int btnH = CREATE_BUTTON_HEIGHT - 8;
        boolean hovered = mouseX >= btnX && mouseX < btnX + btnWidth && mouseY >= btnY + 4
                && mouseY < btnY + 4 + btnH;
        int color = hovered ? theme.getPrimaryAccent()
                : adjustBrightness(theme.getSidebarBackground(), 15);
        graphics.fill(btnX, btnY + 4, btnX + btnWidth, btnY + 4 + btnH, color);
        String text = "+ New File / Folder";
        int tw = font.width(text);
        graphics.drawString(font, text, btnX + (btnWidth - tw) / 2, btnY + 4 + (btnH - 8) / 2, 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        // Search bar click
        int inputX = x + 8;
        int inputY = y + 4;
        int inputWidth = width - 16;
        int inputHeight = SEARCH_HEIGHT - 8;
        if (mouseX >= inputX && mouseX < inputX + inputWidth && mouseY >= inputY && mouseY < inputY + inputHeight) {
            // Clear button
            if (!searchQuery.isEmpty()) {
                int clearX = inputX + inputWidth - 14;
                if (mouseX >= clearX && mouseX < clearX + 10) {
                    searchQuery = "";
                    applyFilter();
                    return true;
                }
            }
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Create button
        int btnY = y + height - CREATE_BUTTON_HEIGHT;
        if (fileProvider != null && !fileProvider.getCreatableCategories().isEmpty()) {
            if (mouseX >= x + 10 && mouseX < x + width - 10 && mouseY >= btnY + 4
                    && mouseY < btnY + CREATE_BUTTON_HEIGHT - 4) {
                showCreateDialog = true;
                createDialogFocused = true;
                createCategory = fileProvider.getCreatableCategories().get(0);
                return true;
            }
        }

        // File selection / Folder toggle / Delete
        int listY = y + SEARCH_HEIGHT;
        int listHeight = height - SEARCH_HEIGHT - CREATE_BUTTON_HEIGHT;
        
        int calcHovered = -1;
        if (mouseX >= x && mouseX < x + width && mouseY >= listY && mouseY < listY + listHeight) {
            int relativeY = (int) mouseY - listY + scrollY;
            calcHovered = relativeY / ITEM_HEIGHT;
            if (calcHovered >= filteredEntries.size()) calcHovered = -1;
        }

        if (button == 0 && calcHovered >= 0) {
            FileEntry entry = filteredEntries.get(calcHovered);
            if (entry.isHeader) {
                return true;
            }

            if (entry.isDirectory) {
                // Toggle expand/collapse
                if (collapsedDirs.contains(entry.dirKey)) {
                    collapsedDirs.remove(entry.dirKey);
                } else {
                    collapsedDirs.add(entry.dirKey);
                }
                refreshFiles();
                return true;
            }

            // File clicked
            final int clickedIndex = calcHovered;
            int itemY = (y + SEARCH_HEIGHT - scrollY) + calcHovered * ITEM_HEIGHT;
            int delX = x + width - 18;
            if (mouseX >= delX && mouseX < delX + 12 && mouseY >= itemY + 4 && mouseY < itemY + 18) {
                if (fileProvider != null) {
                    fileProvider.deleteFile(entry.fullPath, entry.categoryId, success -> {
                        if (success) {
                            refreshFiles();
                            if (selectedIndex == clickedIndex) selectedIndex = -1;
                        }
                    });
                }
                return true;
            }

            selectedIndex = calcHovered;
            if (fileProvider != null) {
                fileProvider.onFileSelected(entry.fullPath, entry.categoryId);
            }
            if (onFileSelect != null) {
                onFileSelect.accept(entry.fullPath);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollDelta, int x, int y,
            int width, int height) {
        int listHeight = height - SEARCH_HEIGHT - CREATE_BUTTON_HEIGHT;
        int totalHeight = filteredEntries.size() * ITEM_HEIGHT;
        int maxScroll = Math.max(0, totalHeight - listHeight);
        scrollY = Math.max(0, Math.min(maxScroll, scrollY - (int) (scrollDelta * ITEM_HEIGHT)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, int x, int y, int width, int height) {
        if (searchFocused) {
            if (keyCode == 259 && !searchQuery.isEmpty()) { // BACKSPACE
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                applyFilter();
                return true;
            }
            if (keyCode == 256) { // ESCAPE
                searchFocused = false;
                return true;
            }
            if (keyCode == 257) { // ENTER
                searchFocused = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers, int x, int y, int width, int height) {
        if (searchFocused) {
            if (Character.isLetterOrDigit(chr) || chr == '_' || chr == '-' || chr == '.' || chr == ' ' || chr == '/') {
                searchQuery += chr;
                applyFilter();
                return true;
            }
        }
        return false;
    }

    public boolean isCreateDialogShowing() {
        return showCreateDialog;
    }

    public void renderCreateDialogOverlay(GuiGraphics graphics, int mouseX, int mouseY, ITheme theme) {
        if (!showCreateDialog)
            return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, screenW, screenH, 0xAA000000);

        int dialogWidth = 320;
        int dialogHeight = 136;
        int dx = (screenW - dialogWidth) / 2;
        int dy = (screenH - dialogHeight) / 2;

        graphics.fill(dx, dy, dx + dialogWidth, dy + dialogHeight, theme.getSidebarBackground());

        int accent = theme.getPrimaryAccent();
        graphics.fill(dx, dy, dx + dialogWidth, dy + 1, accent);
        graphics.fill(dx, dy + dialogHeight - 1, dx + dialogWidth, dy + dialogHeight, accent);
        graphics.fill(dx, dy, dx + 1, dy + dialogHeight, accent);
        graphics.fill(dx + dialogWidth - 1, dy, dx + dialogWidth, dy + dialogHeight, accent);

        graphics.drawString(font, "Create New File / Folder", dx + 15, dy + 14, accent, true);

        int inputY = dy + 36;
        graphics.fill(dx + 15, inputY, dx + dialogWidth - 15, inputY + 24, theme.getEditorBackground());
        String displayText = newFileName.isEmpty() ? "script.kh or folder/script.kh" : newFileName;
        int textColor = newFileName.isEmpty() ? theme.getMutedText() : theme.getPrimaryText();
        graphics.drawString(font, displayText, dx + 20, inputY + 8, textColor, false);

        if (createDialogFocused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = dx + 20 + font.width(newFileName);
            graphics.fill(cursorX, inputY + 6, cursorX + 1, inputY + 18, theme.getPrimaryText());
        }

        // Subtitle hint
        graphics.drawString(font, "§8Tip: Type 'folder/file' to organize into folders", dx + 15, dy + 66, theme.getMutedText(), false);

        // Buttons
        int btnY = dy + 96;
        int btnWidth = 85;

        // Cancel
        boolean cancelHovered = mouseX >= dx + 15 && mouseX < dx + 15 + btnWidth && mouseY >= btnY
                && mouseY < btnY + 24;
        graphics.fill(dx + 15, btnY, dx + 15 + btnWidth, btnY + 24,
                cancelHovered ? theme.getHover() : adjustBrightness(theme.getSidebarBackground(), 15));
        int cancelTw = font.width("Cancel");
        graphics.drawString(font, "Cancel", dx + 15 + (btnWidth - cancelTw) / 2, btnY + 8, theme.getPrimaryText(), false);

        // Create
        boolean createHovered = mouseX >= dx + dialogWidth - 15 - btnWidth && mouseX < dx + dialogWidth - 15
                && mouseY >= btnY && mouseY < btnY + 24;
        graphics.fill(dx + dialogWidth - 15 - btnWidth, btnY, dx + dialogWidth - 15, btnY + 24,
                createHovered ? accent : adjustBrightness(accent, -30));
        int createTw = font.width("Create");
        graphics.drawString(font, "Create", dx + dialogWidth - 15 - btnWidth + (btnWidth - createTw) / 2, btnY + 8, 0xFFFFFFFF, true);
    }

    public boolean handleCreateDialogClick(double mouseX, double mouseY) {
        if (!showCreateDialog)
            return false;

        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int dialogWidth = 320;
        int dialogHeight = 136;
        int dx = (screenW - dialogWidth) / 2;
        int dy = (screenH - dialogHeight) / 2;

        int inputY = dy + 36;
        if (mouseX >= dx + 15 && mouseX < dx + dialogWidth - 15 && mouseY >= inputY && mouseY < inputY + 24) {
            createDialogFocused = true;
            return true;
        }

        int btnY = dy + 96;
        int btnWidth = 85;

        // Cancel
        if (mouseX >= dx + 15 && mouseX < dx + 15 + btnWidth && mouseY >= btnY && mouseY < btnY + 24) {
            showCreateDialog = false;
            newFileName = "";
            return true;
        }

        // Create
        if (mouseX >= dx + dialogWidth - 15 - btnWidth && mouseX < dx + dialogWidth - 15 && mouseY >= btnY
                && mouseY < btnY + 24) {
            submitCreateFile();
            return true;
        }

        return true;
    }

    public boolean handleCreateDialogKey(int keyCode) {
        if (!showCreateDialog)
            return false;

        if (keyCode == 257) { // ENTER
            submitCreateFile();
            return true;
        }
        if (keyCode == 259 && !newFileName.isEmpty()) { // BACKSPACE
            newFileName = newFileName.substring(0, newFileName.length() - 1);
            return true;
        }
        if (keyCode == 256) { // ESCAPE
            showCreateDialog = false;
            newFileName = "";
            return true;
        }
        return false;
    }

    private void submitCreateFile() {
        if (!newFileName.trim().isEmpty() && fileProvider != null && createCategory != null) {
            fileProvider.createFile(newFileName.trim(), createCategory, success -> {
                if (success) {
                    refreshFiles();
                    showCreateDialog = false;
                    newFileName = "";
                }
            });
        }
    }

    public boolean handleCreateDialogChar(char chr) {
        if (!showCreateDialog || !createDialogFocused)
            return false;

        if (Character.isLetterOrDigit(chr) || chr == '_' || chr == '-' || chr == '.' || chr == '/') {
            newFileName += chr;
            return true;
        }
        return false;
    }

    private int adjustBrightness(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + amount));
        int g = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + amount));
        int b = Math.max(0, Math.min(255, (color & 0xFF) + amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
