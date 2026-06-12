package com.vbforge.jvmmemory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo-01 — JVM Memory
 *
 * Entry point. @SpringBootApplication enables component scanning,
 * auto-configuration, and @Configuration support in one annotation.
 *
 * After startup, explore the demo via:
 *   GET /demo/stack       → Q2  stack frames & local variable lifecycle
 *   GET /demo/heap        → Q3  Young/Old generation pressure
 *   GET /demo/gc          → Q4-Q6 trigger minor/major GC, watch gc.log
 *   GET /demo/metaspace   → Q3  class loading into Metaspace
 *   GET /demo/oom         → deliberate OOM — see -XX:HeapDumpOnOutOfMemoryError
 *   GET /actuator/metrics → live heap, GC pause, memory pool metrics
 */
@SpringBootApplication
public class MainApp {

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
