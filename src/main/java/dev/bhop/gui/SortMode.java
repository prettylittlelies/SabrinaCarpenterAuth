package dev.bhop.gui;

import dev.bhop.data.Account;

import java.util.Comparator;

public enum SortMode {

    LAST_USED("Last Used", new Comparator<Account>() {
        @Override
        public int compare(Account a, Account b) {
            return Long.compare(b.getLastUsedAt(), a.getLastUsedAt());
        }
    }),
    NAME_ASC("Name A-Z", new Comparator<Account>() {
        @Override
        public int compare(Account a, Account b) {
            return a.getUsername().compareToIgnoreCase(b.getUsername());
        }
    }),
    NAME_DESC("Name Z-A", new Comparator<Account>() {
        @Override
        public int compare(Account a, Account b) {
            return b.getUsername().compareToIgnoreCase(a.getUsername());
        }
    }),
    DATE_ADDED("Date Added", new Comparator<Account>() {
        @Override
        public int compare(Account a, Account b) {
            return Long.compare(b.getAddedAt(), a.getAddedAt());
        }
    }),
    HAS_CAPES("Has Capes", new Comparator<Account>() {
        @Override
        public int compare(Account a, Account b) {
            return Integer.compare(b.getCapes().size(), a.getCapes().size());
        }
    });

    private final String displayName;
    private final Comparator<Account> comparator;

    SortMode(String displayName, Comparator<Account> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public String getDisplayName() { return displayName; }
    public Comparator<Account> getComparator() { return comparator; }
    public SortMode next() { return values()[(ordinal() + 1) % values().length]; }
}
