package com.vbforge.springcore.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Q74 — Which HTTP methods do you know?
 * Q75 — How do you understand cookies, HTTP headers, and HTTP session?
 * Q76 — What is CORS (Cross-Origin Resource Sharing)?
 * Q77 — What is idempotency (in the context of HTTP APIs)?
 *
 * KEY POINTS:
 *
 *  HTTP methods define the INTENT of a request. The correct method choice
 *  makes APIs predictable and allows intermediaries (caches, proxies) to
 *  behave correctly.
 *
 *  Idempotency: calling the same request N times has the same effect as calling
 *  it once. Safe = read-only. Idempotent = repeated writes produce same state.
 *  GET is both safe and idempotent. PUT/DELETE are idempotent but not safe.
 *  POST is neither.
 *
 *  CORS protects browsers from sending credentialed requests to a different
 *  origin without explicit server permission. It is a BROWSER security feature —
 *  non-browser clients (curl, Postman, server-to-server) are unaffected.
 */
@Component
public class SpringWebDemo {

    private static final Logger log = LoggerFactory.getLogger(SpringWebDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q74 — HTTP methods
    // ─────────────────────────────────────────────────────────────────────────

    public String runHttpMethodsDemo() {
        log.debug("=== WEB: HTTP methods (Q74) ===");
        return """
            Q74 — HTTP methods:

            GET:
              Retrieve a resource. No body. Response is cacheable.
              Safe (no side effects) and idempotent.
              GET /api/orders/42        → return order 42
              GET /api/orders?status=open → return list of open orders

            POST:
              Submit data to create a resource or trigger an action.
              Has a request body. NOT idempotent — calling twice creates two resources.
              POST /api/orders          → create a new order (returns 201 Created)
              POST /api/payments/charge → trigger a charge (not a simple CRUD)

            PUT:
              Replace a resource completely. Idempotent — same result every call.
              Must send the full resource representation.
              PUT /api/orders/42        → replace order 42 entirely
              If the resource doesn't exist, PUT may create it (204 or 201).

            PATCH:
              Partial update. Send only the fields to change.
              Not necessarily idempotent (depends on the patch semantics).
              PATCH /api/orders/42      → { "status": "shipped" }
              Useful when PUT would require sending the whole large object.

            DELETE:
              Remove a resource. Idempotent — deleting an already-deleted resource
              typically returns 404 but the SERVER STATE is the same.
              DELETE /api/orders/42     → 204 No Content (or 404 if not found)

            HEAD:
              Same as GET but WITHOUT a response body.
              Used to check: does the resource exist? What are its headers?
              Useful for: checking If-Modified-Since, Content-Length before downloading.
              HEAD /api/orders/42       → headers only, no body

            OPTIONS:
              Ask the server which methods are allowed on a resource.
              Used automatically by browsers as a CORS PREFLIGHT request.
              OPTIONS /api/orders       → Allow: GET, POST, HEAD, OPTIONS

            TRACE (rarely used):
              Echoes the request back — used for diagnostic loop-back testing.
              Security risk — often disabled on production servers.

            CONNECT:
              Establishes a tunnel (typically for HTTPS through an HTTP proxy).
              Not used in REST APIs.

            QUICK REFERENCE TABLE:
              Method   Body?  Safe?  Idempotent?  Typical status
              ─────────────────────────────────────────────────────────
              GET      No     Yes    Yes          200 OK
              POST     Yes    No     No           201 Created / 200 OK
              PUT      Yes    No     Yes          200 OK / 204 / 201
              PATCH    Yes    No     No*          200 OK / 204
              DELETE   No     No     Yes          204 No Content / 404
              HEAD     No     Yes    Yes          200 OK (no body)
              OPTIONS  No     Yes    Yes          200 OK + Allow header
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q75 — Cookies, headers, HTTP session
    // ─────────────────────────────────────────────────────────────────────────

    public String runCookiesHeadersSessionDemo() {
        log.debug("=== WEB: cookies, headers, session (Q75) ===");
        return """
            Q75 — Cookies, HTTP headers, and HTTP session:

            ════════════════════════════════════════════
            HTTP HEADERS
            ════════════════════════════════════════════
            Key-value metadata sent with every request and response.
            They control caching, content negotiation, auth, CORS, and more.

            REQUEST HEADERS (client → server):
              Content-Type: application/json    → what format I'm sending
              Accept: application/json          → what format I want back
              Authorization: Bearer <token>     → my credentials
              If-None-Match: "abc123"           → only send if ETag changed
              X-Request-ID: uuid                → correlation ID (custom header)

            RESPONSE HEADERS (server → client):
              Content-Type: application/json; charset=UTF-8
              Cache-Control: max-age=3600       → cache for 1 hour
              ETag: "abc123"                    → version fingerprint for caching
              Set-Cookie: JSESSIONID=xyz; HttpOnly; Secure
              Access-Control-Allow-Origin: *    → CORS permission

            IN SPRING:
              @RequestHeader("Authorization") String auth     // read a request header
              ResponseEntity.ok(body)
                  .header("X-Custom-Header", "value")         // set a response header

            ════════════════════════════════════════════
            COOKIES
            ════════════════════════════════════════════
            Small key-value pairs stored by the BROWSER and sent automatically
            on every subsequent request to the same domain.
            Set by the server via Set-Cookie response header.

            Set-Cookie: SESSION=abc123; Path=/; HttpOnly; Secure; SameSite=Strict

            COOKIE ATTRIBUTES:
              HttpOnly    → JS cannot read it (XSS protection)
              Secure      → only sent over HTTPS
              SameSite    → controls cross-site sending
                Strict  → never sent cross-site
                Lax     → sent on top-level navigations (default in modern browsers)
                None    → always sent (requires Secure)
              Max-Age / Expires → persistence; if absent, cookie is session-only
              Domain / Path     → which URLs receive the cookie

            IN SPRING:
              @CookieValue("SESSION") String sessionId  // read a cookie
              ResponseCookie cookie = ResponseCookie.from("token", value)
                  .httpOnly(true).secure(true).build();  // write a cookie

            ════════════════════════════════════════════
            HTTP SESSION
            ════════════════════════════════════════════
            HTTP is stateless — each request is independent.
            A session is a server-side mechanism to associate multiple requests
            from the same client.

            HOW IT WORKS (traditional server-side session):
              1. Client sends first request (no session cookie).
              2. Server creates an HttpSession object in memory, generates JSESSIONID.
              3. Server sends Set-Cookie: JSESSIONID=abc123 in response.
              4. Browser stores the cookie; sends it on every subsequent request.
              5. Server looks up the HttpSession by ID → retrieves the user's state.
              6. Session expires after inactivity (default 30 min in Tomcat/Spring).

            IN SPRING:
              @GetMapping("/profile")
              String profile(HttpSession session) {
                  User user = (User) session.getAttribute("currentUser");
                  session.setAttribute("lastVisit", LocalDateTime.now());
                  return user.name();
              }

            SESSION vs JWT / STATELESS AUTH:
              HttpSession  → state on SERVER (memory or Redis)
                             Easy to invalidate; doesn't scale horizontally without
                             sticky sessions or a shared session store (Redis).
              JWT token    → state in the TOKEN (client holds it)
                             Server is stateless; scales easily.
                             Harder to invalidate before expiry (need a blocklist).
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q76 — CORS
    // ─────────────────────────────────────────────────────────────────────────

    public String runCorsDemo() {
        log.debug("=== WEB: CORS (Q76) ===");
        return """
            Q76 — CORS (Cross-Origin Resource Sharing):

            THE SAME-ORIGIN POLICY:
              Browsers block JavaScript from making requests to a DIFFERENT ORIGIN.
              Origin = scheme + host + port.
              https://app.example.com (frontend) ≠ https://api.example.com (backend)
              Without CORS, the browser blocks the API call from the frontend JS.

            WHAT IS CORS:
              A W3C mechanism that lets the SERVER explicitly grant permission
              for cross-origin requests from specific origins.
              The browser enforces CORS; the server just sets the headers.

            THE PREFLIGHT REQUEST:
              For "non-simple" requests (PUT, DELETE, custom headers, JSON body),
              the browser sends an OPTIONS request first:
                OPTIONS /api/orders HTTP/1.1
                Origin: https://app.example.com
                Access-Control-Request-Method: POST
                Access-Control-Request-Headers: Content-Type, Authorization

              Server response (allowing the request):
                HTTP/1.1 204 No Content
                Access-Control-Allow-Origin: https://app.example.com
                Access-Control-Allow-Methods: GET, POST, PUT, DELETE
                Access-Control-Allow-Headers: Content-Type, Authorization
                Access-Control-Max-Age: 3600   ← cache preflight result for 1 hour

              Then the browser sends the actual POST.

            CORS RESPONSE HEADERS (server sets these):
              Access-Control-Allow-Origin     → which origins are allowed
              Access-Control-Allow-Methods    → which HTTP methods are allowed
              Access-Control-Allow-Headers    → which request headers are allowed
              Access-Control-Allow-Credentials → allow cookies / auth headers
              Access-Control-Max-Age          → cache preflight duration (seconds)
              Access-Control-Expose-Headers   → which response headers JS can read

            CORS IN SPRING — three ways:

            1. @CrossOrigin on a controller or method:
               @CrossOrigin(origins = "https://app.example.com")
               @RestController
               class OrderController { ... }

            2. Global configuration via WebMvcConfigurer:
               @Configuration
               class CorsConfig implements WebMvcConfigurer {
                   @Override
                   public void addCorsMappings(CorsRegistry registry) {
                       registry.addMapping("/api/**")
                               .allowedOrigins("https://app.example.com")
                               .allowedMethods("GET","POST","PUT","DELETE")
                               .allowedHeaders("*")
                               .allowCredentials(true)
                               .maxAge(3600);
                   }
               }

            3. application.yml (Spring Boot only, basic):
               spring.mvc.cors.mappings:
                 /api/**:
                   allowed-origins: "https://app.example.com"
                   allowed-methods: "GET,POST"

            IMPORTANT NOTES:
              ✗ CORS is a BROWSER protection — curl, Postman, server calls are unaffected.
              ✗ Access-Control-Allow-Origin: * CANNOT be combined with
                Access-Control-Allow-Credentials: true (browser rejects it).
              ✗ CORS does not secure your API — it only controls browser access.
                Always use authentication/authorisation separately.

            // JUNIOR NOTE: A common mistake is to add Access-Control-Allow-Origin: *
            // to fix a CORS error when the real problem is a missing authentication
            // header or a wrong URL. Check the browser DevTools Network tab first.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q77 — Idempotency
    // ─────────────────────────────────────────────────────────────────────────

    public String runIdempotencyDemo() {
        log.debug("=== WEB: idempotency (Q77) ===");
        return """
            Q77 — Idempotency in HTTP APIs:

            DEFINITION:
              An operation is IDEMPOTENT if performing it multiple times produces
              the same result as performing it once.
              The SERVER STATE after N calls == server state after 1 call.

            NOTE — Idempotent ≠ Safe:
              SAFE      = no server-side side effects (read-only).
              IDEMPOTENT = repeated calls have the same effect (may write, but consistently).
              All safe operations are idempotent; not all idempotent ops are safe.

            HTTP METHOD CLASSIFICATION:
              Method   Safe?  Idempotent?  Reason
              ─────────────────────────────────────────────────────────────────────
              GET      Yes    Yes          Read-only — repeated calls same result
              HEAD     Yes    Yes          Same as GET, no body
              OPTIONS  Yes    Yes          Read-only — returns capabilities
              PUT      No     Yes          "Set order 42 status = SHIPPED" — same every time
              DELETE   No     Yes          "Delete order 42" — after first call, 42 is gone;
                                           subsequent calls → 404, but state is the same
              PATCH    No     No*          Depends: "set qty=5" is idempotent;
                                           "increment qty by 1" is NOT
              POST     No     No           "Create order" — each call creates a new order

            WHY IT MATTERS — NETWORK RELIABILITY:
              Networks are unreliable. A client may not know if a request arrived.
              If the operation is idempotent, retrying is SAFE — same outcome.
              If the operation is NOT idempotent (POST), retrying may cause duplicates
              (e.g. double charge, duplicate order).

            MAKING POST IDEMPOTENT — Idempotency Keys:
              Client generates a unique key (UUID) for each logical operation
              and sends it as a header: Idempotency-Key: <uuid>

              Server stores (key → result) and on duplicate key:
                → returns the STORED result immediately (no re-processing)

              Used by: Stripe, Braintree, PayPal for payment APIs.
              Prevents double charges when the client retries a timed-out POST.

              POST /api/payments
              Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
              { "amount": 100, "currency": "USD" }

              → First call:  process payment, store result against key, return 201
              → Second call: key exists → return stored 201 immediately, no re-charge

            PUT vs PATCH IDEMPOTENCY:
              PUT  /api/orders/42  { "status": "SHIPPED", "qty": 5 }
              → Idempotent: same body → same result every time.

              PATCH /api/orders/42  { "qty": { "op": "increment", "value": 1 } }
              → NOT idempotent: each call increments again.

              PATCH /api/orders/42  { "qty": 5 }   (absolute set)
              → Idempotent: sets qty to 5 regardless of current value.

            // JUNIOR NOTE: Idempotency is a contract between the API and its clients.
            // Even if PUT is semantically idempotent, your implementation must ensure
            // it actually IS — e.g. don't generate a new timestamp on every PUT call
            // for an "updatedAt" field if the data hasn't changed.
            """;
    }
}
