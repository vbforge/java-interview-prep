package com.vbforge.concurrency.race;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Q53 — How do you safely share mutable state across threads?
 * Q54 — What goes wrong if two threads read–modify–write without coordination?
 * Q55 — What is a data race? Is the outcome always obviously wrong?
 * Q56 — Why doesn't making a field volatile alone make i++ atomic?
 *
 * KEY POINTS:
 *
 *  A data race happens when two or more threads access the same variable
 *  concurrently, at least one access is a write, and there is NO synchronisation.
 *  The result is unpredictable — the JMM makes no guarantee about what value
 *  a thread will see or write.
 *
 *  i++ looks like one operation but is actually THREE:
 *    1. READ  the current value from memory into a register
 *    2. ADD   1 to the register
 *    3. WRITE the result back to memory
 *  Two threads can interleave these steps → lost updates.
 *
 *  volatile guarantees VISIBILITY (every write is immediately flushed to main
 *  memory and every read goes directly to main memory — no CPU cache staleness).
 *  volatile does NOT guarantee ATOMICITY — the read-modify-write sequence is
 *  still three separate operations that can be interleaved.
 *
 *  Safe options for a shared counter:
 *    synchronized block/method  — mutual exclusion, general purpose
 *    AtomicInteger              — lock-free CAS, fastest for single counters
 *    LongAdder                  — better throughput under very high contention
 */
@Component
public class RaceConditionDemo {

    private static final Logger log = LoggerFactory.getLogger(RaceConditionDemo.class);

    private static final int THREADS    = 10;
    private static final int INCREMENTS = 10_000;
    private static final int EXPECTED   = THREADS * INCREMENTS; // 100_000

