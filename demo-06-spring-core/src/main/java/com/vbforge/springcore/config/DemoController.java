package com.vbforge.springcore.config;

import com.vbforge.springcore.beans.BeansDemo;
import com.vbforge.springcore.injection.CircularDepDemo;
import com.vbforge.springcore.lifecycle.LifecycleDemo;
import com.vbforge.springcore.web.SpringWebDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All demo-06-spring-core REST endpoints.
 *
 * Base URL: http://localhost:8086/demo
 *
 * ┌─────────────────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                          │ Q       │ What it shows                     │
 * ├─────────────────────────────────────────────────────────────────────────────────┤
 * │ GET /demo/beans/what-is-a-bean    │ Q64     │ Bean definition, lifecycle, count │
 * │ GET /demo/beans/application-context│Q65     │ ApplicationContext, getBean()     │
 * │ GET /demo/beans/injection-styles  │ Q66     │ Constructor/setter/field compared │
 * │ GET /demo/beans/scopes            │ Q67     │ Singleton/prototype/request/sess  │
 * │ GET /demo/beans/post-construct    │ Q68     │ @PostConstruct order and use cases│
 * │ GET /demo/beans/qualifier         │ Q69     │ @Qualifier by name, custom annot  │
 * │ GET /demo/beans/primary           │ Q70     │ @Primary default, vs @Qualifier   │
 * │ GET /demo/beans/order             │ Q71     │ @Order in lists, filters, aspects │
 * │ GET /demo/beans/startup-hooks     │ Q72     │ Runner, EventListener, SmartLife  │
 * │ GET /demo/beans/circular-dep      │ Q73     │ Cycle detection, resolution       │
 * │ GET /demo/web/http-methods        │ Q74     │ GET/POST/PUT/PATCH/DELETE/HEAD    │
 * │ GET /demo/web/cookies-headers-session│Q75   │ Cookies, headers, HttpSession     │
 * │ GET /demo/web/cors                │ Q76     │ Preflight, @CrossOrigin, config   │
 * │ GET /demo/web/idempotency         │ Q77     │ Safe vs idempotent, Idem-Key      │
 * └─────────────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final BeansDemo      beansDemo;
    private final LifecycleDemo  lifecycleDemo;
    private final CircularDepDemo circularDepDemo;
    private final SpringWebDemo  webDemo;

    public DemoController(BeansDemo beansDemo,
                          LifecycleDemo lifecycleDemo,
                          CircularDepDemo circularDepDemo,
                          SpringWebDemo webDemo) {
        this.beansDemo       = beansDemo;
        this.lifecycleDemo   = lifecycleDemo;
        this.circularDepDemo = circularDepDemo;
        this.webDemo         = webDemo;
    }

    // ── Spring Core ──────────────────────────────────────────────────────────

    @GetMapping("/beans/what-is-a-bean")
    public ResponseEntity<String> whatIsABean() {
        return ResponseEntity.ok(beansDemo.runWhatIsABeanDemo());
    }

    @GetMapping("/beans/application-context")
    public ResponseEntity<String> applicationContext() {
        return ResponseEntity.ok(beansDemo.runApplicationContextDemo());
    }

    @GetMapping("/beans/injection-styles")
    public ResponseEntity<String> injectionStyles() {
        return ResponseEntity.ok(beansDemo.runInjectionStylesDemo());
    }

    @GetMapping("/beans/scopes")
    public ResponseEntity<String> scopes() {
        return ResponseEntity.ok(beansDemo.runScopesDemo());
    }

    @GetMapping("/beans/post-construct")
    public ResponseEntity<String> postConstruct() {
        return ResponseEntity.ok(beansDemo.runPostConstructDemo());
    }

    @GetMapping("/beans/qualifier")
    public ResponseEntity<String> qualifier() {
        return ResponseEntity.ok(beansDemo.runQualifierDemo());
    }

    @GetMapping("/beans/primary")
    public ResponseEntity<String> primary() {
        return ResponseEntity.ok(beansDemo.runPrimaryDemo());
    }

    @GetMapping("/beans/order")
    public ResponseEntity<String> order() {
        return ResponseEntity.ok(beansDemo.runOrderDemo());
    }

    @GetMapping("/beans/startup-hooks")
    public ResponseEntity<String> startupHooks() {
        return ResponseEntity.ok(lifecycleDemo.runStartupHooksDemo());
    }

    @GetMapping("/beans/circular-dep")
    public ResponseEntity<String> circularDep() {
        return ResponseEntity.ok(circularDepDemo.runCircularDependencyDemo());
    }

    // ── Spring Web ───────────────────────────────────────────────────────────

    @GetMapping("/web/http-methods")
    public ResponseEntity<String> httpMethods() {
        return ResponseEntity.ok(webDemo.runHttpMethodsDemo());
    }

    @GetMapping("/web/cookies-headers-session")
    public ResponseEntity<String> cookiesHeadersSession() {
        return ResponseEntity.ok(webDemo.runCookiesHeadersSessionDemo());
    }

    @GetMapping("/web/cors")
    public ResponseEntity<String> cors() {
        return ResponseEntity.ok(webDemo.runCorsDemo());
    }

    @GetMapping("/web/idempotency")
    public ResponseEntity<String> idempotency() {
        return ResponseEntity.ok(webDemo.runIdempotencyDemo());
    }

    // ── Index ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("""
            demo-06-spring-core — available endpoints:

              SPRING CORE (Q64–Q73):
              GET /demo/beans/what-is-a-bean       Bean definition, lifecycle, component scan, bean count
              GET /demo/beans/application-context  ApplicationContext, getBean() by type and name
              GET /demo/beans/injection-styles     Constructor (✓) vs setter vs field injection compared
              GET /demo/beans/scopes               Singleton/prototype/request/session with singleton proof
              GET /demo/beans/post-construct       @PostConstruct order, uses, @PreDestroy complement
              GET /demo/beans/qualifier            @Qualifier by name, custom qualifier annotation
              GET /demo/beans/primary              @Primary default candidate, priority rules
              GET /demo/beans/order                @Order in List injection, filters, aspects, @Configuration
              GET /demo/beans/startup-hooks        CommandLineRunner, ApplicationRunner, EventListener, SmartLifecycle
              GET /demo/beans/circular-dep         Cycle detection at startup, 4 resolution strategies

              SPRING WEB (Q74–Q77):
              GET /demo/web/http-methods           GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS with safe+idempotent table
              GET /demo/web/cookies-headers-session  Cookie attributes, request/response headers, HttpSession vs JWT
              GET /demo/web/cors                   Same-origin policy, preflight, @CrossOrigin, WebMvcConfigurer
              GET /demo/web/idempotency            Safe vs idempotent, Idempotency-Key pattern for POST
            """);
    }
}
