package com.vbforge.concurrency.locks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q59 — ReentrantLock — what does reentrant mean?
 * Q60 — Why must unlock() run in finally?
 * Q61 — What do synchronized and ReentrantLock share regarding mutual exclusion?
 * Q62 — What can ReentrantLock do that synchronized cannot?
 * Q63 — Can the same thread re-enter synchronized on the same monitor — why?
 *
 * KEY POINTS:
 *
 *  Reentrant = a thread that already holds the lock CAN acquire it again
 *  without deadlocking itself. An internal hold-count tracks how many
 *  times the owning thread has acquired it; lock() increments, unlock() decrements.
 *  The lock is only fully released when the hold-count reaches zero.
 *
 *  synchronized is also reentrant — the same thread can enter nested
 *  synchronized(this) blocks without blocking itself.
 *
 *  unlock() MUST be in finally because if the critical section throws,
 *  an exception will unwind the stack without ever reaching unlock().
 *  The lock stays acquired forever → every other thread waits forever → deadlock.
 *
 *  ReentrantLock extras over synchronized:
 *    tryLock()                  — non-blocking attempt; fail fast instead of wait
 *    tryLock(timeout, unit)     — wait at most N ms before giving up
 *    lockInterruptibly()        — waiting thread can be interrupted (Thread.interrupt())
 *    newCondition()             — multiple Condition queues per lock
 *    isFair()                   — FIFO lock acquisition (optional; default: non-fair)
 */
@Component
public class LocksDemo {

