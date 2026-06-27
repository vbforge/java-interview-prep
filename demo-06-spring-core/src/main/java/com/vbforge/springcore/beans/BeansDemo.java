package com.vbforge.springcore.beans;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Q64 — What is a Spring bean?
 * Q65 — Where are beans stored, and how do you retrieve them from the container?
 * Q66 — What ways exist to inject dependencies?
 * Q67 — Which bean scopes do you know?
 * Q68 — What is @PostConstruct used for?
 * Q69 — What is @Qualifier used for?
 * Q70 — What is @Primary used for?
 * Q71 — What is @Order used for?
 *
 * KEY POINTS:
 *
 *  A Spring bean is an object whose lifecycle (creation, wiring, destruction)
 *  is managed by the Spring IoC container (ApplicationContext).
 *  You declare beans with @Component / @Service / @Repository / @Controller,
 *  or with @Bean inside a @Configuration class.
 *
 *  The ApplicationContext is the container — it stores beans in a registry
 *  keyed by name and type. getBean(Class) retrieves them programmatically,
 *  but injection via constructor/field is the normal approach.
 *
 *  Constructor injection is the recommended style:
 *    - Dependencies are final → immutable after construction
 *    - Circular dependencies fail fast at startup (not at runtime)
 *    - No reflection hack — works without Spring annotations on the constructor
 *    - Easy to test: just call new MyService(mockDep) in a unit test
 *
 *  @Qualifier picks a specific bean by name when multiple candidates exist.
 *  @Primary marks the default candidate — used when no @Qualifier is present.
 *  If both exist, @Qualifier always wins.
 *
 *  @Order controls the position of a bean in a List<T> injection.
 *  Lower value = earlier in the list. Does NOT affect which bean is injected
 *  into a single-bean injection point (that is @Primary / @Qualifier's job).
 */
@Component
public class BeansDemo {

    private static final Logger log = LoggerFactory.getLogger(BeansDemo.class);

    // ── Q66 — Constructor injection (recommended) ─────────────────────────────
    // JUNIOR NOTE: Spring 4.3+ auto-detects single-constructor injection —
    // no @Autowired needed. The dependencies are final → truly immutable.
    private final ApplicationContext  context;
    private final NotificationService defaultNotifier;   // gets @Primary (Email)
    private final NotificationService smsNotifier;       // explicit @Qualifier
    private final List<NotificationService> allNotifiers; // all beans, sorted by @Order

    // JUNIOR NOTE: When injecting a List<T>, Spring collects ALL beans of type T
    // and delivers them sorted by @Order. This is the most common use of @Order.
    public BeansDemo(
            ApplicationContext context,
            NotificationService defaultNotifier,                         // @Primary wins
            @Qualifier("smsNotificationService") NotificationService smsNotifier,
            List<NotificationService> allNotifiers) {
        this.context        = context;
        this.defaultNotifier  = defaultNotifier;
        this.smsNotifier    = smsNotifier;
        this.allNotifiers   = allNotifiers;
    }

    // ── Q68 — @PostConstruct ──────────────────────────────────────────────────
    // JUNIOR NOTE: @PostConstruct runs ONCE after:
    //   1. The bean is instantiated (constructor ran)
    //   2. All dependencies are injected
    // It runs BEFORE the bean is made available to other beans.
    // Use it for: validation, resource initialisation, cache warm-up.
    // NOT for heavy I/O — startup time increases for every bean that blocks here.
    @PostConstruct
    void init() {
        log.debug("BeansDemo @PostConstruct — all dependencies injected, performing init checks");
        log.debug("  defaultNotifier  = {}", defaultNotifier.name());
        log.debug("  smsNotifier      = {}", smsNotifier.name());
        log.debug("  allNotifiers ({}) = {}", allNotifiers.size(),
                allNotifiers.stream().map(NotificationService::name).toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q64 — What is a Spring bean?
    // ─────────────────────────────────────────────────────────────────────────

    public String runWhatIsABeanDemo() {
        log.debug("=== BEANS: what is a Spring bean (Q64) ===");

        // Count registered beans in the context
        int beanCount = context.getBeanDefinitionCount();
        String[] beanNames = context.getBeanDefinitionNames();
        long vbforgeBeans = java.util.Arrays.stream(beanNames)
                .filter(n -> {
                    try {
                        Object b = context.getBean(n);
                        return b.getClass().getName().startsWith("com.vbforge");
                    } catch (Exception e) { return false; }
                }).count();

        log.debug("Total beans in context: {} ({} are com.vbforge beans)", beanCount, vbforgeBeans);

        return String.format("""
            Q64 — What is a Spring bean?

            DEFINITION:
              A Spring bean is any object whose full lifecycle is managed by the
              Spring IoC (Inversion of Control) container.
              "Managed" means: Spring creates it, wires its dependencies,
              calls lifecycle callbacks (@PostConstruct, @PreDestroy),
              and destroys it when the context shuts down.

            HOW TO DECLARE A BEAN:
              @Component        → generic managed component
              @Service          → business logic layer (semantic alias of @Component)
              @Repository       → data access layer (adds exception translation)
              @Controller       → Spring MVC request handler
              @RestController   → @Controller + @ResponseBody
              @Bean (in @Configuration class) → explicit factory method

            WHAT THE CONTAINER DOES FOR YOU:
              1. Scans classpath for @Component-annotated classes (component scan)
              2. Instantiates each bean (calls constructor)
              3. Injects dependencies (constructor / setter / field)
              4. Calls @PostConstruct methods
              5. Bean is ready — registered in ApplicationContext
              6. On shutdown: calls @PreDestroy, then discards

            THIS CONTEXT:
              Total beans registered : %d
              Your com.vbforge beans : %d
              (The rest are Spring Boot infrastructure beans)
            """, beanCount, vbforgeBeans);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q65 — ApplicationContext and programmatic retrieval
    // ─────────────────────────────────────────────────────────────────────────

    public String runApplicationContextDemo() {
        log.debug("=== BEANS: ApplicationContext (Q65) ===");

        // Programmatic getBean() — useful in tests and framework code,
        // but NOT how you should retrieve beans in application code.
        // Prefer injection — it keeps code decoupled from Spring API.
        NotificationService byType = context.getBean(NotificationService.class); // gets @Primary
        NotificationService byName = (NotificationService)
                context.getBean("smsNotificationService");                        // by bean name

        log.debug("getBean(type)  → {}", byType.name());
        log.debug("getBean(name)  → {}", byName.name());

        boolean isSingleton = context.isSingleton("emailNotificationService");
        boolean containsBean = context.containsBean("beansDemo");
        log.debug("isSingleton('emailNotificationService'): {}", isSingleton);
        log.debug("containsBean('beansDemo'): {}", containsBean);

        return String.format("""
            Q65 — Where are beans stored, how to retrieve them?

            STORAGE — ApplicationContext:
              The ApplicationContext is the IoC container.
              It holds a BeanDefinitionRegistry — a map of bean name → BeanDefinition.
              A BeanDefinition stores: class, scope, constructor args, init method, etc.
              For singleton beans (the default), the actual instance is cached
              in a separate singletonObjects map after first creation.

            HIERARCHY:
              BeanFactory (low-level)
                └── ApplicationContext (adds events, i18n, AOP, web support)
                      └── WebApplicationContext (adds ServletContext)

            PROGRAMMATIC RETRIEVAL:
              context.getBean(NotificationService.class) → %s
                (returns @Primary when multiple candidates exist)
              context.getBean("smsNotificationService")  → %s

              context.isSingleton("emailNotificationService") → %s
              context.containsBean("beansDemo")               → %s

            HOW BEANS ARE NAMED (default rules):
              @Component / @Service on class Foo      → bean name "foo"
              @Bean method String myDataSource()       → bean name "myDataSource"
              Override with: @Component("customName") or @Bean("customName")

            BEST PRACTICE:
              Never use getBean() in application code — it couples you to Spring API
              and makes unit testing harder. Use constructor injection everywhere.
              getBean() is acceptable in: tests, framework integrations, @PostConstruct.
            """,
                byType.name(), byName.name(), isSingleton, containsBean);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q66 — Injection styles
    // ─────────────────────────────────────────────────────────────────────────

    public String runInjectionStylesDemo() {
        log.debug("=== BEANS: injection styles (Q66) ===");
        return """
            Q66 — Ways to inject dependencies:

            1. CONSTRUCTOR INJECTION (recommended ✓):
               @Service
               class OrderService {
                   private final PaymentService payment;

                   // Spring 4.3+: @Autowired optional if single constructor
                   OrderService(PaymentService payment) {
                       this.payment = payment;
                   }
               }
               ✓ Dependencies are final → immutable, thread-safe
               ✓ Circular deps detected at startup (fail-fast)
               ✓ No Spring API needed — testable with plain new OrderService(mock)
               ✓ Mandatory dependencies are explicit in the constructor signature

            2. SETTER INJECTION (optional / reconfigurable deps):
               @Service
               class ReportService {
                   private NotificationService notifier;

                   @Autowired
                   void setNotifier(NotificationService notifier) {
                       this.notifier = notifier;
                   }
               }
               ✓ Allows optional or late-set dependencies
               ✓ Can be changed after construction (useful in tests)
               ✗ Field is not final — harder to reason about mutability
               ✗ Bean can be used before the setter was called (incomplete state)

            3. FIELD INJECTION (avoid in production code ✗):
               @Service
               class UserService {
                   @Autowired
                   private UserRepository repo; // Spring sets this via reflection
               }
               ✗ Cannot make field final → mutable
               ✗ Cannot instantiate without Spring (unit tests need @SpringBootTest)
               ✗ Hides dependencies — constructor doesn't reveal them
               ✗ Spring team and community recommend against it
               ✓ Less boilerplate — sometimes used in @SpringBootTest test classes

            RULE OF THUMB:
              Mandatory dependencies  → constructor injection
              Optional dependencies   → setter injection with @Autowired(required=false)
              Test helper fields      → field injection in @SpringBootTest is acceptable
              Production code         → constructor injection always

            // JUNIOR NOTE: IntelliJ IDEA shows a warning on @Autowired field injection:
            // "Field injection is not recommended". This is the Spring team's position too.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q67 — Bean scopes
    // ─────────────────────────────────────────────────────────────────────────

    public String runScopesDemo() {
        log.debug("=== BEANS: scopes (Q67) ===");

        // Demonstrate singleton: two getBean() calls return the same instance
        NotificationService first  = context.getBean(NotificationService.class);
        NotificationService second = context.getBean(NotificationService.class);
        boolean sameInstance = first == second;
        log.debug("Singleton: same instance from two getBean() calls? {}", sameInstance);

        return String.format("""
            Q67 — Bean scopes:

            SINGLETON (default):
              One instance per ApplicationContext.
              The same object is returned every time the bean is requested.
              Created at startup (eager) or on first request (lazy with @Lazy).
              getBean() twice → same instance: %s ✓

            PROTOTYPE:
              A NEW instance is created each time the bean is requested or injected.
              Spring creates it and injects dependencies, then hands it off —
              Spring does NOT manage its destruction (@PreDestroy is NOT called).
              @Scope("prototype")  or  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)

              Use when: the bean holds per-operation state (e.g. a stateful builder,
              a command object, a session-scoped computation).

            REQUEST (web only):
              One instance per HTTP request.
              Created when the request arrives, destroyed when the response is sent.
              @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)

              Use when: holding per-request data (e.g. logged-in user context,
              request-specific cache, correlation ID holder).

            SESSION (web only):
              One instance per HTTP session (browser tab group / logged-in user).
              Lives as long as the session (default 30 min inactivity timeout).
              @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)

              Use when: user preferences, shopping cart, wizard step state.

            APPLICATION (web only):
              One instance per ServletContext — effectively like singleton but
              scoped to the web application rather than the Spring context.
              Rarely needed; prefer singleton.

            WEBSOCKET:
              One instance per WebSocket session.

            PROXY NOTE:
              Request/Session scoped beans injected into a Singleton must use
              proxyMode = ScopedProxyMode.TARGET_CLASS.
              Spring injects a proxy; the real bean is resolved per request/session
              when a method is called. Without the proxy, Spring would try to inject
              the narrow-scoped bean into the singleton at startup — before any
              request exists — and fail.

            SUMMARY TABLE:
              Scope        Lifetime                  Typical use
              ─────────────────────────────────────────────────────────────────
              singleton    ApplicationContext         Stateless services, repos
              prototype    Each injection/getBean()   Stateful per-operation beans
              request      HTTP request              Per-request context holders
              session      HTTP session              User cart, preferences
              application  ServletContext            App-wide shared state
            """, sameInstance);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q68 — @PostConstruct
    // ─────────────────────────────────────────────────────────────────────────

    public String runPostConstructDemo() {
        log.debug("=== BEANS: @PostConstruct (Q68) ===");
        return """
            Q68 — @PostConstruct:

            WHAT IT IS:
              A method annotated with @PostConstruct is called by Spring ONCE,
              after the bean is fully constructed and all dependencies are injected,
              but BEFORE the bean is made available to other beans in the context.

            EXECUTION ORDER:
              1. Constructor runs             (dependencies injected via constructor)
              2. Setter/field injection runs  (if any @Autowired setters/fields)
              3. @PostConstruct method runs   ← HERE
              4. Bean is registered and available in the context

            WHAT TO USE IT FOR:
              ✓ Validate injected configuration (throw if invalid — fail fast)
              ✓ Initialise computed fields that depend on injected values
              ✓ Register the bean with an external system (e.g. register as listener)
              ✓ Warm up a cache with initial data
              ✓ Log startup state for debugging

            WHAT NOT TO USE IT FOR:
              ✗ Heavy I/O or long-running startup tasks — delay application start
                → prefer ApplicationRunner or CommandLineRunner for those (Q72)
              ✗ Starting background threads that are hard to stop
                → use SmartLifecycle.start() which integrates with graceful shutdown

            EXAMPLE:
              @Component
              class CacheWarmer {
                  private final ProductRepository repo;
                  private final Map<Long, Product> cache = new HashMap<>();

                  CacheWarmer(ProductRepository repo) { this.repo = repo; }

                  @PostConstruct
                  void warmUp() {
                      repo.findTopSellers(100).forEach(p -> cache.put(p.id(), p));
                      log.info("Cache warmed with {} products", cache.size());
                  }
              }

            COMPLEMENT — @PreDestroy:
              Called before the bean is removed from the context (on shutdown).
              Use to: close connections, cancel scheduled tasks, flush buffers.
              @PreDestroy
              void cleanup() { executorService.shutdown(); }

            // JUNIOR NOTE: @PostConstruct / @PreDestroy are Jakarta EE annotations
            // (jakarta.annotation.PostConstruct), not Spring-specific.
            // They work with any JSR-250-compatible container.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q69 — @Qualifier
    // ─────────────────────────────────────────────────────────────────────────

    public String runQualifierDemo() {
        log.debug("=== BEANS: @Qualifier (Q69) ===");

        String viaDefault  = defaultNotifier.send("Hello via default (@Primary)");
        String viaSms      = smsNotifier.send("Hello via @Qualifier(sms)");

        log.debug("defaultNotifier.send → {}", viaDefault);
        log.debug("smsNotifier.send     → {}", viaSms);

        return String.format("""
            Q69 — @Qualifier:

            THE PROBLEM:
              When multiple beans implement the same interface, Spring cannot
              decide which one to inject. Without guidance it throws:
              NoUniqueBeanDefinitionException: expected single matching bean
              but found 3: emailNotificationService, smsNotificationService,
              pushNotificationService

            THE SOLUTION — @Qualifier("beanName"):
              @Qualifier explicitly names the bean to inject at a specific point.
              It overrides @Primary at that injection point.

              // Gets smsNotificationService even though emailNotificationService is @Primary:
              @Qualifier("smsNotificationService") NotificationService smsNotifier

            DEMO RESULTS:
              No qualifier (gets @Primary)       → %s
              @Qualifier("smsNotificationService") → %s

            CUSTOM QUALIFIER ANNOTATION:
              Instead of magic strings, define a type-safe qualifier:

              @Target({FIELD, PARAMETER, METHOD})
              @Retention(RUNTIME)
              @Qualifier
              public @interface SmsChannel {}

              @Service @SmsChannel
              class SmsNotificationService implements NotificationService { ... }

              // Injection:
              @SmsChannel NotificationService smsNotifier

              ✓ No typo risk, refactor-safe, IDE completion works

            QUALIFIER PRIORITY:
              @Qualifier always wins over @Primary.
              @Primary is only consulted when no @Qualifier is present.
            """, viaDefault, viaSms);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q70 — @Primary
    // ─────────────────────────────────────────────────────────────────────────

    public String runPrimaryDemo() {
        log.debug("=== BEANS: @Primary (Q70) ===");

        NotificationService injected = defaultNotifier; // received @Primary in constructor
        log.debug("Injected without @Qualifier: {}", injected.name());

        return String.format("""
            Q70 — @Primary:

            THE PROBLEM:
              Same as Q69 — multiple beans for one interface.
              @Primary is the "default answer" to that question.

            HOW IT WORKS:
              @Primary marks one bean as the preferred candidate.
              Any injection point that does NOT specify @Qualifier receives this bean.
              There can be only ONE @Primary per type per ApplicationContext.

            DEMO:
              EmailNotificationService is @Primary.
              Injection without @Qualifier → %s

            WHEN TO USE @Primary:
              ✓ You have a sensible "default" implementation and want to use it
                everywhere except a few specific places (which use @Qualifier).
              ✓ Integrating a library that registers a bean of a type you also provide —
                mark yours @Primary to override the library's bean.

            @Primary vs @Qualifier — PRIORITY:
              @Qualifier at the injection point ALWAYS overrides @Primary.
              Think of @Primary as "use this unless told otherwise".

            @Primary vs @ConditionalOnMissingBean:
              @ConditionalOnMissingBean is used in auto-configuration:
              "register this bean only if the user hasn't provided one."
              It is more powerful but specific to Spring Boot auto-config classes.

            // JUNIOR NOTE: Do not put @Primary on every bean "just in case".
            // It only makes sense when there are genuinely multiple candidates
            // and one is the obvious default. Overusing it makes the codebase
            // harder to reason about.
            """, injected.name());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q71 — @Order
    // ─────────────────────────────────────────────────────────────────────────

    public String runOrderDemo() {
        log.debug("=== BEANS: @Order (Q71) ===");

        // allNotifiers was injected as List<NotificationService> — sorted by @Order
        log.debug("allNotifiers order:");
        allNotifiers.forEach(n -> log.debug("  {}", n.name()));

        return String.format("""
            Q71 — @Order:

            WHAT IT DOES:
              @Order(n) defines the position of a bean in a List<T> injection.
              Lower value = earlier in the list. Beans without @Order default to
              Ordered.LOWEST_PRECEDENCE (Integer.MAX_VALUE) → go to the end.

            DEMO — List<NotificationService> injected in order:
              %s

            USE CASES:

            1. LIST INJECTION ORDER (most common):
               @Order(1) EmailNotificationService
               @Order(2) SmsNotificationService
               @Order(3) PushNotificationService
               → Injected as List<NotificationService> in that order.
               Use when: processing a chain of handlers, notification cascade.

            2. SERVLET FILTERS:
               @Component @Order(1) class SecurityFilter implements Filter {}
               @Component @Order(2) class LoggingFilter implements Filter {}
               → SecurityFilter runs before LoggingFilter on every request.

            3. SPRING SECURITY FILTER CHAIN:
               @Bean @Order(1) SecurityFilterChain adminChain(...) {}
               @Bean @Order(2) SecurityFilterChain defaultChain(...) {}
               → First matching chain wins. More specific rules first.

            4. @Configuration CLASSES:
               @Configuration @Order(1) class InfrastructureConfig {}
               @Configuration @Order(2) class ApplicationConfig {}
               → InfrastructureConfig beans are created first.

            5. ASPECTS (@Aspect with @Order):
               @Aspect @Order(1) class SecurityAspect {}
               @Aspect @Order(2) class LoggingAspect {}
               → Security advice wraps Logging advice.

            WHAT @Order DOES NOT DO:
              ✗ Does NOT decide which bean is injected into a single-bean point.
                That is @Primary / @Qualifier's job.
              ✗ Does NOT control ApplicationContext refresh order for @Configuration
                across different modules (use @DependsOn for that).

            // JUNIOR NOTE: Implementing Ordered interface is equivalent to @Order.
            // PriorityOrdered takes precedence over Ordered — used internally
            // by Spring for high-priority infrastructure beans.
            """, allNotifiers.stream().map(n -> "  " + n.name()).toList());
    }
}
