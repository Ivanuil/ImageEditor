package edu.tinkoff.imageeditor.config;

public enum AllowedImageExtension {
    PNG,
    JPEG;

    public static AllowedImageExtension caseIgnoreValueOf(final String value) {
        for (AllowedImageExtension enumValue : values()) {
            if (enumValue.name().equalsIgnoreCase(value)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No enum constant " + AllowedImageExtension.class + "." + value);
    }
}
