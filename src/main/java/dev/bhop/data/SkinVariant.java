package dev.bhop.data;

public enum SkinVariant {

    CLASSIC, SLIM;

    public static SkinVariant fromString(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CLASSIC;
        }
    }
}
