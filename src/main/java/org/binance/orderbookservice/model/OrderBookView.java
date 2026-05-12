package org.binance.orderbookservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OrderBookView {

    private List<PriceLevel> bids;
    private List<PriceLevel> asks;
    BigDecimal spread;
    int levels;
    long timestamp;

}
