package org.binance.orderbookservice.websocket;

import java.net.URI;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class BitstampWebSocketClient {

    @Value("${bitstamp.web-socket-uri}")
    private String websocketUri;

    @Value("${bitstamp.web-socket-channel}")
    private String websocketChannel;

    private final ObjectMapper objectMapper;
    private Flowable<String> sharedStream;

    BitstampWebSocketClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public synchronized Flowable<String> stream() {
        if (sharedStream == null) {

            sharedStream = Flowable.<String>create(emitter -> {

                WebSocketClient client = new WebSocketClient(URI.create(websocketUri)) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        Map<String, Object> msg = Map.of(
                                "event", "bts:subscribe",
                                "data", Map.of("channel", websocketChannel)
                        );
                        log.info("Connected to Bitstamp, subscribing to: {}", websocketChannel);

                        try {
                            this.send(objectMapper.writeValueAsString(msg));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to serialize Bitstamp subscription message", e);
                        }

                    }

                    @Override
                    public void onMessage(String message) {
                        if (!emitter.isCancelled()) {
                            emitter.onNext(message);
                        }
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        if (!emitter.isCancelled()) {
                            emitter.onComplete();
                        }
                        log.warn("WebSocket closed: code={}, reason={}, remote={}", code, reason, remote);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!emitter.isCancelled()) {
                            emitter.onError(e);
                        }
                        log.error("Bitstamp WebSocket error", e);
                    }
                };
                emitter.setCancellable(() -> {
                    if (client.isOpen()) {
                        client.close();
                    }
                });
                client.connect();

            }, BackpressureStrategy.BUFFER).publish().refCount();

        }
        return sharedStream;
    }

}
