package de.abatultra.billingservice.model;

import java.util.Arrays;

public enum FreeOwner {
    HTW("HTW"),
    HTW_SAAR("HTW SAAR"),
    ABAT("ABAT"),
    GUNANESAN("GUNANESAN"),
    DUPPER("DUPPER");

    private final String displayName;

    FreeOwner(String displayName) {
        this.displayName = displayName;
    }

    public static boolean isFreeOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            return false;
        }
        String normalized = owner.trim().toUpperCase();
        return Arrays.stream(values())
                .anyMatch(entry -> entry.name().equals(normalized) || entry.displayName.equals(normalized));
    }
}