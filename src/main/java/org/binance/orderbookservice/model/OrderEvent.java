package org.binance.orderbookservice.model;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
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