    // ─────────────────────────────────────────────────────────────────────────
    // Q53, Q54, Q55 — Live race condition: unsynchronised counter
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Runs three counters in parallel — broken, volatile-broken, and safe —
     * so the difference is visible side-by-side.
     *
     * The BROKEN counter almost always produces a value less than 100_000
     * due to lost updates from concurrent read-modify-write interleaving.
     *
     * The VOLATILE counter is also broken — volatile only fixes visibility,
     * not atomicity. i++ is still three non-atomic steps.
     *
     * The SAFE counter uses AtomicInteger.incrementAndGet() which is a single
     * CAS operation — always produces exactly 100_000.
     */
    public String runDataRaceDemo() throws InterruptedException {
        log.debug("=== RACE: data race demo (Q53, Q54, Q55) ===");
        log.debug("Launching {} threads × {} increments = {} expected", THREADS, INCREMENTS, EXPECTED);

        // ── Broken: plain int field, no synchronisation ───────────────────────
        // JUNIOR NOTE: int[] wrapper is used to capture the field from lambda.
        // The race is real — run this a few times and the value changes each run.
        int[] brokenCounter = {0};
        runThreads(() -> {
            for (int i = 0; i < INCREMENTS; i++) {
                brokenCounter[0]++; // READ → ADD → WRITE — not atomic!
            }
        });
        int brokenResult = brokenCounter[0];
        log.debug("Broken counter result: {} (expected {})", brokenResult, EXPECTED);

        // ── Volatile: volatile int — visibility fixed, atomicity still broken ──
        // JUNIOR NOTE: volatile flushes writes to main memory immediately,
        // so every thread sees the latest value. But i++ is still three steps.
        // Thread A and Thread B can both READ the same value before either WRITES.
        // The second write silently overwrites the first → lost update.
        VolatileCounter volatileCounter = new VolatileCounter();
        runThreads(() -> {
            for (int i = 0; i < INCREMENTS; i++) {
                volatileCounter.increment(); // volatile int — still not atomic
            }
        });
        int volatileResult = volatileCounter.get();
        log.debug("Volatile counter result: {} (expected {}) — volatile ≠ atomic", volatileResult, EXPECTED);

        // ── Safe: AtomicInteger — CAS guarantees atomicity ────────────────────
        AtomicInteger safeCounter = new AtomicInteger(0);
        runThreads(() -> {
            for (int i = 0; i < INCREMENTS; i++) {
                safeCounter.incrementAndGet(); // single CAS instruction — atomic
            }
        });
        int safeResult = safeCounter.get();
        log.debug("Atomic counter result: {} (expected {}) — correct ✓", safeResult, EXPECTED);

        return String.format("""
            Q53/Q54/Q55 — Data race demo (%d threads × %d increments = %d expected):

            BROKEN  (plain int, no sync)  : %,d  ← lost %,d updates
            VOLATILE (volatile int)        : %,d  ← volatile ≠ atomic, still lost updates
            SAFE    (AtomicInteger)        : %,d  ← always correct ✓

            WHY BROKEN loses updates — the i++ interleave:
              Thread A: READ  counter=50000
              Thread B: READ  counter=50000   ← reads SAME stale value
              Thread A: ADD   → 50001
              Thread B: ADD   → 50001         ← also gets 50001
              Thread A: WRITE counter=50001
              Thread B: WRITE counter=50001   ← overwrites A's write — one update LOST

            Q55 — Is the outcome always obviously wrong?
              No. Sometimes the lost updates are so few the result looks "close enough".
              Data races are non-deterministic — they depend on thread scheduling,
              CPU count, JIT optimisation, and load. A race may go undetected for
              months and only surface under production load. Never rely on luck.

            Q53 — Safe options for shared mutable state:
              AtomicInteger / AtomicLong    → single numeric counter, lock-free
              synchronized block / method   → any complex critical section
              ReentrantLock                 → same as synchronized + extras
              ConcurrentHashMap             → thread-safe map
              Collections.synchronizedXxx   → wrapped legacy collections
              volatile                      → ONLY for a single write/read (flag),
                                              NOT for read-modify-write sequences
            """,
            THREADS, INCREMENTS, EXPECTED,
            brokenResult,   EXPECTED - brokenResult,
            volatileResult, EXPECTED - volatileResult,
            safeResult);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q55, Q56 — volatile: visibility yes, atomicity no
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Isolates the volatile demonstration with a clear stop-flag pattern
     * (the one legitimate use of volatile) vs the broken i++ pattern.
     */
    public String runVolatileDemo() throws InterruptedException {
        log.debug("=== RACE: volatile demo (Q55, Q56) ===");

        // ── CORRECT use of volatile: a stop flag ─────────────────────────────
        // JUNIOR NOTE: Without volatile, the JIT compiler may cache `running`
        // in a CPU register and the worker thread may never see the write from
        // the main thread → infinite loop. volatile prevents that cache.
        StopFlag flag = new StopFlag();
        Thread worker = new Thread(() -> {
            int iterations = 0;
            while (flag.isRunning()) { // always reads from main memory
                iterations++;
                if (iterations > 10_000_000) break; // safety guard
            }
            log.debug("Worker stopped after {} iterations — volatile flag worked", iterations);
        });
        worker.start();
        Thread.sleep(10);
        flag.stop();             // write is immediately visible to worker
        worker.join(500);

        // ── INCORRECT use of volatile: i++ is not atomic ─────────────────────
        VolatileCounter vc = new VolatileCounter();
        CountDownLatch latch = new CountDownLatch(2);

        Runnable increment50k = () -> {
            for (int i = 0; i < 50_000; i++) vc.increment();
            latch.countDown();
        };
        new Thread(increment50k).start();
        new Thread(increment50k).start();
        latch.await();
        int result = vc.get();
        log.debug("volatile i++ from 2 threads × 50k: {} (expected 100000)", result);

        return String.format("""
            Q56 — Why volatile alone doesn't make i++ atomic:

            WHAT volatile DOES:
              Guarantees VISIBILITY — writes are immediately flushed to main memory.
              Every thread reads directly from main memory, not a CPU cache copy.
              Prevents the JIT from hoisting the read out of a loop (caching in register).

            WHAT volatile DOES NOT DO:
              Does NOT make compound operations atomic.
              i++ is THREE bytecode instructions:
                GETFIELD  (read current value)
                IADD 1    (add one)
                PUTFIELD  (write back)
              Two threads can interleave these three steps → lost updates.

            CORRECT USE — a boolean stop flag (single write, single read):
              volatile boolean running = true;
              // Main thread: running = false;  → worker sees it immediately ✓
              // Without volatile: JIT may cache `running` in a register → infinite loop

            BROKEN USE — read-modify-write with 2 threads × 50k:
              Expected : 100000
              Got      : %,d  ← %,d updates lost

            RULE:
              volatile = visibility only.
              For atomicity on a counter → AtomicInteger / synchronized.
              For a simple flag (write-once, read-many) → volatile is enough.
            """, result, 100_000 - result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Launches THREADS threads all running the given task, waits for all to finish. */
    private void runThreads(InterruptibleRunnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREADS);
        for (int t = 0; t < THREADS; t++) {
            new Thread(() -> {
                try { task.run(); } catch (Exception e) { log.error("Thread error", e); }
                finally { latch.countDown(); }
            }).start();
        }
        latch.await();
    }

    @FunctionalInterface
    private interface InterruptibleRunnable {
        void run() throws InterruptedException;
    }

    /** Carrier for a volatile int — shows that volatile ≠ atomic for i++. */
    private static class VolatileCounter {
        private volatile int count = 0;
        // JUNIOR NOTE: volatile on the field means each read/write goes to main memory.
        // But increment() is still read → add → write: three steps, not one.
        void increment() { count++; }
        int get()        { return count; }
    }

    /** Correct use of volatile: a single boolean flag written by one thread, read by another. */
    private static class StopFlag {
        private volatile boolean running = true;
        boolean isRunning() { return running; }
        void stop()         { running = false; }
    }
}
