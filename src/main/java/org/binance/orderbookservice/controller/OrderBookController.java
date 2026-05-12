package org.binance.orderbookservice.controller;


import org.binance.orderbookservice.engine.OrderBook;
import org.binance.orderbookservice.model.OrderBookView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderBookController {

    private final OrderBook orderBook;

    OrderBookController(OrderBook OrderBook) {
        this.orderBook = OrderBook;
    }

    @GetMapping("/orderbook")
    public OrderBookView getOrderBook(
            @RequestParam(defaultValue = "10") int levels
    ) {
        if (levels < 0 ) { levels = 0; }
        return orderBook.getView(levels);
    }
}
