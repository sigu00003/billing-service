package de.abatultra.billingservice.model;

import java.util.Arrays;

public enum StateCapital {
    STUTTGART("S"),
    MUENCHEN("M"),
    BERLIN("B"),
    POTSDAM("P"),
    BREMEN("HB"),
    HAMBURG("HH"),
    WIESBADEN("WI"),
    SCHWERIN("SN"),
    HANNOVER("H"),
    DUESSELDORF("D"),
    MAINZ("MZ"),
    SAARBRUECKEN("SB"),
    DRESDEN("DD"),
    MAGDEBURG("MD"),
    KIEL("KI"),
    ERFURT("EF");

    private final String code;

    StateCapital(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static boolean isStateCapital(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String normalized = location.trim().toUpperCase();
        return Arrays.stream(values())
                .anyMatch(city -> city.name().equals(normalized) || city.code.equals(normalized));
    }


}
