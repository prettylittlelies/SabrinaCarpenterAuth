package dev.bhop.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GlassButton extends GuiButton {

    private final int normalColor;
    private final int hoverColor;
    private final int disabledColor;
    private final int borderColor;
    private final int hoverBorderColor;

    public GlassButton(int id, int x, int y, int w, int h, String text) {
        this(id, x, y, w, h, text, 0x60281420, 0x80482838, 0x40151015, 0x40D4639A, 0x90FF69B4);
    }

    public GlassButton(int id, int x, int y, int w, int h, String text,
                        int normalColor, int hoverColor, int disabledColor, int borderColor, int hoverBorderColor) {
        super(id, x, y, w, h, text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.disabledColor = disabledColor;
        this.borderColor = borderColor;
        this.hoverBorderColor = hoverBorderColor;
    }

    public static GlassButton danger(int id, int x, int y, int w, int h, String text) {
        return new GlassButton(id, x, y, w, h, text, 0x60402020, 0x80603030, 0x40251515, 0x40FF6666, 0x80FF4444);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;

        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;

        int bg = !enabled ? disabledColor : hovered ? hoverColor : normalColor;
        int border = hovered && enabled ? hoverBorderColor : borderColor;

        RenderUtils.drawGlassPanel(xPosition, yPosition, width, height, 4.0, bg, border);

        int textColor = enabled ? 0xFFFFFFFF : 0xFF666666;
        int tx = xPosition + (width - mc.fontRendererObj.getStringWidth(displayString)) / 2;
        int ty = yPosition + (height - 8) / 2;
        mc.fontRendererObj.drawStringWithShadow(displayString, tx, ty, textColor);
    }
}
