package com.vbforge.org.strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Q14 — Is there a difference between String s1 = "string" and String s2 = new String("string")?
 * Q15 — s1 == s2 vs s1.equals(s2)
 * Q16 — String.intern()
 * Q17 — Are String objects mutable or immutable?
 *
 * KEY POINTS:
 *
 *  String literals → stored in the String Pool (part of Heap since Java 7+).
 *    The JVM interns literals automatically — two identical literals share one object.
 *
 *  new String("text") → always creates a NEW object on the heap, bypassing the pool.
 *    Almost never the right thing to do.
 *
 *  String.intern() → checks the pool; if the string is there returns the pooled instance,
 *    otherwise adds it and returns it. Useful when you have many duplicate strings.
 *
 *  String is immutable:
 *    • The char[] (or byte[] since Java 9) backing field is final and never modified.
 *    • Every "modification" (concat, replace, substring) creates a NEW String object.
 *    • This enables safe sharing (pool), thread safety without synchronisation, and
 *      reliable use as HashMap keys (hashCode is cached after first computation).
 */
@Component
public class StringsDemo {

    private static final Logger log = LoggerFactory.getLogger(StringsDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q14 — Literal vs new String()
    // ─────────────────────────────────────────────────────────────────────────

    public String runPoolVsHeapDemo() {
        log.debug("=== STRINGS: pool vs heap (Q14) ===");

        // ── String literals → String Pool ─────────────────────────────────────
        // JUNIOR NOTE: The compiler calls String.intern() automatically for literals.
        // Both lit1 and lit2 refer to the SAME object in the pool.
        String lit1 = "hello";
        String lit2 = "hello";
        log.debug("lit1 == lit2: {} — same pool object, same address", lit1 == lit2);

        // ── new String() → always a new heap object ───────────────────────────
        // Bypasses the pool. Creates a new object every time.
        // The pool object for "hello" still exists — new String() does not reuse it.
        String heap1 = new String("hello");
        String heap2 = new String("hello");
        log.debug("heap1 == heap2: {} — different heap objects", heap1 == heap2);
        log.debug("heap1 == lit1: {} — heap object != pool object", heap1 == lit1);
        log.debug("heap1.equals(lit1): {} — content is the same", heap1.equals(lit1));

        // ── Compile-time constant folding ─────────────────────────────────────
        // JUNIOR NOTE: The compiler concatenates adjacent literals at compile time.
        // "hel" + "lo" becomes "hello" in bytecode → same pool entry as lit1.
        String folded = "hel" + "lo"; // resolved at compile time
        log.debug("\"hel\"+\"lo\" == lit1: {} — compile-time constant folding", folded == lit1);

        // ── Runtime concatenation → NOT a pool object ─────────────────────────
        String part = "hel";
        String runtime = part + "lo"; // StringBuilder internally at runtime
        log.debug("runtime concat == lit1: {} — runtime concat is a new heap object", runtime == lit1);

        return String.format("""
            Q14 — String literal vs new String():

            String lit1 = "hello";
            String lit2 = "hello";
              lit1 == lit2 → %s   ← same pool object (JVM interns literals)

            String heap1 = new String("hello");
            String heap2 = new String("hello");
              heap1 == heap2 → %s   ← always different heap objects
              heap1 == lit1  → %s   ← heap bypass; pool object != heap object

            Compile-time folding:
              "hel" + "lo" == lit1 → %s   ← compiler folds to "hello" at compile time

            Runtime concatenation:
              String p = "hel";  p + "lo" == lit1 → %s   ← new heap object at runtime

            PRACTICAL RULE:
              Never write new String("literal") — it's wasteful and confusing.
              It creates an extra heap object for no benefit.
              The only rare legitimate use is to force a distinct copy for identity checks.
            """,
            lit1 == lit2, heap1 == heap2, heap1 == lit1,
            folded == lit1, runtime == lit1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q15 — == vs equals for String
    // ─────────────────────────────────────────────────────────────────────────

    public String runEqualityDemo() {
        log.debug("=== STRINGS: equality (Q15) ===");

        String a = "Java";
        String b = new String("Java");
        String c = "Java";

        // == compares references (memory addresses)
        log.debug("a == b: {} — different objects (one pool, one heap)", a == b);
        log.debug("a == c: {} — same pool object", a == c);

        // equals() compares content — almost always what you want
        log.debug("a.equals(b): {} — same content", a.equals(b));
        log.debug("a.equalsIgnoreCase(\"java\"): {} — case-insensitive content compare", a.equalsIgnoreCase("java"));

        // Null-safe comparison — prefer String.equals on the literal to avoid NPE
        String maybeNull = null;
        // maybeNull.equals("Java") → NullPointerException!
        boolean safeCompare = "Java".equals(maybeNull); // false, no NPE
        log.debug("\"Java\".equals(null): {} — safe, no NPE", safeCompare);

        return String.format("""
            Q15 — == vs equals() for String:

            String a = "Java";          // pool object
            String b = new String("Java"); // heap object
            String c = "Java";          // same pool object as a

            a == b  → %s   (different object addresses)
            a == c  → %s   (same pool address)
            a.equals(b) → %s   (content comparison — always use this)

            NULL-SAFE PATTERN:
              "Java".equals(maybeNull) → %s   (no NPE; literal on the left)
              maybeNull.equals("Java") → NullPointerException!

            // JUNIOR NOTE: Always use .equals() to compare String content.
            // == on strings is correct ONLY when you deliberately need reference identity,
            // which is almost never the case in business logic.
            """, a == b, a == c, a.equals(b), safeCompare);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q16 — String.intern()
    // ─────────────────────────────────────────────────────────────────────────

    public String runInternDemo() {
        log.debug("=== STRINGS: intern() (Q16) ===");

        // intern() → look up this string's content in the pool.
        //   If found:  return the pooled instance.
        //   If absent: add it to the pool, return it.
        // After intern(), == comparison with a literal of the same content returns true.

        String heap = new String("interned");  // bypasses pool
        String pooled = "interned";             // lives in pool
        log.debug("Before intern: heap == pooled: {}", heap == pooled);

        String interned = heap.intern();        // returns the pool instance
        log.debug("After intern:  interned == pooled: {}", interned == pooled);

        // ── Practical use case: deduplication ────────────────────────────────
        // JUNIOR NOTE: If your app loads millions of short strings from a database
        // or CSV (e.g. country codes, status values) and many are duplicates,
        // calling .intern() reduces memory by sharing pool objects.
        // Java 8u20+ added -XX:+UseStringDeduplication for G1GC which does
        // something similar automatically — without intern() overhead.
        String s1 = new String("AU"); // imagine loaded from DB
        String s2 = new String("AU"); // another row, same value
        log.debug("Deduplicated via intern: s1.intern() == s2.intern(): {}", s1.intern() == s2.intern());

        return String.format("""
            Q16 — String.intern():

            String heap    = new String("interned"); // bypasses pool
            String pooled  = "interned";             // in the pool

            heap == pooled → %s   (different objects)

            String interned = heap.intern();  // look up or add to pool
            interned == pooled → %s           (now the same pool object)

            HOW intern() WORKS:
              1. Compute hashCode of the string's content.
              2. Look up in the JVM string table (a native hash table).
              3. If found  → return the existing entry (no new object).
              4. If absent → add the string to the table, return it.

            WHEN TO USE:
              ✓ Deduplicating large numbers of repeated strings (country codes, tags)
              ✓ Reducing heap pressure in string-heavy applications
              ✗ General application code — unnecessary and adds complexity

            MODERN ALTERNATIVE:
              G1GC -XX:+UseStringDeduplication (Java 8u20+) does this
              automatically in the background without code changes.
            """, heap == pooled, interned == pooled);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q17 — String immutability
    // ─────────────────────────────────────────────────────────────────────────

    public String runImmutabilityDemo() {
        log.debug("=== STRINGS: immutability (Q17) ===");

        // Every "modification" creates a NEW String object.
        String original = "hello";
        String upper = original.toUpperCase(); // new object
        String concat = original + " world";   // new object (via StringBuilder)
        String replaced = original.replace("l", "r"); // new object
        log.debug("original: '{}' — unchanged after all operations", original);
        log.debug("upper='{}' concat='{}' replaced='{}'", upper, concat, replaced);

        // ── Immutable String as HashMap key is safe ───────────────────────────
        // JUNIOR NOTE: HashMap computes hashCode(key) when you call put().
        // If the key were mutable and its content changed after insertion,
        // the key would hash to a different bucket → get() would return null.
        // String's immutability makes it a safe map key.
        // String also caches its hashCode after first computation (field `hash`).
        String key = "account-id";
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        map.put(key, 100);
        // Impossible to mutate `key` — String has no setter methods.
        // The map entry is stable forever.
        log.debug("String as HashMap key — always safe: get='{}'", map.get("account-id"));

        // ── StringBuilder for mutable string building ─────────────────────────
        // When you need to build a string in a loop, use StringBuilder.
        // String concatenation inside a loop creates O(n) intermediate objects.
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append("item-").append(i).append(", ");
        }
        String built = sb.toString(); // single String at the end
        log.debug("StringBuilder result: {}", built);

        return """
            Q17 — String immutability:

            WHY String is immutable:
              • The internal byte[] (char[] before Java 9) field is private and final.
              • No method on String ever modifies that array — every "change" returns
                a new String backed by a new array.

            EVIDENCE — every method returns a new object:
              String s = "hello";
              s.toUpperCase()    → new String "HELLO" (s is still "hello")
              s.replace("l","r") → new String "herro" (s is still "hello")
              s + " world"       → new String "hello world" (s is still "hello")

            CONSEQUENCES (all positive):
              ✓ Thread-safe without synchronisation — shared freely between threads
              ✓ Safe as HashMap / HashSet key — hashCode cannot change after insertion
              ✓ String pool is possible — shared objects can't be mutated by one holder
              ✓ hashCode is computed once and cached (field `hash`) — subsequent calls O(1)

            WHEN YOU NEED A MUTABLE STRING:
              StringBuilder  — not thread-safe, fastest for single-thread building
              StringBuffer   — thread-safe (synchronized methods), slower
              String.format() / String.join() / String.formatted() — for one-shot formatting

            // JUNIOR NOTE: String concatenation with + inside a loop is O(n²)
            // because each + creates a new copy of the growing string.
            // The compiler optimises a + b + c in a single expression to StringBuilder,
            // but NOT across multiple statements or loop iterations.
            """;
    }
}
