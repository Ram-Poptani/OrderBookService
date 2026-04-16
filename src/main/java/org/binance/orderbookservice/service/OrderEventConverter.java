package org.binance.orderbookservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.binance.orderbookservice.dto.BitstampOrderData;
import org.binance.orderbookservice.dto.BitstampResponse;
import org.binance.orderbookservice.exceptions.OrderEventParseException;
import org.binance.orderbookservice.model.EventType;
import org.binance.orderbookservice.model.OrderEvent;
import org.binance.orderbookservice.model.OrderType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class OrderEventConverter {
    private final ObjectMapper objectMapper;


    public OrderEventConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    BitstampResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, BitstampResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Bitstamp message: {}", json, e);
            throw new OrderEventParseException("Failed to parse Bitstamp order event JSON", e);
        }
    }

    public OrderEvent toOrderEvent(BitstampResponse response) {
        BitstampOrderData data = response.getData();
        if (data == null) {
            throw new IllegalArgumentException("BitstampResponse has no data: " + response);
        }

        return OrderEvent.builder()
                .id(data.getId())
                .orderType(OrderType.fromCode(data.getOrderType()))
                .eventType(EventType.fromEvent(response.getEvent()))
                .price(new BigDecimal(data.getPriceStr()))
                .amount(new BigDecimal(data.getAmountStr()))
                .amountTraded(new BigDecimal(data.getAmountTraded()))
                .microtimestamp(data.getMicrotimestamp())
                .channel(response.getChannel())
                .build();
    }
}
