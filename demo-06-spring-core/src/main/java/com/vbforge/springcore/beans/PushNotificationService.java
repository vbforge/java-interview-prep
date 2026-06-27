package com.vbforge.springcore.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Q71 — @Order(3) positions this bean last when Spring injects a
 * List&lt;NotificationService&gt;. Lower value = earlier in the list.
 */
@Service
@Order(3)
public class PushNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    @Override
    public String send(String message) {
        log.debug("PushNotificationService sending: {}", message);
        return "[PUSH] " + message;
    }

    @Override
    public String name() { return "PushNotificationService (@Order 3)"; }
}
