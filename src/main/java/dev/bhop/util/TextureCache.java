package dev.bhop.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class TextureCache {

    private static final Map<String, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();
    private static final Map<String, int[]> DIMENSIONS = new ConcurrentHashMap<>();
    private static final Set<String> PENDING = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final Map<String, Long> FAILED = new ConcurrentHashMap<>();
    private static final long RETRY_DELAY = 10000L;

    private TextureCache() {}

    public static ResourceLocation get(String url) {
        return TEXTURES.get(url);
    }

    public static int[] getDimensions(String url) {
        return DIMENSIONS.get(url);
    }

    public static boolean isLoaded(String url) {
        return TEXTURES.containsKey(url);
    }

    public static void loadAsync(String url) {
        if (url == null || url.isEmpty()) return;
        if (TEXTURES.containsKey(url) || !PENDING.add(url)) return;
        Long failedAt = FAILED.get(url);
        if (failedAt != null && System.currentTimeMillis() - failedAt < RETRY_DELAY) {
            PENDING.remove(url);
            return;
        }
        new Thread(() -> {
            try {
                BufferedImage image = ImageIO.read(new URL(url));
                if (image == null) {
                    FAILED.put(url, System.currentTimeMillis());
                    PENDING.remove(url);
                    return;
                }
                FAILED.remove(url);
                DIMENSIONS.put(url, new int[]{image.getWidth(), image.getHeight()});
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation location = Minecraft.getMinecraft().getTextureManager()
                            .getDynamicTextureLocation("sca_" + Integer.toHexString(url.hashCode()), texture);
                    TEXTURES.put(url, location);
                    PENDING.remove(url);
                });
            } catch (Exception e) {
                FAILED.put(url, System.currentTimeMillis());
                PENDING.remove(url);
            }
        }).start();
    }

    public static void evict(String url) {
        TEXTURES.remove(url);
        DIMENSIONS.remove(url);
        FAILED.remove(url);
        PENDING.remove(url);
    }

    public static void evictForUuid(String uuid) {
        evict(headUrl(uuid));
        evict(bodyUrl(uuid));
        evict(capeUrl(uuid));
    }

    public static String headUrl(String uuid) {
        return "https://crafthead.net/helm/" + uuid + "/20";
    }

    public static String bodyUrl(String uuid) {
        return "https://crafthead.net/body/" + uuid;
    }

    public static String capeUrl(String uuid) {
        return "https://crafthead.net/cape/" + uuid;
    }
}
