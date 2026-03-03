package dev.bhop.gui;

import dev.bhop.SabrinaCarpenterAuth;
import dev.bhop.data.Account;
import dev.bhop.data.AccountExporter;
import dev.bhop.data.CapeInfo;
import dev.bhop.util.SessionChanger;
import dev.bhop.util.TextureCache;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AccountListGUI extends GuiScreen {

    private static final int BTN_LOGIN = 5001;
    private static final int BTN_EXPORT = 5002;
    private static final int BTN_DELETE = 5003;
    private static final int BTN_FOLDER = 5004;
    private static final int BTN_ADD = 5005;
    private static final int BTN_EXPORT_ALL = 5006;
    private static final int BTN_BACK = 5007;
    private static final int BTN_COPY = 5008;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final GuiScreen parent;
    private AccountListSlot accountSlot;
    private int selectedIndex = -1;
    private String statusMessage = "";
    private long statusExpiry;

    private GuiButton loginBtn;
    private GuiButton exportBtn;
    private GuiButton deleteBtn;
    private GuiButton folderBtn;
    private GuiButton copyBtn;

    public AccountListGUI(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        accountSlot = new AccountListSlot(this, mc, width, height, 32, height - 48);
        refreshAccounts();

        int rpx = width / 2 + 10;
        int btnY = height - 80;
        int bw = 57;
        int gap = 4;

        loginBtn = new GuiButton(BTN_LOGIN, rpx, btnY, bw, 20, "Login");
        exportBtn = new GuiButton(BTN_EXPORT, rpx + bw + gap, btnY, bw, 20, "Export");
        copyBtn = new GuiButton(BTN_COPY, rpx + (bw + gap) * 2, btnY, bw, 20, "Copy");
        deleteBtn = new GuiButton(BTN_DELETE, rpx, btnY + 24, bw, 20, "\u00a7cDelete");
        folderBtn = new GuiButton(BTN_FOLDER, rpx + bw + gap, btnY + 24, bw * 2 + gap, 20, "Open Folder");

        buttonList.add(loginBtn);
        buttonList.add(exportBtn);
        buttonList.add(copyBtn);
        buttonList.add(deleteBtn);
        buttonList.add(folderBtn);

        int bottomY = height - 28;
        buttonList.add(new GuiButton(BTN_ADD, 5, bottomY, 90, 20, "Add Account"));
        buttonList.add(new GuiButton(BTN_EXPORT_ALL, 100, bottomY, 90, 20, "Export All"));
        buttonList.add(new GuiButton(BTN_BACK, width / 2 - 45, bottomY, 90, 20, "Back"));

        updateDetailButtons();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void refreshAccounts() {
        try {
            List<Account> accounts = SabrinaCarpenterAuth.database.getAll();
            accountSlot.setAccounts(accounts);
            if (selectedIndex >= accounts.size()) selectedIndex = accounts.size() - 1;
            Account selected = getSelectedAccount();
            if (selected != null) preloadTextures(selected);
        } catch (Exception e) {
            setStatus("\u00a74Failed to load accounts");
        }
    }

    private void preloadTextures(Account account) {
        TextureCache.loadAsync(TextureCache.bodyUrl(account.getUuid()));
        TextureCache.loadAsync(TextureCache.capeUrl(account.getUuid()));
    }

    public void onAccountSelected(int index) {
        selectedIndex = index;
        updateDetailButtons();
        Account account = getSelectedAccount();
        if (account != null) preloadTextures(account);
    }

    public void onAccountDoubleClicked(int index) {
        Account account = accountSlot.getAccount(index);
        if (account != null) loginToAccount(account);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private Account getSelectedAccount() {
        return accountSlot.getAccount(selectedIndex);
    }

    private void updateDetailButtons() {
        boolean hasSelection = getSelectedAccount() != null;
        loginBtn.visible = hasSelection;
        exportBtn.visible = hasSelection;
        copyBtn.visible = hasSelection;
        deleteBtn.visible = hasSelection;
        folderBtn.visible = hasSelection;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        accountSlot.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        accountSlot.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(mc.fontRendererObj, "\u00a7fSabrinaCarpenterAuth \u00a77- Accounts (" + accountSlot.getAccounts().size() + ")", width / 4, 14, 0xFFFFFF);

        if (accountSlot.getAccounts().isEmpty()) {
            drawCenteredString(mc.fontRendererObj, "\u00a78No accounts saved", width / 4, height / 2 - 4, 0x888888);
            drawCenteredString(mc.fontRendererObj, "\u00a78Use 'Add Account' to get started", width / 4, height / 2 + 10, 0x888888);
        }

        Account selected = getSelectedAccount();
        if (selected != null) drawDetailPanel(selected);

        if (System.currentTimeMillis() < statusExpiry) {
            drawCenteredString(mc.fontRendererObj, statusMessage, width / 2, 3, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDetailPanel(Account account) {
        int px = width / 2 + 10;
        int py = 36;

        Gui.drawRect(width / 2 + 2, 32, width - 2, height - 48, 0x40000000);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        int bodyW = 55;
        int bodyH = 125;
        ResourceLocation body = TextureCache.get(TextureCache.bodyUrl(account.getUuid()));
        if (body != null) {
            mc.getTextureManager().bindTexture(body);
            Gui.drawModalRectWithCustomSizedTexture(px, py, 0, 0, bodyW, bodyH, bodyW, bodyH);
        } else {
            Gui.drawRect(px, py, px + bodyW, py + bodyH, 0xFF2A2A2A);
            mc.fontRendererObj.drawString("\u00a78...", px + 20, py + 56, 0x888888);
            TextureCache.loadAsync(TextureCache.bodyUrl(account.getUuid()));
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int capeX = px + bodyW + 8;
        int capeW = 32;
        int capeH = 50;
        ResourceLocation cape = TextureCache.get(TextureCache.capeUrl(account.getUuid()));
        if (cape != null) {
            mc.getTextureManager().bindTexture(cape);
            Gui.drawModalRectWithCustomSizedTexture(capeX, py, 0, 0, capeW, capeH, capeW, capeH);
        }

        int tx = px + bodyW + capeW + 20;
        int ty = py;
        int lineH = 11;

        mc.fontRendererObj.drawStringWithShadow("\u00a7f" + account.getUsername(), tx, ty, 0xFFFFFF);
        ty += lineH;

        mc.fontRendererObj.drawStringWithShadow("\u00a78" + account.getFormattedUuid(), tx, ty, 0x888888);
        ty += lineH + 4;

        mc.fontRendererObj.drawStringWithShadow("\u00a77Variant: \u00a7f" + account.getSkinVariant().name(), tx, ty, 0xFFFFFF);
        ty += lineH;

        if (!account.getCapes().isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow("\u00a77Capes:", tx, ty, 0xFFFFFF);
            ty += lineH;
            for (CapeInfo cape2 : account.getCapes()) {
                String stateColor = cape2.getState().name().equals("ACTIVE") ? "\u00a7a" : "\u00a78";
                mc.fontRendererObj.drawStringWithShadow("  " + stateColor + cape2.getAlias(), tx, ty, 0xFFFFFF);
                ty += lineH - 1;
            }
        } else {
            mc.fontRendererObj.drawStringWithShadow("\u00a78No capes", tx, ty, 0x888888);
            ty += lineH;
        }

        ty += 4;
        mc.fontRendererObj.drawStringWithShadow("\u00a78Added: " + DATE_FORMAT.format(new Date(account.getAddedAt())), tx, ty, 0x888888);
        ty += lineH;
        mc.fontRendererObj.drawStringWithShadow("\u00a78Last used: " + DATE_FORMAT.format(new Date(account.getLastUsedAt())), tx, ty, 0x888888);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        Account selected = getSelectedAccount();
        switch (button.id) {
            case BTN_LOGIN:
                if (selected != null) loginToAccount(selected);
                break;
            case BTN_EXPORT:
                if (selected != null) exportAccount(selected);
                break;
            case BTN_COPY:
                if (selected != null) copyToken(selected);
                break;
            case BTN_DELETE:
                if (selected != null) deleteAccount(selected);
                break;
            case BTN_FOLDER:
                if (selected != null) openAccountFolder(selected);
                break;
            case BTN_ADD:
                mc.displayGuiScreen(new SessionGUI(this));
                break;
            case BTN_EXPORT_ALL:
                exportAllAccounts();
                break;
            case BTN_BACK:
                mc.displayGuiScreen(parent);
                break;
        }
    }

    private void loginToAccount(Account account) {
        new Thread(() -> {
            try {
                SessionChanger.setSession(new Session(account.getUsername(), account.getUuid(), account.getAccessToken(), "mojang"));
                SabrinaCarpenterAuth.database.updateLastUsed(account.getUuid(), System.currentTimeMillis());
                setStatus("\u00a72Logged in as " + account.getUsername());
                mc.addScheduledTask(this::refreshAccounts);
            } catch (Exception e) {
                setStatus("\u00a74Login failed");
            }
        }).start();
    }

    private void copyToken(Account account) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(account.getAccessToken()), null);
            setStatus("\u00a72Token copied");
        } catch (Exception e) {
            setStatus("\u00a74Copy failed");
        }
    }

    private void deleteAccount(Account account) {
        try {
            SabrinaCarpenterAuth.database.delete(account.getUuid());
            selectedIndex = -1;
            updateDetailButtons();
            refreshAccounts();
            setStatus("\u00a7eDeleted " + account.getUsername());
        } catch (Exception e) {
            setStatus("\u00a74Delete failed");
        }
    }

    private void exportAccount(Account account) {
        try {
            File dir = AccountExporter.exportSingle(account, SabrinaCarpenterAuth.getModDirectory());
            Desktop.getDesktop().open(dir);
            setStatus("\u00a72Exported " + account.getUsername());
        } catch (Exception e) {
            setStatus("\u00a74Export failed");
        }
    }

    private void exportAllAccounts() {
        try {
            List<Account> all = SabrinaCarpenterAuth.database.getAll();
            if (all.isEmpty()) {
                setStatus("\u00a7eNo accounts to export");
                return;
            }
            File dir = AccountExporter.exportAll(all, SabrinaCarpenterAuth.getModDirectory());
            Desktop.getDesktop().open(dir);
            setStatus("\u00a72Exported " + all.size() + " accounts");
        } catch (Exception e) {
            setStatus("\u00a74Export all failed");
        }
    }

    private void openAccountFolder(Account account) {
        try {
            File folder = new File(SabrinaCarpenterAuth.getModDirectory(), "accounts/" + account.getUuid());
            folder.mkdirs();
            Desktop.getDesktop().open(folder);
        } catch (Exception e) {
            setStatus("\u00a74Failed to open folder");
        }
    }

    private void setStatus(String message) {
        statusMessage = message;
        statusExpiry = System.currentTimeMillis() + 3000;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
        else super.keyTyped(typedChar, keyCode);
    }
}
