package kasperstudios.khgui.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hit-map system for mapping screen coordinates to interactive zones.
 * Used to determine what UI element was clicked.
 */
public class HitMap {
    private final List<HitZone> zones = new ArrayList<>();
    
    /**
     * Registers a clickable zone.
     */
    public void register(HitZone zone) {
        zones.add(zone);
    }
    
    /**
     * Clears all registered zones.
     * Should be called on screen resize or layout change.
     */
    public void clear() {
        zones.clear();
    }
    
    /**
     * Finds the topmost zone at the given coordinates.
     * Zones are checked in reverse order (last registered = topmost).
     */
    public Optional<HitZone> findZone(double mouseX, double mouseY) {
        for (int i = zones.size() - 1; i >= 0; i--) {
            HitZone zone = zones.get(i);
            if (zone.contains(mouseX, mouseY)) {
                return Optional.of(zone);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Represents a clickable zone with bounds and action.
     */
    public static class HitZone {
        public final String id;
        public final int x, y, w, h;
        public final Runnable onClick;
        public final String tooltip;
        
        public HitZone(String id, int x, int y, int w, int h, Runnable onClick) {
            this(id, x, y, w, h, onClick, null);
        }
        
        public HitZone(String id, int x, int y, int w, int h, Runnable onClick, String tooltip) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.onClick = onClick;
            this.tooltip = tooltip;
        }
        
        public boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
        
        public void trigger() {
            if (onClick != null) {
                onClick.run();
            }
        }
    }
}
