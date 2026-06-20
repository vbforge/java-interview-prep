package com.vbforge.jvmmemory.metaspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * Q3 (Metaspace) — What regions is the heap divided into?
 *
 * KEY POINTS this demo makes visible:
 *
 *  1. Metaspace is NOT part of the heap — it grows in NATIVE memory.
 *  2. Metaspace holds: class bytecode/metadata, method tables,
 *     static field REFERENCES (the objects they point to are on the heap),
 *     interned strings (since Java 7 these moved to the heap string pool).
 *  3. It replaced PermGen in Java 8. No more "OutOfMemoryError: PermGen space".
 *  4. By default Metaspace grows without limit — set -XX:MaxMetaspaceSize
 *     in production to get OOM instead of silent native memory exhaustion.
 *  5. Metaspace is reclaimed when a ClassLoader is GC'd (and all classes
 *     it loaded become unreachable).
 */
@Component
public class MetaspaceDemo {

    private static final Logger log = LoggerFactory.getLogger(MetaspaceDemo.class);

    // JUNIOR NOTE: This static field lives in Metaspace (as part of this class's
    // metadata). However, the String OBJECT it refers to lives on the heap (string pool).
    // Metaspace holds the REFERENCE (pointer), not the object itself.
    private static final String STATIC_CONSTANT = "I am a static field reference — I live in Metaspace";

    /**
     * Prints a snapshot of Metaspace usage and class loading stats using
     * the JMX MXBeans available in every JVM.
     *
     * This is the same data you'd see in VisualVM, JConsole, or
     * /actuator/metrics/jvm.memory.used?tag=area:nonheap
     */
    public String runMetaspaceStatsDemo() {
        log.debug("=== METASPACE DEMO: JVM memory pool breakdown ===");

        // ── Memory pools ─────────────────────────────────────────────────────
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        StringBuilder sb = new StringBuilder();

        for (MemoryPoolMXBean pool : pools) {
            long usedMb = pool.getUsage().getUsed() / 1024 / 1024;
            long maxMb  = pool.getUsage().getMax() == -1
                ? -1
                : pool.getUsage().getMax() / 1024 / 1024;

            String line = String.format("  %-40s type=%-8s used=%4dMB max=%s",
                pool.getName(),
                pool.getType(),
                usedMb,
                maxMb == -1 ? "unlimited" : maxMb + "MB");

            log.debug(line);
            sb.append(line).append("\n");
        }

        // ── Class loading ─────────────────────────────────────────────────────
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
        String classInfo = String.format(
            "\nClasses loaded: %d  currently loaded: %d  unloaded: %d",
            cl.getTotalLoadedClassCount(),
            cl.getLoadedClassCount(),
            cl.getUnloadedClassCount());
        log.debug(classInfo);
        sb.append(classInfo).append("\n");

        // ── Overall heap vs non-heap ──────────────────────────────────────────
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        String heapLine    = String.format("\nHeap:     used=%dMB committed=%dMB max=%dMB",
            mem.getHeapMemoryUsage().getUsed()      / 1024 / 1024,
            mem.getHeapMemoryUsage().getCommitted() / 1024 / 1024,
            mem.getHeapMemoryUsage().getMax()       / 1024 / 1024);
        // JUNIOR NOTE: "non-heap" here = Metaspace + Code Cache + Compressed Class Space
        // This is NOT the heap — it's native memory managed by the JVM outside the GC heap.
        String nonHeapLine = String.format("Non-heap: used=%dMB committed=%dMB (includes Metaspace, Code Cache)",
            mem.getNonHeapMemoryUsage().getUsed()      / 1024 / 1024,
            mem.getNonHeapMemoryUsage().getCommitted() / 1024 / 1024);

        log.debug(heapLine);
        log.debug(nonHeapLine);
        sb.append(heapLine).append("\n").append(nonHeapLine);

        log.debug("Static constant from Metaspace: '{}'", STATIC_CONSTANT);

        return sb.toString();
    }
}
