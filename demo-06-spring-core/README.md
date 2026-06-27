# demo-06 — Spring Core and Spring Web

- > **Theory file:** [08-spring-core-web.md](08-spring-core-web.md)
- > **Return to root README:** [java-interview-prep README](../README.md)
- > **[GitHub Pages site](https://vbforge.github.io/java-interview-prep)**
- > **Questions covered:** Q64–Q77
- > **Port:** 8086
  
Standalone Spring Boot module. No database, no Docker required.

---

## How to run

```bash
cd demos/demo-06-spring-core
mvn spring-boot:run
```

Then open: `http://localhost:8086/demo`

---

## Endpoints

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/beans/what-is-a-bean` | Q64 | Bean definition, lifecycle steps, `@Component` vs `@Bean`, live bean count |
| `GET /demo/beans/application-context` | Q65 | `ApplicationContext` hierarchy, `getBean()` by type/name, bean naming rules |
| `GET /demo/beans/injection-styles` | Q66 | Constructor (✓ recommended) vs setter vs field injection — pros/cons |
| `GET /demo/beans/scopes` | Q67 | Singleton/prototype/request/session/application — singleton proof with `==` |
| `GET /demo/beans/post-construct` | Q68 | Execution order (ctor → inject → @PostConstruct), use cases, `@PreDestroy` |
| `GET /demo/beans/qualifier` | Q69 | `@Qualifier("name")`, custom qualifier annotation, priority over `@Primary` |
| `GET /demo/beans/primary` | Q70 | `@Primary` as default candidate, `@Primary` vs `@ConditionalOnMissingBean` |
| `GET /demo/beans/order` | Q71 | `@Order` in `List<T>` injection, Servlet filters, Security chains, `@Aspect` |
| `GET /demo/beans/startup-hooks` | Q72 | `CommandLineRunner`, `ApplicationRunner`, `ContextRefreshedEvent`, `SmartLifecycle` |
| `GET /demo/beans/circular-dep` | Q73 | Cycle detection at startup, 4 resolution strategies, event-based decoupling |
| `GET /demo/web/http-methods` | Q74 | All 8 HTTP methods with safe/idempotent table |
| `GET /demo/web/cookies-headers-session` | Q75 | Cookie attributes (`HttpOnly`, `Secure`, `SameSite`), request/response headers, session vs JWT |
| `GET /demo/web/cors` | Q76 | Same-origin policy, preflight flow, `@CrossOrigin`, `WebMvcConfigurer`, pitfalls |
| `GET /demo/web/idempotency` | Q77 | Safe vs idempotent classification, `Idempotency-Key` pattern for POST |

---

## Key things to observe in the logs

**On startup** — notice the lifecycle firing order:
```
[Q72] ContextRefreshedEvent received — context fully started
[Q72] CommandLineRunner.run() — raw args: []
[Q72] ApplicationRunner.run() — option names: []
```

**`@PostConstruct` fires during startup** — before the app is ready:
```
BeansDemo @PostConstruct — all dependencies injected, performing init checks
  defaultNotifier  = EmailNotificationService (@Primary, @Order 1)
  smsNotifier      = SmsNotificationService (explicit @Qualifier)
  allNotifiers (3) = [EmailNotificationService, SmsNotificationService, PushNotificationService]
```

**`/demo/beans/qualifier`** — `@Primary` vs `@Qualifier`:
```
defaultNotifier.send → [EMAIL] Hello via default (@Primary)
smsNotifier.send     → [SMS]   Hello via @Qualifier(sms)
```

**`/demo/beans/order`** — List sorted by `@Order`:
```
@Order(1) → EmailNotificationService
@Order(2) → SmsNotificationService
@Order(3) → PushNotificationService
```

**`/demo/beans/circular-dep`** — event-based resolution:
```
SafeOrderService: placing order for ITEM-001
SafeNotificationService: sending notification for order ITEM-001
```

---

## Key concepts cheat sheet

```
BEAN:
  Object managed by Spring IoC (creation, injection, destruction).
  Declared with @Component / @Service / @Repository / @Controller or @Bean in @Configuration.

INJECTION STYLES (preference order):
  Constructor  → final fields, fail-fast, no Spring API, best for testing ✓
  Setter       → optional/reconfigurable deps, field mutable
  Field        → least boilerplate, but hides deps and prevents final — avoid in prod ✗

SCOPES:
  singleton   → 1 instance per ApplicationContext (default)
  prototype   → new instance on every injection / getBean()
  request     → 1 per HTTP request  (web only, needs ScopedProxy)
  session     → 1 per HTTP session  (web only, needs ScopedProxy)

@PostConstruct  → runs once after ctor + injection, before bean is usable
@PreDestroy     → runs on context shutdown (cleanup)

MULTIPLE BEAN CANDIDATES:
  @Primary    → default when no @Qualifier present
  @Qualifier  → always wins; picks by name or custom annotation

@Order(n):
  Controls position in List<T> injection (lower = earlier).
  Also controls Servlet filter order, Security chain order, Aspect precedence.
  Does NOT decide which single bean is injected.

STARTUP HOOKS (in order):
  @PostConstruct → ContextRefreshedEvent → CommandLineRunner → ApplicationRunner
  → ApplicationReadyEvent → SmartLifecycle.start()

CIRCULAR DEPENDENCY:
  Constructor injection → detected at startup (BeanCurrentlyInCreationException) ✓
  Resolution options (best→worst):
    1. Redesign — extract third bean / use events (ApplicationEventPublisher)
    2. @Lazy on one constructor parameter
    3. Setter injection for one side
    4. @PostConstruct + programmatic getBean()

HTTP METHODS — safe / idempotent:
  GET, HEAD, OPTIONS  → safe + idempotent
  PUT, DELETE         → idempotent only
  POST, PATCH         → neither (PATCH can be idempotent if using absolute sets)

IDEMPOTENCY KEY:
  POST /api/payments  Idempotency-Key: <uuid>
  Server stores (key → result); duplicate key → return stored result, no re-processing.

CORS:
  Browser-only protection (curl/Postman unaffected).
  Preflight = OPTIONS request browser sends before non-simple cross-origin request.
  @CrossOrigin / WebMvcConfigurer.addCorsMappings() / application.yml.
  Access-Control-Allow-Origin: * + credentials: true → browser rejects.

COOKIES:
  HttpOnly  → JS cannot read (XSS protection)
  Secure    → HTTPS only
  SameSite  → Strict/Lax/None (CSRF protection)
```

---

## Project structure

```
demo-06-spring-core/
├── pom.xml
├── README.md
└── src/main/java/com/vbforge/springcore/
    ├── MainApp.java
    ├── config/
    │   └── DemoController.java              ← all 14 endpoints
    ├── beans/
    │   ├── NotificationService.java         ← shared interface (3 implementations)
    │   ├── EmailNotificationService.java    ← @Primary @Order(1)
    │   ├── SmsNotificationService.java      ← @Order(2), selected via @Qualifier
    │   ├── PushNotificationService.java     ← @Order(3)
    │   └── BeansDemo.java                   ← Q64–Q71
    ├── lifecycle/
    │   └── LifecycleDemo.java               ← Q72 (+ CommandLineRunner, ApplicationRunner)
    ├── injection/
    │   └── CircularDepDemo.java             ← Q73 (+ SafeOrderService, SafeNotificationService)
    └── web/
        └── SpringWebDemo.java               ← Q74–Q77
```

---
