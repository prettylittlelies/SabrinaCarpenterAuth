package dev.bhop.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GlassTextField extends GuiTextField {

    private static final int BG_COLOR = 0x80101020;
    private static final int BORDER_NORMAL = 0x40FFFFFF;
    private static final int BORDER_FOCUSED = 0x804488FF;

    public GlassTextField(int id, FontRenderer font, int x, int y, int w, int h) {
        super(id, font, x, y, w, h);
    }

    @Override
    public void drawTextBox() {
        if (!getVisible()) return;
        int border = isFocused() ? BORDER_FOCUSED : BORDER_NORMAL;
        RenderUtils.drawGlassPanel(xPosition - 1, yPosition - 1, width + 2, height + 2, 4.0, BG_COLOR, border);
        boolean bg = getEnableBackgroundDrawing();
        if (bg) {
            setEnableBackgroundDrawing(false);
            xPosition += 4;
            width -= 8;
            super.drawTextBox();
            width += 8;
            xPosition -= 4;
            setEnableBackgroundDrawing(true);
        } else {
            super.drawTextBox();
        }
    }
}
