package com.vbforge.concurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo-05 — Concurrency
 *
 * After startup explore the demo via:
 *   GET /demo                               → index of all endpoints
 *
 *   GET /demo/race/data-race               → Q53,Q54,Q55  live race condition, lost updates
 *   GET /demo/race/volatile                → Q55,Q56       volatile visibility vs atomicity
 *
 *   GET /demo/atomic/cas                   → Q57   CAS inside AtomicInteger
 *   GET /demo/atomic/vs-synchronized       → Q57   CAS benchmark vs synchronized
 *   GET /demo/atomic/when-to-use           → Q58   atomics vs locks decision guide
 *
 *   GET /demo/locks/reentrant-meaning      → Q59   what reentrant means with proof
 *   GET /demo/locks/unlock-in-finally      → Q60   why unlock must be in finally
 *   GET /demo/locks/synchronized-vs-lock   → Q61   what they share — mutual exclusion
 *   GET /demo/locks/reentrantlock-extras   → Q62   tryLock, timed lock, interruptible, conditions
 *   GET /demo/locks/synchronized-reentry  → Q63   same thread re-entering its own monitor
 */

@SpringBootApplication
public class MainApp {

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

}
