package com.vbforge.streams.optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Q48 — Does Optional have terminal/intermediate operations like a stream,
 *        or is it a one-shot container for zero or one value?
 *
 * KEY POINTS:
 *
 *  Optional<T> is a CONTAINER — not a stream.
 *  It holds zero or one value. It has no concept of a pipeline.
 *  map/flatMap/filter on Optional do NOT pipeline like Stream —
 *  they transform or short-circuit the single contained value.
 *
 *  PURPOSE: eliminate NullPointerException by making the absence of a value
 *  explicit in the type system. Forces callers to handle the empty case.
 *
 *  COMMON MISUSE: using Optional as a field type, method parameter, or
 *  in collections. It is designed for METHOD RETURN TYPES only.
 */
@Component
public class OptionalDemo {

    private static final Logger log = LoggerFactory.getLogger(OptionalDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q48 — Optional basics
    // ─────────────────────────────────────────────────────────────────────────

    public String runBasicsDemo() {
        log.debug("=== OPTIONAL DEMO: basics (Q48) ===");

        // ── Creation ──────────────────────────────────────────────────────────
        Optional<String> present = Optional.of("hello");           // value present; NPE if null
        Optional<String> empty   = Optional.empty();               // explicitly empty
        Optional<String> maybe   = Optional.ofNullable(getValue()); // safe — null → empty

        log.debug("present: isPresent={} value={}", present.isPresent(), present.get());
        log.debug("empty:   isPresent={}", empty.isPresent());
        log.debug("maybe:   isEmpty={}",  maybe.isEmpty());

        // ── Extracting value ──────────────────────────────────────────────────
        // get() — throws NoSuchElementException if empty. Prefer alternatives below.
        String val1 = present.get();                              // OK — we know it's present

        // orElse — default value if empty (always evaluated)
        String val2 = empty.orElse("default");
        log.debug("orElse: '{}'", val2);

        // orElseGet — default from Supplier (lazy — only evaluated if empty)
        // JUNIOR NOTE: prefer orElseGet over orElse when the default is expensive
        // to compute. orElse("default") always evaluates "default" even if present.
        String val3 = empty.orElseGet(() -> "computed-default");
        log.debug("orElseGet: '{}'", val3);

        // orElseThrow — throw if empty (Java 10+)
        try {
            empty.orElseThrow(() -> new IllegalStateException("Value required"));
        } catch (IllegalStateException e) {
            log.debug("orElseThrow: {}", e.getMessage());
        }

        // ifPresent — consume only if value exists; does nothing if empty
        present.ifPresent(v -> log.debug("ifPresent: '{}'", v));
        empty.ifPresent(v -> log.debug("This will NOT print — empty"));

        // ifPresentOrElse (Java 9+)
        empty.ifPresentOrElse(
            v -> log.debug("value: {}", v),
            () -> log.debug("ifPresentOrElse empty branch — no value")
        );

        return String.format("""
            Optional basics (Q48):
            
            WHAT Optional IS:
              A container for zero or one value. NOT a stream.
              map/flatMap/filter transform the contained value — not a pipeline.
            
            CREATION:
              Optional.of(value)          — present; throws NPE if value is null
              Optional.empty()            — explicitly empty
              Optional.ofNullable(value)  — safe; null → empty, non-null → present
            
            EXTRACTING:
              get()                       — present=%s  (NoSuchElementException if empty)
              orElse("default")           — empty result: '%s'  (always evaluates arg)
              orElseGet(() -> compute())  — empty result: '%s'  (lazy — preferred)
              orElseThrow(exSupplier)     — throws on empty
              ifPresent(consumer)         — side effect only if present
              ifPresentOrElse(c, action)  — branch on present/empty (Java 9+)
            """, val1, val2, val3);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q48 — Optional chaining: map, flatMap, filter
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Optional.map / flatMap / filter let you chain operations without
     * explicit null checks. If Optional is empty at any step, the rest
     * of the chain short-circuits and returns Optional.empty().
     *
     * This is NOT a stream pipeline — it's a single value passing through
     * transformations, with automatic short-circuit on empty.
     */
    public String runChainingDemo() {
        log.debug("=== OPTIONAL DEMO: chaining (Q48) ===");

        // ── map — transform the value if present ─────────────────────────────
        // Optional<T>.map(T→R) → Optional<R>
        Optional<String> name = Optional.of("  vlad  ");
        Optional<Integer> nameLength = name
            .map(String::trim)          // Optional<String> → Optional<String>
            .map(String::length);       // Optional<String> → Optional<Integer>
        log.debug("map chain: '  vlad  ' → trim → length = {}", nameLength);

        // If any step returns null, map wraps it in Optional.empty()
        Optional<String> nullResult = Optional.of("hello")
            .map(s -> (String) null);   // map returns null → Optional.empty()
        log.debug("map returning null → {}", nullResult);

        // ── flatMap — avoid Optional<Optional<T>> nesting ────────────────────
        // When the mapping function itself returns Optional<R>, use flatMap.
        // map would give Optional<Optional<R>> — flatMap flattens it to Optional<R>.
        Optional<String> user = Optional.of("vlad");
        Optional<String> email = user
            .flatMap(this::findEmail);   // findEmail returns Optional<String>
        log.debug("flatMap findEmail('vlad') = {}", email);

        Optional<String> unknown = Optional.of("unknown-user");
        Optional<String> noEmail = unknown
            .flatMap(this::findEmail);   // findEmail returns empty
        log.debug("flatMap findEmail('unknown-user') = {}", noEmail);

        // ── filter — keep value only if predicate passes ──────────────────────
        Optional<String> longName = Optional.of("alexander")
            .filter(s -> s.length() > 5); // passes → stays present
        Optional<String> shortName = Optional.of("bob")
            .filter(s -> s.length() > 5); // fails → becomes empty
        log.debug("filter(len>5) on 'alexander' = {}", longName);
        log.debug("filter(len>5) on 'bob' = {}", shortName);

        // ── or — alternative Optional if empty (Java 9+) ─────────────────────
        Optional<String> fallback = Optional.<String>empty()
            .or(() -> Optional.of("fallback-value"));
        log.debug("or() fallback = {}", fallback);

        // ── Full chain — the Optional anti-NPE pattern ────────────────────────
        // JUNIOR NOTE: This is what Optional is FOR — replacing:
        //   if (user != null && user.getEmail() != null && user.getEmail().contains("@")) ...
        // with a clean chain that can't accidentally NPE.
        String result = Optional.of("vlad")
            .flatMap(this::findEmail)
            .filter(e -> e.contains("@"))
            .map(String::toUpperCase)
            .orElse("NO EMAIL FOUND");
        log.debug("Full chain result: '{}'", result);

        return String.format("""
            Optional chaining (Q48):
            
            map(T → R):
              Transforms the contained value if present.
              If mapping function returns null → Optional.empty().
              '  vlad  ' → trim → length = %s
            
            flatMap(T → Optional<R>):
              Like map, but the mapping function itself returns Optional<R>.
              flatMap FLATTENS it — avoids Optional<Optional<R>>.
              findEmail('vlad')         = %s
              findEmail('unknown-user') = %s
            
            filter(Predicate<T>):
              Keeps the value only if predicate passes; otherwise → empty.
              'alexander' filter(len>5) = %s
              'bob'       filter(len>5) = %s
            
            or(Supplier<Optional<T>>):  (Java 9+)
              Returns self if present, otherwise the supplier's Optional.
              empty().or(() -> Optional.of("fallback")) = %s
            
            KEY POINT — Optional is a container, not a stream:
              A Stream pipeline processes N elements through M operations.
              Optional processes ZERO OR ONE value through chained transforms,
              short-circuiting the moment the value becomes empty.
            """,
            nameLength, email, noEmail, longName, shortName, fallback);
    }

    // Simulated repository method returning Optional
    private Optional<String> findEmail(String username) {
        java.util.Map<String, String> db = Map.of("vlad", "vlad@vbforge.com", "alice", "alice@example.com");
        return Optional.ofNullable(db.get(username));
    }

    private String getValue() {
        return null; // simulates a nullable return
    }

    // Needed for the demo — import not added to top to keep it readable inline
    private static final class Map {
        static <K,V> java.util.Map<K,V> of(K k1, V v1, K k2, V v2) {
            return java.util.Map.of(k1, v1, k2, v2);
        }
    }
}