    private static final Logger log = LoggerFactory.getLogger(LocksDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q59 — What does reentrant mean?
    // ─────────────────────────────────────────────────────────────────────────

    public String runReentrantMeaningDemo() throws InterruptedException {
        log.debug("=== LOCKS: reentrant meaning (Q59) ===");

        ReentrantLock lock = new ReentrantLock();

        // ── Prove reentrancy: same thread acquires the lock 3 times ───────────
        // JUNIOR NOTE: A non-reentrant lock would deadlock here — the thread
        // would try to acquire a lock it already holds → waits forever for itself.
        // ReentrantLock tracks the owning thread and a hold-count.
        // lock() increments the count; unlock() decrements it.
        // The lock is only fully released when count reaches zero.
        lock.lock();
        log.debug("Acquired lock — holdCount={}", lock.getHoldCount()); // 1

        lock.lock();
        log.debug("Re-acquired lock — holdCount={}", lock.getHoldCount()); // 2

        lock.lock();
        log.debug("Re-acquired lock — holdCount={}", lock.getHoldCount()); // 3

        // Must unlock the same number of times as lock()
        lock.unlock(); log.debug("Unlocked once — holdCount={}", lock.getHoldCount()); // 2
        lock.unlock(); log.debug("Unlocked twice — holdCount={}", lock.getHoldCount()); // 1
        lock.unlock(); log.debug("Unlocked thrice — holdCount={} (fully released)", lock.getHoldCount()); // 0

        // ── Real-world use: reentrant recursive method ────────────────────────
        ReentrantLock recursiveLock = new ReentrantLock();
        int factorial = computeFactorialSafe(5, recursiveLock);
        log.debug("factorial(5) with reentrant lock = {}", factorial);

        // ── Fair vs non-fair lock ─────────────────────────────────────────────
        // JUNIOR NOTE: new ReentrantLock(true) creates a FAIR lock — threads
        // acquire in FIFO order (the longest-waiting thread goes next).
        // Fair locks prevent starvation but have lower throughput.
        // Default (false) = non-fair: any waiting thread may jump the queue.
        ReentrantLock fairLock = new ReentrantLock(true);
        log.debug("fairLock.isFair()={} — FIFO acquisition order", fairLock.isFair());

        return """
            Q59 — What does reentrant mean in ReentrantLock?

            DEFINITION:
              A reentrant lock allows the thread that ALREADY HOLDS the lock
              to acquire it again without blocking itself.
              An internal hold-count tracks how many times the owning thread locked it.

            HOW IT WORKS:
              lock()   → if no owner: acquire, holdCount=1
                          if caller == owner: holdCount++  (no block)
                          if caller != owner: block until released
              unlock() → holdCount--
                          if holdCount == 0: release the lock → wake waiting threads

            PROOF (this demo, single thread):
              lock()   → holdCount=1  ← acquired
              lock()   → holdCount=2  ← re-entered (would deadlock in non-reentrant)
              lock()   → holdCount=3
              unlock() → holdCount=2
              unlock() → holdCount=1
              unlock() → holdCount=0  ← fully released

            WHY REENTRANCY IS NECESSARY:
              class Service {
                  synchronized void outer() { inner(); }  // acquires monitor
                  synchronized void inner() { ... }       // re-acquires SAME monitor
              }
              Without reentrancy: outer() holds the monitor, inner() waits for it
              → outer() waits for inner() to finish → deadlock with itself.
              Java's synchronized is also reentrant for exactly this reason.

            FAIR vs NON-FAIR:
              new ReentrantLock()       → non-fair (default) — any waiter may go next
              new ReentrantLock(true)   → fair — FIFO order, prevents starvation
              Fair locks are ~2–10× slower in throughput — use only when needed.
            """;
    }

    private int computeFactorialSafe(int n, ReentrantLock lock) {
        lock.lock();
        try {
            if (n <= 1) return 1;
            return n * computeFactorialSafe(n - 1, lock); // recursive re-entry
        } finally {
            lock.unlock();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q60 — Why must unlock() run in finally?
    // ─────────────────────────────────────────────────────────────────────────

    public String runUnlockInFinallyDemo() {
        log.debug("=== LOCKS: unlock in finally (Q60) ===");

        ReentrantLock lock = new ReentrantLock();

        // ── WRONG pattern: unlock after the critical section ──────────────────
        // JUNIOR NOTE: If doWork() throws a RuntimeException, the thread
        // unwinds the stack past the unlock() line — it is NEVER called.
        // The lock stays acquired. All other threads waiting on this lock
        // block forever. This is a deadlock that is very hard to diagnose.
        boolean wrongPatternLockStuck = false;
        lock.lock();
        try {
            // doWork() — imagine this throws
            if (true) throw new RuntimeException("simulated failure");
            lock.unlock(); // NEVER REACHED if exception thrown above!
        } catch (RuntimeException e) {
            log.warn("WRONG pattern: exception thrown, unlock() was skipped");
            wrongPatternLockStuck = lock.isLocked(); // lock is still held!
        }
        // Force release for demo to continue
        if (lock.isHeldByCurrentThread()) lock.unlock();

        // ── CORRECT pattern: unlock always runs in finally ────────────────────
        // The finally block runs whether the try block succeeds or throws.
        // This is the canonical ReentrantLock usage pattern.
        boolean correctPatternLockReleased = false;
        lock.lock();
        try {
            // doWork() — same exception
            if (true) throw new RuntimeException("simulated failure");
        } catch (RuntimeException e) {
            log.debug("CORRECT pattern: exception caught, finally will still unlock");
        } finally {
            lock.unlock(); // ALWAYS runs — lock is always released
            correctPatternLockReleased = !lock.isLocked();
            log.debug("CORRECT pattern: lock released in finally? {}", correctPatternLockReleased);
        }

        boolean finalWrongStuck  = wrongPatternLockStuck;
        boolean finalCorrectFree = correctPatternLockReleased;

        return String.format("""
            Q60 — Why must unlock() run in finally?

            THE DANGER — unlock() after the critical section:
              lock.lock();
              doWork();          // throws RuntimeException
              lock.unlock();     // ← NEVER REACHED — stack unwinds past this line

              Result: lock stays acquired → all threads waiting on it block FOREVER.
              This is a deadlock that is invisible in stack traces and very hard to debug.

            WRONG pattern — lock still held after exception: %s

            THE CANONICAL CORRECT PATTERN:
              lock.lock();
              try {
                  doWork();      // may throw — doesn't matter
              } finally {
                  lock.unlock(); // ALWAYS runs — exception or not
              }

              Rule: the try block begins AFTER lock.lock() — not inside.
              If lock.lock() itself throws (InterruptedException on lockInterruptibly),
              the lock was never acquired so unlock() must not run.

            CORRECT pattern — lock released after same exception: %s

            WHY synchronized DOESN'T HAVE THIS PROBLEM:
              synchronized (monitor) { doWork(); }
              The JVM automatically releases the monitor when the block exits,
              whether normally or via exception. No finally needed.
              This is one reason to prefer synchronized for simple use cases.
            """, finalWrongStuck ? "YES — lock stuck! ✗" : "released (forced for demo)",
                 finalCorrectFree ? "YES — lock free ✓" : "still locked ✗");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q61 — What synchronized and ReentrantLock share
    // ─────────────────────────────────────────────────────────────────────────

    public String runSynchronizedVsLockSharedDemo() throws InterruptedException {
        log.debug("=== LOCKS: shared behaviour (Q61) ===");

        // Both enforce mutual exclusion — only one thread in the critical section
        SharedAccount account = new SharedAccount(1000);
        CountDownLatch latch = new CountDownLatch(4);

        // Two threads using synchronized, two using ReentrantLock — same account
        Runnable withdraw100viaSync = () -> {
            account.withdrawSynchronized(100);
            latch.countDown();
        };
        Runnable withdraw100viaLock = () -> {
            account.withdrawWithLock(100);
            latch.countDown();
        };

        new Thread(withdraw100viaSync).start();
        new Thread(withdraw100viaSync).start();
        new Thread(withdraw100viaLock).start();
        new Thread(withdraw100viaLock).start();
        latch.await();

        log.debug("Final balance: {} (expected 600)", account.getBalance());

        return String.format("""
            Q61 — What synchronized and ReentrantLock share:

            BOTH PROVIDE MUTUAL EXCLUSION:
              Only one thread can hold the critical section at a time.
              Other threads block (park in OS scheduler) until the lock is released.
              This prevents data races on shared mutable state.

            BOTH ARE REENTRANT:
              The thread that holds the lock can re-acquire it.
              This allows synchronized or locked methods to call each other.

            BOTH ESTABLISH A HAPPENS-BEFORE RELATIONSHIP:
              Everything done by a thread before unlock() is visible to
              the next thread that acquires the same lock after lock().
              This is the Java Memory Model guarantee — no stale cache values.

            BOTH RELEASE ON EXIT (normally or via exception):
              synchronized → JVM releases monitor on block exit (automatic)
              ReentrantLock → must call unlock() in finally (manual)

            DEMO — 4 threads, 2 using synchronized, 2 using ReentrantLock,
            all withdrawing 100 from the same account (start=1000):
              Final balance = %d  (expected 600 — no lost updates) ✓
            """, account.getBalance());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q62 — What ReentrantLock can do that synchronized cannot
    // ─────────────────────────────────────────────────────────────────────────

    public String runReentrantLockExtrasDemo() throws InterruptedException {
        log.debug("=== LOCKS: ReentrantLock extras (Q62) ===");

        ReentrantLock lock = new ReentrantLock();

        // ── tryLock() — non-blocking attempt ─────────────────────────────────
        // JUNIOR NOTE: synchronized always blocks if the monitor is held.
        // tryLock() returns immediately with false if the lock is unavailable.
        // Useful for "try to do the work, otherwise do something else" patterns.
        lock.lock(); // hold the lock from this thread
        boolean acquired = false;
        Thread tryLockThread = new Thread(() -> {
            boolean got = lock.tryLock(); // returns false immediately — lock is held
            log.debug("tryLock() while lock held: {} — did not block ✓", got);
            if (got) lock.unlock();
        });
        tryLockThread.start();
        tryLockThread.join();
        lock.unlock();

        // ── tryLock(timeout) — wait at most N ms ─────────────────────────────
        lock.lock();
        Thread[] timedThread = {null};
        boolean[] timedResult = {false};
        timedThread[0] = new Thread(() -> {
            try {
                timedResult[0] = lock.tryLock(50, TimeUnit.MILLISECONDS);
                log.debug("tryLock(50ms): {} — timed out after 50ms ✓", timedResult[0]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timedThread[0].start();
        timedThread[0].join(200);
        lock.unlock();

        // ── lockInterruptibly() — waiting thread can be woken by interrupt ────
        // JUNIOR NOTE: With synchronized you CANNOT interrupt a thread that is
        // waiting to enter a synchronized block. It waits indefinitely.
        // lockInterruptibly() throws InterruptedException when interrupted —
        // the thread can abort the wait gracefully.
        lock.lock(); // hold the lock
        Thread interruptibleThread = new Thread(() -> {
            try {
                lock.lockInterruptibly(); // would block — but we interrupt it
                lock.unlock();
            } catch (InterruptedException e) {
                log.debug("lockInterruptibly() interrupted — thread escaped the wait ✓");
            }
        });
        interruptibleThread.start();
        Thread.sleep(20);
        interruptibleThread.interrupt(); // wake it up
        interruptibleThread.join(200);
        lock.unlock();

        // ── Multiple Condition queues ─────────────────────────────────────────
        // JUNIOR NOTE: synchronized has a single wait/notify queue per monitor.
        // ReentrantLock can create MULTIPLE Condition objects — separate queues
        // for different waiting conditions. Classic use: producer/consumer with
        // separate "not full" and "not empty" conditions.
        ReentrantLock bufferLock = new ReentrantLock();
        Condition notFull  = bufferLock.newCondition(); // producers wait here
        Condition notEmpty = bufferLock.newCondition(); // consumers wait here
        log.debug("Created 2 Condition queues on one lock: notFull={}, notEmpty={}", notFull, notEmpty);

        return String.format("""
            Q62 — What ReentrantLock can do that synchronized CANNOT:

            1. tryLock() — non-blocking attempt:
               boolean got = lock.tryLock();
               → Returns true if lock available NOW, false immediately if not.
               → synchronized always blocks — no way to "give up and move on".
               tryLock() while lock held by another thread → %s ✓

            2. tryLock(timeout, unit) — bounded wait:
               boolean got = lock.tryLock(100, TimeUnit.MILLISECONDS);
               → Waits at most 100ms, then returns false.
               → synchronized will wait INDEFINITELY — no timeout possible.
               tryLock(50ms) while lock held → timed out: %s ✓

            3. lockInterruptibly() — interruptible wait:
               lock.lockInterruptibly(); // throws InterruptedException if interrupted
               → A thread blocked on synchronized CANNOT be interrupted.
               → With lockInterruptibly(), Thread.interrupt() wakes the waiter.
               → Useful for cancellable tasks (e.g. shutdown hooks, task cancellation).

            4. Multiple Condition queues:
               Condition notFull  = lock.newCondition(); // producers await here
               Condition notEmpty = lock.newCondition(); // consumers await here
               → notEmpty.signal() wakes only consumers — not producers.
               → synchronized has ONE implicit wait/notify queue per monitor.
                 notifyAll() wakes ALL waiters; impossible to target a subset.

            5. Fair lock option:
               new ReentrantLock(true) — FIFO acquisition order.
               synchronized has no fairness guarantee.

            WHEN synchronized IS STILL FINE:
              For most use cases synchronized is simpler and equally correct.
              Use ReentrantLock only when you actually need one of the above extras.
            """, !acquired ? "false (correct)" : "true (unexpected)",
                 !timedResult[0] ? "false (correct)" : "true (unexpected)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q63 — Can the same thread re-enter its own synchronized monitor?
    // ─────────────────────────────────────────────────────────────────────────

    public String runSynchronizedReentryDemo() {
        log.debug("=== LOCKS: synchronized re-entry (Q63) ===");

        SynchronizedReentryExample example = new SynchronizedReentryExample();
        int result = example.outer();
        log.debug("Reentrant synchronized result: {}", result);

        return String.format("""
            Q63 — Can the same thread re-enter synchronized on the same monitor?

            YES — Java's synchronized is reentrant by design.

            HOW IT WORKS:
              Every Java object has a monitor with an owner thread and a hold-count.
              When a thread calls synchronized(obj):
                → If no owner: set owner=thisThread, holdCount=1 → enter
                → If owner == thisThread: holdCount++ → re-enter immediately
                → If owner != thisThread: block until owner releases

              When the synchronized block exits:
                → holdCount--
                → If holdCount == 0: clear owner, wake one blocked thread

            PROOF (SynchronizedReentryExample):
              class SynchronizedReentryExample {
                  synchronized int outer() {  // acquires monitor, holdCount=1
                      return inner();         // calls another synchronized method
                  }
                  synchronized int inner() {  // re-enters SAME monitor, holdCount=2
                      return 42;             // exits: holdCount=1
                  }                          // outer exits: holdCount=0 → released
              }
              outer() result = %d ✓  (no deadlock)

            WITHOUT REENTRANCY THIS WOULD DEADLOCK:
              outer() holds the monitor and calls inner().
              inner() tries to acquire the same monitor.
              If the monitor didn't track the owner, inner() would wait for outer() to release.
              outer() can't release until inner() returns → deadlock with itself.

            // JUNIOR NOTE: This is why both synchronized and ReentrantLock are
            // reentrant — it's essential for safe object-oriented code where
            // methods of the same class call each other while holding a lock.
            """, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner helper types
    // ─────────────────────────────────────────────────────────────────────────

    /** Shared bank account using both synchronization styles — for Q61 demo. */
    private static class SharedAccount {
        private int balance;
        private final ReentrantLock lock = new ReentrantLock();

        SharedAccount(int initialBalance) { this.balance = initialBalance; }

        synchronized void withdrawSynchronized(int amount) {
            balance -= amount;
            log.debug("  [synchronized] withdrew {}, balance={}", amount, balance);
        }

        void withdrawWithLock(int amount) {
            lock.lock();
            try {
                balance -= amount;
                log.debug("  [ReentrantLock] withdrew {}, balance={}", amount, balance);
            } finally {
                lock.unlock();
            }
        }

        synchronized int getBalance() { return balance; }
    }

    /** Demonstrates synchronized re-entry via nested method calls. */
    private static class SynchronizedReentryExample {
        synchronized int outer() {
            log.debug("outer() — monitor acquired (holdCount would be 1)");
            return inner(); // calls another synchronized method on SAME object
        }

        synchronized int inner() {
            // JUNIOR NOTE: This works because the thread already owns `this` monitor.
            // holdCount becomes 2 here, then drops back to 1 when inner() returns.
            log.debug("inner() — monitor re-entered (holdCount would be 2)");
            return 42;
        }
    }
}
