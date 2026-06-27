# 08 — Spring Core & Web

- > **Questions covered:** Q64–Q77
- > **Demo:** [demo-06-spring-core](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q64 — What is a Spring bean?

**Short answer**

A **Spring bean** is an object that is instantiated, assembled, and managed by the Spring IoC (Inversion of Control) container. Beans are the backbone of a Spring application — they are the objects that form the application's business logic and infrastructure.

**In depth**

**Key characteristics:**
- **Managed lifecycle:** Created and destroyed by the Spring container
- **Dependency injection:** Dependencies are injected automatically
- **Singleton by default:** One instance per Spring container (unless scoped otherwise)
- **Configurable:** Defined via annotations or XML configuration

```java
// Bean definition via annotation
@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User findUser(Long id) {
        return userRepository.findById(id);
    }
}

// Bean definition via @Configuration
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}

// XML bean definition (legacy)
// <bean id="userService" class="com.vbforge.UserService"/>
```

> **// JUNIOR NOTE:** Any Java object can be a Spring bean, but typically beans are services, repositories, controllers, or configuration objects. Spring beans are **singletons by default** — the container creates one instance per bean definition.

---

## Q65 — Where are beans stored, and how do you retrieve them from the container?

**Short answer**

Beans are stored in the **Spring IoC container** (ApplicationContext or BeanFactory). You retrieve them using `getBean()` method by name, type, or both. In practice, you rarely retrieve beans manually — you use **dependency injection** instead.

**In depth**

**Bean storage:**
- **ApplicationContext:** The main container interface, provides bean definitions and lifecycle management
- **BeanFactory:** The root interface, less feature-rich than ApplicationContext
- **BeanDefinition:** Metadata about each bean (class, scope, dependencies)
- **Singleton cache:** For singleton beans, the container caches the instance

```java
// Retrieving beans manually (rarely needed)
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        
        // By name
        UserService service1 = (UserService) context.getBean("userService");
        
        // By type (preferred)
        UserService service2 = context.getBean(UserService.class);
        
        // By name and type
        UserService service3 = context.getBean("userService", UserService.class);
    }
}

// ✅ Preferred: dependency injection
@RestController
public class UserController {
    @Autowired
    private UserService userService;  // Injected automatically
}
```

> **// JUNIOR NOTE:** You should almost never call `getBean()` in your application code. That defeats the purpose of dependency injection. Use DI via `@Autowired`, constructor injection, or `@Resource` instead.

---

## Q66 — What ways exist to inject dependencies (constructor, setter, field, etc.)?

**Short answer**

Spring supports **constructor injection**, **setter injection**, and **field injection**. **Constructor injection** is recommended because it makes dependencies explicit, ensures immutability, and simplifies testing.

**In depth**

**Comparison of injection types:**

| Type | How it works | Pros | Cons |
|------|--------------|------|------|
| **Constructor** | Dependencies passed via constructor parameters | Immutable, testable, required dependencies | Verbose for many dependencies |
| **Setter** | Dependencies injected via setter methods | Optional dependencies, reconfigurable | Mutable, can be forgotten |
| **Field** | Dependencies injected directly into fields | Concise, less code | Hard to test, hidden dependencies |

```java
// ✅ Constructor injection (recommended)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // No @Autowired needed in Spring 4.3+ for single constructor
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}

// ✅ Setter injection (optional dependencies)
@Service
public class NotificationService {
    private SmsService smsService;
    
    @Autowired
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;  // Optional dependency
    }
}

// ⚠️ Field injection (not recommended)
@Service
public class LegacyService {
    @Autowired
    private UserRepository userRepository;  // Hard to test
}
```

**Spring's recommendation:** Since Spring 4.3, constructor injection is preferred. It makes dependencies explicit and allows fields to be `final` (immutable).

> **// JUNIOR NOTE:** Constructor injection is the **recommended** approach in modern Spring. Field injection is convenient but makes unit testing difficult and hides dependencies. In interviews, always mention constructor injection as the preferred method.

---

## Q67 — Which bean scopes do you know?

**Short answer**

Spring provides **singleton** (default, one per container), **prototype** (new instance on each request), and web-aware scopes: **request** (one per HTTP request), **session** (one per HTTP session), and **application** (one per ServletContext).

**In depth**

| Scope | Description | When to use |
|-------|-------------|-------------|
| **singleton** | One instance per Spring container (default) | Stateless services, repositories, controllers |
| **prototype** | New instance every time requested | Stateful objects, objects with mutable state |
| **request** | One instance per HTTP request | Request-scoped data, DTOs |
| **session** | One instance per HTTP session | User preferences, shopping cart |
| **application** | One instance per ServletContext | Application-wide singletons |
| **websocket** | One per WebSocket session | WebSocket connections |

```java
// Singleton (default)
@Component
public class UserService {
    // One instance for the entire application
}

// Prototype
@Component
@Scope("prototype")
public class ShoppingCart {
    // New instance for each injection point
}

// Request scope
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestData {
    // One instance per HTTP request
}

// Session scope
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserPreferences {
    // One instance per HTTP session
}

// Using @Scope with string constant
@Scope(BeanDefinition.SCOPE_PROTOTYPE)  // Better than "prototype"
```

> **// JUNIOR NOTE:** Singleton is the default scope. **Never** store mutable state in singleton beans unless it's thread-safe. For web applications, request/session scopes are useful for storing user-specific data. When injecting request/session scoped beans into singletons, use `ScopedProxyMode.TARGET_CLASS` to avoid proxy issues.

---

## Q68 — What is @PostConstruct used for?

**Short answer**

`@PostConstruct` is a lifecycle annotation that marks a method to be **executed after dependency injection** is complete and before the bean is put into service. It's used for initialization logic that depends on injected dependencies.

**In depth**

```java
@Component
public class DatabaseInitializer {
    @Autowired
    private DataSource dataSource;
    
    @PostConstruct
    public void init() {
        // Called AFTER dependency injection
        // dataSource is already injected here
        initializeDatabase();
        loadDefaultData();
    }
    
    private void initializeDatabase() {
        // Use dataSource to set up tables, indexes, etc.
    }
}

// Bean lifecycle order:
// 1. Constructor (object instantiated)
// 2. Dependencies injected (@Autowired)
// 3. @PostConstruct method called
// 4. Bean ready for use
// 5. @PreDestroy method called before destruction
```

**Alternatives to @PostConstruct:**
- **InitializingBean interface:** `afterPropertiesSet()` method
- **@Bean initMethod:** `@Bean(initMethod = "init")`
- **@EventListener:** `@EventListener(ApplicationReadyEvent.class)` for app startup

```java
// Alternative: InitializingBean
@Component
public class MyBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        // Initialization logic
    }
}
```

> **// JUNIOR NOTE:** `@PostConstruct` is a standard Java EE annotation (javax.annotation) that Spring supports. It's preferred over `InitializingBean` because it doesn't tie your code to Spring-specific interfaces. Methods annotated with `@PostConstruct` must have `void` return type and no parameters.

---

## Q69 — What is @Qualifier used for?

**Short answer**

`@Qualifier` is used to **disambiguate** when multiple beans of the same type exist. It specifies **which bean** should be injected when there are multiple candidates matching the injection point.

**In depth**

```java
// Multiple implementations of the same interface
interface PaymentService {
    void processPayment(double amount);
}

@Component
@Qualifier("creditCard")
public class CreditCardPayment implements PaymentService {
    @Override
    public void processPayment(double amount) {
        // Credit card payment logic
    }
}

@Component
@Qualifier("paypal")
public class PayPalPayment implements PaymentService {
    @Override
    public void processPayment(double amount) {
        // PayPal payment logic
    }
}

// Injection with @Qualifier
@Service
public class OrderService {
    @Autowired
    @Qualifier("creditCard")  // Inject CreditCardPayment
    private PaymentService paymentService;
    
    // Constructor injection with @Qualifier
    public OrderService(@Qualifier("paypal") PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}

// Custom qualifier annotation (type-safe)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface CreditCard {
}

// Usage
@CreditCard
private PaymentService paymentService;  // Type-safe, no string literals
```

> **// JUNIOR NOTE:** `@Qualifier` works together with `@Autowired`. It's essential when you have multiple beans of the same type. Use meaningful qualifier names (like the examples above). For better type safety, consider creating custom qualifier annotations.

---

## Q70 — What is @Primary used for?

**Short answer**

`@Primary` designates a **default bean** to be injected when there are multiple beans of the same type and no `@Qualifier` is specified. It gives the bean **priority** over other beans of the same type.

**In depth**

```java
// Multiple beans of the same type
@Component
public class H2DataSource implements DataSource {
    // H2 in-memory database
}

@Component
@Primary  // This is the default!
public class PostgresDataSource implements DataSource {
    // PostgreSQL production database
}

// Injection without @Qualifier
@Service
public class DatabaseService {
    @Autowired
    private DataSource dataSource;  // Gets PostgresDataSource (primary)
}

// Can still use @Qualifier to override
@Service
public class TestService {
    @Autowired
    @Qualifier("h2DataSource")  // Overrides @Primary
    private DataSource dataSource;
}
```

**@Primary vs @Qualifier:**

| Feature | @Primary | @Qualifier |
|---------|----------|------------|
| **Purpose** | Set default bean | Select specific bean |
| **Scope** | Global (all injection points) | Local (specific injection point) |
| **Overridable** | ✅ With @Qualifier | N/A |
| **Use case** | Default implementation | Explicit selection |

> **// JUNIOR NOTE:** Use `@Primary` when you have a "default" implementation and `@Qualifier` for specific selections. A common pattern is having a `@Primary` production implementation and using `@Qualifier` for test/mock implementations.

---

## Q71 — What is @Order used for?

**Short answer**

`@Order` defines the **execution order** of beans when multiple beans of the same type are processed. Lower values have **higher precedence** and execute first. It's used for ordered collections, interceptors, filters, and configuration classes.

**In depth**

```java
// Ordered filters
@Component
@Order(1)  // Executes first
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) {
        System.out.println("Logging filter");
        chain.doFilter(req, resp);
    }
}

@Component
@Order(2)  // Executes second
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) {
        System.out.println("Authentication filter");
        chain.doFilter(req, resp);
    }
}

// Ordered configuration classes
@Configuration
@Order(1)
public class SecurityConfig {
    // Security configuration loaded first
}

@Configuration
@Order(2)
public class AppConfig {
    // Application configuration loaded second
}

// Ordered advice (AspectJ)
@Aspect
@Component
@Order(1)
public class LoggingAspect {
    @Around("execution(* com.vbforge.*.*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Before");
        Object result = joinPoint.proceed();
        System.out.println("After");
        return result;
    }
}
```

> **// JUNIOR NOTE:** `@Order` is most commonly used with filters, interceptors, and aspects. Lower numbers execute first. For the same order, execution is non-deterministic. Spring also uses `@Order` for ordered collections injected with `@Autowired` into a `List`.

---

## Q72 — Can Spring run code after the web application has finished starting up?

**Short answer**

**Yes.** You can use `@EventListener(ApplicationReadyEvent.class)` or implement `ApplicationRunner` or `CommandLineRunner`. These run **after** the application context is fully initialized and the web server is ready.

**In depth**

```java
// Method 1: @EventListener (recommended for web apps)
@Component
public class StartupListener {
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("Application is ready! Running startup code...");
        // Database migrations, cache warming, etc.
    }
}

// Method 2: ApplicationRunner (has access to arguments)
@Component
public class AppRunner implements ApplicationRunner {
    @Autowired
    private SomeService someService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Run after application starts
        someService.initialize();
    }
}

// Method 3: CommandLineRunner (simpler, no argument access)
@Component
public class CLRunner implements CommandLineRunner {
    @Autowired
    private DataLoader dataLoader;
    
    @Override
    public void run(String... args) throws Exception {
        dataLoader.loadInitialData();
    }
}

// Method 4: @PostConstruct + @EventListener(ApplicationStartedEvent.class)
@Component
public class EarlyStartup {
    @EventListener(ApplicationStartedEvent.class)
    public void onStartup() {
        // Runs before ApplicationReadyEvent
        System.out.println("Application started");
    }
}
```

**Event order:**
- `ApplicationStartingEvent` — before any processing
- `ApplicationEnvironmentPreparedEvent` — environment ready
- `ApplicationContextInitializedEvent` — context ready
- `ApplicationPreparedEvent` — context loaded
- `ApplicationStartedEvent` — context refreshed
- `ApplicationReadyEvent` — web server ready (use this!)
- `ApplicationFailedEvent` — on failure

> **// JUNIOR NOTE:** `@PostConstruct` runs **before** the web server starts, so it's not suitable for tasks that depend on the web application being fully ready. Use `ApplicationReadyEvent` for startup tasks that require the full application context, including web server.

---

## Q73 — What is a circular dependency between beans, and how can you resolve or avoid it?

**Short answer**

A **circular dependency** occurs when Bean A depends on Bean B, and Bean B depends on Bean A. Spring can resolve **setter/field** circular dependencies but **not constructor** circular dependencies. Best practices include using **constructor injection** and **@Lazy** to break the cycle.

**In depth**

```java
// ❌ Circular dependency via constructor (fails)
@Service
public class ServiceA {
    private final ServiceB serviceB;
    
    public ServiceA(ServiceB serviceB) {  // Constructor injection
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;
    
    public ServiceB(ServiceA serviceA) {  // Constructor injection
        this.serviceA = serviceA;  // Circular! → Application fails to start
    }
}

// ✅ Solution 1: Use @Lazy on one dependency
@Service
public class ServiceA {
    private final ServiceB serviceB;
    
    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;  // Resolved with proxy
    }
}

// ✅ Solution 2: Use setter/field injection (not recommended)
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;  // Field injection — Spring can handle
}

@Service
public class ServiceB {
    @Autowired
    private ServiceA serviceA;  // Works but not recommended
}

// ✅ Solution 3: Restructure to avoid circular dependency
// Extract common functionality to a third bean
@Service
public class ServiceA {
    private final CommonService commonService;
    
    public ServiceA(CommonService commonService) {
        this.commonService = commonService;
    }
}

@Service
public class ServiceB {
    private final CommonService commonService;
    
    public ServiceB(CommonService commonService) {
        this.commonService = commonService;  // No circular dependency
    }
}

// ✅ Solution 4: Use @PostConstruct for initialization
@Service
public class ServiceA {
    @Autowired
    private ServiceB serviceB;  // Field injection works
    
    @PostConstruct
    public void init() {
        // Use serviceB here
    }
}
```

> **// JUNIOR NOTE:** Spring **cannot** resolve constructor circular dependencies. Always use constructor injection (it's recommended), but if you have a circular dependency, use `@Lazy` on one of the constructors or restructure your code. Circular dependencies are a design smell — consider if your beans should really depend on each other.

---

## Q74 — Which HTTP methods do you know?

**Short answer**

The main HTTP methods are **GET**, **POST**, **PUT**, **DELETE**, **PATCH**, **HEAD**, **OPTIONS**, and **TRACE**. REST APIs primarily use GET, POST, PUT, DELETE, and PATCH.

**In depth**

| Method | Purpose | Idempotent? | Safe? | Body? |
|--------|---------|-------------|-------|-------|
| **GET** | Retrieve data | ✅ Yes | ✅ Yes | ❌ No |
| **POST** | Create new resource | ❌ No | ❌ No | ✅ Yes |
| **PUT** | Update/replace resource (full) | ✅ Yes | ❌ No | ✅ Yes |
| **DELETE** | Delete resource | ✅ Yes | ❌ No | ❌ No |
| **PATCH** | Partial update | ❌ No | ❌ No | ✅ Yes |
| **HEAD** | Same as GET but no body | ✅ Yes | ✅ Yes | ❌ No |
| **OPTIONS** | Get supported methods | ✅ Yes | ✅ Yes | ❌ No |
| **TRACE** | Echo request for debugging | ✅ Yes | ✅ Yes | ❌ No |

```java
// Spring REST controller examples
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping  // GET /api/users
    public List<User> getAll() { ... }
    
    @GetMapping("/{id}")  // GET /api/users/{id}
    public User getById(@PathVariable Long id) { ... }
    
    @PostMapping  // POST /api/users
    public User create(@RequestBody User user) { ... }
    
    @PutMapping("/{id}")  // PUT /api/users/{id}
    public User update(@PathVariable Long id, @RequestBody User user) { ... }
    
    @PatchMapping("/{id}")  // PATCH /api/users/{id}
    public User patch(@PathVariable Long id, @RequestBody Map<String, Object> updates) { ... }
    
    @DeleteMapping("/{id}")  // DELETE /api/users/{id}
    public void delete(@PathVariable Long id) { ... }
}
```

> **// JUNIOR NOTE:** Understanding HTTP methods is crucial for REST API design. Remember: GET is safe and idempotent, POST is neither (creates resources), PUT is idempotent (replace entire resource), PATCH is not idempotent (partial update). In interviews, be ready to explain when to use PUT vs PATCH.

---

## Q75 — How do you understand cookies, HTTP headers, and HTTP session?

**Short answer**

**HTTP Headers** are metadata sent with requests/responses. **Cookies** are small pieces of data stored client-side, sent with every request. **HTTP Session** is a server-side object that persists data across multiple requests from the same client, typically identified by a session ID stored in a cookie.

**In depth**

**HTTP Headers — key types:**
- **Request headers:** `Authorization`, `Content-Type`, `Accept`, `User-Agent`
- **Response headers:** `Content-Type`, `Set-Cookie`, `Cache-Control`, `Location`
- **Entity headers:** `Content-Length`, `Content-Type`
- **General headers:** `Date`, `Connection`

```java
// Accessing headers in Spring
@GetMapping("/data")
public String getData(@RequestHeader("Authorization") String auth) {
    return "Auth: " + auth;
}

// Setting response headers
@GetMapping("/download")
public ResponseEntity<byte[]> download() {
    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=file.pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(data);
}
```

**Cookies:**
- **Client-side storage:** Stored in browser
- **Sent automatically:** Sent with every request to the domain
- **Size limit:** ~4KB per cookie
- **Attributes:** `Secure` (HTTPS only), `HttpOnly` (no JS access), `SameSite`, `Path`
- **Used for:** Session tracking, preferences, authentication

```java
// Working with cookies in Spring
@GetMapping("/set-cookie")
public String setCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie("userId", "123");
    cookie.setMaxAge(3600);  // 1 hour
    cookie.setSecure(true);     // HTTPS only
    cookie.setHttpOnly(true);   // Not accessible via JS
    cookie.setPath("/");
    response.addCookie(cookie);
    return "Cookie set!";
}

@GetMapping("/get-cookie")
public String getCookie(@CookieValue("userId") String userId) {
    return "User ID: " + userId;
}
```

**HTTP Session:**
- **Server-side storage:** Data stored on server
- **Session ID:** Unique identifier sent via cookie (`JSESSIONID`)
- **Lifetime:** Typically 30 minutes, configurable
- **Used for:** Shopping cart, user authentication, temporary data

```java
// Working with session in Spring
@GetMapping("/login")
public String login(HttpSession session, @RequestParam String username) {
    session.setAttribute("username", username);
    return "Logged in!";
}

@GetMapping("/profile")
public String profile(HttpSession session) {
    String username = (String) session.getAttribute("username");
    return "Welcome: " + username;
}

// Spring Session scoped bean
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    private String username;
    // Getters and setters
}
```

> **// JUNIOR NOTE:** In modern applications, JWT tokens (stateless) are often preferred over sessions (stateful) for scalability. However, sessions are still common in traditional web apps. Know the difference: cookies are client-side, sessions are server-side.

---

## Q76 — What is CORS (Cross-Origin Resource Sharing)?

**Short answer**

**CORS** is a security mechanism that allows servers to specify which origins are permitted to access resources. It prevents malicious websites from making unauthorized requests to your API. Browsers enforce CORS policies for security.

**In depth**

**Why CORS exists:**
- **Same-Origin Policy:** Browsers prevent scripts from accessing resources from different origins
- **Cross-origin requests:** Needed for modern web apps (frontend on different domain)
- **CORS headers:** Server specifies allowed origins, methods, headers

```java
// Spring CORS configuration

// Method 1: @CrossOrigin annotation
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @GetMapping("/users")
    public List<User> getUsers() { ... }
}

// Method 2: Global CORS configuration
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "https://myapp.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);  // Cache preflight for 1 hour
    }
}

// Method 3: CORS filter (most flexible)
@Bean
public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    source.registerCorsConfiguration("/api/**", config);
    return new CorsFilter(source);
}
```

**Preflight requests:**
- **Simple requests:** GET, POST (with simple content types) — no preflight
- **Preflighted requests:** PUT, DELETE, custom headers, certain content types
- **OPTIONS request:** Sent automatically by browser to check CORS permissions
- **Response headers:** Server responds with allowed methods, origins, headers

> **// JUNIOR NOTE:** CORS is often a source of confusion. Remember: CORS is enforced by the **browser**, not the server. If you're getting CORS errors with tools like Postman (which don't enforce CORS), it's a frontend issue. The server must send the correct CORS headers for the browser to allow the request.

---

## Q77 — What is idempotency (in the context of HTTP APIs)?

**Short answer**

**Idempotency** means that making the same request multiple times produces the **same result** as making it once. In HTTP, **GET**, **PUT**, **DELETE**, and **HEAD** are idempotent. **POST** is not idempotent because it creates new resources.

**In depth**

**Idempotent HTTP methods:**

| Method | Idempotent? | Why? |
|--------|-------------|------|
| **GET** | ✅ Yes | Reads data without side effects |
| **PUT** | ✅ Yes | Replaces resource with same data each time |
| **DELETE** | ✅ Yes | Deleting same resource multiple times has same effect |
| **POST** | ❌ No | Creates new resource each time (different IDs) |
| **PATCH** | ⚠️ Can be | Can be idempotent if implemented correctly |

```java
// Idempotent example: PUT
// Request 1: PUT /users/123 { "name": "Alice" }
// → Creates/updates user with id 123, name "Alice"
// Request 2: PUT /users/123 { "name": "Alice" }
// → Same result, user still has name "Alice"

// Non-idempotent example: POST
// Request 1: POST /users { "name": "Bob" }
// → Creates user with id 456
// Request 2: POST /users { "name": "Bob" }
// → Creates user with id 457 (different resource!)

// Implementing idempotency in APIs
@PostMapping("/orders")
public Order createOrder(@RequestHeader("Idempotency-Key") String idempotencyKey,
                         @RequestBody OrderRequest request) {
    // Store idempotencyKey and result
    // If same key received, return cached result
}
```

**Why idempotency matters:**
- **Retry safety:** Clients can retry failed requests without causing duplicates
- **Network resilience:** Handle network timeouts gracefully
- **Consistency:** Guarantees deterministic behavior
- **Distributed systems:** Essential for reliable messaging

**Idempotency patterns:**
- **Idempotency keys:** Client generates unique key, server deduplicates
- **Versioning:** Use ETag or version fields
- **Check-and-set:** Conditional updates with If-Match headers
- **Optimistic locking:** Version field prevents lost updates

> **// JUNIOR NOTE:** Idempotency is crucial for building reliable APIs. Always remember: **PUT is idempotent, POST is not**. If you're designing an API for creating resources, consider using POST with idempotency keys instead of using PUT. In interviews, explain why idempotency matters for distributed systems and retry mechanisms.

---

## Quick-reference cheat sheet

```
Spring Bean:
  - Object managed by Spring IoC container
  - Singleton by default, defined with @Component/@Bean

Dependency Injection:
  Constructor (✅ recommended) → immutable, testable
  Setter (⚠️ optional) → mutable, reconfigurable
  Field (❌ not recommended) → hard to test, hidden dependencies

Bean Scopes:
  singleton   → one per container (default)
  prototype   → new instance each time
  request     → one per HTTP request
  session     → one per HTTP session
  application → one per ServletContext

@Qualifier → disambiguate multiple beans of same type
@Primary   → set default bean when multiple exist
@Order     → define execution order (lower = first)

Startup Code:
  @EventListener(ApplicationReadyEvent.class) → app fully ready
  ApplicationRunner → access to arguments
  CommandLineRunner → simple startup code

Circular Dependency:
  ❌ Constructor injection (fails)
  ✅ @Lazy on one dependency
  ✅ Setter/field injection (works but not recommended)
  ✅ Restructure to avoid

HTTP Methods:
  GET     → read (idempotent, safe)
  POST    → create (not idempotent)
  PUT     → full update (idempotent)
  DELETE  → delete (idempotent)
  PATCH   → partial update (can be idempotent)

CORS:
  - Browser security mechanism
  - Server allows cross-origin requests via headers
  - @CrossOrigin or global configuration

Idempotency:
  - Same request multiple times → same result
  - GET, PUT, DELETE are idempotent
  - POST is NOT idempotent
  - Use idempotency keys for safe retries
```

---

## Bonus Q & A

**Q1: What is the difference between `@Component` and `@Bean`?**

**Q2: What is the difference between `@Autowired` and `@Resource`?**

**Q3: What is the difference between `@Controller` and `@RestController`?**

**Q4: What is the difference between `@RequestMapping` and `@GetMapping`?**

**Q5: What is the difference between `@PathVariable` and `@RequestParam`?**

**Q6: What is the difference between `@RequestBody` and `@RequestPart`?**

**Q7: What is the difference between `@ExceptionHandler` and `@ControllerAdvice`?**

**Q8: What is the difference between `@Transactional` and `@Async`?**

**Q9: What is the difference between `@Profile` and `@Conditional`?**

**Q10: What is the difference between `ApplicationContext` and `BeanFactory`?**

---

