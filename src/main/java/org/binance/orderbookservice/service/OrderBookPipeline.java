package org.binance.orderbookservice.service;

import org.binance.orderbookservice.engine.OrderBook;
import org.binance.orderbookservice.model.OrderBookSnapshot;
import org.binance.orderbookservice.model.SequenceState;
import org.binance.orderbookservice.websocket.BitstampWebSocketClient;
import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.processors.BehaviorProcessor;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderBookPipeline {
    private final BitstampWebSocketClient webSocketClient;
    private final OrderEventConverter converter;
    private final OrderBook orderBook;

    private Disposable disposable;

    private final FlowableProcessor<OrderBookSnapshot> snapshotProcessor = BehaviorProcessor.<OrderBookSnapshot>create()
            .toSerialized();

    OrderBookPipeline(
            BitstampWebSocketClient webSocketClient,
            OrderEventConverter converter,
            OrderBook orderBook) {
        this.webSocketClient = webSocketClient;
        this.converter = converter;
        this.orderBook = orderBook;
    }

    public Flowable<OrderBookSnapshot> snapshotStream() {
        return snapshotProcessor.onBackpressureLatest();
    }

    @PostConstruct
    public void start() {
        disposable = webSocketClient.stream()
                .doOnSubscribe(subscription -> {
                    orderBook.clear();
                    log.info("(Re)Starting order book pipeline");
                })
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
                .scan(new SequenceState(null, null, false), (previousState, currentEvent) -> {
                    boolean gap = previousState.getLastEventId() != null
                            && currentEvent.getPreEventId() != null
                            && !previousState.getLastEventId().equals(currentEvent.getPreEventId());

                    return new SequenceState(currentEvent.getEventId(), currentEvent, gap);
                })
                .skip(1)
                .doOnNext(state -> {
                    if (state.isGapDetected()) {
                        log.warn("Gap detected! last={}, preEventId={}, eventId={}",
                                state.getLastEventId(), state.getOrderEvent().getPreEventId(),
                                state.getOrderEvent().getEventId());
                        orderBook.clear();
                    }
                })
                .map(SequenceState::getOrderEvent)
                .map(orderBook::applyEvent)
                .distinctUntilChanged()
                .doOnNext(snapshotProcessor::onNext)
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
