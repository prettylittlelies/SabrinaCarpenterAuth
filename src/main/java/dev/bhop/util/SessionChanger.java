package dev.bhop.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class SessionChanger {

    private SessionChanger() {}

    public static void setSession(Session session) {
        Field field = ReflectionHelper.findField(Minecraft.class, "session", "field_71449_j");
        ReflectionHelper.setPrivateValue(Field.class, field, field.getModifiers() & ~Modifier.FINAL, "modifiers");
        ReflectionHelper.setPrivateValue(Minecraft.class, Minecraft.getMinecraft(), session, "session", "field_71449_j");
    }
}
