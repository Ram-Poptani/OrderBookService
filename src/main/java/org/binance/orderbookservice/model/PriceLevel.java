package org.binance.orderbookservice.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class PriceLevel {
    BigDecimal price;
    BigDecimal amount;
}
