package org.binance.orderbookservice.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderEventParseException extends RuntimeException {
    public OrderEventParseException(String message) {
        super(message);
    }
    public OrderEventParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
