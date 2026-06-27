package com.vbforge.springcore.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Q70 — @Primary marks this bean as the default candidate when Spring finds
 * multiple implementations of NotificationService.
 * Q71 — @Order(1) makes it first in List&lt;NotificationService&gt; injection.
 */
@Service
@Primary
@Order(1)
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public String send(String message) {
        log.debug("EmailNotificationService sending: {}", message);
        return "[EMAIL] " + message;
    }

    @Override
    public String name() { return "EmailNotificationService (@Primary)"; }
}
