package com.vbforge.concurrency.config;

import com.vbforge.concurrency.atomic.AtomicDemo;
import com.vbforge.concurrency.locks.LocksDemo;
import com.vbforge.concurrency.race.RaceConditionDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * All demo-05-concurrency REST endpoints.
 *
 * Base URL: http://localhost:8085/demo
 *
 * ┌──────────────────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                          │ Q       │ What it shows                      │
 * ├──────────────────────────────────────────────────────────────────────────────────┤
 * │ GET /demo/race/data-race          │ Q53–Q55 │ Live race: broken/volatile/atomic  │
 * │ GET /demo/race/volatile           │ Q55,Q56 │ volatile: visibility vs atomicity  │
 * │ GET /demo/atomic/cas              │ Q57     │ CAS loop, AtomicReference swap     │
 * │ GET /demo/atomic/vs-synchronized  │ Q57     │ Benchmark: Atomic/sync/LongAdder   │
 * │ GET /demo/atomic/when-to-use      │ Q58     │ Decision guide atomics vs locks    │
 * │ GET /demo/locks/reentrant-meaning │ Q59     │ holdCount proof, recursive lock    │
 * │ GET /demo/locks/unlock-in-finally │ Q60     │ Deadlock from missing finally      │
 * │ GET /demo/locks/synchronized-vs-lock │ Q61  │ Shared behaviour: mutex + HB       │
 * │ GET /demo/locks/reentrantlock-extras │ Q62  │ tryLock, timeout, interrupt, Cond  │
 * │ GET /demo/locks/synchronized-reentry │ Q63  │ Same thread re-enters own monitor  │
 * └──────────────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final RaceConditionDemo raceDemo;
    private final AtomicDemo        atomicDemo;
    private final LocksDemo         locksDemo;

    public DemoController(RaceConditionDemo raceDemo,
                          AtomicDemo atomicDemo,
                          LocksDemo locksDemo) {
        this.raceDemo   = raceDemo;
        this.atomicDemo = atomicDemo;
        this.locksDemo  = locksDemo;
    }

    // ── Race / volatile ──────────────────────────────────────────────────────

    @GetMapping("/race/data-race")
    public ResponseEntity<String> dataRace() throws InterruptedException {
        return ResponseEntity.ok(raceDemo.runDataRaceDemo());
    }

    @GetMapping("/race/volatile")
    public ResponseEntity<String> volatileDemo() throws InterruptedException {
        return ResponseEntity.ok(raceDemo.runVolatileDemo());
    }

    // ── Atomics ──────────────────────────────────────────────────────────────

    @GetMapping("/atomic/cas")
    public ResponseEntity<String> cas() {
        return ResponseEntity.ok(atomicDemo.runCasDemo());
    }

    @GetMapping("/atomic/vs-synchronized")
    public ResponseEntity<String> vsSynchronized() throws InterruptedException {
        return ResponseEntity.ok(atomicDemo.runVsSynchronizedDemo());
    }

    @GetMapping("/atomic/when-to-use")
    public ResponseEntity<String> whenToUse() {
        return ResponseEntity.ok(atomicDemo.runWhenToUseDemo());
    }

    // ── Locks ────────────────────────────────────────────────────────────────

    @GetMapping("/locks/reentrant-meaning")
    public ResponseEntity<String> reentrantMeaning() throws InterruptedException {
        return ResponseEntity.ok(locksDemo.runReentrantMeaningDemo());
    }

    @GetMapping("/locks/unlock-in-finally")
    public ResponseEntity<String> unlockInFinally() {
        return ResponseEntity.ok(locksDemo.runUnlockInFinallyDemo());
    }

    @GetMapping("/locks/synchronized-vs-lock")
    public ResponseEntity<String> synchronizedVsLock() throws InterruptedException {
        return ResponseEntity.ok(locksDemo.runSynchronizedVsLockSharedDemo());
    }

    @GetMapping("/locks/reentrantlock-extras")
    public ResponseEntity<String> reentrantLockExtras() throws InterruptedException {
        return ResponseEntity.ok(locksDemo.runReentrantLockExtrasDemo());
    }

    @GetMapping("/locks/synchronized-reentry")
    public ResponseEntity<String> synchronizedReentry() {
        return ResponseEntity.ok(locksDemo.runSynchronizedReentryDemo());
    }

    // ── Index ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("""
            demo-05-concurrency — available endpoints:

              RACE CONDITIONS (Q53–Q56):
              GET /demo/race/data-race           Live race: broken/volatile/atomic counters side-by-side
              GET /demo/race/volatile            volatile: stop-flag (correct) vs i++ (still broken)

              ATOMICS (Q57–Q58):
              GET /demo/atomic/cas               CAS loop internals, AtomicReference swap
              GET /demo/atomic/vs-synchronized   Benchmark: AtomicInteger vs synchronized vs LongAdder
              GET /demo/atomic/when-to-use       Decision guide: atomics vs locks

              LOCKS (Q59–Q63):
              GET /demo/locks/reentrant-meaning  holdCount proof, recursive factorial, fair lock
              GET /demo/locks/unlock-in-finally  Deadlock from skipped unlock, correct finally pattern
              GET /demo/locks/synchronized-vs-lock  What they share: mutex + happens-before
              GET /demo/locks/reentrantlock-extras  tryLock, timed, interruptible, Condition queues
              GET /demo/locks/synchronized-reentry  Same thread re-entering its own monitor (Q63)
            """);
    }
}
