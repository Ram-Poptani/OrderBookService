package org.binance.orderbookservice.model;

import java.util.Arrays;

public enum OrderType {
    BID(0),
    ASK(1);

    private final int code;

    OrderType(int code) {
        this.code = code;
    }

    public static OrderType fromCode(int code) {
        return Arrays.stream(values())
                .filter(o -> o.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown code: " + code));
    }
}
