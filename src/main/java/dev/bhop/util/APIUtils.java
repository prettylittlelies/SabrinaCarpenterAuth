package dev.bhop.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.bhop.data.Account;
import dev.bhop.data.CapeInfo;
import dev.bhop.data.CapeState;
import dev.bhop.data.SkinVariant;
import net.minecraft.client.Minecraft;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class APIUtils {

    private static final String PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final String SKIN_URL = "https://api.minecraftservices.com/minecraft/profile/skins";
    private static final String ONLINE_URL = "https://api.slothpixel.me/api/players/";

    private APIUtils() {}

    public static Account fetchFullProfile(String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(PROFILE_URL);
            request.setHeader("Authorization", "Bearer " + token);
            try (CloseableHttpResponse response = client.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                if (status != 200) throw new IOException("API returned " + status);
                JsonObject json = new JsonParser().parse(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)).getAsJsonObject();
                return parseProfile(json, token);
            }
        }
    }

    private static Account parseProfile(JsonObject json, String token) {
        String uuid = json.get("id").getAsString();
        String username = json.get("name").getAsString();

        String skinUrl = null;
        String skinTextureKey = null;
        SkinVariant skinVariant = SkinVariant.CLASSIC;

        if (json.has("skins") && json.get("skins").isJsonArray()) {
            for (JsonElement elem : json.getAsJsonArray("skins")) {
                JsonObject skin = elem.getAsJsonObject();
                if ("ACTIVE".equals(getStr(skin, "state"))) {
                    skinUrl = getStr(skin, "url");
                    skinTextureKey = getStr(skin, "textureKey");
                    skinVariant = SkinVariant.fromString(getStr(skin, "variant"));
                    break;
                }
            }
        }

        List<CapeInfo> capes = new ArrayList<>();
        if (json.has("capes") && json.get("capes").isJsonArray()) {
            for (JsonElement elem : json.getAsJsonArray("capes")) {
                JsonObject cape = elem.getAsJsonObject();
                capes.add(new CapeInfo(
                    getStr(cape, "id"),
                    getStr(cape, "alias"),
                    getStr(cape, "url"),
                    CapeState.fromString(getStr(cape, "state"))
                ));
            }
        }

        long now = System.currentTimeMillis();
        return new Account(uuid, username, token, skinUrl, skinTextureKey, skinVariant,
                capes.isEmpty() ? Collections.<CapeInfo>emptyList() : capes, now, now);
    }

    public static boolean validateSession(String token) {
        try {
            Account profile = fetchFullProfile(token);
            Minecraft mc = Minecraft.getMinecraft();
            return profile.getUsername().equals(mc.getSession().getUsername())
                    && profile.getUuid().equals(mc.getSession().getPlayerID());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkOnline(String username) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(ONLINE_URL + username);
            try (CloseableHttpResponse response = client.execute(request)) {
                JsonObject json = new JsonParser().parse(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)).getAsJsonObject();
                return json.has("online") && json.get("online").getAsBoolean();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static int changeSkin(String url, String token) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(SKIN_URL);
            request.setHeader("Authorization", "Bearer " + token);
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity("{\"variant\":\"classic\",\"url\":\"" + url + "\"}"));
            try (CloseableHttpResponse response = client.execute(request)) {
                return response.getStatusLine().getStatusCode();
            }
        }
    }

    private static String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
