package com.vbforge.springcore.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Q69 — To inject this bean instead of the @Primary one, the injection point
 * must use @Qualifier("smsNotificationService").
 * Q71 — @Order(2) places it second in List&lt;NotificationService&gt; injection.
 */
@Service
@Order(2)
public class SmsNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Override
    public String send(String message) {
        log.debug("SmsNotificationService sending: {}", message);
        return "[SMS] " + message;
    }

    @Override
    public String name() { return "SmsNotificationService (explicit @Qualifier)"; }
}
