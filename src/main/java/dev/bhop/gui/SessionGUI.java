package dev.bhop.gui;

import dev.bhop.SabrinaCarpenterAuth;
import dev.bhop.data.Account;
import dev.bhop.util.APIUtils;
import dev.bhop.util.SessionChanger;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class SessionGUI extends GuiScreen {

    private final GuiScreen parent;
    private GlassTextField tokenField;
    private ScaledResolution sr;
    private String status = "Enter Session Token";

    public SessionGUI(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        sr = new ScaledResolution(mc);
        int cx = sr.getScaledWidth() / 2;
        int cy = sr.getScaledHeight() / 2;

        tokenField = new GlassTextField(1, mc.fontRendererObj, cx - 100, cy, 200, 20);
        tokenField.setMaxStringLength(32767);
        tokenField.setFocused(true);

        buttonList.add(new GlassButton(1400, cx - 100, cy + 25, 97, 20, "Login"));
        buttonList.add(new GlassButton(1500, cx + 3, cy + 25, 97, 20, "Restore"));
        buttonList.add(new GlassButton(1600, cx - 100, cy + 50, 200, 20, "Back"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        tokenField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int cx = sr.getScaledWidth() / 2;
        int cy = sr.getScaledHeight() / 2;
        RenderUtils.drawGlassPanel(cx - 110, cy - 45, 220, 125, 8.0, 0x80101020, 0x40FFFFFF);
        mc.fontRendererObj.drawStringWithShadow(status, cx - mc.fontRendererObj.getStringWidth(status) / 2, cy - 30, 0xFFFFFFFF);
        tokenField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        tokenField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1400:
                new Thread(() -> {
                    try {
                        String token = tokenField.getText().trim();
                        if (token.isEmpty()) {
                            status = "\u00a74Token cannot be empty";
                            return;
                        }
                        status = "\u00a7eAuthenticating...";
                        Account account = APIUtils.fetchFullProfile(token);
                        SabrinaCarpenterAuth.database.upsert(account);
                        SessionChanger.setSession(new Session(account.getUsername(), account.getUuid(), token, "mojang"));
                        status = "\u00a72Logged in as " + account.getUsername();
                    } catch (Exception e) {
                        status = "\u00a74Invalid token";
                    }
                }).start();
                break;
            case 1500:
                SessionChanger.setSession(SabrinaCarpenterAuth.originalSession);
                status = "\u00a72Restored session";
                break;
            case 1600:
                mc.displayGuiScreen(parent);
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        tokenField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }
}
