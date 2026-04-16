package org.binance.orderbookservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.binance.orderbookservice.dto.BitstampResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

}
