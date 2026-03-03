package dev.bhop.data;

import java.util.Collections;
import java.util.List;

public final class Account {

    private final String uuid;
    private final String username;
    private final String accessToken;
    private final String skinUrl;
    private final String skinTextureKey;
    private final SkinVariant skinVariant;
    private final List<CapeInfo> capes;
    private final long addedAt;
    private final long lastUsedAt;

    public Account(String uuid, String username, String accessToken, String skinUrl,
                   String skinTextureKey, SkinVariant skinVariant, List<CapeInfo> capes,
                   long addedAt, long lastUsedAt) {
        this.uuid = uuid;
        this.username = username;
        this.accessToken = accessToken;
        this.skinUrl = skinUrl;
        this.skinTextureKey = skinTextureKey;
        this.skinVariant = skinVariant;
        this.capes = Collections.unmodifiableList(capes);
        this.addedAt = addedAt;
        this.lastUsedAt = lastUsedAt;
    }

    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getAccessToken() { return accessToken; }
    public String getSkinUrl() { return skinUrl; }
    public String getSkinTextureKey() { return skinTextureKey; }
    public SkinVariant getSkinVariant() { return skinVariant; }
    public List<CapeInfo> getCapes() { return capes; }
    public long getAddedAt() { return addedAt; }
    public long getLastUsedAt() { return lastUsedAt; }

    public String getFormattedUuid() {
        if (uuid.length() != 32) return uuid;
        return uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" +
               uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
    }

    public Account withLastUsedAt(long timestamp) {
        return new Account(uuid, username, accessToken, skinUrl, skinTextureKey, skinVariant, capes, addedAt, timestamp);
    }

    public Account withToken(String token) {
        return new Account(uuid, username, token, skinUrl, skinTextureKey, skinVariant, capes, addedAt, lastUsedAt);
    }
}
