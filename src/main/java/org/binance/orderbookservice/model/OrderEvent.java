package org.binance.orderbookservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderEvent {

    long id;

    @NonNull
    OrderType orderType;

    @NonNull
    EventType eventType;

    @NonNull
    BigDecimal price;

    @NonNull
    BigDecimal amount;

    @NonNull
    BigDecimal amountTraded;

    @NonNull
    String microtimestamp;

    @NonNull
    String channel;

}
