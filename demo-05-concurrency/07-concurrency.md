# 07 — Concurrency

- > **Questions covered:** Q53–Q63
- > **Demo:** [demo-05-concurrency](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q53 — How do you safely share mutable state across threads?

**Short answer**

Safely share mutable state across threads using **synchronization**, **volatile**, **atomic classes**, or **thread-safe collections** from `java.util.concurrent`. The simplest approach is `synchronized` blocks or methods.

**In depth**

**Options for thread-safe sharing:**

| Method | When to use | Example |
|--------|-------------|---------|
| **synchronized** | Simple mutual exclusion, fine-grained control | `synchronized (lock) { }` |
| **Atomic classes** | Single variables, CAS operations | `AtomicInteger`, `AtomicReference` |
| **volatile** | Visibility without atomicity (read/write only) | `volatile boolean flag` |
| **Concurrent collections** | Thread-safe collections | `ConcurrentHashMap`, `CopyOnWriteArrayList` |
| **Locks** | Advanced synchronization needs | `ReentrantLock` |

```java
// ❌ NOT thread-safe
class Counter {
    private int count = 0;
    public void increment() { count++; }  // Race condition!
    public int get() { return count; }
}

// ✅ Thread-safe with synchronized
class ThreadSafeCounter {
    private int count = 0;
    public synchronized void increment() { count++; }
    public synchronized int get() { return count; }
}

// ✅ Thread-safe with AtomicInteger (best for counters)
class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    public void increment() { count.incrementAndGet(); }
    public int get() { return count.get(); }
}

// ✅ Thread-safe with ConcurrentHashMap
Map<String, Integer> map = new ConcurrentHashMap<>();
map.compute("key", (k, v) -> v == null ? 1 : v + 1);  // Atomic update
```

> **// JUNIOR NOTE:** The most common mistake is using `HashMap` or `ArrayList` in multi-threaded code without synchronization. Always use `ConcurrentHashMap` instead of `HashMap` when multiple threads access it.

---

## Q54 — What goes wrong if two threads read-modify-write a shared variable without coordination?

**Short answer**

You get a **race condition**. The result becomes unpredictable because operations interleave unpredictably. The classic example is the **lost update** problem where increments are lost.

**In depth**

**The lost update problem:**

```
Race Condition — Lost Update Timeline

Expected: 2    Actual: 1    One increment was lost — threads both read 0 before either wrote back

Time | Thread 1                | Thread 2                | count
-----|-------------------------|-------------------------|-------
t1   | read count → 0          | —                       | 0
t2   | —                       | read count → 0          | 0
t3   | increment (local)       | —                       | 0
t4   | —                       | increment (local)       | 0
t5   | write count → 1         | —                       | 1
t6   | —                       | write count → 1         | 1 ⚠ lost!
```

```java
// Classic race condition example
class Counter {
    private int count = 0;

    public void increment() {
        count++;  // Read-modify-write: NOT atomic!
    }
}

// 1000 threads each calling increment() 1000 times
// Expected: 1,000,000
// Actual: random value less than 1,000,000 (lost updates)

// Other problems:
// 1. Invalid state — inconsistent object state
// 2. Infinite loops — stale values in while loops
// 3. Memory consistency errors — missing updates
```

> **// JUNIOR NOTE:** The lost update problem is the classic concurrency bug. Even if you run the code 1000 times and it works, it can fail on the 1001st run. This is why concurrency bugs are so hard to find — they're non-deterministic.

---

## Q55 — What is a data race? Is the outcome always obviously wrong?

**Short answer**

A **data race** occurs when two threads access the same variable concurrently, at least one access is a **write**, and there is no proper synchronization. The outcome is **not always obviously wrong** — it can produce correct-looking results intermittently, making it very hard to debug.

**In depth**

**Data race characteristics:**
- **Unpredictable:** Order of execution is non-deterministic
- **Reproducibility issues:** May fail in production but work in tests
- **Subtle corruption:** Values may be partially updated (e.g., torn reads/writes)
- **Hard to detect:** No compiler warnings, no runtime exceptions

```java
// Data race example
class SharedData {
    private int value = 0;
    private boolean ready = false;
    
    public void writer() {
        value = 42;       // Write
        ready = true;     // Write — data race with reader
    }
    
    public void reader() {
        if (ready) {          // Read — data race with writer
            System.out.println(value);  // Might print 0 or 42!
        }
    }
}

// Why outcome isn't obviously wrong:
// - Threads might execute in a "lucky" order (works 99% of the time)
// - Different CPUs have different memory models
// - JIT compiler can reorder instructions

// ✅ Fix: use volatile or synchronized
class FixedData {
    private volatile int value = 0;
    private volatile boolean ready = false;
    // volatile ensures visibility and prevents reordering
}
```

**Why data races are dangerous:**
- **Heisenbugs:** Bugs that disappear when you try to debug them
- **Production only:** May only occur under specific load patterns
- **Hard to reproduce:** Different environments behave differently
- **Security issues:** Can lead to inconsistent state and vulnerabilities

> **// JUNIOR NOTE:** This is why we use tools like `ThreadSanitizer` and static analysis to detect data races. In interviews, emphasize that data races are **not always obvious** — they can appear correct 99% of the time but fail catastrophically in production.

---

## Q56 — Why doesn't making a field volatile alone make i++ atomic?

**Short answer**

`volatile` ensures **visibility** (all threads see the latest value) but **does not** provide atomicity for compound operations. `i++` is three operations: **read → increment → write**, and these can interleave between threads.

**In depth**

**Why volatile is not enough:**
```java
class Counter {
    private volatile int count = 0;
    
    public void increment() {
        count++;  // STILL NOT ATOMIC!
    }
}

// count++ expands to three operations:
// 1. int temp = count;     // Read
// 2. temp = temp + 1;      // Increment
// 3. count = temp;         // Write

// Two threads interleaving:
// Thread 1: read count (0) → temp=0
// Thread 2: read count (0) → temp=0
// Thread 1: increment → temp=1 → write count=1
// Thread 2: increment → temp=1 → write count=1
// Result: lost update! Expected: 2, Actual: 1
```

**volatile guarantees:**
- **Visibility:** Writes to volatile are immediately visible to all threads
- **Ordering:** Operations are not reordered by JIT or CPU
- **But NOT atomicity:** Compound operations still need synchronization

**When volatile IS sufficient:**
```java
// ✅ Single read/write operation is safe with volatile
class Flag {
    private volatile boolean running = true;
    
    public void stop() {
        running = false;  // Single write → safe!
    }
    
    public void work() {
        while (running) {    // Single read → safe!
            // do work
        }
    }
}
```

> **// JUNIOR NOTE:** Many juniors think `volatile` makes operations atomic. It doesn't. For atomic increments, use `AtomicInteger` or `synchronized`. `volatile` is for visibility, not atomicity.

---

## Q57 — How does CAS in AtomicInteger differ from i++ under synchronized?

**Short answer**

**CAS (Compare-And-Swap)** is a hardware-level atomic instruction that updates a variable if it still has the expected value. It's **non-blocking** and more lightweight than `synchronized`, which uses **blocking** mutual exclusion with locks.

**In depth**

| Feature | AtomicInteger (CAS) | synchronized |
|---------|---------------------|--------------|
| **Mechanism** | Hardware-level atomic compare-and-swap | Monitor lock (blocking) |
| **Thread blocking** | No blocking (spin-wait) | Blocks threads (context switch) |
| **Performance** | Faster for low contention | Better for high contention |
| **Scalability** | Scales better (no context switching) | Can suffer from contention |
| **Complexity** | Simple for single variables | Can protect multiple variables |

```java
// AtomicInteger uses CAS internally
class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();  // CAS in a loop (retry on failure)
    }
}

// CAS algorithm (simplified):
// while (true) {
//     int current = value;
//     int next = current + 1;
//     if (compareAndSwap(current, next)) {
//         break;  // Success
//     }
//     // Retry with new current value
// }

// Synchronized uses blocking lock
class SynchronizedCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;  // Thread blocks on lock
    }
}

// Performance comparison:
// CAS:  → No context switching, fast path, retries on failure
// Lock: → Acquire lock, block, context switch, release lock
```

**CAS advantages:**
- **Non-blocking:** No thread suspension
- **No deadlocks:** No lock hierarchy issues
- **Better scalability:** Works well with many threads
- **Low overhead:** For single variables

**CAS disadvantages:**
- **ABA problem:** Value changes A→B→A, CAS succeeds incorrectly
- **Spin-waste:** CPU spins on high contention
- **Limited scope:** Only works for single variables
- **Complex retry logic:** Need to handle failures

> **// JUNIOR NOTE:** CAS is the foundation of Java's non-blocking algorithms. `AtomicInteger`, `AtomicLong`, and most `java.util.concurrent` classes use CAS internally. It's what makes them so fast and scalable compared to `synchronized`.

---

## Q58 — When prefer atomics vs locks / synchronized?

**Short answer**

**Atomics** are preferred for **single variables** and **low contention** scenarios. **Locks** are preferred for **complex operations**, **multiple variables**, or **high contention** situations.

**In depth**

**Prefer atomics when:**
- Operating on a single variable (counter, flag, reference)
- Operation is simple (increment, compare-and-set)
- Contention is low to moderate
- Need high performance and scalability
- Want to avoid blocking threads

```java
// Good use of atomics
class Counter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public int incrementAndGet() {
        return count.incrementAndGet();  // Single variable, simple op
    }
}
```

**Prefer locks when:**
- Operation involves multiple variables (consistent state)
- Need to perform complex updates
- Contention is high (CAS retries would waste CPU)
- Need additional features (condition variables, tryLock, etc.)
- Operation is long-running (avoid spin-waste)

```java
// Good use of locks
class BankAccount {
    private double balance;
    private ReentrantLock lock = new ReentrantLock();
    
    public void transfer(double amount, BankAccount target) {
        lock.lock();          // Multiple variables involved
        try {
            if (balance >= amount) {
                balance -= amount;
                target.balance += amount;
            }
        } finally {
            lock.unlock();
        }
    }
}
```

**Decision matrix:**

| Scenario | Recommendation |
|----------|----------------|
| Simple counter increment | ✅ AtomicInteger |
| Multiple variables update | ✅ synchronized/ReentrantLock |
| High contention | ✅ Lock (avoids spin-waste) |
| Low contention, simple op | ✅ Atomic (faster) |
| Need waiting/notification | ✅ Lock with Condition |
| Need tryLock timeout | ✅ ReentrantLock |

> **// JUNIOR NOTE:** A common mistake is using `synchronized` for simple counters when `AtomicInteger` would be faster and simpler. Conversely, using atomics for complex operations leads to inconsistent state. Choose based on the operation, not habit.

---

## Q59 — ReentrantLock — what does reentrant mean here?

**Short answer**

**Reentrant** means that a thread that already holds the lock can **re-enter** the same lock **multiple times** without blocking itself. The lock maintains a **hold count** that increments on each `lock()` and decrements on each `unlock()`.

**In depth**

```java
// Reentrancy in action
class ReentrantExample {
    private ReentrantLock lock = new ReentrantLock();
    
    public void methodA() {
        lock.lock();                    // Hold count: 1
        try {
            System.out.println("Method A");
            methodB();                  // Re-enters the same lock
        } finally {
            lock.unlock();              // Hold count: 0
        }
    }
    
    public void methodB() {
        lock.lock();                    // Hold count: 2
        try {
            System.out.println("Method B");
        } finally {
            lock.unlock();              // Hold count: 1
        }
    }
}

// Hold count tracking:
// 1. lock.lock()  → holdCount = 1
// 2. methodA calls methodB
// 3. lock.lock()  → holdCount = 2 (reentrant!)
// 4. lock.unlock() → holdCount = 1
// 5. lock.unlock() → holdCount = 0 (lock released)
```

**Why reentrancy matters:**
- **Self-block prevention:** Thread can call synchronized methods recursively
- **Cleaner code:** Methods can call each other without deadlock
- **Same behavior as synchronized:** `synchronized` is also reentrant
- **Hold count:** Lock is released only when hold count reaches 0

**Reentrant vs non-reentrant lock:**
```java
// ❌ Non-reentrant lock would deadlock here
// methodA() acquires lock → calls methodB() → tries to acquire same lock
// → DEADLOCK (thread blocks waiting for itself)

// ✅ Reentrant lock allows this pattern safely
```

> **// JUNIOR NOTE:** "Reentrant" is often confused with "recursive." They're related but different. Reentrant locks are designed to be safe for recursive calls. The same thread can lock the same lock multiple times, preventing self-deadlock.

---

## Q60 — Why must unlock run in finally?

**Short answer**

`unlock()` must be in a **finally** block to ensure the lock is **always released**, even if an exception is thrown. If an exception occurs and the lock isn't released, the lock remains held, causing **deadlocks** and thread starvation.

**In depth**

```java
// ❌ WRONG — lock can leak
class Dangerous {
    private ReentrantLock lock = new ReentrantLock();
    
    public void dangerous() {
        lock.lock();
        // If exception occurs here, lock is never released!
        if (someCondition) {
            throw new RuntimeException("Error!");
        }
        lock.unlock();  // This line may never execute
    }
}

// ✅ CORRECT — lock always released
class Safe {
    private ReentrantLock lock = new ReentrantLock();
    
    public void safe() {
        lock.lock();
        try {
            // Critical section
            if (someCondition) {
                throw new RuntimeException("Error!");
            }
        } finally {
            lock.unlock();  // Always executes!
        }
    }
}
```

**Consequences of not using finally:**
- **Deadlock:** Other threads wait forever for the lock
- **Thread starvation:** Threads pile up waiting
- **Resource exhaustion:** Threads cannot proceed
- **System hangs:** Application becomes unresponsive
- **Hard to debug:** Symptoms appear unrelated

**Best practice pattern:**
```java
// ✅ Standard lock pattern
lock.lock();
try {
    // Do work safely
} finally {
    lock.unlock();
}

// ✅ Try with multiple locks (avoid deadlock)
ReentrantLock lock1 = new ReentrantLock();
ReentrantLock lock2 = new ReentrantLock();

lock1.lock();
try {
    lock2.lock();
    try {
        // Both locks held
    } finally {
        lock2.unlock();
    }
} finally {
    lock1.unlock();
}
```

> **// JUNIOR NOTE:** This is a critical pattern in concurrent programming. **Always use finally for unlock()**. It's one of the most common causes of production deadlocks. `synchronized` handles this automatically, but with `ReentrantLock` you must do it yourself.

---

## Q61 — What do synchronized and ReentrantLock share regarding mutual exclusion?

**Short answer**

Both `synchronized` and `ReentrantLock` provide **mutual exclusion** — only one thread can hold the lock at a time. Both are **reentrant**, meaning the same thread can acquire the lock multiple times. Both ensure **happens-before** relationships for visibility.

**In depth**

| Feature | synchronized | ReentrantLock |
|---------|--------------|---------------|
| **Mutual exclusion** | ✅ Yes | ✅ Yes |
| **Reentrant** | ✅ Yes | ✅ Yes |
| **Visibility guarantees** | ✅ Happens-before | ✅ Happens-before |
| **Memory consistency** | ✅ Same as lock | ✅ Same as synchronized |

```java
// Both provide mutual exclusion and reentrancy

// synchronized — automatic
class SyncExample {
    public synchronized void method() {
        // Only one thread at a time
        this.method2();  // Reentrant — same lock
    }
    
    public synchronized void method2() {
        // Same lock, reentrant
    }
}

// ReentrantLock — manual
class LockExample {
    private ReentrantLock lock = new ReentrantLock();
    
    public void method() {
        lock.lock();
        try {
            // Only one thread at a time
            method2();  // Reentrant — same lock
        } finally {
            lock.unlock();
        }
    }
    
    public void method2() {
        lock.lock();  // Reentrant — same thread can re-enter
        try {
            // Critical section
        } finally {
            lock.unlock();
        }
    }
}
```

**Shared properties — why they matter:**
- **Mutual exclusion:** Prevents race conditions
- **Reentrancy:** Prevents self-deadlock
- **Visibility:** Threads see the most up-to-date values
- **Atomicity:** Operations inside the lock are atomic as a group

> **// JUNIOR NOTE:** Both `synchronized` and `ReentrantLock` provide the same fundamental guarantees. The difference is in flexibility, features, and syntax. `synchronized` is simpler; `ReentrantLock` is more powerful.

---

## Q62 — What can ReentrantLock do that synchronized cannot?

**Short answer**

`ReentrantLock` provides advanced features: **tryLock** (non-blocking attempt), **timed lock** (timeout), **interruptible lock**, **multiple condition variables**, and **fairness** (FIFO ordering).

**In depth**

| Feature | synchronized | ReentrantLock |
|---------|--------------|---------------|
| **Non-blocking attempt** | ❌ No | ✅ `tryLock()` |
| **Timed lock** | ❌ No | ✅ `tryLock(timeout, unit)` |
| **Interruptible lock** | ❌ No (wait() only) | ✅ `lockInterruptibly()` |
| **Multiple conditions** | ❌ One wait set | ✅ Multiple `Condition` objects |
| **Fairness** | ❌ Not guaranteed | ✅ Fair mode available |
| **Lock state inspection** | ❌ No | ✅ `getHoldCount()`, `isLocked()` |

```java
// tryLock — non-blocking attempt
if (lock.tryLock()) {
    try {
        // Got the lock, do work
    } finally {
        lock.unlock();
    }
} else {
    // Lock was busy, do something else
}

// tryLock with timeout — timed attempt
try {
    if (lock.tryLock(5, TimeUnit.SECONDS)) {
        try {
            // Got the lock within 5 seconds
        } finally {
            lock.unlock();
        }
    } else {
        // Timeout, couldn't acquire lock
    }
} catch (InterruptedException e) {
    // Interrupted while waiting
}

// lockInterruptibly — interruptible lock
lock.lockInterruptibly();
try {
    // Can be interrupted while waiting for lock
} catch (InterruptedException e) {
    // Handle interruption
} finally {
    lock.unlock();
}

// Multiple conditions — like wait/notify for different events
Condition notFull = lock.newCondition();
Condition notEmpty = lock.newCondition();

// Producer
lock.lock();
try {
    while (queue.isFull()) {
        notFull.await();  // Wait for not full
    }
    queue.add(item);
    notEmpty.signal();    // Signal not empty
} finally {
    lock.unlock();
}

// Consumer
lock.lock();
try {
    while (queue.isEmpty()) {
        notEmpty.await();  // Wait for not empty
    }
    Item item = queue.remove();
    notFull.signal();      // Signal not full
} finally {
    lock.unlock();
}

// Fairness — FIFO order
ReentrantLock fairLock = new ReentrantLock(true);  // Fair mode
// Waiting threads acquire lock in FIFO order
```

> **// JUNIOR NOTE:** The extra features of `ReentrantLock` come with complexity. Use `synchronized` for simple cases. Only use `ReentrantLock` when you need its advanced features. And **always** unlock in finally!

---

## Q63 — Can the same thread re-enter synchronized on the same monitor? Why?

**Short answer**

**Yes.** `synchronized` is **reentrant**. The same thread can re-enter a synchronized block on the same monitor because the JVM tracks **hold count** per thread. This prevents **self-deadlock** when a synchronized method calls another synchronized method on the same object.

**In depth**

```java
// Reentrancy demonstration
class ReentrantDemo {
    public synchronized void methodA() {
        System.out.println("Method A");
        methodB();  // Calls another synchronized method
        // Thread already holds the lock, so re-entrance is allowed
    }
    
    public synchronized void methodB() {
        System.out.println("Method B");
    }
}

// How it works internally:
// 1. Thread T enters methodA() → acquires monitor
//    → monitor.owner = T, monitor.count = 1
// 2. methodA() calls methodB()
//    → T already owns monitor → count++ → monitor.count = 2
// 3. methodB() returns → count-- → monitor.count = 1
// 4. methodA() returns → count-- → monitor.count = 0 → lock released
```

**Why reentrancy is essential:**
- **Recursive methods:** Synchronized recursive calls would deadlock
- **Method chains:** Calling synchronized methods from synchronized methods
- **Inheritance:** Superclass methods calling subclass methods
- **Interfaces:** Default methods calling other synchronized methods

**What if synchronized was NOT reentrant?**
```java
// ❌ Non-reentrant lock would cause deadlock
class NonReentrantDemo {
    public synchronized void methodA() {
        // Lock acquired by thread T
        methodB();  // DEADLOCK! T tries to acquire lock it already holds
        // Thread T would block forever waiting for itself
    }
    
    public synchronized void methodB() {
        // Would need to acquire the same lock
    }
}
```

**Monitor implementation:**
- Each object has a monitor (lock)
- Monitor tracks: **owner thread** and **entry count**
- On lock: if owner == current → count++
- On unlock: count-- → if count == 0 → release
- If owner != current → thread blocks

> **// JUNIOR NOTE:** This is why you can safely have synchronized methods calling each other. The JVM tracks reentrant locks automatically. Without reentrancy, Java's concurrency model would be much harder to use. This is one of the core features of Java's synchronization.

---

## Quick-reference cheat sheet

```
Thread Safety Mechanisms:
  synchronized  → Mutual exclusion, reentrant, simple
  volatile      → Visibility only (no atomicity)
  Atomic        → CAS, non-blocking, fast for single variables
  Concurrent    → Thread-safe collections

Race Condition:
  Two+ threads access shared data without synchronization
  Read-modify-write is NOT atomic
  → Lost updates, inconsistent state

Data Race:
  Unsynchronized concurrent access (at least one write)
  Outcome is non-deterministic, can appear correct
  → Hard to debug, may fail in production

CAS (Compare-And-Swap):
  Hardware atomic instruction
  Non-blocking, retry on failure
  → AtomicInteger, AtomicLong, AtomicReference

volatile:
  Visibility → all threads see latest value
  Ordering → prevents instruction reordering
  NOT atomic → i++ still needs synchronized/atomic

ReentrantLock vs synchronized:
  same: mutual exclusion, reentrant, visibility
  different: tryLock, timeout, interruptible, conditions, fairness

Reentrant meaning:
  Same thread can acquire lock multiple times
  Hold count tracks acquisitions
  Prevents self-deadlock

Lock pattern:
  lock.lock();
  try {
    // critical section
  } finally {
    lock.unlock();  // ALWAYS!
  }
```

---

## Bonus Q & A

**Q1: What is the difference between `synchronized` and `Lock`?**

**Q2: What is the difference between `wait()` and `sleep()`?**

**Q3: What is the difference between `notify()` and `notifyAll()`?**

**Q4: What is the difference between `volatile` and `AtomicInteger`?**

**Q5: What is the difference between `ConcurrentHashMap` and `HashMap`?**

**Q6: What is the difference between `CopyOnWriteArrayList` and `ArrayList`?**

**Q7: What is the difference between `CountDownLatch` and `CyclicBarrier`?**

**Q8: What is the difference between `ExecutorService` and `Thread`?**

**Q9: What is the difference between `submit()` and `execute()` on `ExecutorService`?**

**Q10: What is the difference between `invokeAll()` and `invokeAny()` on `ExecutorService`?**

---


