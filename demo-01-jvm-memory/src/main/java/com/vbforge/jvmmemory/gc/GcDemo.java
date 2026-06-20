package com.vbforge.jvmmemory.gc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Q4 — Explain garbage collection in Java.
 * Q5 — Which garbage collectors do you know?
 * Q6 — How does the collector know an object has become garbage?
 *
 * KEY POINTS this demo makes visible:
 *
 *  1. REACHABILITY — an object is garbage when no GC root can reach it.
 *     Java does NOT use reference counting (so cycles are handled correctly).
 *  2. GC ROOTS — thread stacks, static fields, JNI refs, monitor references.
 *  3. REFERENCE TYPES — Strong > Soft > Weak > Phantom.
 *     Weak references are cleared at the next GC; soft references survive
 *     until memory pressure.
 *  4. System.gc() is a HINT — the JVM may ignore it entirely.
 *     Never use it in production code.
 */
@Component
public class GcDemo {

    private static final Logger log = LoggerFactory.getLogger(GcDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Reachability and GC roots — Q6
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates that removing all strong references makes an object
     * eligible for collection — regardless of whether other objects still
     * reference each other (the cyclic reference case).
     */
    public String runReachabilityDemo() {
        log.debug("=== GC DEMO: reachability ===");

        // ── simple case ──────────────────────────────────────────────────────
        Object obj = new Object();
        // 'obj' is a local variable on the stack → GC root → object is reachable
        log.debug("obj is reachable: {}", obj);

        obj = null;
        // 'obj' no longer holds a reference → the Object on the heap is unreachable
        // → it is now ELIGIBLE for GC (not necessarily collected immediately)
        log.debug("obj set to null — original Object is now unreachable (eligible for GC)");

        // ── cyclic reference case ────────────────────────────────────────────
        // JUNIOR NOTE: In reference-counting languages (Python, Swift) this would
        // leak. In Java the GC traces from roots and neither node is reachable
        // → both are collected correctly.
        Node a = new Node("A");
        Node b = new Node("B");
        a.next = b;
        b.next = a;   // cycle: A → B → A

        log.debug("Before nulling: A={}, B={} — both reachable via local vars (GC roots)", a.name, b.name);

        a = null;
        b = null;
        // Both local references nulled → neither node is reachable from any GC root
        // → the cycle is NOT a problem — GC collects both
        log.debug("After nulling: both nodes unreachable despite cycle — GC handles correctly");

        System.gc(); // hint only — may or may not run immediately
        return "Reachability demo complete — see logs. Cyclic references do NOT leak in Java.";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reference types — Q6
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates Strong, Soft, and Weak references.
     *
     * STRONG — normal variable. Object lives as long as the reference exists.
     * SOFT   — cleared when JVM needs memory. Good for caches.
     * WEAK   — cleared at the next GC cycle when no strong refs remain.
     *          Used by WeakHashMap, event listener registries.
     */
    public String runReferenceTypesDemo() {
        log.debug("=== GC DEMO: reference types ===");

        // ── Strong reference ─────────────────────────────────────────────────
        byte[] strongData = new byte[1024 * 1024]; // 1 MB
        log.debug("Strong ref: strongData={} — will NOT be collected while this reference exists",
            strongData.length + " bytes");

        // ── Soft reference ───────────────────────────────────────────────────
        // JUNIOR NOTE: SoftReference is cleared only when the JVM is about to
        // throw OutOfMemoryError. Perfect for in-memory caches: the cache
        // survives under normal conditions but gives up memory under pressure.
        SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024 * 1024]);
        log.debug("Soft ref: get()={} — cleared only under memory pressure",
            softRef.get() != null ? "alive" : "cleared");

        // ── Weak reference ───────────────────────────────────────────────────
        // WeakReference is cleared at the NEXT GC cycle once no strong refs remain.
        String strongString = new String("I am weakly referenced"); // force heap allocation
        WeakReference<String> weakRef = new WeakReference<>(strongString);

        log.debug("Before dropping strong ref: weakRef.get()={}", weakRef.get());

        // Drop the strong reference — object now only weakly reachable
        strongString = null;
        System.gc(); // hint — ask GC to run so we can observe

        // After GC, weakRef.get() should return null
        // (not guaranteed immediately but almost always null after System.gc() hint)
        log.debug("After GC hint: weakRef.get()={} (null means GC cleared the weak reference)",
            weakRef.get());

        strongData[0] = 1; // keep strongData reference alive until here
        return String.format(
            "Strong: always alive | Soft: alive=%s | Weak after GC hint: alive=%s",
            softRef.get() != null, weakRef.get() != null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GC pressure — trigger Minor GC and observe — Q4
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Triggers observable GC activity by allocating and releasing many objects.
     *
     * HOW TO OBSERVE:
     *  With GC logging enabled (-Xlog:gc*) you will see log lines like:
     *    [0.123s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 45M->12M(256M) 4.123ms
     *
     *  Reading a G1 GC log line:
     *    GC(3)                     → GC event number 3
     *    Pause Young               → Minor GC — only Young Gen collected
     *    G1 Evacuation Pause       → G1 moved live objects out of Eden/Survivor
     *    45M->12M(256M)            → heap before → after (total heap size)
     *    4.123ms                   → stop-the-world pause duration
     */
    public String runGcPressureDemo(int allocationMb) {
        log.debug("=== GC DEMO: pressure — allocating {}MB of short-lived objects ===", allocationMb);

        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        List<byte[]> temp = new ArrayList<>();

        for (int i = 0; i < allocationMb; i++) {
            temp.add(new byte[1024 * 1024]); // 1 MB per iteration
        }
        log.debug("Allocated {}MB — now releasing all references", allocationMb);
        temp.clear(); // all objects become unreachable

        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return String.format(
            "Allocated and released %dMB. heap delta: %+d MB. " +
            "Check gc.log for 'Pause Young' entries.", allocationMb, (after - before) / 1024 / 1024);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner helper — used to demonstrate cyclic references
    // ─────────────────────────────────────────────────────────────────────────

    // JUNIOR NOTE: This is a static nested class (no reference to outer class).
    // If it were a non-static inner class it would hold an implicit reference
    // to the enclosing GcDemo instance — a common source of accidental memory
    // retention (e.g. anonymous Runnable keeping an Activity alive in Android).
    private static class Node {
        final String name;
        Node next;
        Node(String name) { this.name = name; }
    }
}
