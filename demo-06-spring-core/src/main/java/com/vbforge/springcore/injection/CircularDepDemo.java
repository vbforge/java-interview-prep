package com.vbforge.springcore.injection;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Q73 — What is a circular dependency between beans, and how to resolve or avoid it?
 *
 * KEY POINTS:
 *
 *  A circular dependency exists when Bean A depends on Bean B, and Bean B
 *  depends on Bean A (directly or transitively via C, D…).
 *
 *  With CONSTRUCTOR injection (the recommended style), Spring detects the cycle
 *  at startup and throws BeanCurrentlyInCreationException — fail-fast.
 *
 *  With FIELD / SETTER injection, Spring can work around cycles by injecting
 *  a partially-constructed proxy. This is risky: the bean may be used before
 *  it is fully initialised, and the cycle itself is often a design smell.
 *
 *  RESOLUTION STRATEGIES (best to worst):
 *    1. Redesign — extract shared logic into a third bean (best)
 *    2. Introduce a @Lazy parameter — defer creation of one side
 *    3. Use setter injection for one side — Spring injects after construction
 *    4. Use @PostConstruct + ApplicationContext.getBean() — manual lookup
 *    5. @DependsOn — only controls order, does not break a true cycle
 *
 *  NOTE: Spring Boot 2.6+ disallows circular deps by default.
 *  You must set spring.main.allow-circular-references=true to re-enable.
 *  This is intentional — cycles are almost always a design problem.
 */
@Component
public class CircularDepDemo {

    private static final Logger log = LoggerFactory.getLogger(CircularDepDemo.class);

    private final ApplicationContext context;

    public CircularDepDemo(ApplicationContext context) {
        this.context = context;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q73 — Circular dependency explanation and resolution patterns
    // ─────────────────────────────────────────────────────────────────────────

    public String runCircularDependencyDemo() {
        log.debug("=== INJECTION: circular dependency (Q73) ===");

        // Demonstrate safe resolution — EventPublisher uses lazy lookup via context
        SafeOrderService orderService = context.getBean(SafeOrderService.class);
        String result = orderService.placeOrder("ITEM-001");
        log.debug("SafeOrderService.placeOrder result: {}", result);

        return """
            Q73 — Circular dependency between beans:

            THE PROBLEM:
              class OrderService {
                  OrderService(NotificationService ns) { ... }  // needs NotificationService
              }
              class NotificationService {
                  NotificationService(OrderService os) { ... }  // needs OrderService
              }
              Spring tries to create OrderService → needs NotificationService
              → tries to create NotificationService → needs OrderService
              → OrderService is not ready yet → BeanCurrentlyInCreationException

            CONSTRUCTOR INJECTION → FAILS FAST (good!):
              Spring Boot 2.6+ throws at startup:
              "The dependencies of some of the beans form a cycle:
               orderService → notificationService → orderService"

            WHY THIS IS ALWAYS A DESIGN SMELL:
              Two beans that depend on each other are tightly coupled.
              They are doing too much — their responsibilities overlap or one
              should not know about the other.

            ─────────────────────────────────────────────────────────────────
            RESOLUTION 1 — BEST: Extract shared logic into a third bean
            ─────────────────────────────────────────────────────────────────
              class EventPublisher { void publish(String event) {...} }
              class OrderService        { OrderService(EventPublisher ep) {...} }
              class NotificationService { NotificationService(EventPublisher ep) {...} }
              → No cycle. Both depend on EventPublisher; neither depends on the other.
              → Use Spring's ApplicationEventPublisher for decoupled event dispatch.

            ─────────────────────────────────────────────────────────────────
            RESOLUTION 2 — @Lazy on one constructor parameter
            ─────────────────────────────────────────────────────────────────
              class OrderService {
                  OrderService(@Lazy NotificationService ns) { this.ns = ns; }
              }
              → Spring injects a PROXY for NotificationService at OrderService
                creation time. The real NotificationService is created on first
                method call to the proxy.
              ✓ Breaks the cycle without redesign
              ✗ Hides the design problem; proxy adds a small overhead

            ─────────────────────────────────────────────────────────────────
            RESOLUTION 3 — Setter injection for one side
            ─────────────────────────────────────────────────────────────────
              class OrderService {
                  private NotificationService ns;
                  OrderService() {}   // no cycle in constructor
                  @Autowired void setNotificationService(NotificationService ns) {
                      this.ns = ns;  // injected after OrderService is constructed
                  }
              }
              ✓ Breaks the cycle at construction time
              ✗ Bean is temporarily in incomplete state (between ctor and setter call)
              ✗ Requires spring.main.allow-circular-references=true in Boot 2.6+

            ─────────────────────────────────────────────────────────────────
            RESOLUTION 4 — @PostConstruct + programmatic lookup
            ─────────────────────────────────────────────────────────────────
              class OrderService {
                  @Autowired ApplicationContext ctx;
                  private NotificationService ns;
                  @PostConstruct void init() {
                      this.ns = ctx.getBean(NotificationService.class);
                  }
              }
              ✓ Clean constructor — no cycle at construction time
              ✗ Couples code to Spring API; harder to unit test

            ─────────────────────────────────────────────────────────────────
            THIS DEMO — SafeOrderService uses ApplicationEventPublisher (Resolution 1):
            ─────────────────────────────────────────────────────────────────
              SafeOrderService depends on ApplicationEventPublisher (Spring-provided).
              SafeNotificationService listens for OrderPlacedEvent via @EventListener.
              No cycle — they communicate through events, not direct references.
              Result: order placed and notification dispatched without coupling.

            // JUNIOR NOTE: spring.main.allow-circular-references=true is a safety
            // valve, not a solution. If you find yourself setting it, it means
            // your beans need a redesign. The Spring team made the default false
            // in 2.6 precisely to surface these design issues early.
            """;
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Supporting beans demonstrating the "extract shared EventPublisher" solution
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Q73 — SafeOrderService avoids a circular dep by publishing an event
 * instead of holding a direct reference to NotificationService.
 * Spring's ApplicationEventPublisher is always available for injection.
 */
@Component
class SafeOrderService {

    private static final Logger log = LoggerFactory.getLogger(SafeOrderService.class);
    private final org.springframework.context.ApplicationEventPublisher publisher;

    SafeOrderService(org.springframework.context.ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    String placeOrder(String itemId) {
        log.debug("SafeOrderService: placing order for {}", itemId);
        // Publish event — NotificationService listens; no direct reference needed
        publisher.publishEvent(new OrderPlacedEvent(itemId));
        return "Order placed for " + itemId + " (event published)";
    }
}

/**
 * Q73 — SafeNotificationService listens for OrderPlacedEvent.
 * It has NO dependency on OrderService — no cycle possible.
 */
@Component
class SafeNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SafeNotificationService.class);

    @org.springframework.context.event.EventListener
    void onOrderPlaced(OrderPlacedEvent event) {
        log.debug("SafeNotificationService: sending notification for order {}", event.itemId());
    }
}

/** Simple event record — carries the order payload. */
record OrderPlacedEvent(String itemId) {}
