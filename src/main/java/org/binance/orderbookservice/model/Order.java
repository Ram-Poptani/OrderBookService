package org.binance.orderbookservice.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class Order {

    @Positive
    long id;
    @NonNull
    @DecimalMin(value = "0.0001", inclusive = true, message = "price must be at least 0.0001")
    BigDecimal price;
    @NonNull
    @DecimalMin(value = "0.0001", inclusive = true, message = "amount must be at least 0.0001")
    BigDecimal amount;
    @NonNull
    OrderType type;
}
