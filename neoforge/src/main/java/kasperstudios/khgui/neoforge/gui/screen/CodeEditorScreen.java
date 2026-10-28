package kasperstudios.khgui.neoforge.gui.screen;

import kasperstudios.khgui.editor.CodeDocument;
import kasperstudios.khgui.core.GuiLayout;
import kasperstudios.khgui.neoforge.gui.atlas.UiAtlas;
import com.mojang.blaze3d.systems.RenderSystem;
import kasperstudios.khgui.api.IEditorContext;
import kasperstudios.khgui.core.HitMap;
import kasperstudios.khgui.api.IEditorMenu;
import kasperstudios.khgui.api.IMenuItem;
import kasperstudios.khgui.api.ITitleAction;
import java.util.ArrayList;
import java.util.List;
import kasperstudios.khgui.core.InputHandler;
import kasperstudios.khgui.core.PanelManager;
import kasperstudios.khgui.core.ThemeManager;
import kasperstudios.khgui.core.IconManager;
import kasperstudios.khgui.core.ToolbarManager;
import kasperstudios.khgui.renderer.ThemedCodeRenderer;
import kasperstudios.khgui.completion.CompletionPopup;
import kasperstudios.khgui.completion.HoverTooltip;
import kasperstudios.khgui.panels.ConsolePanel;
import kasperstudios.khgui.panels.FileTreePanel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class CodeEditorScreen extends net.minecraft.client.gui.screens.Screen {
    private final CodeDocument document = new CodeDocument();
    private final ThemedCodeRenderer codeRenderer;
    private final GuiLayout layout = new GuiLayout();
    private final UiAtlas atlas;
    private final IEditorContext context;
    private final HitMap hitMap = new HitMap();
    private final InputHandler inputHandler;
    private final PanelManager panelManager = new PanelManager();
    private final IconManager iconManager = new IconManager();
    private final ToolbarManager toolbarManager;
    private final CompletionPopup completionPopup = new CompletionPopup();
    private final HoverTooltip hoverTooltip = new HoverTooltip();
    private final List<IEditorMenu> currentMenus = new ArrayList<>();
    private IEditorMenu activeMenu = null;
    private final List<ITitleAction> currentActions = new ArrayList<>();
    private final ConsolePanel consolePanel;

    private int animationTick = 0;
    private boolean atlasLoaded = false;
    private int tabScrollX = 0;
    private String lastContent = "";
    private int lastTabIndex = -1;
    private String lastFile = null;

    // Hover system
    private long lastMouseMoveTime = 0;
    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private static final long HOVER_DELAY = 500;

    public CodeEditorScreen(IEditorContext context) {
        super(net.minecraft.network.chat.Component.literal("KHGui Editor"));
        this.context = context;
        this.codeRenderer = new ThemedCodeRenderer(document);
        this.inputHandler = new InputHandler(document);
        this.toolbarManager = new kasperstudios.khgui.core.ToolbarManager(iconManager);
        this.consolePanel = new ConsolePanel(this.layout);

        // Initialize atlas
        this.atlas = new UiAtlas(
                ResourceLocation.fromNamespaceAndPath("khgui", "textures/gui/vscode_ui.png"),
                ResourceLocation.fromNamespaceAndPath("khgui", "textures/gui/vscode_ui.json"));

        // Register PNG icons
        iconManager.registerIcon("explorer", "textures/icons/explorer.png");

        // Register default panels
        panelManager.register(new FileTreePanel());

        // Register custom context panels
        if (context != null) {
            for (kasperstudios.khgui.api.IEditorPanel panel : context.getCustomSidePanels()) {
                panelManager.register(panel);
            }
        }

        // Load content and toolbar buttons from context
        if (context != null) {
            // Register custom panel icons from "namespace:path" strings
            context.getCustomPanelIconTextures().forEach((id, texPath) -> {
                String[] parts = texPath.split(":", 2);
                if (parts.length == 2) {
                    iconManager.registerIcon(id, ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]));
                }
            });
            if (context.getContent() != null) {
                document.setText(context.getContent());
            }
            
            for (kasperstudios.khgui.api.IToolbarButton button : context.getCustomToolbarButtons()) {
                registerToolbarButton(button);
            }

            this.currentMenus.addAll(context.getTitleMenus());
            this.currentActions.addAll(context.getTitleActions());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick) {
        // Don't render default background
    }

    private void recalculateLayout() {
        layout.deploy(this.width, this.height);
        codeRenderer.setBounds(
                layout.editorArea.x,
                layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                layout.editorArea.w,
                layout.editorArea.h - GuiLayout.TAB_HEIGHT);
        setupHitZones();
    }

    @Override
    protected void init() {
        super.init();
        if (!atlasLoaded) {
            atlas.load();
            atlasLoaded = true;
        }

        recalculateLayout();

        // Connect file provider to explorer panel
        if (context != null) {
            if (context.getFileProvider() != null) {
                panelManager.getPanel("explorer").ifPresent(panel -> {
                    if (panel instanceof FileTreePanel ftp) {
                        ftp.setFileProvider(context.getFileProvider());
                        ftp.setOnFileSelect(fileName -> {
                            context.openFile(fileName);
                            document.setText(context.getContent());
                        });
                    }
                });
            }

            // Register custom side panels from context
            for (kasperstudios.khgui.api.IEditorPanel panel : context.getCustomSidePanels()) {
                panelManager.register(panel);
            }

            // Register custom panel icons
            var customIcons = context.getCustomPanelIconTextures();
            if (customIcons != null) {
                for (var entry : customIcons.entrySet()) {
                    String logoTex = entry.getValue();
                    String[] parts = logoTex.split(":", 2);
                    if (parts.length == 2) {
                        iconManager.registerIcon(entry.getKey(), 
                            ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]));
                    }
                }
            }

            if (context instanceof kasperstudios.khgui.api.IConsoleProvider cp) {
                consolePanel.setProvider(cp);
            }
            
            consolePanel.setOnClose(() -> {
                consolePanel.setFocused(false);
                recalculateLayout();
            });
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (inputHandler.isSaveRequested()) {
            if (context != null) {
                context.save(document.getText());
            }
        }
        if (inputHandler.isNewRequested()) {
            context.createNewUntitled();
        }
        if (inputHandler.isRunRequested()) {
            // Trigger run via first run button if found
            toolbarManager.getButtons().stream()
                    .filter(b -> b.getId().equals("run"))
                    .findFirst()
                    .ifPresent(kasperstudios.khgui.api.IToolbarButton::onClick);
        }

        // Detect tab or content change from context (e.g. via terminal 'open' or explorer click)
        if (context != null) {
            int currentTabIndex = context.getActiveTabIndex();
            String currentFile = (context.getOpenTabs() != null && currentTabIndex >= 0 && currentTabIndex < context.getOpenTabs().size()) 
                ? context.getOpenTabs().get(currentTabIndex) : null;
            
            if (currentTabIndex != lastTabIndex || (currentFile != null && !currentFile.equals(lastFile))) {
                if (lastTabIndex >= 0 && context.getOpenTabs() != null && lastTabIndex < context.getOpenTabs().size()) {
                    context.updateContent(lastTabIndex, document.getText());
                }
                String content = context.getContent();
                document.setText(content != null ? content : "");
                lastTabIndex = currentTabIndex;
                lastFile = currentFile;
                lastContent = content != null ? content : "";
            }

            if (context.shouldRefreshLayout()) {
                this.currentMenus.clear();
                this.currentMenus.addAll(context.getTitleMenus());
                this.currentActions.clear();
                this.currentActions.addAll(context.getTitleActions());
                
                this.toolbarManager.clear();
                for (kasperstudios.khgui.api.IToolbarButton button : context.getCustomToolbarButtons()) {
                    registerToolbarButton(button);
                }

                panelManager.getPanel("explorer").ifPresent(panel -> {
                    if (panel instanceof FileTreePanel ftp) {
                        ftp.refreshFiles();
                    }
                });
                
                setupHitZones(); // Rebuild hit zones for new actions/menus
            }
        }
    }

    private void setupHitZones() {
        hitMap.clear();

        // Activity Bar Zones
        var panels = panelManager.getAllPanels();
        for (int i = 0; i < panels.size(); i++) {
            var panel = panels.get(i);
            hitMap.register(new HitMap.HitZone("activity_" + panel.getId(),
                    layout.activityBar.x, layout.activityBar.y + (i * 48),
                    layout.activityBar.w, 48,
                    () -> {
                        // Если кликнули на уже активную панель - toggle sidebar
                        if (panelManager.isActive(panel.getId())) {
                            layout.toggleSidebar();
                        } else {
                            // Иначе активируем панель и показываем sidebar
                            if (!layout.isSidebarVisible()) {
                                layout.toggleSidebar();
                            }
                            panelManager.activate(panel.getId());
                        }
                        layout.deploy(this.width, this.height);
                        codeRenderer.setBounds(
                                layout.editorArea.x,
                                layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                                layout.editorArea.w,
                                layout.editorArea.h - GuiLayout.TAB_HEIGHT);
                        setupHitZones();
                    },
                    panel.getDisplayName()));
        }

        // Editor Zone: Removed from hitMap to prevent intercepting clicks before updateCursorFromMouse()

        // Sidebar Zones (if visible)
        if (layout.isSidebarVisible()) {
            panelManager.getPanel("explorer").ifPresent(panel -> {
                if (panelManager.getActivePanel().map(p -> p == panel).orElse(false)) {
                    // New File Button
                    int btnX = layout.sidebar.x + 10;
                    int btnY = layout.sidebar.y + layout.sidebar.h - 40;
                    int btnW = layout.sidebar.w - 20;
                    int btnH = 30;
                    hitMap.register(new HitMap.HitZone("sidebar_new_file", btnX, btnY, btnW, btnH, () -> {
                        if (context != null) {
                            context.createNewUntitled();
                        }
                    }, "New File"));
                }
            });
        }
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        animationTick++;
        renderInternal(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void handleTabSwitch(int newIndex) {
        if (context == null || newIndex == lastTabIndex) return;
        context.updateContent(document.getText());
        context.switchTab(newIndex);
        String newContent = context.getContent();
        document.setText(newContent != null ? newContent : "");
        lastTabIndex = newIndex;
        lastContent = newContent != null ? newContent : "";
        List<String> tabs = context.getOpenTabs();
        lastFile = (tabs != null && newIndex >= 0 && newIndex < tabs.size()) ? tabs.get(newIndex) : null;
        codeRenderer.clampScroll();
        codeRenderer.ensureCursorVisible();
    }

    private void applyCompletion(kasperstudios.khgui.api.ICompletionProvider.CompletionItem item) {
        if (item == null) return;
        int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
        String currentLine = document.getLines().get(safeLine);
        String prefix = getWordAtCursor(currentLine, document.cursorColumn);
        int replaceLen = (prefix != null) ? prefix.length() : 0;
        document.replaceBeforeCursor(replaceLen, item.insertText);
        completionPopup.hide();
        codeRenderer.ensureCursorVisible();
    }

    @Override
    public void onClose() {
        if (context != null) {
            context.updateContent(document.getText());
            context.onClose();
        }
        super.onClose();
    }

    private void renderInternal(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick) {
        try {
            var theme = ThemeManager.getCurrentTheme();
            var mc = net.minecraft.client.Minecraft.getInstance();

            // Background
            graphics.fill(0, 0, width, height, theme.getEditorBackground());

            if (!atlasLoaded) {
                graphics.drawCenteredString(mc.font, "Atlas not loaded!", width / 2, height / 2,
                        theme.getPrimaryAccent());
                return;
            }

            // Title Bar
            graphics.fill(layout.titleBar.x, layout.titleBar.y,
                    layout.titleBar.x + layout.titleBar.w, layout.titleBar.y + layout.titleBar.h,
                    theme.getTitleBarBackground());

            // Title icon/logo
            int titleX = layout.titleBar.x + 2;
            if (context != null) {
                String logoTex = context.getTitleLogoTexture();
                if (logoTex != null) {
                    String[] parts = logoTex.split(":", 2);
                    if (parts.length == 2) {
                        int texW = context.getTitleLogoWidth();
                        int texH = context.getTitleLogoHeight();
                        int screenH = 20;
                        int screenW = (int) (screenH * ((float) texW / texH));
                        
                        ResourceLocation logoLoc = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
                        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                        graphics.blit(logoLoc, titleX, layout.titleBar.y + 5, screenW, screenH, 0.0f, 0.0f, texW, texH, texW, texH);
                        RenderSystem.disableBlend();
                        titleX += screenW + 10;
                    }
                } else if (context.getTitleIcon() != null && !context.getTitleIcon().isEmpty()) {
                    String icon = context.getTitleIcon();
                    graphics.drawString(mc.font, icon, titleX, layout.titleBar.y + 10, theme.getPrimaryAccent(), true);
                    titleX += mc.font.width(icon) + 8;
                }
            }

            String titleText = context != null ? context.getTitleText() : "KHGui Editor";
            graphics.drawString(mc.font, titleText, titleX, layout.titleBar.y + 10,
                    theme.getPrimaryText(), false);

            int menuX = titleX + mc.font.width(titleText) + 20;

            for (IEditorMenu menu : currentMenus) {
                int labelW = mc.font.width(menu.getLabel());
                boolean isMenuHovered = mouseX >= menuX && mouseX < menuX + labelW + 10 && mouseY >= layout.titleBar.y && mouseY < layout.titleBar.y + layout.titleBar.h;
                
                if (isMenuHovered || activeMenu == menu) {
                    graphics.fill(menuX, layout.titleBar.y, menuX + labelW + 10, layout.titleBar.y + layout.titleBar.h, 0x22FFFFFF);
                }
                
                graphics.drawString(mc.font, menu.getLabel(), menuX + 5, layout.titleBar.y + 10, theme.getPrimaryText(), false);
                menuX += labelW + 15;
            }

            // Title Actions
            for (ITitleAction action : currentActions) {
                String label = action.getLabel();
                String icon = action.getIcon();
                String fullText = (icon != null && !icon.isEmpty()) ? icon + " " + label : label;
                int labelW = mc.font.width(fullText);
                boolean isHovered = mouseX >= menuX && mouseX < menuX + labelW + 10 && mouseY >= layout.titleBar.y && mouseY < layout.titleBar.y + layout.titleBar.h;

                if (isHovered) {
                    graphics.fill(menuX, layout.titleBar.y, menuX + labelW + 10, layout.titleBar.y + layout.titleBar.h, 0x22FFFFFF);
                } else if (action.getBackgroundColor() != 0) {
                    graphics.fill(menuX, layout.titleBar.y, menuX + labelW + 10, layout.titleBar.y + layout.titleBar.h, action.getBackgroundColor());
                }

                graphics.drawString(mc.font, fullText, menuX + 5, layout.titleBar.y + 10, theme.getPrimaryAccent(), true);
                menuX += labelW + 15;
            }

            // Activity Bar
            graphics.fill(layout.activityBar.x, layout.activityBar.y,
                    layout.activityBar.x + layout.activityBar.w, layout.activityBar.y + layout.activityBar.h,
                    theme.getActivityBarBackground());

            // Activity Bar Icons
            var panels = panelManager.getAllPanels();
            int iconSize = 44;
            for (int i = 0; i < panels.size(); i++) {
                var panel = panels.get(i);
                int iconY = layout.activityBar.y + (i * iconSize) + 5;
                boolean isActive = panelManager.isActive(panel.getId());
                boolean isHovered = mouseX >= layout.activityBar.x &&
                        mouseX < layout.activityBar.x + layout.activityBar.w &&
                        mouseY >= iconY && mouseY < iconY + iconSize;

                if (isActive) {
                    graphics.fill(layout.activityBar.x, iconY,
                            layout.activityBar.x + 2, iconY + iconSize, theme.getPrimaryAccent());
                    graphics.fill(layout.activityBar.x + 2, iconY,
                            layout.activityBar.x + layout.activityBar.w, iconY + iconSize,
                            (theme.getPrimaryAccent() & 0x00FFFFFF) | 0x33000000);
                } else if (isHovered) {
                    graphics.fill(layout.activityBar.x, iconY,
                            layout.activityBar.x + layout.activityBar.w, iconY + iconSize, 0x22FFFFFF);
                }

                if (iconManager.hasIcon(panel.getId())) {
                    float alpha = isActive ? 1.0f : (isHovered ? 0.8f : 0.6f);
                    iconManager.renderIcon(graphics, panel.getId(),
                            layout.activityBar.x + 8, iconY + 10, 24, alpha);
                } else {
                    int iconColor = isActive ? theme.getPrimaryText()
                            : (isHovered ? theme.getSecondaryText() : theme.getMutedText());
                    String iconStr = panel.getIcon();
                    int iconW = mc.font.width(iconStr);
                    graphics.drawString(mc.font, iconStr,
                            layout.activityBar.x + (layout.activityBar.w - iconW) / 2,
                            iconY + (iconSize - 9) / 2, iconColor, false);
                }
            }

            // Sidebar
            if (layout.isSidebarVisible()) {
                graphics.fill(layout.sidebar.x, layout.sidebar.y,
                        layout.sidebar.x + layout.sidebar.w, layout.sidebar.y + layout.sidebar.h,
                        theme.getSidebarBackground());

                var activePanel = panelManager.getActivePanel();
                String sidebarTitle = activePanel.isPresent() ? activePanel.get().getDisplayName()
                        : (context != null ? context.getSidebarTitle() : "EXPLORER");
                int maxTitleWidth = layout.sidebar.w - 20;
                if (mc.font.width(sidebarTitle) > maxTitleWidth) {
                    while (mc.font.width(sidebarTitle + "...") > maxTitleWidth && sidebarTitle.length() > 0) {
                        sidebarTitle = sidebarTitle.substring(0, sidebarTitle.length() - 1);
                    }
                    sidebarTitle += "...";
                }
                graphics.drawString(mc.font, sidebarTitle, layout.sidebar.x + 10, layout.sidebar.y + 8,
                        theme.getPrimaryText(), false);

                graphics.fill(layout.sidebar.x, layout.sidebar.y + 25,
                        layout.sidebar.x + layout.sidebar.w, layout.sidebar.y + 26, theme.getBorder());

                activePanel.ifPresent(p -> p.render(graphics, mouseX, mouseY, 0,
                        layout.sidebar.x, layout.sidebar.y + 26, layout.sidebar.w, layout.sidebar.h - 26, theme));

                // Sidebar Resize handle
                var handle = layout.getSidebarResizeHandle(this.width);
                if (handle != null) {
                    boolean isHovered = mouseX >= handle.x && mouseX < handle.x + handle.w &&
                            mouseY >= handle.y && mouseY < handle.y + handle.h;
                    int handleColor = (isHovered || layout.isResizing()) ? theme.getPrimaryAccent() : 0x00000000;
                    if (handleColor != 0) {
                        graphics.fill(handle.x, handle.y, handle.x + handle.w, handle.y + handle.h, handleColor);
                    }
                }
            }

            // Tab Bar
            graphics.fill(layout.editorArea.x, layout.editorArea.y,
                    layout.editorArea.x + layout.editorArea.w, layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                    theme.getTabBarBackground());

            // Toolbar buttons
            int toolbarX = layout.editorArea.x + layout.editorArea.w - 10;
            int toolbarY = layout.editorArea.y + 3;
            toolbarManager.render(graphics, toolbarX, toolbarY, mouseX, mouseY);

            renderTabs(graphics, mouseX, mouseY, theme, mc);

            // Editor Background
            graphics.fill(layout.editorArea.x, layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                    layout.editorArea.x + layout.editorArea.w,
                    layout.editorArea.y + layout.editorArea.h,
                    theme.getEditorBackground());

            // Status Bar
            graphics.fill(layout.statusBar.x, layout.statusBar.y,
                    layout.statusBar.x + layout.statusBar.w, layout.statusBar.y + layout.statusBar.h,
                    theme.getStatusBarBackground());

            String cursorStatus = String.format("Ln %d, Col %d", document.cursorLine + 1, document.cursorColumn + 1);
            graphics.drawString(mc.font, cursorStatus, layout.statusBar.x + 10, layout.statusBar.y + 5,
                    theme.getPrimaryText(), false);

            int currentX = layout.statusBar.x + 10 + mc.font.width(cursorStatus) + 20;

            // Terminal Toggle Button
            String termBtn = layout.isConsoleVisible() ? "[x] Terminal" : "[+] Terminal";
            int termBtnWidth = mc.font.width(termBtn);
            boolean termHover = mouseX >= currentX && mouseX < currentX + termBtnWidth 
                                && mouseY >= layout.statusBar.y && mouseY < layout.statusBar.y + layout.statusBar.h;
            graphics.drawString(mc.font, termBtn, currentX, layout.statusBar.y + 5, 
                    termHover ? theme.getPrimaryText() : theme.getSecondaryText(), false);

            String rightStatus = (context != null ? context.getLanguage() : "Plain Text") + "  |  " + theme.getName();
            int rightWidth = mc.font.width(rightStatus);
            graphics.drawString(mc.font, rightStatus, layout.statusBar.x + layout.statusBar.w - rightWidth - 10,
                    layout.statusBar.y + 5, theme.getSecondaryText(), false);

            if (context != null && context.getStatusBarText() != null) {
                String ctxStatus = context.getStatusBarText();
                int ctxWidth = mc.font.width(ctxStatus);
                graphics.drawString(mc.font, ctxStatus, layout.statusBar.x + (layout.statusBar.w - ctxWidth) / 2,
                        layout.statusBar.y + 5, theme.getPrimaryText(), false);
            }

            // Render code
            codeRenderer.render(graphics, mouseX, mouseY, context);

            // Render Console Panel
            if (layout.isConsoleVisible()) {
                consolePanel.render(graphics, mouseX, mouseY, partialTick);

                // Console Resize Handle
                var cHandle = layout.getConsoleResizeHandle(this.height);
                if (cHandle != null) {
                    boolean cHover = mouseX >= cHandle.x && mouseX < cHandle.x + cHandle.w &&
                                     mouseY >= cHandle.y && mouseY < cHandle.y + cHandle.h;
                    int cColor = (cHover || layout.isResizingConsole()) ? theme.getPrimaryAccent() : 0x00000000;
                    if (cColor != 0) {
                        graphics.fill(cHandle.x, cHandle.y, cHandle.x + cHandle.w, cHandle.y + cHandle.h, cColor);
                    }
                }
            }

            var active = panelManager.getActivePanel();
            if (active.isPresent() && active.get() instanceof FileTreePanel ftp) {
                ftp.renderCreateDialogOverlay(graphics, mouseX, mouseY, theme);
            }

            // Render completion popup
            completionPopup.render(graphics, mouseX, mouseY);

            // Render hover tooltip on top of everything
            hoverTooltip.render(graphics);
            
            // Render active menu dropdown
            if (activeMenu != null) {
                int titleXX = layout.titleBar.x + 2;
                if (context != null) {
                    if (context.getTitleLogoTexture() != null) {
                        int texW = context.getTitleLogoWidth();
                        int texH = context.getTitleLogoHeight();
                        titleXX += (int) (20 * ( (float)texW / texH)) + 12;
                    } else if (context.getTitleIcon() != null) {
                        titleXX += mc.font.width(context.getTitleIcon()) + 10;
                    }
                }
                String tText = context != null ? context.getTitleText() : "KHGui Editor";
                int mX = titleXX + mc.font.width(tText) + 20;

                for (IEditorMenu menu : currentMenus) {
                    int labelW = mc.font.width(menu.getLabel());
                    if (activeMenu == menu) {
                        renderDropdown(graphics, mc.font, mX, layout.titleBar.y + layout.titleBar.h, menu, mouseX, mouseY, theme);
                        break;
                    }
                    mX += labelW + 15;
                }
            }

        } catch (Exception e) {
            System.err.println("KHGui NeoForge: CRASH in renderInternal!");
            e.printStackTrace();
            graphics.fill(0, 0, width, height, 0xFF1E1E1E);
            graphics.drawCenteredString(minecraft.font, "RENDER ERROR: " + e.getMessage(), width / 2, height / 2,
                    0xFFFF6B9D);
        }
    }

     private void renderTabs(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
            kasperstudios.khgui.api.ITheme theme, net.minecraft.client.Minecraft mc) {
        if (context == null || context.getOpenTabs().isEmpty())
            return;

        int availableWidth = layout.editorArea.w - 10 - toolbarManager.getButtons().size() * 24;
        int tabX = layout.editorArea.x + 10 - tabScrollX;

        int minTabWidth = 20;
        int maxTabWidth = 200;
        int tabPadding = 12;

        double scale = mc.getWindow().getGuiScale();
        int sx = (int) (layout.editorArea.x * scale);
        int sy = (int) (mc.getWindow().getHeight() - (layout.editorArea.y + GuiLayout.TAB_HEIGHT) * scale);
        int sw = (int) (availableWidth * scale);
        int sh = (int) (GuiLayout.TAB_HEIGHT * scale);

        RenderSystem.enableScissor(sx, sy, sw, sh);

        for (int i = 0; i < context.getOpenTabs().size(); i++) {
            String tabName = context.getOpenTabs().get(i);
            boolean isActive = i == context.getActiveTabIndex();

            int textWidth = mc.font.width(tabName);
            int tabWidth = Math.max(minTabWidth, Math.min(maxTabWidth, textWidth + tabPadding + 20));

            boolean isHovered = mouseX >= tabX && mouseX < tabX + tabWidth &&
                    mouseY >= layout.editorArea.y && mouseY < layout.editorArea.y + GuiLayout.TAB_HEIGHT &&
                    mouseX < layout.editorArea.x + availableWidth;

            if (isActive) {
                graphics.fill(tabX, layout.editorArea.y,
                        tabX + tabWidth, layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                        theme.getEditorBackground());
                graphics.fill(tabX, layout.editorArea.y,
                        tabX + tabWidth, layout.editorArea.y + 2,
                        theme.getPrimaryAccent());
            } else if (isHovered) {
                graphics.fill(tabX, layout.editorArea.y,
                        tabX + tabWidth, layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                        theme.getHover());
            }

            int tabColor = isActive ? theme.getPrimaryText()
                    : (isHovered ? theme.getSecondaryText() : theme.getMutedText());

            String displayName = (context.isTabDirty(i) ? "• " : "") + tabName;
            String renderText = displayName;
            if (mc.font.width(displayName) > maxTabWidth - tabPadding - 20) {
                renderText = mc.font.plainSubstrByWidth(displayName, maxTabWidth - tabPadding - 25) + "...";
            }

            graphics.drawString(mc.font, renderText, tabX + 10, layout.editorArea.y + 8, tabColor, false);

            if (isActive || isHovered) {
                int closeX = tabX + tabWidth - 20;
                int closeY = layout.editorArea.y + 8;
                boolean closeHovered = mouseX >= closeX - 4 && mouseX < closeX + 12 &&
                        mouseY >= closeY - 2 && mouseY < closeY + 12 && mouseX < layout.editorArea.x + availableWidth;
                int closeColor = closeHovered ? theme.getPrimaryText() : theme.getMutedText();
                
                var closeIcon = theme.getCloseTabIcon();
                if (closeIcon != null) {
                    RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    graphics.blit(closeIcon, closeX, closeY, 12, 12, 0, 0, 16, 16, 16, 16);
                } else {
                    String symbol = theme.getCloseTabSymbol();
                    if (symbol == null || symbol.isEmpty()) symbol = "x";
                    graphics.drawString(mc.font, symbol, closeX, closeY, closeColor, false);
                }
            }

            tabX += tabWidth + 2;
        }

        RenderSystem.disableScissor();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        hoverTooltip.hide();
        var activePanel = panelManager.getActivePanel();
        if (activePanel.isPresent() && activePanel.get() instanceof FileTreePanel ftp) {
            if (ftp.isCreateDialogShowing()) {
                return ftp.handleCreateDialogKey(keyCode);
            }
        }
        if (layout.isSidebarVisible()) {
            var active = panelManager.getActivePanel();
            if (active.isPresent()) {
                if (active.get().keyPressed(keyCode, scanCode, modifiers, layout.sidebar.x, layout.sidebar.y + 26, layout.sidebar.w, layout.sidebar.h - 26)) {
                    return true;
                }
            }
        }

        if (layout.isConsoleVisible()) {
            if (consolePanel.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        boolean isGraveKey = (keyCode == GLFW.GLFW_KEY_GRAVE_ACCENT || keyCode == 161 || keyCode == 162 || keyCode == 167 || keyCode == 192);
        if (isGraveKey && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            layout.toggleConsole();
            consolePanel.setFocused(layout.isConsoleVisible());
            recalculateLayout();
            return true;
        }

        if (completionPopup.isVisible()) {
            if (keyCode == 265) {
                completionPopup.selectPrevious();
                return true;
            } else if (keyCode == 264) {
                completionPopup.selectNext();
                return true;
            } else if (keyCode == 257 || keyCode == 258) {
                applyCompletion(completionPopup.getSelectedItem());
                return true;
            } else if (keyCode == 256) {
                completionPopup.hide();
                return true;
            }
        }

        if (keyCode == 32 && (modifiers & 2) != 0) {
            if (context != null && context.getCompletionProvider() != null) {
                int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
                String currentLine = document.getLines().get(safeLine);
                String prefix = getWordAtCursor(currentLine, document.cursorColumn);
                var items = context.getCompletionProvider().getCompletions(currentLine, document.cursorColumn, prefix);
                if (!items.isEmpty()) {
                    var theme = ThemeManager.getCurrentTheme();
                    int gutterWidth = theme.getGutterWidth();
                    int lineNumberWidth = theme.getLineNumberWidth();
                    int codeStartX = layout.editorArea.x + gutterWidth + lineNumberWidth + theme.getCodeLeftPadding();
                    
                    int safeCol = Math.max(0, Math.min(currentLine.length(), document.cursorColumn));
                    int textWidthBeforeCursor = minecraft.font.width(currentLine.substring(0, safeCol));
                    int cursorX = codeStartX + textWidthBeforeCursor - codeRenderer.scrollX;
                    int cursorY = layout.editorArea.y + GuiLayout.TAB_HEIGHT +
                            (document.cursorLine * codeRenderer.lineHeight) - codeRenderer.scrollY;
                    
                    int popupW = 260;
                    int popupH = Math.min(180, items.size() * 20 + 10);
                    int targetX = Math.max(layout.editorArea.x, Math.min(this.width - popupW - 10, cursorX));
                    int targetY = cursorY + codeRenderer.lineHeight;
                    if (targetY + popupH > this.height - GuiLayout.STATUS_BAR_HEIGHT) {
                        targetY = Math.max(layout.editorArea.y + GuiLayout.TAB_HEIGHT, cursorY - popupH);
                    }
                    completionPopup.show(items, targetX, targetY);
                }
            }
            return true;
        }

        if (inputHandler.handleKeyPress(keyCode, scanCode, modifiers)) {
            codeRenderer.ensureCursorVisible();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        hoverTooltip.hide();
        var activePanel = panelManager.getActivePanel();
        if (activePanel.isPresent() && activePanel.get() instanceof FileTreePanel ftp) {
            if (ftp.isCreateDialogShowing()) {
                return ftp.handleCreateDialogChar(character);
            }
        }
        if (layout.isSidebarVisible()) {
            var active = panelManager.getActivePanel();
            if (active.isPresent()) {
                if (active.get().charTyped(character, modifiers, layout.sidebar.x, layout.sidebar.y + 26, layout.sidebar.w, layout.sidebar.h - 26)) {
                    return true;
                }
            }
        }

        if (layout.isConsoleVisible()) {
            if (consolePanel.charTyped(character, modifiers)) {
                return true;
            }
        }

        if (inputHandler.handleCharTyped(character, modifiers)) {
            codeRenderer.ensureCursorVisible();
            return true;
        }
        return super.charTyped(character, modifiers);
    }

    private long lastClickTime = 0;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        hoverTooltip.hide();

        // Forward non-left-click events (e.g. right-click context menus) to sidebar and console panels
        if (button != 0) {
            if (layout.isSidebarVisible() && layout.sidebar.contains(mouseX, mouseY)) {
                var active = panelManager.getActivePanel();
                if (active.isPresent() && active.get().mouseClicked(mouseX, mouseY, button, layout.sidebar.x, layout.sidebar.y + 26, layout.sidebar.w, layout.sidebar.h - 26)) {
                    return true;
                }
            }
            if (layout.isConsoleVisible() && layout.consoleArea.contains(mouseX, mouseY)) {
                if (consolePanel.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            var activePanel = panelManager.getActivePanel();
            if (activePanel.isPresent() && activePanel.get() instanceof FileTreePanel ftp) {
                if (ftp.isCreateDialogShowing()) {
                    return ftp.handleCreateDialogClick(mouseX, mouseY);
                }
            }
            if (activeMenu != null) {
                int dWidth = 140;
                for (IMenuItem item : activeMenu.getItems()) {
                    dWidth = Math.max(dWidth, minecraft.font.width(item.getLabel()) + 40);
                }

                int menuX = findMenuX(activeMenu);
                int dropdownY = layout.titleBar.y + layout.titleBar.h;
                int dHeight = activeMenu.getItems().size() * 22 + 6;

                if (mouseX >= menuX && mouseX < menuX + dWidth && mouseY >= dropdownY && mouseY < dropdownY + dHeight) {
                    int itemY = dropdownY + 3;
                    for (IMenuItem item : activeMenu.getItems()) {
                        if (mouseY >= itemY && mouseY < itemY + 22) {
                            item.onClick();
                            activeMenu = null;
                            return true;
                        }
                        itemY += 22;
                    }
                }
                activeMenu = null;
            }

            var handle = layout.getSidebarResizeHandle(this.width);
            if (handle != null && handle.contains(mouseX, mouseY)) {
                layout.startResize();
                return true;
            }

            var cHandle = layout.getConsoleResizeHandle(this.height);
            if (cHandle != null && cHandle.contains(mouseX, mouseY)) {
                layout.startConsoleResize();
                return true;
            }

            if (layout.statusBar.contains(mouseX, mouseY)) {
                String cursorStatus = String.format("Ln %d, Col %d", document.cursorLine + 1, document.cursorColumn + 1);
                int cursorStatusWidth = minecraft.font.width(cursorStatus);
                int termBtnStart = layout.statusBar.x + 10 + cursorStatusWidth + 20;
                String termBtnLabel = layout.isConsoleVisible() ? "[x] Terminal" : "[+] Terminal";
                int termBtnWidth = minecraft.font.width(termBtnLabel);
                
                if (mouseX >= termBtnStart && mouseX < termBtnStart + termBtnWidth) {
                    layout.toggleConsole();
                    consolePanel.setFocused(layout.isConsoleVisible());
                    recalculateLayout();
                    return true;
                }
            }

            int titleX = layout.titleBar.x + 2;
            if (context != null) {
                if (context.getTitleLogoTexture() != null) {
                    int texW = context.getTitleLogoWidth();
                    int texH = context.getTitleLogoHeight();
                    titleX += (int) (20 * ( (float)texW / texH)) + 12;
                } else if (context.getTitleIcon() != null) {
                    titleX += minecraft.font.width(context.getTitleIcon()) + 10;
                }
            }
            
            String titleText = context != null ? context.getTitleText() : "KHGui Editor";
            int menuX = titleX + minecraft.font.width(titleText) + 20;
            
            for (IEditorMenu menu : currentMenus) {
                int labelW = minecraft.font.width(menu.getLabel());
                if (mouseX >= menuX && mouseX < menuX + labelW + 10 && mouseY >= layout.titleBar.y && mouseY < layout.titleBar.y + layout.titleBar.h) {
                    activeMenu = (activeMenu == menu) ? null : menu;
                    return true;
                }
                menuX += labelW + 15;
            }

            for (ITitleAction action : currentActions) {
                String label = action.getLabel();
                String icon = action.getIcon();
                String fullText = (icon != null && !icon.isEmpty()) ? icon + " " + label : label;
                int labelW = minecraft.font.width(fullText);
                if (mouseX >= menuX && mouseX < menuX + labelW + 10 && mouseY >= layout.titleBar.y && mouseY < layout.titleBar.y + layout.titleBar.h) {
                    action.onClick();
                    return true;
                }
                menuX += labelW + 15;
            }

            if (layout.isSidebarVisible() && layout.sidebar.contains(mouseX, mouseY)) {
                var active = panelManager.getActivePanel();
                if (active.isPresent() && active.get().mouseClicked(mouseX, mouseY, button, layout.sidebar.x, layout.sidebar.y + 26, layout.sidebar.w, layout.sidebar.h - 26)) {
                    return true;
                }
            }

            if (layout.isConsoleVisible() && layout.consoleArea.contains(mouseX, mouseY)) {
                if (consolePanel.mouseClicked(mouseX, mouseY, button)) {
                    consolePanel.setFocused(layout.isConsoleVisible());
                    return true;
                }
            } else {
                consolePanel.setFocused(false);
            }

            int toolbarX = layout.editorArea.x + layout.editorArea.w - 10;
            int toolbarY = layout.editorArea.y + 3;
            if (toolbarManager.handleClick(mouseX, mouseY, toolbarX, toolbarY)) {
                return true;
            }

            if (completionPopup.isVisible() && completionPopup.handleClick(mouseX, mouseY)) {
                applyCompletion(completionPopup.getSelectedItem());
                return true;
            }

            if (codeRenderer.handleGutterClick(mouseX, mouseY)) {
                return true;
            }

            var zone = hitMap.findZone(mouseX, mouseY);
            if (zone.isPresent()) {
                zone.get().trigger();
                return true;
            }

            if (context != null && !context.getOpenTabs().isEmpty() &&
                    mouseY >= layout.editorArea.y && mouseY < layout.editorArea.y + GuiLayout.TAB_HEIGHT) {

                int availableWidth = layout.editorArea.w - 10 - toolbarManager.getButtons().size() * 24;
                if (mouseX >= layout.editorArea.x && mouseX < layout.editorArea.x + availableWidth) {
                    int tabX = layout.editorArea.x + 10 - tabScrollX;
                    int minTabWidth = 20;
                    int maxTabWidth = 200;
                    int tabPadding = 12;

                    for (int i = 0; i < context.getOpenTabs().size(); i++) {
                        String tabName = context.getOpenTabs().get(i);
                        int textWidth = minecraft.font.width(tabName);
                        int tabWidth = Math.max(minTabWidth, Math.min(maxTabWidth, textWidth + tabPadding + 20));

                        if (mouseX >= tabX && mouseX < tabX + tabWidth) {
                            int closeX = tabX + tabWidth - 20;
                            int closeY = layout.editorArea.y + 8;
                            if (mouseX >= closeX - 4 && mouseX < closeX + 12 &&
                                    mouseY >= closeY - 2 && mouseY < closeY + 12) {
                                context.updateContent(document.getText());
                                context.closeTab(i);
                                document.setText(context.getContent());
                                lastTabIndex = context.getActiveTabIndex();
                                lastContent = context.getContent();
                                List<String> tabs = context.getOpenTabs();
                                lastFile = (tabs != null && lastTabIndex >= 0 && lastTabIndex < tabs.size()) ? tabs.get(lastTabIndex) : null;
                            } else {
                                handleTabSwitch(i);
                            }
                            return true;
                        }
                        tabX += tabWidth + 2;
                    }
                }
            }

            if (layout.editorArea.contains(mouseX, mouseY) && mouseY >= layout.editorArea.y + GuiLayout.TAB_HEIGHT) {
                updateCursorFromMouse(mouseX, mouseY);
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 350) {
                    int safeLine = Math.max(0, Math.min(document.getLines().size() - 1, document.cursorLine));
                    String line = document.getLines().get(safeLine);
                    int[] bounds = getWordBounds(line, document.cursorColumn);
                    if (bounds != null) {
                        document.cursorLine = safeLine;
                        document.selectionStartLine = safeLine;
                        document.selectionStartColumn = bounds[0];
                        document.hasSelection = true;
                        document.cursorColumn = bounds[1];
                    }
                } else {
                    document.clearSelection();
                }
                lastClickTime = now;
                codeRenderer.ensureCursorVisible();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            if (layout.isResizing()) {
                float newPercent = (float) (mouseX - GuiLayout.ACTIVITY_BAR_WIDTH) / this.width;
                layout.setSidebarWidthPercent(newPercent);
                layout.deploy(this.width, this.height);
                codeRenderer.setBounds(
                        layout.editorArea.x,
                        layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                        layout.editorArea.w,
                        layout.editorArea.h - GuiLayout.TAB_HEIGHT);
                setupHitZones();
                return true;
            }
            if (layout.isResizingConsole()) {
                int contentHeight = this.height - GuiLayout.TITLE_BAR_HEIGHT - GuiLayout.STATUS_BAR_HEIGHT;
                int bottomY = this.height - GuiLayout.STATUS_BAR_HEIGHT;
                float newPercent = (float) (bottomY - mouseY) / contentHeight;
                layout.setConsoleHeightPercent(newPercent);
                layout.deploy(this.width, this.height);
                codeRenderer.setBounds(
                        layout.editorArea.x,
                        layout.editorArea.y + GuiLayout.TAB_HEIGHT,
                        layout.editorArea.w,
                        layout.editorArea.h - GuiLayout.TAB_HEIGHT);
                return true;
            }

            if (layout.editorArea.contains(mouseX, mouseY)
                    && mouseY >= layout.editorArea.y + GuiLayout.TAB_HEIGHT) {
                if (!document.hasSelection) {
                    document.selectionStartLine = document.cursorLine;
                    document.selectionStartColumn = document.cursorColumn;
                    document.hasSelection = true;
                }
                updateCursorFromMouse(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && layout.isResizing()) {
            layout.stopResize();
            return true;
        }
        if (button == 0 && layout.isResizingConsole()) {
            layout.stopConsoleResize();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateCursorFromMouse(double mouseX, double mouseY) {
        var theme = ThemeManager.getCurrentTheme();
        int codeOffsetX = layout.editorArea.x + theme.getGutterWidth() + theme.getLineNumberWidth() + theme.getCodeLeftPadding();
        int relativeY = (int) mouseY - (layout.editorArea.y + GuiLayout.TAB_HEIGHT) + codeRenderer.scrollY;
        int newCursorLine = Math.max(0, relativeY / codeRenderer.lineHeight);
        newCursorLine = Math.min(newCursorLine, document.getLines().size() - 1);
        String line = document.getLines().get(newCursorLine).replace("\r", "");
        int relativeX = (int) mouseX - codeOffsetX + codeRenderer.scrollX;
        int newCursorCol = 0, currentWidth = 0;
        for (int i = 0; i < line.length(); i++) {
            int charWidth = minecraft.font.width(String.valueOf(line.charAt(i)));
            if (currentWidth + charWidth / 2 > relativeX) break;
            currentWidth += charWidth;
            newCursorCol++;
        }
        document.cursorLine = newCursorLine;
        document.cursorColumn = Math.max(0, Math.min(newCursorCol, line.length()));
        document.preferredColumn = document.cursorColumn;
    }

    private String lastHoverWord = null;
    private int lastHoverLine = -1;
    private int lastHoverCol = -1;

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            lastMouseMoveTime = System.currentTimeMillis();
            if (!hoverTooltip.contains((int) mouseX, (int) mouseY)) {
                hoverTooltip.hide();
            }
        }

        // Wait for hover delay
        long now = System.currentTimeMillis();
        if (now - lastMouseMoveTime < HOVER_DELAY) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        // Don't show hover if tooltip is already visible (to avoid flickering)
        if (hoverTooltip.isVisible()) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        // Don't show hover if completion popup is visible
        if (completionPopup.isVisible()) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        // Check if mouse is within code area
        if (mouseX < codeRenderer.x || mouseX >= codeRenderer.x + codeRenderer.width ||
                mouseY < codeRenderer.y || mouseY >= codeRenderer.y + codeRenderer.height) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        // Map mouse coordinates to document coordinates
        int lineIndex = ((int) mouseY - codeRenderer.y + codeRenderer.scrollY) / codeRenderer.lineHeight;
        if (lineIndex < 0 || lineIndex >= document.getLines().size()) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        String line = document.getLines().get(lineIndex).replace("\r", "");
        
        var theme = ThemeManager.getCurrentTheme();
        int gutterWidth = theme.getGutterWidth();
        int lineNumberWidth = theme.getLineNumberWidth();
        int codeStartX = codeRenderer.x + gutterWidth + lineNumberWidth + theme.getCodeLeftPadding();
        
        int clickX = (int) mouseX - codeStartX + codeRenderer.scrollX;
        if (clickX < 0) {
            super.mouseMoved(mouseX, mouseY);
            return;
        }

        int columnIndex = -1;
        for (int i = 0; i <= line.length(); i++) {
            if (minecraft.font.width(line.substring(0, i)) > clickX) {
                columnIndex = Math.max(0, i - 1);
                break;
            }
            columnIndex = i;
        }

        if (columnIndex >= 0 && columnIndex < line.length()) {
            String word = getWordAtCursor(line, columnIndex);
            if (!word.isEmpty() && context != null && context.getHoverProvider() != null) {
                var info = context.getHoverProvider().getHoverInfo(line, columnIndex, word);
                if (info != null) {
                    hoverTooltip.show(info, (int) mouseX, (int) mouseY + 10);
                }
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    public void registerToolbarButton(kasperstudios.khgui.api.IToolbarButton button) {
        toolbarManager.register(button);
        if (button.getIconPath() != null) {
            iconManager.registerIcon(button.getId(), button.getIconPath());
        }
    }

    public void unregisterToolbarButton(String buttonId) {
        toolbarManager.unregister(buttonId);
    }

    private int[] getWordBounds(String line, int column) {
        if (line.isEmpty() || column < 0) return null;
        int safeCol = Math.min(column, line.length() - 1);
        if (safeCol < 0 || !Character.isLetterOrDigit(line.charAt(safeCol))) return null;
        int start = safeCol, end = safeCol;
        while (start > 0 && Character.isLetterOrDigit(line.charAt(start - 1))) start--;
        while (end < line.length() && Character.isLetterOrDigit(line.charAt(end))) end++;
        return new int[] { start, end };
    }

    private String getWordAtCursor(String line, int column) {
        int[] bounds = getWordBounds(line, column > 0 ? column - 1 : 0);
        return bounds != null ? line.substring(bounds[0], bounds[1]) : "";
    }

    private void renderDropdown(net.minecraft.client.gui.GuiGraphics graphics, net.minecraft.client.gui.Font font, int x, int y, IEditorMenu menu, int mouseX, int mouseY, kasperstudios.khgui.api.ITheme theme) {
        int dWidth = 140;
        for (IMenuItem item : menu.getItems()) {
            dWidth = Math.max(dWidth, font.width(item.getLabel()) + 40);
        }
        int dHeight = menu.getItems().size() * 22 + 6;
        graphics.fill(x + 3, y + 3, x + dWidth + 3, y + dHeight + 3, 0x55000000);
        graphics.fill(x, y, x + dWidth, y + dHeight, theme.getSidebarBackground());
        int accent = theme.getPrimaryAccent();
        graphics.fill(x, y, x + dWidth, y + 1, accent);
        graphics.fill(x, y + dHeight - 1, x + dWidth, y + dHeight, accent);
        graphics.fill(x, y, x + 1, y + dHeight, accent);
        graphics.fill(x + dWidth - 1, y, x + dWidth, y + dHeight, accent);
        int itemY = y + 3;
        for (IMenuItem item : menu.getItems()) {
            boolean isHovered = mouseX >= x && mouseX < x + dWidth && mouseY >= itemY && mouseY < itemY + 22;
            if (isHovered) graphics.fill(x + 2, itemY, x + dWidth - 2, itemY + 22, theme.getHover());
            graphics.drawString(font, item.getLabel(), x + 12, itemY + 7, isHovered ? theme.getPrimaryText() : theme.getSecondaryText(), false);
            itemY += 22;
        }
    }

    private int findMenuX(IEditorMenu target) {
        int tX = layout.titleBar.x + 2;
        if (context != null) {
            if (context.getTitleLogoTexture() != null) {
                int texW = context.getTitleLogoWidth();
                int texH = context.getTitleLogoHeight();
                tX += (int) (20 * ( (float)texW / texH)) + 12;
            } else if (context.getTitleIcon() != null) {
                tX += minecraft.font.width(context.getTitleIcon()) + 10;
            }
        }
        String titleText = context != null ? context.getTitleText() : "KHGui Editor";
        int menuX = tX + minecraft.font.width(titleText) + 20;
        for (IEditorMenu menu : currentMenus) {
            if (menu == target) return menuX;
            menuX += minecraft.font.width(menu.getLabel()) + 15;
        }
        return 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (completionPopup.isVisible() && completionPopup.contains(mouseX, mouseY)) {
            if (scrollY > 0) completionPopup.selectPrevious(); else completionPopup.selectNext();
            return true;
        }
        if (hoverTooltip.isVisible() && hoverTooltip.contains((int) mouseX, (int) mouseY)) {
            hoverTooltip.scroll((int) -scrollY);
            return true;
        }
        if (layout.isConsoleVisible() && layout.consoleArea.contains(mouseX, mouseY)) {
            return consolePanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (layout.isSidebarVisible() && mouseX >= layout.sidebar.x && mouseX < layout.sidebar.x + layout.sidebar.w
                && mouseY >= layout.sidebar.y && mouseY < layout.sidebar.y + layout.sidebar.h) {
            var active = panelManager.getActivePanel();
            if (active.isPresent()) return active.get().mouseScrolled(mouseX, mouseY, scrollX, scrollY, layout.sidebar.x, layout.sidebar.y + 26, layout.sidebar.w, layout.sidebar.h - 26);
        }
        if (context != null && !context.getOpenTabs().isEmpty() &&
                mouseY >= layout.editorArea.y && mouseY < layout.editorArea.y + GuiLayout.TAB_HEIGHT) {
            int availableWidth = layout.editorArea.w - 10 - toolbarManager.getButtons().size() * 24;
            if (mouseX >= layout.editorArea.x && mouseX < layout.editorArea.x + availableWidth) {
                int totalTabsWidth = 0;
                for (String tabName : context.getOpenTabs()) {
                    totalTabsWidth += Math.max(20, Math.min(200, minecraft.font.width(tabName) + 32)) + 2;
                }
                int maxScroll = Math.max(0, totalTabsWidth - availableWidth + 10);
                tabScrollX = Math.max(0, Math.min(maxScroll, tabScrollX - (int) (scrollY * 30)));
                return true;
            }
        }
        if (layout.editorArea.contains(mouseX, mouseY) && mouseY >= layout.editorArea.y + GuiLayout.TAB_HEIGHT) {
            if (Screen.hasShiftDown() || scrollX != 0) {
                codeRenderer.scrollX = Math.max(0, Math.min(codeRenderer.getMaxScrollX(), 
                        codeRenderer.scrollX - (int) ((scrollX != 0 ? scrollX : scrollY) * 30)));
            } else {
                codeRenderer.scrollY = Math.max(0, Math.min(codeRenderer.getMaxScrollY(), 
                        codeRenderer.scrollY - (int) (scrollY * 30)));
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }


    public void jumpToLine(int line) {
        int targetLine = Math.max(0, Math.min(document.getLines().size() - 1, line - 1));
        document.cursorLine = targetLine;
        document.cursorColumn = 0;
        document.clearSelection();
        codeRenderer.scrollY = Math.max(0, (targetLine * codeRenderer.lineHeight) - (codeRenderer.height / 2));
        codeRenderer.scrollX = 0;
    }
}
