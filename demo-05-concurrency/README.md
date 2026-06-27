# demo-05 — Concurrency

- > **Theory file:** [07-concurrency.md](07-concurrency.md)
- > **Return to root README:** [java-interview-prep README](../README.md)
- > **[GitHub Pages site](https://vbforge.github.io/java-interview-prep)**
- > **Questions covered:** Q53–Q63
- > **Port:** 8085
  
Standalone Spring Boot module. No database, no Docker required.

---

## How to run

```bash
cd demos/demo-05-concurrency
mvn spring-boot:run
```

Then open: `http://localhost:8085/demo`

---

## Endpoints

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/race/data-race` | Q53–Q55 | Three counters in parallel: broken (plain int), volatile (still broken), safe (AtomicInteger) |
| `GET /demo/race/volatile` | Q55, Q56 | Correct use of volatile (stop flag) vs broken use (i++ still races) |
| `GET /demo/atomic/cas` | Q57 | Manual CAS loop, AtomicReference swap, CAS vs synchronized explanation |
| `GET /demo/atomic/vs-synchronized` | Q57 | Live benchmark: AtomicInteger vs synchronized vs LongAdder |
| `GET /demo/atomic/when-to-use` | Q58 | Decision guide: single variable → atomic, multiple variables → lock |
| `GET /demo/locks/reentrant-meaning` | Q59 | holdCount proof (1→2→3→2→1→0), recursive factorial, fair vs non-fair |
| `GET /demo/locks/unlock-in-finally` | Q60 | Simulated exception: wrong pattern (lock stuck) vs correct finally pattern |
| `GET /demo/locks/synchronized-vs-lock` | Q61 | Both enforce mutual exclusion + happens-before; 4 threads on one account |
| `GET /demo/locks/reentrantlock-extras` | Q62 | tryLock, tryLock(timeout), lockInterruptibly, multiple Condition queues |
| `GET /demo/locks/synchronized-reentry` | Q63 | outer() → inner() on same monitor; no deadlock because synchronized is reentrant |

---

## Key things to observe in the logs

**`/demo/race/data-race`** — the race is real, result changes each run:
```
Broken counter result:   97,341  (expected 100,000) ← lost 2,659 updates
Volatile counter result: 98,109  (expected 100,000) ← volatile ≠ atomic
Atomic counter result:  100,000  (expected 100,000) ← always correct ✓
```

**`/demo/locks/reentrant-meaning`** — holdCount incrementing and decrementing:
```
Acquired lock    — holdCount=1
Re-acquired lock — holdCount=2
Re-acquired lock — holdCount=3
Unlocked once    — holdCount=2
Unlocked twice   — holdCount=1
Unlocked thrice  — holdCount=0 (fully released)
```

**`/demo/locks/unlock-in-finally`** — wrong vs correct pattern:
```
WRONG pattern: exception thrown, unlock() was skipped   ← lock stuck!
CORRECT pattern: exception caught, finally will still unlock
CORRECT pattern: lock released in finally? true ✓
```

**`/demo/locks/reentrantlock-extras`** — tryLock returns immediately:
```
tryLock() while lock held: false — did not block ✓
tryLock(50ms): false — timed out after 50ms ✓
lockInterruptibly() interrupted — thread escaped the wait ✓
```

**`/demo/locks/synchronized-reentry`** — no deadlock on nested calls:
```
outer() — monitor acquired (holdCount would be 1)
inner() — monitor re-entered (holdCount would be 2)
Reentrant synchronized result: 42 ✓
```

---

## Key concepts cheat sheet

```
DATA RACE:
  Two threads access the same variable concurrently.
  At least one is a write. No synchronisation. → undefined behaviour.

i++ IS THREE STEPS (not atomic):
  READ current value → ADD 1 → WRITE back
  Two threads can interleave these → lost updates.

volatile:
  Guarantees VISIBILITY  — writes flush to main memory immediately.
  Does NOT guarantee ATOMICITY — i++ still races.
  Correct use: a boolean stop flag (single write, read-many).

CAS (Compare-And-Swap):
  Single CPU instruction: if (current == expected) { current = newValue; }
  AtomicInteger.incrementAndGet() spins on CAS until it wins.
  No thread is ever blocked → lock-free, very fast under low contention.

ATOMICS vs LOCKS:
  AtomicInteger/Long/Reference → single variable, low contention
  LongAdder                    → single counter, very high contention
  synchronized / ReentrantLock → multiple variables must change together

REENTRANT LOCK:
  holdCount tracks how many times the owning thread acquired it.
  lock() → holdCount++    unlock() → holdCount--
  Released only when holdCount reaches 0.
  Both synchronized and ReentrantLock are reentrant.

unlock() MUST be in finally:
  Exception without finally → unlock() skipped → lock held forever → deadlock.
  Canonical pattern:
    lock.lock();
    try { ... } finally { lock.unlock(); }

ReentrantLock EXTRAS over synchronized:
  tryLock()                — non-blocking attempt
  tryLock(n, unit)         — bounded wait
  lockInterruptibly()      — waiting thread can be interrupted
  newCondition()           — multiple wait queues per lock
  new ReentrantLock(true)  — fair FIFO acquisition
```

---

## Project structure

```
demo-05-concurrency/
├── pom.xml
├── README.md
└── src/main/java/com/vbforge/concurrency/
    ├── MainApp.java
    ├── config/
    │   └── DemoController.java          ← all endpoints
    ├── race/
    │   └── RaceConditionDemo.java       ← Q53–Q56
    ├── atomic/
    │   └── AtomicDemo.java              ← Q57–Q58
    └── locks/
        └── LocksDemo.java               ← Q59–Q63
```

---
