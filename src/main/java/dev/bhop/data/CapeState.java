package dev.bhop.data;

public enum CapeState {

    ACTIVE, INACTIVE;

    public static CapeState fromString(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INACTIVE;
        }
    }
}
