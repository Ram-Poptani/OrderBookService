package org.binance.orderbookservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class BitstampOrderData {
    private long id;

    @JsonProperty("id_str")
    private String idStr;

    private long datetime;

    @JsonProperty("order_type")
    private int orderType;

    private BigDecimal price;

    @JsonProperty("price_str")
    private String priceStr;

    private BigDecimal amount;

    @JsonProperty("amount_str")
    private String amountStr;

    @JsonProperty("amount_traded")
    private String amountTraded;

    @JsonProperty("amount_at_create")
    private String amountAtCreate;

    private String microtimestamp;

    @JsonProperty("is_liquidation")
    private Boolean isLiquidation;
}
