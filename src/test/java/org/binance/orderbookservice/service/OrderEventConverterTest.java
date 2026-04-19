package org.binance.orderbookservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.binance.orderbookservice.dto.BitstampOrderData;
import org.binance.orderbookservice.dto.BitstampResponse;
import org.binance.orderbookservice.exceptions.OrderEventParseException;
import org.binance.orderbookservice.model.EventType;
import org.binance.orderbookservice.model.OrderEvent;
import org.binance.orderbookservice.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderEventConverterTest {

    private OrderEventConverter converter;

    @BeforeEach
    void setUp() {
        converter = new OrderEventConverter(new ObjectMapper());
    }

    @Test
    void deserialize_validJson_returnsResponse() {
        String json = """
                {
                  "data": {
                    "id": 1994262360649728,
                    "id_str": "1994262360649728",
                    "order_type": 0,
                    "order_subtype": 5,
                    "datetime": "1775715434",
                    "microtimestamp": "1775715433800000",
                    "amount": 0.045,
                    "amount_str": "0.04500000",
                    "amount_traded": "0",
                    "amount_at_create": "0.04500000",
                    "price": 71009,
                    "price_str": "71009",
                    "is_liquidation": false
                  },
                  "channel": "live_orders_btcusd",
                  "event": "order_deleted",
                  "event_id": "00064f00-f7a7-6140-0001-000103000020",
                  "pre_event_id": "00064f00-f7a7-6140-0000-000102000020",
                  "order_source": "orderbook"
                }
                """;

        BitstampResponse response = converter.deserialize(json);

        assertThat(response).isNotNull();

        assertThat(response.getEvent()).isEqualTo("order_deleted");
        assertThat(response.getChannel()).isEqualTo("live_orders_btcusd");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isPositive();
        assertThat(response.getData().getAmount()).isPositive();
        assertThat(response.getData().getPrice()).isPositive();
    }

    @Test
    void deserialize_invalidJson_throwsException() {
        String invalidJson = "{ invalid json }";


        assertThatThrownBy(() -> converter.deserialize(invalidJson))
                .isInstanceOf(OrderEventParseException.class)
                .hasMessageContaining("Failed to parse Bitstamp order event JSON");
    }

    @Test
    void toOrderEvent_validResponse_returnsOrderEvent() {
        BitstampOrderData data = new BitstampOrderData();
        data.setId(123L);
        data.setOrderType(0);
        data.setPriceStr("71009");
        data.setAmountStr("0.045");
        data.setAmountTraded("0");
        data.setMicrotimestamp("1775715433800000");

        BitstampResponse response = new BitstampResponse();
        response.setEvent("order_created");
        response.setChannel("live_orders_btcusd");
        response.setData(data);

        OrderEvent orderEvent = converter.toOrderEvent(response);

        assertThat(orderEvent.getOrderType()).isEqualTo(OrderType.BID);
        assertThat(orderEvent.getEventType()).isEqualTo(EventType.ORDER_CREATED);
        assertThat(orderEvent.getPrice()).isEqualByComparingTo("71009");
        assertThat(orderEvent.getAmount()).isEqualByComparingTo("0.045");
    }

    @Test
    void toOrderEvent_invalidResponse_throwsException() {
        BitstampOrderData data = new BitstampOrderData();
        data.setId(123L);
        data.setOrderType(0);
        data.setPriceStr("71009");
        data.setAmountStr("0.045");
        data.setAmountTraded("0");
        data.setMicrotimestamp("1775715433800000");

        BitstampResponse response = new BitstampResponse();
        response.setEvent("bts:subscription_succeeded");
        response.setChannel("live_orders_btcusd");
        response.setData(data);

        assertThatThrownBy(() -> converter.toOrderEvent(response))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
