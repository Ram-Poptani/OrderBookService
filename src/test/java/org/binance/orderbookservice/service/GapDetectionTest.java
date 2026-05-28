package org.binance.orderbookservice.service;

import org.binance.orderbookservice.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GapDetectionTest {

    @Test
    void noGap_whenSequenceIsContinuous() {
        SequenceState previous = new SequenceState("100", null, false);
        OrderEvent current = orderEventWithSequence("101", "100");

        boolean gap = previous.getLastEventId() != null
                && current.getPreEventId() != null
                && !previous.getLastEventId().equals(current.getPreEventId());

        assertThat(gap).isFalse();
    }

    @Test
    void gapDetected_whenSequenceBreaks() {
        SequenceState previous = new SequenceState("100", null, false);
        OrderEvent current = orderEventWithSequence("101", "99");

        boolean gap = previous.getLastEventId() != null
                && current.getPreEventId() != null
                && !previous.getLastEventId().equals(current.getPreEventId());

        assertThat(gap).isTrue();
    }

    @Test
    void noGap_onFirstEvent_whenLastEventIdIsNull() {
        SequenceState previous = new SequenceState(null, null, false);
        OrderEvent current = orderEventWithSequence("51", "50");

        boolean gap = previous.getLastEventId() != null
                && current.getPreEventId() != null
                && !previous.getLastEventId().equals(current.getPreEventId());

        assertThat(gap).isFalse();
    }

    @Test
    void noGap_whenPreEventIdIsNull() {
        SequenceState previous = new SequenceState("100", null, false);
        OrderEvent current = orderEventWithSequence("101", null);

        boolean gap = previous.getLastEventId() != null
                && current.getPreEventId() != null
                && !previous.getLastEventId().equals(current.getPreEventId());

        assertThat(gap).isFalse();
    }

    private OrderEvent orderEventWithSequence(String eventId, String preEventId) {
        return OrderEvent.builder()
                .id(1L)
                .orderType(OrderType.BID)
                .eventType(EventType.ORDER_CREATED)
                .price(BigDecimal.ONE)
                .amount(BigDecimal.ONE)
                .amountTraded(BigDecimal.ZERO)
                .microtimestamp("0")
                .channel("test")
                .eventId(eventId)
                .preEventId(preEventId)
                .build();
    }
}
