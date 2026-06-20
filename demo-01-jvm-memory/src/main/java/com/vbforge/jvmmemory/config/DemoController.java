package com.vbforge.jvmmemory.config;

import com.vbforge.jvmmemory.gc.GcDemo;
import com.vbforge.jvmmemory.heap.HeapDemo;
import com.vbforge.jvmmemory.metaspace.MetaspaceDemo;
import com.vbforge.jvmmemory.stack.StackDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Single controller that wires all demo endpoints together.
 *
 * Base URL: http://localhost:8081/demo
 *
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                          │ Covers │ What it shows          │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ GET /demo/stack/call-chain        │ Q2     │ frames, local vars     │
 * │ GET /demo/stack/overflow          │ Q2     │ StackOverflowError     │
 * │ GET /demo/stack/pass-by-value     │ Q2     │ primitive vs reference │
 * │ GET /demo/heap/short-lived        │ Q3, Q7 │ Eden allocation        │
 * │ GET /demo/heap/long-lived         │ Q3     │ Old Gen promotion      │
 * │ GET /demo/heap/clear              │ Q3     │ release Old Gen        │
 * │ GET /demo/heap/humongous          │ Q3, Q8 │ humongous regions      │
 * │ GET /demo/heap/primitive-vs-boxed │ Q7     │ GC pressure comparison │
 * │ GET /demo/gc/reachability         │ Q6     │ GC roots, cycles       │
 * │ GET /demo/gc/references           │ Q6     │ Soft/Weak refs         │
 * │ GET /demo/gc/pressure             │ Q4     │ trigger Minor GC       │
 * │ GET /demo/metaspace/stats         │ Q3     │ Metaspace / pool stats │
 * │ GET /demo/oom                     │ Q1–Q3  │ deliberate OOM         │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final StackDemo stackDemo;
    private final HeapDemo heapDemo;
    private final GcDemo gcDemo;
    private final MetaspaceDemo metaspaceDemo;

    // JUNIOR NOTE: constructor injection — no @Autowired needed when there is
    // exactly one constructor. Preferred over field injection because it makes
    // dependencies explicit and the class testable without a Spring context.
    public DemoController(StackDemo stackDemo,
                          HeapDemo heapDemo,
                          GcDemo gcDemo,
                          MetaspaceDemo metaspaceDemo) {
        this.stackDemo     = stackDemo;
        this.heapDemo      = heapDemo;
        this.gcDemo        = gcDemo;
        this.metaspaceDemo = metaspaceDemo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Stack demos — Q2
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/stack/call-chain")
    public ResponseEntity<String> stackCallChain() {
        return ResponseEntity.ok(stackDemo.runCallChainDemo());
    }

    @GetMapping("/stack/overflow")
    public ResponseEntity<String> stackOverflow() {
        return ResponseEntity.ok(stackDemo.runStackOverflowDemo());
    }

    @GetMapping("/stack/pass-by-value")
    public ResponseEntity<String> stackPassByValue() {
        return ResponseEntity.ok(stackDemo.runPassByValueDemo());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Heap demos — Q3, Q7
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param rounds number of 100KB chunks to allocate and release (default 500 = ~50MB)
     */
    @GetMapping("/heap/short-lived")
    public ResponseEntity<String> heapShortLived(
            @RequestParam(defaultValue = "500") int rounds) {
        return ResponseEntity.ok(heapDemo.runShortLivedAllocation(rounds));
    }

    /**
     * @param chunks number of 1MB chunks to keep alive in Old Gen (default 20 = 20MB)
     */
    @GetMapping("/heap/long-lived")
    public ResponseEntity<String> heapLongLived(
            @RequestParam(defaultValue = "20") int chunks) {
        return ResponseEntity.ok(heapDemo.runLongLivedAllocation(chunks));
    }

    @GetMapping("/heap/clear")
    public ResponseEntity<String> heapClear() {
        return ResponseEntity.ok(heapDemo.clearLongLived());
    }

    @GetMapping("/heap/humongous")
    public ResponseEntity<String> heapHumongous() {
        return ResponseEntity.ok(heapDemo.runHumongousAllocation());
    }

    @GetMapping("/heap/primitive-vs-boxed")
    public ResponseEntity<String> heapPrimitiveVsBoxed() {
        return ResponseEntity.ok(heapDemo.runPrimitiveVsBoxedDemo());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GC demos — Q4, Q5, Q6
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/gc/reachability")
    public ResponseEntity<String> gcReachability() {
        return ResponseEntity.ok(gcDemo.runReachabilityDemo());
    }

    @GetMapping("/gc/references")
    public ResponseEntity<String> gcReferences() {
        return ResponseEntity.ok(gcDemo.runReferenceTypesDemo());
    }

    /**
     * @param mb megabytes to allocate and release (default 100)
     */
    @GetMapping("/gc/pressure")
    public ResponseEntity<String> gcPressure(
            @RequestParam(defaultValue = "100") int mb) {
        return ResponseEntity.ok(gcDemo.runGcPressureDemo(mb));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Metaspace — Q3
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/metaspace/stats")
    public ResponseEntity<String> metaspaceStats() {
        return ResponseEntity.ok(metaspaceDemo.runMetaspaceStatsDemo());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OOM — Q1–Q3
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Deliberately causes OutOfMemoryError: Java heap space.
     *
     * Run with -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/
     * to get a heap dump you can open in Eclipse MAT or VisualVM.
     *
     * WARNING: this will crash the JVM process.
     * Use only deliberately for learning. Never in production.
     */
    @GetMapping("/oom")
    public ResponseEntity<String> oom() {
        // JUNIOR NOTE: returning before the throw just to satisfy the compiler.
        // In reality the JVM throws before we can return anything.
        java.util.List<byte[]> leak = new java.util.ArrayList<>();
        try {
            while (true) {
                leak.add(new byte[10 * 1024 * 1024]); // 10 MB per iteration
            }
        } catch (OutOfMemoryError e) {
            // We catch it here so Spring can still return a response, but in a
            // real scenario OutOfMemoryError should almost never be caught —
            // the JVM state after OOM is unpredictable.
            return ResponseEntity.status(500)
                .body("OutOfMemoryError triggered: " + e.getMessage() +
                      " — check logs/ for heap dump if -XX:+HeapDumpOnOutOfMemoryError is set");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Index
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("""
            demo-01-jvm-memory — available endpoints:
            
              GET /demo/stack/call-chain        Q2  — frames and local variable lifecycle
              GET /demo/stack/overflow          Q2  — StackOverflowError demo
              GET /demo/stack/pass-by-value     Q2  — primitive vs reference passing
              GET /demo/heap/short-lived        Q3  — Eden allocation, Minor GC trigger
              GET /demo/heap/long-lived         Q3  — Old Gen promotion
              GET /demo/heap/clear              Q3  — release Old Gen objects
              GET /demo/heap/humongous          Q3  — humongous object (bypasses Eden)
              GET /demo/heap/primitive-vs-boxed Q7  — GC pressure: int vs Integer
              GET /demo/gc/reachability         Q6  — GC roots, cyclic reference safety
              GET /demo/gc/references           Q6  — Strong / Soft / Weak references
              GET /demo/gc/pressure?mb=100      Q4  — trigger observable GC activity
              GET /demo/metaspace/stats         Q3  — Metaspace and memory pool snapshot
              GET /demo/oom                     Q1  — deliberate OutOfMemoryError
              
              GET /actuator/metrics             —   all JVM metrics (heap, gc pauses, pools)
            """);
    }
}
