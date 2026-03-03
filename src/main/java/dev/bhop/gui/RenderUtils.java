package dev.bhop.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class RenderUtils {

    private RenderUtils() {}

    public static void drawRoundedRect(double x, double y, double w, double h, double radius, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);

        double cx = x + w / 2.0;
        double cy = y + h / 2.0;
        wr.pos(cx, cy, 0).endVertex();

        int segments = 8;
        double[][] corners = {
            {x + w - radius, y + radius, -Math.PI / 2, 0},
            {x + w - radius, y + h - radius, 0, Math.PI / 2},
            {x + radius, y + h - radius, Math.PI / 2, Math.PI},
            {x + radius, y + radius, Math.PI, Math.PI * 1.5}
        };

        for (double[] c : corners) {
            for (int i = 0; i <= segments; i++) {
                double angle = c[2] + (c[3] - c[2]) * i / segments;
                wr.pos(c[0] + Math.cos(angle) * radius, c[1] + Math.sin(angle) * radius, 0).endVertex();
            }
        }

        wr.pos(x + w, y + radius, 0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRoundedRectOutline(double x, double y, double w, double h, double radius, int color, float lineWidth) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        int segments = 8;
        double[][] corners = {
            {x + w - radius, y + radius, -Math.PI / 2, 0},
            {x + w - radius, y + h - radius, 0, Math.PI / 2},
            {x + radius, y + h - radius, Math.PI / 2, Math.PI},
            {x + radius, y + radius, Math.PI, Math.PI * 1.5}
        };

        for (double[] c : corners) {
            for (int i = 0; i <= segments; i++) {
                double angle = c[2] + (c[3] - c[2]) * i / segments;
                wr.pos(c[0] + Math.cos(angle) * radius, c[1] + Math.sin(angle) * radius, 0).endVertex();
            }
        }

        tess.draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawGradientRect(double x, double y, double w, double h, int colorTop, int colorBottom) {
        float a1 = (colorTop >> 24 & 0xFF) / 255.0F;
        float r1 = (colorTop >> 16 & 0xFF) / 255.0F;
        float g1 = (colorTop >> 8 & 0xFF) / 255.0F;
        float b1 = (colorTop & 0xFF) / 255.0F;
        float a2 = (colorBottom >> 24 & 0xFF) / 255.0F;
        float r2 = (colorBottom >> 16 & 0xFF) / 255.0F;
        float g2 = (colorBottom >> 8 & 0xFF) / 255.0F;
        float b2 = (colorBottom & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
        wr.pos(x, y + h, 0).color(r2, g2, b2, a2).endVertex();
        wr.pos(x + w, y + h, 0).color(r2, g2, b2, a2).endVertex();
        wr.pos(x + w, y, 0).color(r1, g1, b1, a1).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawHGradientRect(double x, double y, double w, double h, int colorLeft, int colorRight) {
        float a1 = (colorLeft >> 24 & 0xFF) / 255.0F;
        float r1 = (colorLeft >> 16 & 0xFF) / 255.0F;
        float g1 = (colorLeft >> 8 & 0xFF) / 255.0F;
        float b1 = (colorLeft & 0xFF) / 255.0F;
        float a2 = (colorRight >> 24 & 0xFF) / 255.0F;
        float r2 = (colorRight >> 16 & 0xFF) / 255.0F;
        float g2 = (colorRight >> 8 & 0xFF) / 255.0F;
        float b2 = (colorRight & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
        wr.pos(x, y + h, 0).color(r1, g1, b1, a1).endVertex();
        wr.pos(x + w, y + h, 0).color(r2, g2, b2, a2).endVertex();
        wr.pos(x + w, y, 0).color(r2, g2, b2, a2).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawGlassPanel(double x, double y, double w, double h, double radius, int bgColor, int borderColor) {
        drawRoundedRect(x, y, w, h, radius, bgColor);
        drawRoundedRectOutline(x, y, w, h, radius, borderColor, 1.0F);
    }

    public static void drawCircle(double cx, double cy, double radius, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        wr.pos(cx, cy, 0).endVertex();

        for (int i = 0; i <= 24; i++) {
            double angle = Math.PI * 2.0 * i / 24;
            wr.pos(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 0).endVertex();
        }

        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
