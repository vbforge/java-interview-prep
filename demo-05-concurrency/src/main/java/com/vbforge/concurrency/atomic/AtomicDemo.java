package com.vbforge.concurrency.atomic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Q57 — How does CAS in AtomicInteger differ from i++ under synchronized?
 * Q58 — When prefer atomics vs locks / synchronized?
 *
 * KEY POINTS:
 *
 *  CAS (Compare-And-Swap) is a single CPU instruction (CMPXCHG on x86).
 *  It atomically does: if (current == expected) { current = newValue; return true; }
 *  If the value changed since we read it, CAS returns false → we retry (spin).
 *  No thread is ever blocked — this is "optimistic" or "lock-free" concurrency.
 *
 *  synchronized / ReentrantLock use a mutex:
 *  Only one thread enters the critical section; others park (OS-level block).
 *  Context switches are expensive (~microseconds), so locks are slower under
 *  low-to-medium contention compared to CAS spinning.
 *
 *  AtomicInteger.incrementAndGet() internally:
 *    do {
 *        int current = get();        // plain read
 *        int next    = current + 1;
 *    } while (!compareAndSet(current, next)); // retry if someone else changed it
 *
 *  WHEN TO CHOOSE:
 *    AtomicXxx  → single variable, numeric counter, reference swap — low contention
 *    LongAdder  → single counter under VERY high contention (sharded internally)
 *    synchronized / Lock → complex invariants spanning multiple variables
 */
@Component
public class AtomicDemo {

    private static final Logger log = LoggerFactory.getLogger(AtomicDemo.class);

    private static final int THREADS    = 8;
    private static final int INCREMENTS = 100_000;
    private static final int EXPECTED   = THREADS * INCREMENTS;

    // ─────────────────────────────────────────────────────────────────────────
    // Q57 — CAS internals
    // ─────────────────────────────────────────────────────────────────────────

