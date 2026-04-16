package org.binance.orderbookservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BitstampResponse {

    private String event;

    private String channel;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("pre_event_id")
    private String preEventId;

    @JsonProperty("order_source")
    private String orderSource;

    private BitstampOrderData data;
}
