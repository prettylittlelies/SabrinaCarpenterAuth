package dev.bhop.gui;

import dev.bhop.data.Account;
import dev.bhop.util.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class AccountListSlot extends GuiSlot {

    private final AccountListGUI parent;
    private final List<Account> accounts = new ArrayList<>();

    public AccountListSlot(AccountListGUI parent, Minecraft mc, int screenWidth, int screenHeight, int top, int bottom) {
        super(mc, screenWidth / 2, screenHeight, top, bottom, 26);
        this.parent = parent;
    }

    public void setAccounts(List<Account> list) {
        accounts.clear();
        accounts.addAll(list);
        for (Account account : accounts) TextureCache.loadAsync(TextureCache.headUrl(account.getUuid()));
    }

    public Account getAccount(int index) {
        return index >= 0 && index < accounts.size() ? accounts.get(index) : null;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    @Override
    protected int getSize() {
        return accounts.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
        parent.onAccountSelected(index);
        if (doubleClick) parent.onAccountDoubleClicked(index);
    }

    @Override
    protected boolean isSelected(int index) {
        return parent.getSelectedIndex() == index;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawSlot(int index, int x, int y, int slotHeight, int mouseX, int mouseY) {
        Account account = accounts.get(index);
        Minecraft mc = Minecraft.getMinecraft();

        ResourceLocation head = TextureCache.get(TextureCache.headUrl(account.getUuid()));
        if (head != null) {
            mc.getTextureManager().bindTexture(head);
            Gui.drawModalRectWithCustomSizedTexture(x, y + 1, 0, 0, 20, 20, 20, 20);
        } else {
            Gui.drawRect(x, y + 1, x + 20, y + 21, 0xFF333333);
        }

        mc.fontRendererObj.drawStringWithShadow(account.getUsername(), x + 25, y + 3, 0xFFFFFF);

        String variant = "\u00a77" + account.getSkinVariant().name();
        mc.fontRendererObj.drawStringWithShadow(variant, x + 25, y + 14, 0x888888);
    }

    @Override
    public int getListWidth() {
        return this.width - 20;
    }

    @Override
    protected int getScrollBarX() {
        return this.width - 8;
    }
}
