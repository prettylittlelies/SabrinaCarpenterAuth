package dev.bhop.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;

public final class AccountDatabase {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ACCOUNT_MAP_TYPE = new TypeToken<LinkedHashMap<String, Account>>() {}.getType();

    private final File file;
    private final LinkedHashMap<String, Account> accounts;

    public AccountDatabase(File file) {
        this.file = file;
        this.accounts = load();
    }

    private LinkedHashMap<String, Account> load() {
        if (!file.exists()) return new LinkedHashMap<>();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"))) {
            LinkedHashMap<String, Account> map = GSON.fromJson(reader, ACCOUNT_MAP_TYPE);
            return map != null ? map : new LinkedHashMap<String, Account>();
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private void save() {
        file.getParentFile().mkdirs();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"))) {
            GSON.toJson(accounts, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void upsert(Account account) {
        accounts.put(account.getUuid(), account);
        save();
    }

    public synchronized void delete(String uuid) {
        accounts.remove(uuid);
        save();
    }

    public synchronized void updateLastUsed(String uuid, long timestamp) {
        Account existing = accounts.get(uuid);
        if (existing != null) {
            accounts.put(uuid, existing.withLastUsedAt(timestamp));
            save();
        }
    }

    public synchronized List<Account> getAll() {
        List<Account> list = new ArrayList<>(accounts.values());
        Collections.sort(list, new Comparator<Account>() {
            @Override
            public int compare(Account a, Account b) {
                return Long.compare(b.getLastUsedAt(), a.getLastUsedAt());
            }
        });
        return list;
    }

    public synchronized int count() {
        return accounts.size();
    }
}
