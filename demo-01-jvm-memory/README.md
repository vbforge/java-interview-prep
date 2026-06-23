# demo-01 — JVM Memory

> **Theory file:** [theory: 01-jvm-memory](01-jvm-memory.md)
> **Questions covered:** Q1–Q8
> **Port:** 8081

This is a standalone Spring Boot application. No database, no Docker required.
Its only job is to make the JVM memory model **visible** — through log output,
HTTP responses, and live Actuator metrics.

---

## How to run

### Option A — IntelliJ IDEA (recommended for GC log visibility)

1. Open the module in IDEA.
2. Go to **Run → Edit Configurations → Modify options → Add VM options**.
3. Paste the JVM flags below.
4. Run `MainApp`.

### Option B — Maven

```bash
cd demos/demo-01-jvm-memory
 
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Xms64m -Xmx256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xlog:gc*:file=logs/gc.log:time,uptime,level,tags:filecount=3,filesize=5m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/heap-dump.hprof"
  
```

### Recommended JVM flags (copy-paste block)

```
-Xms64m
-Xmx256m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-Xlog:gc*:file=logs/gc.log:time,uptime,level,tags:filecount=3,filesize=5m
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=logs/heap-dump.hprof
```

| Flag                              | Purpose                                                            |
|-----------------------------------|--------------------------------------------------------------------|
| `-Xms64m`                         | Initial heap size — JVM starts with 64 MB                          |
| `-Xmx256m`                        | Max heap — kept small so GC fires quickly and you can observe it   |
| `-XX:+UseG1GC`                    | Explicit G1 (it's the default on Java 9+, but explicit is clearer) |
| `-XX:MaxGCPauseMillis=200`        | G1 pause-time target — G1 tries to stay under 200 ms               |
| `-Xlog:gc*:file=logs/gc.log`      | Unified GC logging to file (Java 9+ syntax)                        |
| `-XX:+HeapDumpOnOutOfMemoryError` | Write heap dump on OOM — needed for `/demo/oom`                    |
| `-XX:HeapDumpPath=logs/`          | Where to write the dump                                            |

Make sure `logs/` exists before running:

```bash
mkdir -p logs
```

---

## Endpoints

After startup, the index is at:

```
GET http://localhost:8081/demo
```

| Endpoint                                | Theory Q | What to observe                                                   |
|-----------------------------------------|----------|-------------------------------------------------------------------|
| `GET /demo/stack/call-chain`            | Q2       | Log output shows frame depth and local variable lifecycle         |
| `GET /demo/stack/overflow`              | Q2       | `StackOverflowError` caught and returned — not an OOM             |
| `GET /demo/stack/pass-by-value`         | Q2       | Primitive unchanged, array element mutated — proves pass-by-value |
| `GET /demo/heap/short-lived?rounds=500` | Q3, Q7   | Eden fills up → Minor GC fires → heap shrinks back                |
| `GET /demo/heap/long-lived?chunks=20`   | Q3       | Objects promoted to Old Gen — heap stays high                     |
| `GET /demo/heap/clear`                  | Q3       | Old Gen released — watch heap drop in gc.log                      |
| `GET /demo/heap/humongous`              | Q3, Q8   | 4 MB object bypasses Eden, goes to Humongous region               |
| `GET /demo/heap/primitive-vs-boxed`     | Q7       | Primitive loop: no GC; boxed loop: GC pressure visible            |
| `GET /demo/gc/reachability`             | Q6       | Cyclic reference collected correctly — no leak                    |
| `GET /demo/gc/references`               | Q6       | Weak ref cleared after GC hint; Soft ref survives                 |
| `GET /demo/gc/pressure?mb=100`          | Q4       | 100 MB allocated and released — several Minor GCs in gc.log       |
| `GET /demo/metaspace/stats`             | Q3       | Live Metaspace usage, class counts, heap vs non-heap              |
| `GET /demo/oom`                         | Q1       | `OutOfMemoryError: Java heap space` — check `logs/` for dump      |

---

## Reading gc.log

After running `/demo/gc/pressure` or `/demo/heap/short-lived`, open `logs/gc.log`.
A typical G1 Minor GC line looks like:

```
[1.234s][info][gc] GC(5) Pause Young (Normal) (G1 Evacuation Pause) 78M->21M(256M) 3.456ms
```

| Part                  | Meaning                                                      |
|-----------------------|--------------------------------------------------------------|
| `1.234s`              | JVM uptime when GC started                                   |
| `GC(5)`               | 5th GC event                                                 |
| `Pause Young`         | Only Young Generation collected (Minor GC)                   |
| `G1 Evacuation Pause` | G1 moved live objects out of Eden/Survivor into Survivor/Old |
| `78M->21M(256M)`      | Heap used before → after (total heap capacity)               |
| `3.456ms`             | Stop-the-world pause — all threads paused for this duration  |

A Mixed GC (Old Generation also collected) looks like:

```
[4.567s][info][gc] GC(12) Pause Young (Mixed) (G1 Evacuation Pause) 180M->45M(256M) 8.901ms
```

---

## Actuator metrics

With the app running, useful metrics to check:

```bash
# Overall heap usage
curl http://localhost:8081/actuator/metrics/jvm.memory.used?tag=area:heap

# GC pause time (G1 Young and Mixed)
curl http://localhost:8081/actuator/metrics/jvm.gc.pause

# Specific memory pool — G1 Eden Space
curl "http://localhost:8081/actuator/metrics/jvm.memory.used?tag=id:G1%20Eden%20Space"

# Class loading
curl http://localhost:8081/actuator/metrics/jvm.classes.loaded
```

Or open `/actuator/metrics` in a browser to see all available metric names.

---

## Suggested learning sequence

1. Read `theory: 01-jvm-memory.md` Q1–Q8 first.
2. Run the app with GC logging flags.
3. Hit each endpoint in theory-file order (stack → heap → gc → metaspace).
4. For each endpoint: read the response, check the logs, map back to the theory answer.
5. Rate yourself on the scorecard.

---

## Project structure

```
demo-01-jvm-memory/
├── pom.xml
├── README.md
├── logs/                                     ← gc.log and heap dump go here
└── src/main/
    ├── java/com/vbforge/jvmmemory/
    │   ├── MainApp.java                       ← entry point
    │   ├── config/
    │   │   └── DemoController.java            ← all REST endpoints
    │   ├── stack/
    │   │   └── StackDemo.java                 ← Q2
    │   ├── heap/
    │   │   └── HeapDemo.java                  ← Q3, Q7
    │   ├── gc/
    │   │   └── GcDemo.java                    ← Q4, Q5, Q6
    │   └── metaspace/
    │       └── MetaspaceDemo.java             ← Q3 (Metaspace)
    └── resources/
        └── application.yml
```
