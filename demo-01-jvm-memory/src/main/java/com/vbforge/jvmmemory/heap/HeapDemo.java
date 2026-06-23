package com.vbforge.jvmmemory.heap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Q3 — What regions is the heap divided into?
 * Q7 — Do primitives need to be collected?
 *
 * KEY POINTS this demo makes visible:
 *
 *  1. New objects are allocated in EDEN (part of Young Generation).
 *  2. Objects that survive Minor GC rounds get promoted to OLD Generation.
 *  3. Objects larger than ~50% of a G1 region go straight to HUMONGOUS regions.
 *  4. Primitives that are LOCAL VARIABLES never touch the heap at all.
 *  5. Autoboxed primitives (Integer, Long) DO go to the heap — GC pressure.
 *
 * HOW TO OBSERVE:
 *  Run with GC logging enabled (see README) and watch gc.log while
 *  hitting these endpoints. You will see Eden fill → Minor GC → some
 *  objects promoted to Old → eventually Major/Mixed GC.
 *
 *  Also check: GET /actuator/metrics/jvm.memory.used?tag=area:heap
 */
@Component
public class HeapDemo {

    private static final Logger log = LoggerFactory.getLogger(HeapDemo.class);

    // JUNIOR NOTE: This list is a FIELD — it lives on the heap (inside this
    // Spring bean, which itself is a heap object). Items added to it are
    // kept alive by this strong reference — they will NOT be GC'd until
    // the list is cleared or this bean is destroyed.
    private final List<byte[]> longLivedObjects = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    // Young Generation pressure — short-lived objects
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Allocates many small, short-lived objects.
     * These go to EDEN. Because nothing holds a reference to them after the
     * loop iteration, they are unreachable and will be collected in the next
     * Minor GC — never reaching Old Generation.
     *
     * Watch gc.log: you should see several "GC(N) Pause Young" entries.
     */
    public String runShortLivedAllocation(int rounds) {
        log.debug("=== HEAP DEMO: short-lived allocation ({} rounds) ===", rounds);

        long before = usedHeapMb();
        for (int i = 0; i < rounds; i++) {
            // Each iteration allocates ~100 KB on Eden.
            // The reference 'chunk' is a local variable — it lives on the stack.
            // After the iteration ends, 'chunk' goes out of scope → object unreachable.
            byte[] chunk = new byte[100 * 1024]; // 100 KB
            // We do something with it so the JIT doesn't optimise the allocation away.
            chunk[0] = (byte) i;
        }

        long after = usedHeapMb();
        String msg = String.format(
            "Short-lived allocation done. heap before=%dMB after=%dMB " +
            "(GC likely ran - check gc.log for 'Pause Young')", before, after);
        log.debug(msg);
        return msg;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Old Generation pressure — long-lived objects
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Allocates objects and keeps strong references to them in a list field.
     * Because the field keeps them alive across GC cycles, they survive
     * multiple Minor GCs and eventually get PROMOTED to Old Generation.
     *
     * JUNIOR NOTE: This is the classic "memory leak in Java" pattern —
     * not a real leak (GC can still collect if the list is cleared) but
     * a demonstration of objects growing old because references are held.
     *
     * Call /demo/heap/clear to release all references and watch a Major GC.
     */
    public String runLongLivedAllocation(int chunks) {
        log.debug("=== HEAP DEMO: long-lived allocation ({} chunks kept alive) ===", chunks);

        for (int i = 0; i < chunks; i++) {
            // 1 MB chunk kept in the list → strong reference → survives GC → Old Gen
            longLivedObjects.add(new byte[1024 * 1024]);
        }

        String msg = String.format(
            "Kept %d x 1MB objects alive. Total retained: %d MB. " +
            "These will be promoted to Old Generation. heap used: %d MB",
            chunks, longLivedObjects.size(), usedHeapMb());
        log.debug(msg);
        return msg;
    }

    /**
     * Releases all long-lived references.
     * The objects are now unreachable → eligible for GC from Old Generation.
     * Watch gc.log for a "Pause Young (Prepare Mixed)" or "Pause Mixed" entry.
     */
    public String clearLongLived() {
        int count = longLivedObjects.size();
        longLivedObjects.clear();
        // Suggest GC — not guaranteed to run immediately, but usually does
        // in a demo scenario. NEVER do this in production code.
        System.gc();
        String msg = String.format(
            "Cleared %d long-lived objects. They are now unreachable. " +
            "heap used after clear: %d MB (GC should reclaim Old Gen — check gc.log)",
            count, usedHeapMb());
        log.debug(msg);
        return msg;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Humongous objects (G1-specific)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Allocates a single object larger than 50% of a G1 region.
     * G1 default region size is 1–32 MB (JVM picks based on heap size).
     * With -Xmx256m the region size is typically 1–2 MB.
     * A 4 MB allocation is humongous and goes directly to Old Generation,
     * bypassing Eden entirely.
     *
     * Watch gc.log for "Humongous" in the allocation log lines.
     */
    public String runHumongousAllocation() {
        log.debug("=== HEAP DEMO: humongous object allocation ===");

        // 4 MB — larger than any typical G1 region at -Xmx256m
        // Goes straight to Humongous region in Old Gen, SKIPS Young Gen entirely
        byte[] humongous = new byte[4 * 1024 * 1024];
        humongous[0] = 1; // prevent JIT elimination

        String msg = String.format(
            "Allocated 4 MB humongous object. " +
            "It lives in Humongous regions (Old Gen), not Eden. heap used: %d MB",
            usedHeapMb());
        log.debug(msg);

        // humongous goes out of scope here → unreachable → cleaned up at next
        // GC cycle during the Cleanup phase (G1 handles humongous specially)
        return msg;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Primitives vs autoboxed — Q7
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates that local primitives never touch the heap,
     * while autoboxed equivalents (Integer, Long) do.
     *
     * Run this with -verbose:gc and compare the GC output for
     * the primitive loop vs the Integer loop — the latter creates
     * GC pressure because each Integer is a heap object.
     */
    public String runPrimitiveVsBoxedDemo() {
        log.debug("=== HEAP DEMO: primitive vs autoboxed (Q7) ===");

        long heapBefore = usedHeapMb();

        // ── primitive loop ──────────────────────────────────────────────────
        // JUNIOR NOTE: 'sum' and 'i' are local int variables on the STACK.
        // Zero heap allocations in this loop. No GC pressure whatsoever.
        long sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            sum += i;
        }
        long heapAfterPrimitive = usedHeapMb();

        // ── autoboxed loop ───────────────────────────────────────────────────
        // JUNIOR NOTE: each 'i' is autoboxed to new Integer(i) — a heap object.
        // 1_000_000 Integer objects allocated → significant GC pressure.
        // The compiler inserts Integer.valueOf(i) at every iteration.
        Long boxedSum = 0L; // boxed accumulator compounds the problem
        for (int i = 0; i < 1_000_000; i++) {
            boxedSum += i;  // unbox boxedSum, add i (autoboxed), rebox result
        }
        long heapAfterBoxed = usedHeapMb();

        String msg = String.format(
            "Primitive loop sum=%d heap delta=%dMB | " +
            "Boxed loop sum=%d heap delta=%dMB " +
            "(boxed creates far more GC pressure — check gc.log)",
            sum, heapAfterPrimitive - heapBefore,
            boxedSum, heapAfterBoxed - heapAfterPrimitive);
        log.debug(msg);
        return msg;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private long usedHeapMb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    public long usedHeapMbPublic() {
        return usedHeapMb();
    }
}
