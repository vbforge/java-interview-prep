# 01 — JVM Memory

> **Questions covered:** Q1–Q8
> **Demo:** [demo-01-jvm-memory](../demos/demo-01-jvm-memory)
> **Sections that can't be skipped** per recruiter screen ✓

---

## Q1 — What kinds of memory exist in Java? What is the difference between them? Which is used when?

**Short answer**

The JVM divides memory into two main areas: the **stack** and the **heap**. The stack holds method call frames and local variables; the heap holds all objects. Beyond those two, the JVM also uses **Metaspace** (class metadata), the **Code Cache** (JIT-compiled native code), and **native/off-heap memory** (used by NIO buffers, for example).

**In depth**

| Area | What lives there | Lifecycle |
|------|-----------------|-----------|
| Stack | Primitive local variables, references (not the objects they point to), method call frames | Created per thread; a frame is pushed on each method call and popped on return |
| Heap | All objects created with `new`, their instance fields | From `new` until the GC determines no references remain |
| Metaspace (Java 8+) | Class definitions, method bytecode, static fields | Lives as long as the ClassLoader that loaded the class |
| Code Cache | Machine code produced by the JIT compiler | Managed by the JVM; old code is evicted when the cache is full |
| Off-heap | `ByteBuffer.allocateDirect()`, memory-mapped files | Managed manually or via `Cleaner`; not subject to GC pauses |

> **// JUNIOR NOTE:** A common mistake is thinking the *object* lives on the stack because the *variable* is declared inside a method. The variable (a reference — just an address) lives on the stack. The object it points to always lives on the heap. The only exception is scalar replacement by the JIT after escape analysis, but that is a JVM optimisation, not something you control.

---

## Q2 — What is the stack for, and what is stored in it?

**Short answer**

The stack tracks the execution of method calls. Each thread has its own stack. When a method is called, the JVM pushes a new **stack frame** onto that thread's stack. When the method returns, the frame is popped. If the call depth exceeds the stack limit you get `StackOverflowError`.

**In depth**

Each stack frame contains:

- **Local variable array** — all primitive variables declared in the method (`int`, `long`, `boolean`, etc.) and all reference variables (i.e. the pointer, not the object).
- **Operand stack** — a working area the JVM uses to evaluate expressions (like a scratch pad for bytecode instructions).
- **Reference to the runtime constant pool** — so the method can resolve symbolic names.

```java
void example() {
    int x = 42;           // x lives on the stack (primitive)
    String s = "hello";   // the reference s lives on the stack;
                          // the String object lives on the heap
    helper(x);            // pushes a new frame; x is copied by value
}
```

The stack is **thread-local** — no two threads share a stack, which is why local variables are inherently thread-safe.

> **// JUNIOR NOTE:** Interviewers sometimes ask "where does a local `int` live vs a field `int`?" Local primitive → stack. Instance field primitive → heap (inside the object). Static field primitive → Metaspace (inside the class object).

---

## Q3 — What regions is the heap divided into?

**Short answer**

The heap is split into **Young Generation** (Eden + two Survivor spaces) and **Old Generation** (Tenured). Class metadata lives in **Metaspace**, which is technically off-heap but logically part of the memory model discussion.

**In depth**

```
Heap
├── Young Generation
│   ├── Eden          ← new objects are allocated here
│   ├── Survivor S0   ← objects that survived at least one Minor GC
│   └── Survivor S1   ← only one Survivor is active at a time
└── Old Generation (Tenured)
    └── long-lived objects promoted from Young
```

**Why this split exists — the generational hypothesis:**
Most objects die young. By collecting the small, frequently-dying Young Generation quickly (Minor GC) rather than scanning the entire heap every time, the GC can keep pause times short. Only objects that survive enough Minor GC cycles get promoted to the Old Generation and are collected less frequently (Major / Full GC).

**Promotion threshold:** controlled by `-XX:MaxTenuringThreshold` (default 15). After surviving that many Minor GCs, an object is promoted to Old.

**Metaspace** (replaced PermGen in Java 8): stores class metadata, interned strings (moved to heap in Java 7+), and static variables. It grows dynamically in native memory by default; you can cap it with `-XX:MaxMetaspaceSize`.

> **// JUNIOR NOTE:** PermGen no longer exists as of Java 8. If you see `OutOfMemoryError: PermGen space` that is a Java 7 or earlier application. On Java 8+ the equivalent is `OutOfMemoryError: Metaspace`.

---

## Q4 — Explain garbage collection in Java.

