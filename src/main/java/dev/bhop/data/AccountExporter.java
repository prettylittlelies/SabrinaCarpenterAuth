package dev.bhop.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class AccountExporter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AccountExporter() {}

    public static File exportSingle(Account account, File baseDir) throws IOException {
        File accountDir = new File(baseDir, "accounts/" + account.getUuid());
        accountDir.mkdirs();
        File output = new File(accountDir, "profile.json");
        try (FileWriter writer = new FileWriter(output)) {
            GSON.toJson(toJson(account), writer);
        }
        return accountDir;
    }

    public static File exportAll(List<Account> accounts, File baseDir) throws IOException {
        File exportsDir = new File(baseDir, "exports");
        exportsDir.mkdirs();
        File output = new File(exportsDir, "all_accounts.json");
        JsonArray array = new JsonArray();
        for (Account account : accounts) array.add(toJson(account));
        try (FileWriter writer = new FileWriter(output)) {
            GSON.toJson(array, writer);
        }
        return exportsDir;
    }

    private static JsonObject toJson(Account account) {
        JsonObject obj = new JsonObject();
        obj.addProperty("uuid", account.getFormattedUuid());
        obj.addProperty("username", account.getUsername());
        obj.addProperty("accessToken", account.getAccessToken());
        obj.addProperty("skinUrl", account.getSkinUrl());
        obj.addProperty("skinVariant", account.getSkinVariant().name());
        JsonArray capesArr = new JsonArray();
        for (CapeInfo cape : account.getCapes()) {
            JsonObject c = new JsonObject();
            c.addProperty("alias", cape.getAlias());
            c.addProperty("url", cape.getUrl());
            c.addProperty("state", cape.getState().name());
            capesArr.add(c);
        }
        obj.add("capes", capesArr);
        obj.addProperty("addedAt", account.getAddedAt());
        obj.addProperty("lastUsedAt", account.getLastUsedAt());
        return obj;
    }
}
