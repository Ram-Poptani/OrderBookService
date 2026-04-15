package org.binance.orderbookservice.model;

import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class Order {

    long id;

    @NonNull
    BigDecimal price;

    @NonNull
    BigDecimal amount;

    @NonNull
    OrderType type;
}
