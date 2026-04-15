package org.binance.orderbookservice.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderBookSnapshot {

    private BigDecimal bestBidPrice;

    private BigDecimal bestBidAmount;

    private BigDecimal bestAskPrice;

    private BigDecimal bestAskAmount;

    private BigDecimal spread;

    private Integer bidLevels;

    private Integer askLevels;

    private Long timestamp;
}
