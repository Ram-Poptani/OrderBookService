package org.binance.orderbookservice.model;

import lombok.Value;

@Value
public class SequenceState {
    String lastEventId;
    OrderEvent orderEvent;
    boolean gapDetected;
}