package org.binance.orderbookservice.engine;

import org.binance.orderbookservice.model.EventType;
import org.binance.orderbookservice.model.OrderBookSnapshot;
import org.binance.orderbookservice.model.OrderEvent;
import org.binance.orderbookservice.model.OrderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookTest {

    @Test
    void applyEvent_orderCreated_addsToBids() {
        OrderBook orderBook = new OrderBook();

        OrderBookSnapshot snapshot = orderBook.applyEvent(
                orderEvent(1L, OrderType.BID, EventType.ORDER_CREATED, "100.00", "1.50")
        );

        assertThat(snapshot.getBestBidPrice()).isEqualByComparingTo("100.00");
        assertThat(snapshot.getBestBidAmount()).isEqualByComparingTo("1.50");
        assertThat(snapshot.getBidLevels()).isEqualTo(1);
        assertThat(snapshot.getBestAskPrice()).isNull();
        assertThat(snapshot.getSpread()).isNull();
    }

    @Test
    void applyEvent_twoOrdersSamePrice_aggregates() {
        OrderBook orderBook = new OrderBook();

        orderBook.applyEvent(orderEvent(1L, OrderType.BID, EventType.ORDER_CREATED, "100.00", "1.20"));
        OrderBookSnapshot snapshot = orderBook.applyEvent(
                orderEvent(2L, OrderType.BID, EventType.ORDER_CREATED, "100.00", "2.30")
        );

        assertThat(snapshot.getBestBidPrice()).isEqualByComparingTo("100.00");
        assertThat(snapshot.getBestBidAmount()).isEqualByComparingTo("3.50");
        assertThat(snapshot.getBidLevels()).isEqualTo(1);
    }

    @Test
    void applyEvent_orderChanged_updatesAmount() {
        OrderBook orderBook = new OrderBook();

        orderBook.applyEvent(orderEvent(10L, OrderType.BID, EventType.ORDER_CREATED, "99.50", "1.00"));
        OrderBookSnapshot snapshot = orderBook.applyEvent(
                orderEvent(10L, OrderType.BID, EventType.ORDER_CHANGED, "99.50", "2.25")
        );

        assertThat(snapshot.getBestBidPrice()).isEqualByComparingTo("99.50");
        assertThat(snapshot.getBestBidAmount()).isEqualByComparingTo("2.25");
        assertThat(snapshot.getBidLevels()).isEqualTo(1);
    }

    @Test
    void applyEvent_orderDeleted_removesFromBook() {
        OrderBook orderBook = new OrderBook();

        orderBook.applyEvent(orderEvent(1L, OrderType.BID, EventType.ORDER_CREATED, "100.00", "1.00"));
        orderBook.applyEvent(orderEvent(2L, OrderType.BID, EventType.ORDER_CREATED, "99.00", "2.00"));

        OrderBookSnapshot snapshot = orderBook.applyEvent(
                orderEvent(1L, OrderType.BID, EventType.ORDER_DELETED, "100.00", "1.00")
        );

        assertThat(snapshot.getBestBidPrice()).isEqualByComparingTo("99.00");
        assertThat(snapshot.getBestBidAmount()).isEqualByComparingTo("2.00");
        assertThat(snapshot.getBidLevels()).isEqualTo(1);
    }

    @Test
    void applyEvent_orderDeletedLastAtLevel_removesLevel() {
        OrderBook orderBook = new OrderBook();

        orderBook.applyEvent(orderEvent(7L, OrderType.BID, EventType.ORDER_CREATED, "100.00", "1.00"));
        OrderBookSnapshot snapshot = orderBook.applyEvent(
                orderEvent(7L, OrderType.BID, EventType.ORDER_DELETED, "100.00", "1.00")
        );

        assertThat(snapshot.getBestBidPrice()).isNull();
        assertThat(snapshot.getBestBidAmount()).isNull();
        assertThat(snapshot.getBidLevels()).isEqualTo(0);
    }

    @Test
    void snapshot_emptyBook_returnsNullFields() {
        OrderBook orderBook = new OrderBook();

        OrderBookSnapshot snapshot = orderBook.snapshot();

        assertThat(snapshot.getBestBidPrice()).isNull();
        assertThat(snapshot.getBestBidAmount()).isNull();
        assertThat(snapshot.getBestAskPrice()).isNull();
        assertThat(snapshot.getBestAskAmount()).isNull();
        assertThat(snapshot.getSpread()).isNull();
        assertThat(snapshot.getBidLevels()).isEqualTo(0);
        assertThat(snapshot.getAskLevels()).isEqualTo(0);
    }

    @Test
    void snapshot_withBothSides_correctSpread() {
        OrderBook orderBook = new OrderBook();

        orderBook.applyEvent(orderEvent(1L, OrderType.BID, EventType.ORDER_CREATED, "100.00", "1.00"));
        orderBook.applyEvent(orderEvent(2L, OrderType.ASK, EventType.ORDER_CREATED, "101.25", "3.00"));

        OrderBookSnapshot snapshot = orderBook.snapshot();

        assertThat(snapshot.getBestBidPrice()).isEqualByComparingTo("100.00");
        assertThat(snapshot.getBestAskPrice()).isEqualByComparingTo("101.25");
        assertThat(snapshot.getSpread()).isEqualByComparingTo("1.25");
    }

    private OrderEvent orderEvent(
            long id,
            OrderType orderType,
            EventType eventType,
            String price,
            String amount
    ) {
        return OrderEvent.builder()
                .id(id)
                .orderType(orderType)
                .eventType(eventType)
                .price(new BigDecimal(price))
                .amount(new BigDecimal(amount))
                .amountTraded(BigDecimal.ZERO)
                .microtimestamp("0")
                .channel("test")
                .build();
    }
}
