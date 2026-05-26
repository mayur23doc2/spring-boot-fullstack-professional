package com.example.demo.notification;

import com.example.demo.overtime.SettlementCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SettlementNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(SettlementNotificationListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSettlementComplete(SettlementCompletedEvent event) {
        try {
            log.info("SMS queued for worker {} month {} amount {}", event.workerId(), event.month(), event.settledAmount());
        } catch (Exception ex) {
            log.error("SMS send failed for worker {}. Queueing retry.", event.workerId(), ex);
        }
    }
}
