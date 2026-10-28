package kasperstudios.khgui.core;

import kasperstudios.khgui.api.IEditorPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages sidebar panels (Explorer, Search, Debug, etc.)
 */
public class PanelManager {
    private final Map<String, IEditorPanel> panels = new HashMap<>();
    private final List<String> panelOrder = new ArrayList<>();
    private String activePanel = null;
    
    /**
     * Registers a new panel.
     */
    public void register(IEditorPanel panel) {
        panels.put(panel.getId(), panel);
        if (!panelOrder.contains(panel.getId())) {
            panelOrder.add(panel.getId());
        }
        
        // Set as active if first panel
        if (activePanel == null) {
            activePanel = panel.getId();
        }
    }
    
    /**
     * Unregisters a panel.
     */
    public void unregister(String panelId) {
        panels.remove(panelId);
        panelOrder.remove(panelId);
        
        if (panelId.equals(activePanel)) {
            activePanel = panelOrder.isEmpty() ? null : panelOrder.get(0);
        }
    }
    
    /**
     * Activates a panel by ID.
     */
    public void activate(String panelId) {
        if (panels.containsKey(panelId)) {
            activePanel = panelId;
            panels.get(panelId).onOpen();
        }
    }
    
    /**
     * Gets the currently active panel.
     */
    public Optional<IEditorPanel> getActivePanel() {
        return activePanel != null ? Optional.ofNullable(panels.get(activePanel)) : Optional.empty();
    }
    
    /**
     * Gets a panel by ID.
     */
    public Optional<IEditorPanel> getPanel(String panelId) {
        return Optional.ofNullable(panels.get(panelId));
    }
    
    /**
     * Gets all registered panels in order.
     */
    public List<IEditorPanel> getAllPanels() {
        List<IEditorPanel> result = new ArrayList<>();
        for (String id : panelOrder) {
            IEditorPanel panel = panels.get(id);
            if (panel != null) {
                result.add(panel);
            }
        }
        return result;
    }
    
    /**
     * Gets the ID of the active panel.
     */
    public String getActivePanelId() {
        return activePanel;
    }
    
    /**
     * Checks if a panel is active.
     */
    public boolean isActive(String panelId) {
        return panelId.equals(activePanel);
    }
}
