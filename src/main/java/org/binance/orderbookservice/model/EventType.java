package org.binance.orderbookservice.model;

import java.util.Arrays;

public enum EventType {
    
    ORDER_CREATED("order_created"),
    ORDER_CHANGED("order_changed"),
    ORDER_DELETED("order_deleted");

    private final String event;

    EventType(String event) {
        this.event = event;
    }

    public static EventType fromEvent(String event) {
        return Arrays.stream(values())
                .filter(eventType -> eventType.event.equals(event))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Event: " + event));
    }
}
