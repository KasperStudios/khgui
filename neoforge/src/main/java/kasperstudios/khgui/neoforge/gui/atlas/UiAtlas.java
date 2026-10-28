package kasperstudios.khgui.neoforge.gui.atlas;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UiAtlas {
    private final ResourceLocation textureLocation;
    private final ResourceLocation jsonLocation;
    private final Map<String, AtlasRegion> regions = new HashMap<>();
    private static final Gson GSON = new Gson();

    private int textureWidth = 1920;
    private int textureHeight = 1020;

    public static class AtlasRegion {
        public int x, y, w, h;
        public int[] slice9;
        public Map<String, AtlasRegion> children = new HashMap<>();

        public AtlasRegion(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public UiAtlas(ResourceLocation textureLocation, ResourceLocation jsonLocation) {
        this.textureLocation = textureLocation;
        this.jsonLocation = jsonLocation;
    }

    public void setTextureSize(int w, int h) {
        this.textureWidth = w;
        this.textureHeight = h;
    }

    public void load() {
        Minecraft mc = Minecraft.getInstance();
        
        System.out.println("KHGui: Loading atlas from " + jsonLocation);
        
        Optional<Resource> resourceOpt = mc.getResourceManager().getResource(jsonLocation);
        if (resourceOpt.isEmpty()) {
            System.err.println("KHGui: Atlas JSON not found: " + jsonLocation);
            return;
        }
        
        try (InputStreamReader reader = new InputStreamReader(resourceOpt.get().open(), StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            regions.clear();
            parseRegions(root, "", regions);
            
            System.out.println("KHGui: Loaded " + regions.size() + " regions from atlas");
            
            mc.getTextureManager().getTexture(textureLocation).setFilter(false, false);
            
            System.out.println("KHGui: Atlas loaded successfully: " + textureLocation);
        } catch (Exception e) {
            System.err.println("KHGui: Failed to load atlas JSON: " + jsonLocation);
            e.printStackTrace();
        }
    }

    private void parseRegions(JsonObject obj, String prefix, Map<String, AtlasRegion> targetMap) {
        for (String key : obj.keySet()) {
            if (key.equals("children") || key.equals("slice9"))
                continue;

            var jsonElement = obj.get(key);
            if (!jsonElement.isJsonObject())
                continue;

            JsonObject regionObj = jsonElement.getAsJsonObject();

            if (!regionObj.has("x") || !regionObj.has("y") || !regionObj.has("w") || !regionObj.has("h")) {
                continue;
            }

            int x = regionObj.get("x").getAsInt();
            int y = regionObj.get("y").getAsInt();
            int w = regionObj.get("w").getAsInt();
            int h = regionObj.get("h").getAsInt();

            AtlasRegion region = new AtlasRegion(x, y, w, h);

            if (regionObj.has("slice9")) {
                var s9 = regionObj.getAsJsonArray("slice9");
                if (s9.size() == 4) {
                    region.slice9 = new int[] {
                            s9.get(0).getAsInt(),
                            s9.get(1).getAsInt(),
                            s9.get(2).getAsInt(),
                            s9.get(3).getAsInt()
                    };
                }
            }

            if (regionObj.has("children")) {
                parseRegions(regionObj.getAsJsonObject("children"), "", region.children);
            }

            String fullPath = prefix.isEmpty() ? key : prefix + "." + key;
            targetMap.put(key, region);
            if (targetMap != this.regions) {
                this.regions.put(fullPath, region);
            }
        }
    }

    public void blit(GuiGraphics graphics, String regionId, int x, int y) {
        AtlasRegion region = regions.get(regionId);
        if (region != null) {
            blitRaw(graphics, x, y, region.w, region.h, region.x, region.y, region.w, region.h);
        }
    }

    public void blit(GuiGraphics graphics, String regionId, int x, int y, int w, int h) {
        AtlasRegion region = regions.get(regionId);
        if (region != null) {
            if (region.slice9 != null) {
                blit9Slice(graphics, region, x, y, w, h);
            } else {
                blitRaw(graphics, x, y, w, h, region.x, region.y, region.w, region.h);
            }
        }
    }
    
    private void blitRaw(GuiGraphics graphics, int x, int y, int width, int height, int uOffset, int vOffset, int uWidth, int vHeight) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, textureLocation);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        com.mojang.blaze3d.platform.GlStateManager._texParameter(3553, 10241, 9728);
        com.mojang.blaze3d.platform.GlStateManager._texParameter(3553, 10240, 9728);
        
        float u0 = (float) uOffset / textureWidth;
        float u1 = (float) (uOffset + uWidth) / textureWidth;
        float v0 = (float) vOffset / textureHeight;
        float v1 = (float) (vOffset + vHeight) / textureHeight;
        
        com.mojang.blaze3d.vertex.PoseStack poseStack = graphics.pose();
        org.joml.Matrix4f matrix = poseStack.last().pose();
        
        com.mojang.blaze3d.vertex.BufferBuilder bufferBuilder = com.mojang.blaze3d.vertex.Tesselator.getInstance().begin(
            com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
            com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX
        );
        
        bufferBuilder.addVertex(matrix, x, y + height, 0).setUv(u0, v1);
        bufferBuilder.addVertex(matrix, x + width, y + height, 0).setUv(u1, v1);
        bufferBuilder.addVertex(matrix, x + width, y, 0).setUv(u1, v0);
        bufferBuilder.addVertex(matrix, x, y, 0).setUv(u0, v0);
        
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    private void blit9Slice(GuiGraphics graphics, AtlasRegion r, int x, int y, int w, int h) {
        int[] s9 = r.slice9;
        int top = s9[0];
        int right = s9[1];
        int bottom = s9[2];
        int left = s9[3];

        int centerW = r.w - left - right;
        int centerH = r.h - top - bottom;
        int innerW = w - left - right;
        int innerH = h - top - bottom;

        blitRaw(graphics, x, y, left, top, r.x, r.y, left, top);
        blitRaw(graphics, x + w - right, y, right, top, r.x + r.w - right, r.y, right, top);
        blitRaw(graphics, x, y + h - bottom, left, bottom, r.x, r.y + r.h - bottom, left, bottom);
        blitRaw(graphics, x + w - right, y + h - bottom, right, bottom, r.x + r.w - right, r.y + r.h - bottom, right, bottom);

        blitRaw(graphics, x + left, y, innerW, top, r.x + left, r.y, centerW, top);
        blitRaw(graphics, x + left, y + h - bottom, innerW, bottom, r.x + left, r.y + r.h - bottom, centerW, bottom);
        blitRaw(graphics, x, y + top, left, innerH, r.x, r.y + top, left, centerH);
        blitRaw(graphics, x + w - right, y + top, right, innerH, r.x + r.w - right, r.y + top, right, centerH);

        blitRaw(graphics, x + left, y + top, innerW, innerH, r.x + left, r.y + top, centerW, centerH);
    }

    public Optional<AtlasRegion> getRegion(String id) {
        return Optional.ofNullable(regions.get(id));
    }
}
