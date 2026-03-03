package dev.bhop.gui;

import dev.bhop.SabrinaCarpenterAuth;
import dev.bhop.data.Account;
import dev.bhop.data.AccountExporter;
import dev.bhop.data.CapeInfo;
import dev.bhop.util.APIUtils;
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
import java.util.ArrayList;
import java.util.Collections;
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
    private static final int BTN_SORT = 5009;
    private static final int BTN_RESTORE = 5010;
    private static final int BTN_REFRESH = 5011;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final GuiScreen parent;
    private AccountListSlot accountSlot;
    private int selectedIndex = -1;
    private String statusMessage = "";
    private long statusExpiry;

    private GlassTextField searchField;
    private GlassButton sortButton;
    private SortMode sortMode = SortMode.LAST_USED;
    private List<Account> allAccounts = new ArrayList<>();
    private List<Account> filteredAccounts = new ArrayList<>();

    private GlassButton loginBtn;
    private GlassButton exportBtn;
    private GlassButton deleteBtn;
    private GlassButton folderBtn;
    private GlassButton copyBtn;
    private GlassButton refreshBtn;
    private boolean refreshing;

    public AccountListGUI(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        searchField = new GlassTextField(0, mc.fontRendererObj, 8, 6, width / 2 - 110, 16);
        searchField.setMaxStringLength(64);

        sortButton = new GlassButton(BTN_SORT, width / 2 - 95, 4, 90, 18, "Sort: " + sortMode.getDisplayName());
        buttonList.add(sortButton);
        buttonList.add(new GlassButton(BTN_RESTORE, width - 88, 4, 80, 18, "Restore"));

        accountSlot = new AccountListSlot(this, mc, width, height, 28, height - 32);

        int rpx = width / 2 + 8;
        int btnY = height - 28;
        int bw = 50;
        int gap = 3;

        loginBtn = new GlassButton(BTN_LOGIN, rpx, btnY, bw, 18, "Login");
        exportBtn = new GlassButton(BTN_EXPORT, rpx + (bw + gap), btnY, bw, 18, "Export");
        copyBtn = new GlassButton(BTN_COPY, rpx + (bw + gap) * 2, btnY, bw, 18, "Copy");
        deleteBtn = GlassButton.danger(BTN_DELETE, rpx + (bw + gap) * 3, btnY, bw, 18, "Delete");
        folderBtn = new GlassButton(BTN_FOLDER, rpx + (bw + gap) * 4, btnY, bw, 18, "Folder");
        refreshBtn = new GlassButton(BTN_REFRESH, width - 78, height - 95, 70, 16, "Refresh");

        buttonList.add(loginBtn);
        buttonList.add(exportBtn);
        buttonList.add(copyBtn);
        buttonList.add(deleteBtn);
        buttonList.add(folderBtn);
        buttonList.add(refreshBtn);

        buttonList.add(new GlassButton(BTN_ADD, 5, btnY, 70, 18, "Add"));
        buttonList.add(new GlassButton(BTN_EXPORT_ALL, 80, btnY, 70, 18, "Export All"));
        buttonList.add(new GlassButton(BTN_BACK, 155, btnY, 55, 18, "Back"));

        updateDetailButtons();
        refreshAccounts();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
    }

    private void refreshAccounts() {
        try {
            allAccounts = SabrinaCarpenterAuth.database.getAll();
            applyFilter();
            Account selected = getSelectedAccount();
            if (selected != null) preloadTextures(selected);
        } catch (Exception e) {
            setStatus("\u00a74Failed to load accounts");
        }
    }

    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase();
        filteredAccounts.clear();
        for (Account a : allAccounts) {
            if (query.isEmpty() || a.getUsername().toLowerCase().contains(query)) {
                filteredAccounts.add(a);
            }
        }
        Collections.sort(filteredAccounts, sortMode.getComparator());
        accountSlot.setAccounts(filteredAccounts);
        if (selectedIndex >= filteredAccounts.size()) selectedIndex = filteredAccounts.size() - 1;
        updateDetailButtons();
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
        boolean has = getSelectedAccount() != null;
        loginBtn.visible = has;
        exportBtn.visible = has;
        copyBtn.visible = has;
        deleteBtn.visible = has;
        folderBtn.visible = has;
        refreshBtn.visible = has;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        accountSlot.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        RenderUtils.drawGradientRect(0, 0, width, height, 0x40180818, 0x60100610);

        RenderUtils.drawGlassPanel(2, 1, width - 4, 24, 6.0, 0x90120812, 0x30D4639A);

        String title = "\u00a7d\u00a7lSabrinaCarpenterAuth \u00a78- \u00a7dAccounts (" + allAccounts.size() + ")";
        mc.fontRendererObj.drawStringWithShadow(title, width / 2 + 4, 9, 0xFFE8A0BF);

        accountSlot.drawScreen(mouseX, mouseY, partialTicks);

        searchField.drawTextBox();

        String countText = "\u00a78" + filteredAccounts.size() + "/" + allAccounts.size();
        mc.fontRendererObj.drawStringWithShadow(countText, width / 2 - 100 - mc.fontRendererObj.getStringWidth(countText), 9, 0x888888);

        Account selected = getSelectedAccount();
        if (selected != null) drawDetailPanel(selected);

        if (accountSlot.getAccounts().isEmpty()) {
            drawCenteredString(mc.fontRendererObj, "\u00a78No accounts found", width / 4, height / 2, 0x888888);
        }

        if (System.currentTimeMillis() < statusExpiry) {
            int sw = mc.fontRendererObj.getStringWidth(statusMessage) + 16;
            RenderUtils.drawRoundedRect(width / 2 - sw / 2, height - 50, sw, 14, 4.0, 0xC0120812);
            drawCenteredString(mc.fontRendererObj, statusMessage, width / 2, height - 48, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDetailPanel(Account account) {
        int px = width / 2 + 4;
        int py = 30;
        int pw = width / 2 - 8;
        int ph = height - 64;

        RenderUtils.drawGlassPanel(px, py, pw, ph, 6.0, 0x90120812, 0x30D4639A);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        int bodyX = px + 8;
        int bodyY = py + 8;
        int bodyW = 55;
        int bodyH = 125;
        ResourceLocation body = TextureCache.get(TextureCache.bodyUrl(account.getUuid()));
        if (body != null) {
            mc.getTextureManager().bindTexture(body);
            Gui.drawModalRectWithCustomSizedTexture(bodyX, bodyY, 0, 0, bodyW, bodyH, bodyW, bodyH);
        } else {
            RenderUtils.drawRoundedRect(bodyX, bodyY, bodyW, bodyH, 4.0, 0xFF1A0A14);
            mc.fontRendererObj.drawString("\u00a78...", bodyX + 20, bodyY + 56, 0x888888);
            TextureCache.loadAsync(TextureCache.bodyUrl(account.getUuid()));
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int capeX = bodyX + bodyW + 6;
        int capeW = 32;
        int capeH = 50;
        ResourceLocation cape = TextureCache.get(TextureCache.capeUrl(account.getUuid()));
        if (cape != null) {
            mc.getTextureManager().bindTexture(cape);
            Gui.drawModalRectWithCustomSizedTexture(capeX, bodyY, 0, 0, capeW, capeH, capeW, capeH);
        }

        int tx = bodyX + bodyW + capeW + 14;
        int ty = bodyY;
        int lineH = 11;

        mc.fontRendererObj.drawStringWithShadow("\u00a7d" + account.getUsername(), tx, ty, 0xFFE8A0BF);
        ty += lineH;
        mc.fontRendererObj.drawStringWithShadow("\u00a78" + account.getFormattedUuid(), tx, ty, 0x888888);
        ty += lineH + 4;
        mc.fontRendererObj.drawStringWithShadow("\u00a77Variant: \u00a7f" + account.getSkinVariant().name(), tx, ty, 0xFFFFFF);
        ty += lineH;

        if (!account.getCapes().isEmpty()) {
            mc.fontRendererObj.drawStringWithShadow("\u00a77Capes:", tx, ty, 0xFFFFFF);
            ty += lineH;
            for (CapeInfo c : account.getCapes()) {
                String color = c.getState().name().equals("ACTIVE") ? "\u00a7a" : "\u00a78";
                mc.fontRendererObj.drawStringWithShadow("  " + color + c.getAlias(), tx, ty, 0xFFFFFF);
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
            case BTN_SORT:
                sortMode = sortMode.next();
                sortButton.displayString = "Sort: " + sortMode.getDisplayName();
                applyFilter();
                break;
            case BTN_RESTORE:
                SessionChanger.setSession(SabrinaCarpenterAuth.originalSession);
                setStatus("\u00a72Restored original session");
                break;
            case BTN_REFRESH:
                if (selected != null && !refreshing) refreshProfile(selected);
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

    private void refreshProfile(Account account) {
        refreshing = true;
        refreshBtn.displayString = "...";
        refreshBtn.enabled = false;
        new Thread(() -> {
            try {
                Account fresh = APIUtils.fetchFullProfile(account.getAccessToken());
                Account updated = new Account(fresh.getUuid(), fresh.getUsername(), account.getAccessToken(),
                        fresh.getSkinUrl(), fresh.getSkinTextureKey(), fresh.getSkinVariant(),
                        fresh.getCapes(), account.getAddedAt(), account.getLastUsedAt());
                SabrinaCarpenterAuth.database.upsert(updated);
                TextureCache.evictForUuid(account.getUuid());
                mc.addScheduledTask(() -> {
                    refreshAccounts();
                    setStatus("\u00a72Refreshed " + updated.getUsername());
                    refreshBtn.displayString = "Refresh";
                    refreshBtn.enabled = true;
                    refreshing = false;
                });
            } catch (Exception e) {
                mc.addScheduledTask(() -> {
                    setStatus("\u00a74Refresh failed");
                    refreshBtn.displayString = "Refresh";
                    refreshBtn.enabled = true;
                    refreshing = false;
                });
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
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (searchField.isFocused()) {
            searchField.textboxKeyTyped(typedChar, keyCode);
            applyFilter();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
