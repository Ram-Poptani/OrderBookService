package org.binance.orderbookservice.metrics;

import org.binance.orderbookservice.engine.OrderBook;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;

@Component
@Getter
public class OrderBookMetrics {

    private final Counter updates;
    private final Counter gapRebuilds;
    private final Timer applyTimer;

    public OrderBookMetrics(MeterRegistry registry, OrderBook orderBook) {
        this.updates = Counter.builder("orderbook.updates.total")
                .description("Total order book snapshots emitted downstream")
                .register(registry);

        this.gapRebuilds = Counter.builder("orderbook.gap.rebuilds.total")
                .description("Sequence gaps detected (book cleared and rebuilt)")
                .register(registry);

        this.applyTimer = Timer.builder("orderbook.apply.event")
                .description("Latency of applying one order event to the book")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        Gauge.builder("orderbook.bid.levels", orderBook, OrderBook::bidLevelCount)
                .description("Distinct bid price levels currently in the book")
                .register(registry);

        Gauge.builder("orderbook.ask.levels", orderBook, OrderBook::askLevelCount)
                .description("Distinct ask price levels currently in the book")
                .register(registry);
    }
}
