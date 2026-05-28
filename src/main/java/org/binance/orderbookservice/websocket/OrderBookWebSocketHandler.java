package org.binance.orderbookservice.websocket;

import java.util.concurrent.TimeUnit;

import org.binance.orderbookservice.engine.OrderBook;
import org.binance.orderbookservice.service.OrderBookPipeline;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class OrderBookWebSocketHandler implements WebSocketHandler {

    private final OrderBookPipeline pipeline;
    private final OrderBook orderBook;
    private final ObjectMapper objectMapper;

    public OrderBookWebSocketHandler(
            OrderBookPipeline pipeline,
            OrderBook orderBook,
            ObjectMapper objectMapper) {
        this.pipeline = pipeline;
        this.orderBook = orderBook;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        int levels = parseLevels(session);
        log.info("WebSocket client connected: sessionId={}, levels={}", session.getId(), levels);

        Flux<String> messages = Flux.from(
            pipeline.snapshotStream()
                    .sample(1, TimeUnit.SECONDS))
            .map(snapshot -> orderBook.getView(levels))
            .map(this::toJson);

        return session.send(messages.map(session::textMessage))
                .doFinally(sig -> log.info("WebSocket client disconnected: sessionId={}, reason={}",
                        session.getId(), sig));
    }

    private int parseLevels(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query == null)
            return 10;
        for (String part : query.split("&")) {
            if (part.startsWith("levels=")) {
                try {
                    return Integer.parseInt(part.substring(7));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 10;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize", e);
            return "{}";
        }
    }
}