    public String runCasDemo() {
        log.debug("=== ATOMIC: CAS internals (Q57) ===");

        AtomicInteger counter = new AtomicInteger(0);

        // ── Manual CAS loop — exactly what incrementAndGet() does internally ──
        // JUNIOR NOTE: This is the pattern inside every AtomicInteger method.
        // The CPU executes compareAndSet as a single CMPXCHG instruction.
        // If another thread sneaked in and changed the value, CAS returns false
        // and we loop back to re-read the current value and try again.
        int retries = 0;
        for (int i = 0; i < 1000; i++) {
            int current, next;
            do {
                current = counter.get();           // 1. READ current value
                next    = current + 1;             // 2. COMPUTE desired value
                retries += counter.get() != current ? 1 : 0; // count contention (single thread here)
            } while (!counter.compareAndSet(current, next)); // 3. SWAP if still current
        }
        log.debug("Manual CAS loop: counter={} retries={} (0 expected — single thread)", counter.get(), retries);

        // ── AtomicReference — CAS works on any object reference too ───────────
        // JUNIOR NOTE: AtomicReference<T> lets you atomically swap an immutable
        // value object — a common pattern for lock-free config updates.
        AtomicReference<String> config = new AtomicReference<>("v1");
        boolean swapped = config.compareAndSet("v1", "v2"); // swap if still "v1"
        log.debug("AtomicReference CAS: swapped={}, value='{}'", swapped, config.get());

        boolean failedSwap = config.compareAndSet("v1", "v3"); // "v1" is stale — fails
        log.debug("AtomicReference CAS stale: swapped={}, value='{}'", failedSwap, config.get());

        return """
            Q57 — CAS (Compare-And-Swap) inside AtomicInteger:

            WHAT CAS DOES (single CPU instruction: CMPXCHG on x86):
              atomically: if (memory[addr] == expected) {
                              memory[addr] = newValue;
                              return true;
                          } else {
                              return false;  // someone else changed it → retry
                          }

            HOW AtomicInteger.incrementAndGet() IS IMPLEMENTED:
              int incrementAndGet() {
                  int current, next;
                  do {
                      current = get();              // 1. read current value
                      next    = current + 1;        // 2. compute new value
                  } while (!compareAndSet(current, next)); // 3. retry if changed
                  return next;
              }

            HOW i++ UNDER synchronized WORKS:
              synchronized (lock) {
                  i++;   // exclusive access — only one thread ever runs this
              }
              Other threads block (park in OS scheduler) until the lock is released.
              Context switch: ~1–10 µs. CAS retry: ~1–5 ns.

            CAS vs synchronized DIFFERENCE:
              CAS        → optimistic — assumes no contention; retries on collision
                           No thread is ever blocked; no OS context switch
                           CPU-local spin → very fast when collisions are rare
              synchronized → pessimistic — acquires a mutex; others block
                           One context switch per contention event (~µs penalty)
                           Better when contention is high (long waits waste CPU)

            AtomicReference<T>:
              compareAndSet("v1", "v2") → true   (was "v1", swapped to "v2")
              compareAndSet("v1", "v3") → false  (was "v2" — stale expected value)
              Pattern: swap an immutable config/state object lock-free.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q57 — Benchmark: AtomicInteger vs synchronized vs LongAdder
    // ─────────────────────────────────────────────────────────────────────────

    public String runVsSynchronizedDemo() throws InterruptedException {
        log.debug("=== ATOMIC: benchmark atomic vs synchronized (Q57) ===");

        // ── AtomicInteger ─────────────────────────────────────────────────────
        AtomicInteger atomicCounter = new AtomicInteger(0);
        long atomicTime = benchmarkThreads(() -> atomicCounter.incrementAndGet());
        log.debug("AtomicInteger: result={} time={}ms", atomicCounter.get(), atomicTime);

        // ── synchronized method ───────────────────────────────────────────────
        SynchronizedCounter syncCounter = new SynchronizedCounter();
        long syncTime = benchmarkThreads(syncCounter::increment);
        log.debug("synchronized:  result={} time={}ms", syncCounter.get(), syncTime);

        // ── LongAdder — best under high contention ────────────────────────────
        // JUNIOR NOTE: LongAdder maintains an array of cells, each updated by
        // a subset of threads. This reduces contention dramatically under high load.
        // sum() merges all cells. Use when throughput matters more than latest value.
        LongAdder adder = new LongAdder();
        long adderTime = benchmarkThreads(adder::increment);
        log.debug("LongAdder:     result={} time={}ms", adder.sum(), adderTime);

        return String.format("""
            Q57 — Benchmark: %d threads × %,d increments = %,d expected:

              AtomicInteger   : %,d  time=%dms
              synchronized    : %,d  time=%dms
              LongAdder       : %,d  time=%dms

            ALL THREE produce exactly %,d — correctness is identical.
            The difference is throughput under contention.

            WHEN EACH WINS:
              AtomicInteger  → best for low-to-medium contention.
                               CAS retries are cheap when collisions are rare.
              synchronized   → predictable; better when contention is very high
                               (spinning threads waste CPU; parking is cheaper).
              LongAdder      → best throughput under very high contention.
                               Each thread updates its own cell; sum() at the end.
                               Trade-off: sum() is not instantaneously consistent.
            """,
            THREADS, INCREMENTS, EXPECTED,
            atomicCounter.get(), atomicTime,
            syncCounter.get(),   syncTime,
            adder.sum(),         adderTime,
            EXPECTED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q58 — When to choose atomics vs locks
    // ─────────────────────────────────────────────────────────────────────────

    public String runWhenToUseDemo() {
        log.debug("=== ATOMIC: when to use atomics vs locks (Q58) ===");
        return """
            Q58 — When to prefer atomics vs locks / synchronized:

            USE ATOMICS (AtomicInteger, AtomicLong, AtomicReference, LongAdder) WHEN:
              ✓ Single variable needs to be updated atomically
                  — counters (request count, error count, sequence IDs)
                  — a single reference you want to swap atomically (config reload)
              ✓ Low-to-medium contention expected
              ✓ You want lock-free progress guarantees
                  (no thread can permanently block another, unlike with locks)
              ✓ CAS retries are acceptable (each retry is nanoseconds)

              Examples:
                AtomicInteger requestCount;             // metric counter
                AtomicLong    sequenceId;               // ID generator
                AtomicReference<Config> currentConfig;  // hot-swap config
                LongAdder     highThroughputCounter;    // under heavy load

            USE synchronized / ReentrantLock WHEN:
              ✓ Critical section spans MULTIPLE variables that must stay consistent
                  — e.g. update both balance AND transaction log atomically
              ✓ You need to call blocking operations inside the critical section
                  (you cannot hold a CAS spin while doing I/O)
              ✓ Very high contention — blocking is cheaper than spinning
              ✓ You need advanced lock features: tryLock, timed lock,
                interruptible wait, multiple Condition queues (ReentrantLock only)

              Examples:
                synchronized (this) { balance -= amount; log.add(tx); } // two fields
                lock.tryLock(100, MILLISECONDS)  // timeout instead of indefinite block

            QUICK DECISION TABLE:
              Situation                              → Choice
              ─────────────────────────────────────────────────────────────
              Single counter, low contention         → AtomicInteger
              Single counter, extreme contention     → LongAdder
              Swap one reference (config, snapshot)  → AtomicReference CAS
              Multiple fields must change together   → synchronized / Lock
              Need tryLock / timeout / interrupt     → ReentrantLock
              Simple flag (stop signal)              → volatile boolean

            // JUNIOR NOTE: The golden rule — start with AtomicXxx for single
            // variables. Switch to synchronized / Lock only when you need to
            // coordinate changes across multiple variables simultaneously.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private long benchmarkThreads(Runnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(THREADS);
        long start = System.currentTimeMillis();
        for (int t = 0; t < THREADS; t++) {
            new Thread(() -> {
                for (int i = 0; i < INCREMENTS; i++) task.run();
                latch.countDown();
            }).start();
        }
        latch.await();
        return System.currentTimeMillis() - start;
    }

    private static class SynchronizedCounter {
        private int count = 0;
        synchronized void increment() { count++; }
        synchronized int get()        { return count; }
    }
}
