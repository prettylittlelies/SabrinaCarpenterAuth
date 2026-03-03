package dev.bhop.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AccountDatabase implements AutoCloseable {

    private static final Type CAPE_LIST_TYPE = new TypeToken<List<CapeInfo>>() {}.getType();
    private static final Gson GSON = new Gson();

    private final Connection connection;

    public AccountDatabase(File dbFile) throws SQLException {
        dbFile.getParentFile().mkdirs();
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        connection.setAutoCommit(true);
        initialize();
    }

    private void initialize() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "uuid TEXT PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "access_token TEXT NOT NULL," +
                "skin_url TEXT," +
                "skin_texture_key TEXT," +
                "skin_variant TEXT DEFAULT 'CLASSIC'," +
                "capes_json TEXT DEFAULT '[]'," +
                "added_at INTEGER NOT NULL," +
                "last_used_at INTEGER NOT NULL)"
            );
        }
    }

    public void upsert(Account account) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO accounts (uuid, username, access_token, skin_url, skin_texture_key, skin_variant, capes_json, added_at, last_used_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, account.getUuid());
            ps.setString(2, account.getUsername());
            ps.setString(3, account.getAccessToken());
            ps.setString(4, account.getSkinUrl());
            ps.setString(5, account.getSkinTextureKey());
            ps.setString(6, account.getSkinVariant().name());
            ps.setString(7, GSON.toJson(account.getCapes()));
            ps.setLong(8, account.getAddedAt());
            ps.setLong(9, account.getLastUsedAt());
            ps.executeUpdate();
        }
    }

    public void delete(String uuid) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM accounts WHERE uuid = ?")) {
            ps.setString(1, uuid);
            ps.executeUpdate();
        }
    }

    public void updateLastUsed(String uuid, long timestamp) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE accounts SET last_used_at = ? WHERE uuid = ?")) {
            ps.setLong(1, timestamp);
            ps.setString(2, uuid);
            ps.executeUpdate();
        }
    }

    public List<Account> getAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM accounts ORDER BY last_used_at DESC")) {
            while (rs.next()) accounts.add(fromResultSet(rs));
        }
        return accounts;
    }

    public int count() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM accounts")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Account fromResultSet(ResultSet rs) throws SQLException {
        List<CapeInfo> capes;
        try {
            capes = GSON.fromJson(rs.getString("capes_json"), CAPE_LIST_TYPE);
        } catch (Exception e) {
            capes = Collections.emptyList();
        }
        return new Account(
            rs.getString("uuid"),
            rs.getString("username"),
            rs.getString("access_token"),
            rs.getString("skin_url"),
            rs.getString("skin_texture_key"),
            SkinVariant.fromString(rs.getString("skin_variant")),
            capes != null ? capes : Collections.<CapeInfo>emptyList(),
            rs.getLong("added_at"),
            rs.getLong("last_used_at")
        );
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
