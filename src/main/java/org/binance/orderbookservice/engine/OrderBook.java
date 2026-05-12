package org.binance.orderbookservice.engine;

import lombok.extern.slf4j.Slf4j;
import org.binance.orderbookservice.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

@Component
@Slf4j
public class OrderBook {

    private final ConcurrentSkipListMap<BigDecimal, BigDecimal> bids =
            new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    private final ConcurrentSkipListMap<BigDecimal, BigDecimal> asks =
            new ConcurrentSkipListMap<>();

    private final ConcurrentHashMap<Long, Order> orderIndex =
            new ConcurrentHashMap<>();

    private void handleCreate(OrderEvent event) {
        Order order = new Order(
                event.getId(),
                event.getPrice(),
                event.getAmount(),
                event.getOrderType()
        );
        orderIndex.put(order.getId(), order);
        if (order.getType() == OrderType.BID) {
            bids.merge(order.getPrice(), order.getAmount(), BigDecimal::add);
        } else {
            asks.merge(order.getPrice(), order.getAmount(), BigDecimal::add);
        }
    }


    private void handleChange(OrderEvent event) {
        Order old = orderIndex.get(event.getId());
        if (old == null) {
            log.warn("Order not found in index for {}: {}", event.getEventType(), event.getId());
            return;
        }

        ConcurrentSkipListMap<BigDecimal, BigDecimal> side =
                old.getType() == OrderType.BID ? bids : asks;

        BigDecimal delta = event.getAmount().subtract(old.getAmount());
        side.merge(old.getPrice(), delta, BigDecimal::add);

        Order updated = new Order(old.getId(), old.getPrice(), event.getAmount(), old.getType());
        orderIndex.put(event.getId(), updated);
    }

    private void handleDelete(OrderEvent event) {
        Order old = orderIndex.get(event.getId());
        if (old == null) {
            log.warn("Order not found in index for {}: {}", event.getEventType(), event.getId());
            return;
        }

        ConcurrentSkipListMap<BigDecimal, BigDecimal> side =
                old.getType() == OrderType.BID ? bids : asks;

        side.merge(old.getPrice(), old.getAmount().negate(), BigDecimal::add);

        side.computeIfPresent(old.getPrice(), (price, total) ->
                total.signum() <= 0 ? null : total);

        orderIndex.remove(event.getId());
    }

    public OrderBookSnapshot applyEvent(OrderEvent event) {
        switch (event.getEventType()) {
            case ORDER_CREATED -> handleCreate(event);
            case ORDER_CHANGED -> handleChange(event);
            case ORDER_DELETED -> handleDelete(event);
        }
        return snapshot();    }

    public OrderBookSnapshot snapshot() {

        Map.Entry<BigDecimal, BigDecimal> bestBid = bids.isEmpty() ? null : bids.firstEntry();
        Map.Entry<BigDecimal, BigDecimal> bestAsk = asks.isEmpty() ? null : asks.firstEntry();
        BigDecimal bestBidPrice = bestBid != null ? bestBid.getKey() : null;
        BigDecimal bestAskPrice = bestAsk != null ? bestAsk.getKey() : null;

        BigDecimal spread = (bestBidPrice != null && bestAskPrice != null)
                ? bestAskPrice.subtract(bestBidPrice)
                : null;
        
        int bidLevelCount = bids.size();
        int askLevelCount = asks.size();

        return OrderBookSnapshot.builder()
                .bestBidPrice(bestBidPrice)
                .bestBidAmount(bestBid != null ? bestBid.getValue() : null)
                .bestAskPrice(bestAskPrice)
                .bestAskAmount(bestAsk != null ? bestAsk.getValue() : null)
                .spread(spread)
                .bidLevels(bidLevelCount)
                .askLevels(askLevelCount)
                .timestamp(System.currentTimeMillis())
                .build();

    }

    private List<PriceLevel> sliceLevels(ConcurrentSkipListMap<BigDecimal, BigDecimal> side, int levels) {
        Stream<Map.Entry<BigDecimal, BigDecimal>> stream = side.entrySet().stream();
        if (levels > 0) {
            stream = stream.limit(levels);
        }
        return stream.map(e ->
                            new PriceLevel(e.getKey(), e.getValue())
                        ).toList();
    }

    public OrderBookView getView(int levels) {

        List<PriceLevel> bidLevels = sliceLevels(bids, levels);
        List<PriceLevel> askLevels = sliceLevels(asks, levels);

        BigDecimal spread = (!bidLevels.isEmpty() && !askLevels.isEmpty())
                ? askLevels.get(0).getPrice().subtract(bidLevels.get(0).getPrice())
                : null;

        return new OrderBookView(
                bidLevels,
                askLevels,
                spread,
                levels,
                System.currentTimeMillis()
        );
    }
}