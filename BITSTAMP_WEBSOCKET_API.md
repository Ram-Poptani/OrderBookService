# Bitstamp WebSocket API — Live Orders

## WebSocket URL

```
wss://ws.bitstamp.net
```

No authentication required.

## Subscribe Message

```json
{
  "event": "bts:subscribe",
  "data": {
    "channel": "live_orders_btcusd"
  }
}
```

Replace `btcusd` with other market symbols as needed (e.g., `live_orders_ethusd`).

## Response — Order Events

Three event types are received: `order_created`, `order_changed`, `order_deleted`.

### Example Response (`order_deleted`)

```json
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
```

## Field Reference

### Top-Level Fields

| Field | Type | Description |
|---|---|---|
| `event` | string | Event type: `order_created`, `order_changed`, `order_deleted` |
| `channel` | string | The subscribed channel (e.g., `live_orders_btcusd`) |
| `event_id` | string | Unique ID for this event — use for sequencing and gap detection |
| `pre_event_id` | string | The previous event ID — chain to detect missed events |
| `order_source` | string | `"orderbook"` for regular limit orders, `"stop_order"` for stop orders |

### Data Fields

| Field | Type | Description |
|---|---|---|
| `id` | number | Unique order ID — primary key to track across events |
| `id_str` | string | Same order ID as string (for large integer safety) |
| `order_type` | number | **0 = Buy (bid), 1 = Sell (ask)** |
| `order_subtype` | number | Order subtype |
| `datetime` | string | Unix timestamp (seconds) when the order was created |
| `microtimestamp` | string | Unix timestamp in microseconds — higher precision |
| `amount` | number | Order size at the time of this event |
| `amount_str` | string | Same amount as string with full decimal precision |
| `amount_traded` | string | How much of this order has been filled |
| `amount_at_create` | string | Original order size when first placed |
| `price` | number | Order price |
| `price_str` | string | Same price as string |
| `is_liquidation` | boolean | Whether this is a forced liquidation order |

## Event Handling for OrderBook Construction

| Event | Action |
|---|---|
| `order_created` | Add the order to the book at its price level |
| `order_changed` | Update the order's remaining size (partial fill) |
| `order_deleted` | Remove the order from the book |

### Determining Cancel vs Fill on `order_deleted`

- `amount_traded = "0"` → order was **canceled**
- `amount_traded > "0"` → order was **filled** (partially or fully)

## Bitstamp API Documentation

- REST API: https://www.bitstamp.net/api/
- WebSocket v2: https://www.bitstamp.net/websocket/v2/
