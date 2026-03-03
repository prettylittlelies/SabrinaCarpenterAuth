package dev.bhop;

import dev.bhop.data.AccountDatabase;
import dev.bhop.gui.AccountListGUI;
import dev.bhop.gui.SessionGUI;
import dev.bhop.util.APIUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.File;

@Mod(modid = SabrinaCarpenterAuth.MODID, version = SabrinaCarpenterAuth.VERSION)
public class SabrinaCarpenterAuth {

    public static final String MODID = "sabrinacarpenterauth";
    public static final String VERSION = "1.0";
    public static Session originalSession;
    public static AccountDatabase database;
    public static String sessionValidity = "\u00a72\u2714 Valid";

    private static File modDirectory;

    private static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        Display.setTitle("SabrinaCarpenterAuth " + VERSION);
        originalSession = mc().getSession();
        try {
            modDirectory = new File(mc().mcDataDir, MODID);
            modDirectory.mkdirs();
            database = new AccountDatabase(new File(modDirectory, "accounts.json"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static File getModDirectory() {
        return modDirectory;
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post e) {
        if (!(e.gui instanceof GuiMultiplayer)) return;
        e.buttonList.add(new GuiButton(2100, e.gui.width - 90, 5, 80, 20, "Login"));
        e.buttonList.add(new GuiButton(2200, e.gui.width - 180, 5, 80, 20, "Accounts"));
        new Thread(() -> {
            try {
                sessionValidity = APIUtils.validateSession(mc().getSession().getToken())
                        ? "\u00a72\u2714 Valid" : "\u00a74\u2573 Invalid";
            } catch (Exception ignored) {}
        }).start();
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (!(e.gui instanceof GuiMultiplayer)) return;
        Minecraft mc = mc();
        String info = "\u00a7fUser: " + mc.getSession().getUsername() + "  \u00a7f|  " + sessionValidity;
        mc.fontRendererObj.drawString(info, 5, 10, Color.RED.getRGB());
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre e) {
        if (!(e.gui instanceof GuiMultiplayer)) return;
        Minecraft mc = mc();
        if (e.button.id == 2100) mc.displayGuiScreen(new SessionGUI(e.gui));
        if (e.button.id == 2200) mc.displayGuiScreen(new AccountListGUI(e.gui));
    }
}
