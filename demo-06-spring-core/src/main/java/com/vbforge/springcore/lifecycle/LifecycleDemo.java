package com.vbforge.springcore.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Q72 — Can Spring run code after the web application has finished starting up?
 *
 * KEY POINTS:
 *
 *  Yes — several mechanisms exist, each with different timing and use cases:
 *
 *  @EventListener(ContextRefreshedEvent)  — fires when the ApplicationContext
 *    is fully refreshed (all beans created and wired). Fires on every refresh,
 *    including context restarts. Can fire multiple times in some setups.
 *
 *  CommandLineRunner               — runs after full context startup,
 *    receives raw String[] args from main(). Ordered with @Order.
 *    Simple, good for scripts and data seeding.
 *
 *  ApplicationRunner               — same timing as CommandLineRunner but
 *    receives a typed ApplicationArguments object (parsed --key=value pairs).
 *    Prefer over CommandLineRunner when you need to read CLI args.
 *
 *  SmartLifecycle / Lifecycle      — start()/stop() hooks that integrate
 *    with Spring's lifecycle management and graceful shutdown.
 *    Use for background threads, scheduled tasks, connection pools.
 */
@Component
public class LifecycleDemo {

    private static final Logger log = LoggerFactory.getLogger(LifecycleDemo.class);

    // ── ContextRefreshedEvent listener ────────────────────────────────────────
    // JUNIOR NOTE: @EventListener is a cleaner alternative to implementing
    // ApplicationListener<ContextRefreshedEvent>. Both work identically.
    // Watch out: in web apps with a parent+child context (DispatcherServlet),
    // this fires TWICE — once per context. Guard with a flag if needed.
    @EventListener(ContextRefreshedEvent.class)
    @Order(1)
    void onContextRefreshed(ContextRefreshedEvent event) {
        log.debug("[Q72] ContextRefreshedEvent received — context fully started");
        log.debug("      Context display name: {}", event.getApplicationContext().getDisplayName());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q72 — Startup hooks explanation
    // ─────────────────────────────────────────────────────────────────────────

    public String runStartupHooksDemo() {
        log.debug("=== LIFECYCLE: startup hooks (Q72) ===");
        return """
            Q72 — Running code after the web application has started:

            YES — Spring provides several hooks. They run in this order:
              1. All beans created and dependencies injected
              2. @PostConstruct methods called
              3. ApplicationContext fully refreshed
              4. ContextRefreshedEvent fired              ← hook 1
              5. CommandLineRunner.run() called           ← hook 2
              6. ApplicationRunner.run() called           ← hook 3
              7. ApplicationStartedEvent fired
              8. ApplicationReadyEvent fired

            HOOK 1 — @EventListener(ContextRefreshedEvent.class):
              @EventListener(ContextRefreshedEvent.class)
              void onReady(ContextRefreshedEvent e) {
                  log.info("Context '{}' is ready", e.getApplicationContext().getDisplayName());
              }
              ✓ Fine-grained — receives the event object with context info
              ✗ Can fire twice in Spring MVC apps (parent + child context)
              Use for: reacting to context restarts, conditional setup

            HOOK 2 — CommandLineRunner:
              @Component @Order(1)
              class DataSeeder implements CommandLineRunner {
                  @Override
                  public void run(String... args) throws Exception {
                      // args = raw ["--profile=dev", "--seed=true"]
                      seedDatabase();
                  }
              }
              ✓ Simple — implement one method, return void
              ✓ @Order controls execution order across multiple runners
              ✗ args are raw strings — parse manually

            HOOK 3 — ApplicationRunner (prefer over CommandLineRunner):
              @Component @Order(2)
              class CacheWarmer implements ApplicationRunner {
                  @Override
                  public void run(ApplicationArguments args) throws Exception {
                      // args.getOptionValues("profile") → ["dev"]
                      // args.containsOption("seed")     → true/false
                      if (args.containsOption("seed")) { seedData(); }
                  }
              }
              ✓ Typed ApplicationArguments — option values parsed for you
              ✓ @Order controls execution order
              ✓ Prefer this when reading CLI arguments

            HOOK 4 — @EventListener(ApplicationReadyEvent.class):
              @EventListener(ApplicationReadyEvent.class)
              void onAppReady() {
                  log.info("Application is fully ready to serve requests");
                  // HTTP server is up and accepting traffic here
              }
              ✓ Fires AFTER CommandLineRunner and ApplicationRunner
              ✓ HTTP server is already accepting requests at this point
              ✓ Does NOT fire twice (unlike ContextRefreshedEvent)
              Use for: readiness notifications, health check warm-up

            HOOK 5 — SmartLifecycle (for background threads):
              @Component
              class BackgroundWorker implements SmartLifecycle {
                  private boolean running = false;
                  @Override public void start() { running = true; startWorker(); }
                  @Override public void stop()  { running = false; stopWorker(); }
                  @Override public boolean isRunning() { return running; }
              }
              ✓ Integrated with Spring's graceful shutdown
              ✓ stop() is called on context close — threads are stopped cleanly
              Use for: background threads, message consumers, scheduled polls

            WHICH HOOK TO USE:
              One-shot logic after startup    → ApplicationRunner (typed args)
              React to context restart        → ContextRefreshedEvent
              After HTTP server is up         → ApplicationReadyEvent
              Background threads              → SmartLifecycle
              Simple data seeding / scripts   → CommandLineRunner

            // JUNIOR NOTE: If your startup code can fail (DB seed, remote call),
            // prefer CommandLineRunner / ApplicationRunner — an exception there
            // prevents the app from starting, which is what you want (fail-fast).
            // An exception in @PostConstruct also prevents startup, which is fine
            // for configuration validation but heavy for I/O tasks.
            """;
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Supporting classes — declared at package level so Spring can find them
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Q72 — CommandLineRunner example: raw String[] args.
 * @Order(1) ensures this runs before ApplicationRunnerExample.
 */
@Component
@Order(1)
class CommandLineRunnerExample implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CommandLineRunnerExample.class);

    @Override
    public void run(String... args) {
        log.debug("[Q72] CommandLineRunner.run() — raw args: {}", java.util.Arrays.toString(args));
        log.debug("      Application is fully started (all beans ready, context refreshed)");
    }
}

/**
 * Q72 — ApplicationRunner example: typed ApplicationArguments.
 * @Order(2) ensures this runs after CommandLineRunnerExample.
 */
@Component
@Order(2)
class ApplicationRunnerExample implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ApplicationRunnerExample.class);

    @Override
    public void run(ApplicationArguments args) {
        log.debug("[Q72] ApplicationRunner.run() — option names: {}", args.getOptionNames());
        log.debug("      Non-option args: {}", args.getNonOptionArgs());
        log.debug("      Prefer ApplicationRunner over CommandLineRunner when reading CLI args");
    }
}
