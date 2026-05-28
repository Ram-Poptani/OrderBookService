package org.binance.orderbookservice.service;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.binance.orderbookservice.engine.OrderBook;
import org.binance.orderbookservice.websocket.BitstampWebSocketClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderBookPipeline {
    private final BitstampWebSocketClient webSocketClient;
    private final OrderEventConverter converter;
    private final OrderBook orderBook;

    private Disposable disposable;

    OrderBookPipeline(
        BitstampWebSocketClient webSocketClient,
        OrderEventConverter converter,
        OrderBook orderBook
    ) {
        this.webSocketClient = webSocketClient;
        this.converter = converter;
        this.orderBook = orderBook;
    }

    @PostConstruct
    public void start() {
        disposable =  webSocketClient.stream()
                .filter(json -> json.contains("order_created")
                                        || json.contains("order_changed")
                                        || json.contains("order_deleted"))
                .flatMap(json -> {
                    try {
                        return Flowable.just(converter.deserialize(json));
                    } catch (Exception e) {
                        log.warn("Skipping unparseable message: {}", e.getMessage());
                        return Flowable.empty();
                    }
                })
                .flatMap(response -> {
                    try {
                        return Flowable.just(converter.toOrderEvent(response));
                    } catch (Exception e) {
                        log.warn("Skipping unconvertible event: {}", e.getMessage());
                        return Flowable.empty();
                    }
                })
                .map(orderBook::applyEvent)
                .distinctUntilChanged()
                .subscribe(snapshot -> log.info("Book Updated: bid = {} @ {}, ask = {} @ {}, spread = {}",
                                                                snapshot.getBestBidAmount(), snapshot.getBestBidPrice(),
                                                                snapshot.getBestAskAmount(), snapshot.getBestAskPrice(),
                                                                snapshot.getSpread()),
                                    error -> log.error("Error processing order book event", error),
                                                () -> log.info("WebSocket stream completed"));
    }

    @PreDestroy
    public void stop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
            log.info("WebSocket stream stopped");
        }
    }

}