**Short answer**

Garbage collection is the automatic process of identifying objects on the heap that are no longer reachable from any live thread and reclaiming their memory. The programmer does not call `free()` — the GC runs periodically, finds unreachable objects, and reclaims their space.

**In depth**

The GC process has two logical phases:

**1. Mark** — starting from a set of **GC roots** (local variables on all thread stacks, static fields, JNI references), the GC traverses the object graph and marks every reachable object.

**2. Sweep / Compact** — unmarked objects are declared garbage. Depending on the collector, memory is either swept in place (leaving gaps) or compacted (live objects are moved together to eliminate fragmentation).

**GC roots** — the starting points of reachability:
- Local variables and method parameters on live thread stacks
- Static fields of loaded classes
- References held by JNI native code
- Objects referenced by synchronisation monitors

**Minor GC** — collects only the Young Generation. Fast, happens frequently.  
**Major GC** — collects the Old Generation. Slower, less frequent.  
**Full GC** — collects the entire heap including Metaspace. Can cause a noticeable pause; something you want to minimise in production.

> **// JUNIOR NOTE:** You cannot force a GC with `System.gc()` — that is only a *hint* to the JVM and may be ignored entirely (and usually should be). Never rely on it in production code.

---

## Q5 — Which garbage collectors do you know?

**Short answer**

The main collectors in modern Java are **Serial**, **Parallel**, **CMS** (deprecated), **G1** (default since Java 9), and **ZGC** / **Shenandoah** (low-latency, available from Java 11/15+).

**In depth**

| Collector | Flag | Strengths | Weaknesses | Use case |
|-----------|------|-----------|------------|----------|
| Serial | `-XX:+UseSerialGC` | Simple, low overhead | Stop-the-world pauses on full heap | Single-core, tiny heaps, CLI tools |
| Parallel (Throughput) | `-XX:+UseParallelGC` | High throughput, uses all cores | Longer pauses acceptable | Batch jobs, offline processing |
| CMS | `-XX:+UseConcMarkSweepGC` | Concurrent, low pause | Fragmentation, deprecated Java 9, removed Java 14 | Legacy apps |
| G1 | `-XX:+UseG1GC` | Balanced pause/throughput, region-based | Some concurrent overhead | General-purpose, default since Java 9 |
| ZGC | `-XX:+UseZGC` | Sub-millisecond pauses, scales to TB heaps | Higher CPU overhead | Latency-sensitive services |
| Shenandoah | `-XX:+UseShenandoahGC` | Similar to ZGC, Red Hat maintained | Not in all JDK distributions | Latency-sensitive services |

> **// JUNIOR NOTE:** In most Spring Boot microservice interviews the answer they want is "G1 is the default and I know roughly how it works." You do not need to memorise all flags, but you should be able to say why you might switch away from G1 (e.g. to ZGC for a latency-critical service).

---

## Q6 — How does the collector know an object has become garbage?

**Short answer**

An object is garbage when it is **not reachable** from any GC root. The GC uses a **mark-and-trace** (reachability) algorithm, not reference counting, so cyclic references are handled correctly.

**In depth**

**Reachability, not reference counting.**
Some languages (Python, Objective-C) use reference counting: when the count hits zero, the object is freed. Java does not. It uses a full reachability trace from GC roots, which correctly handles cycles:

```java
class Node { Node next; }

Node a = new Node();
Node b = new Node();
a.next = b;
b.next = a;   // cycle
a = null;
b = null;
// a and b reference each other but are unreachable from any root.
// A reference-counting collector would leak them.
// Java's GC collects them correctly.
```

**Reachability levels** (from `java.lang.ref`):

| Level | Class | Collected when | Typical use |
|-------|-------|---------------|-------------|
| Strong | Normal reference | Never while reachable | Everyday objects |
| Soft | `SoftReference` | When JVM needs memory | Caches |
| Weak | `WeakReference` | Next GC cycle when no strong refs | `WeakHashMap`, listeners |
| Phantom | `PhantomReference` | After finalization | Cleanup/off-heap resource tracking |

> **// JUNIOR NOTE:** `finalize()` is deprecated since Java 9 and removed in Java 18. Do not use it. For cleanup logic use `try-with-resources` and `AutoCloseable` instead.

---

## Q7 — Do primitives need to be collected?

**Short answer**

**No** — primitives that are local variables live on the stack and are automatically discarded when their method frame is popped. No GC involvement needed.

**In depth**

Three places a primitive can live, and what happens to it:

