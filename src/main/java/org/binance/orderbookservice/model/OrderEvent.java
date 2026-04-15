package org.binance.orderbookservice.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderEvent {

    @Positive
    long id;

    @NonNull
    OrderType type;

    @NonNull
    EventType event;

    @NonNull
    @DecimalMin(value = "0.0001", inclusive = true, message = "price must be at least 0.0001")
    BigDecimal price;

    @NonNull
    @DecimalMin(value = "0.0001", inclusive = true, message = "amount must be at least 0.0001")
    BigDecimal amount;

    @NonNull
    @DecimalMin(value = "0", inclusive = true, message = "amountTraded must be at least 0")
    BigDecimal amountTraded;

    @NonNull
    String microtimestamp;

    @NonNull
    String channel;

}