| Where | Example | How memory is reclaimed |
|-------|---------|------------------------|
| Local variable in a method | `int x = 5;` | Stack frame is popped when method returns — instant, no GC |
| Instance field of an object | `class Foo { int x; }` | Lives inside the object on the heap; reclaimed when the *object* is GC'd |
| Static field | `static int COUNT;` | Lives in Metaspace; reclaimed only when the class is unloaded |

The GC collects *objects* (heap-allocated). Primitives are not objects — they have no header, no identity, no reference. When a primitive is a field, it is part of the object's memory block and is freed as a unit with the object, not tracked individually.

**Autoboxed primitives are a different story:**

```java
Integer i = 42;  // autoboxed — an Integer object IS on the heap and WILL be GC'd
int    j = 42;   // plain primitive — stack or field, never individually GC'd
```

> **// JUNIOR NOTE:** This is why autoboxing in tight loops (e.g. using `Integer` instead of `int` as a counter) creates GC pressure. Prefer primitive streams (`IntStream`, `LongStream`) over `Stream<Integer>` for performance-critical code.

---

## Q8 — How does G1 work?

**Short answer**

G1 (Garbage-First) divides the heap into many equal-sized **regions** (typically 1–32 MB each) instead of one contiguous Young/Old area. It predicts which regions contain the most garbage and collects those first — hence "Garbage-First" — while trying to meet a configurable pause-time target.

**In depth**

**Region layout:**

```
Heap (e.g. 4 GB)
┌────┬────┬────┬────┬────┬────┬────┬────┐
│ E  │ E  │ S  │ O  │ O  │ H  │ E  │ O  │  ...  (2048 regions total)
└────┴────┴────┴────┴────┴────┴────┴────┘
  E = Eden   S = Survivor   O = Old   H = Humongous (large objects)
```

Regions are dynamically assigned roles — a region that was Old can become Eden after collection. There are no fixed generational boundaries in the heap layout.

**Collection phases:**

1. **Young-only GC (Minor)** — collects Eden and Survivor regions. Stop-the-world but short, because only Young regions are in scope.

2. **Concurrent Marking** — runs alongside the application (mostly). Builds a live-object map of the Old regions without a long pause.
   - Initial Mark (STW, piggybacks on a Minor GC)
   - Root Region Scan
   - Concurrent Mark
   - Remark (STW, short)
   - Cleanup (STW, short + concurrent)

3. **Mixed GC** — after marking completes, G1 collects all Young regions *plus* a selection of Old regions that are most full of garbage (highest garbage-to-live ratio). This is the "Garbage-First" selection.

4. **Full GC** — last resort, single-threaded (parallel since Java 10), compacts the entire heap. Indicates the heap is too small or the mixed GC cannot keep up.

**Key tuning knob:** `-XX:MaxGCPauseMillis=200` (default 200 ms). G1 adjusts the number of regions it collects per cycle to try to stay within this target. It is a *goal*, not a hard guarantee.

**Humongous objects:** Objects larger than 50% of a region are allocated directly in one or more contiguous Old regions. They are collected during the Cleanup phase and can be a source of fragmentation if the heap has many of them.

> **// JUNIOR NOTE:** The most common interview follow-up is "what would you change if you were seeing long GC pauses?" Good answer: increase heap size (`-Xmx`), lower `-XX:MaxGCPauseMillis` (G1 collects less per cycle), or switch to ZGC if latency is the primary concern. Never say "I'd call `System.gc()`."

---

## Quick-reference cheat sheet

```
Stack       → per thread, method frames, local primitives + references, StackOverflowError
Heap        → all objects, divided into Young (Eden + S0 + S1) and Old
Metaspace   → class metadata, static fields, grows in native memory
GC roots    → thread stacks, static fields, JNI refs, monitor refs
Reachable   → has a path from a GC root → NOT garbage
Unreachable → no path from any GC root → garbage → collected

Collectors (know these three for most interviews):
  Parallel  → max throughput, batch jobs
  G1        → default, balanced, region-based, pause-time target
  ZGC       → sub-ms pauses, latency-critical services

G1 key ideas:
  - heap split into ~2048 equal regions (~1–32 MB each)
  - regions assigned Eden / Survivor / Old / Humongous roles dynamically
  - concurrent marking + mixed GC = collects highest-garbage Old regions first
  - tuning knob: -XX:MaxGCPauseMillis (default 200 ms)
```

---

*Next: [02-data-types-strings.md](02-data-types-strings.md) — Q9–Q17*
