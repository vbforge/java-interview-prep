package com.vbforge.org.datatypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Q9  — The two main groups of data types.
 * Q10 — How to compare primitives and objects.
 * Q11 — Passing method arguments by value vs by reference.
 * Q12 — Object o = new Object(); — what is stored in o? Where does new Object() live?
 * Q13 — Boxing / unboxing.
 *
 * KEY POINTS:
 *
 *  Java has exactly two type categories:
 *   • Primitive types  — byte, short, int, long, float, double, char, boolean.
 *                        Stored directly on the stack (or inside objects on the heap).
 *                        Value semantics: assignment copies the value.
 *   • Reference types  — every class, interface, array, enum, record.
 *                        The variable holds a REFERENCE (pointer) to the object on the heap.
 *
 *  == compares VALUES:
 *    On primitives → compares the numeric value.
 *    On references → compares the memory addresses (are they the same object?).
 *
 *  Java is ALWAYS pass-by-value.
 *    For primitives: a copy of the value is passed → caller's variable is never changed.
 *    For references: a copy of the reference is passed → the object can be mutated,
 *    but reassigning the parameter inside the method does NOT affect the caller.
 *
 *  Autoboxing/unboxing: the compiler automatically converts between int ↔ Integer
 *  (and all other primitive/wrapper pairs). The Integer cache (-128..127) is a frequent
 *  interview trap for == comparisons.
 */
@Component
public class DataTypesDemo {

    private static final Logger log = LoggerFactory.getLogger(DataTypesDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q9 — Primitives vs reference types
    // ─────────────────────────────────────────────────────────────────────────

    public String runPrimitivesVsObjectsDemo() {
        log.debug("=== DATA TYPES: primitives vs objects (Q9) ===");

        // Primitive: the variable IS the value, stored directly on the stack.
        int primitiveA = 42;
        int primitiveB = primitiveA; // copies the VALUE
        primitiveB = 99;
        log.debug("primitiveA={} — unchanged after primitiveB=99", primitiveA);

        // Reference: the variable holds a pointer to a heap object.
        // Assigning copies the pointer, not the object.
        int[] arrayA = {1, 2, 3};
        int[] arrayB = arrayA;    // copies the REFERENCE (both point to same array)
        arrayB[0] = 999;
        log.debug("arrayA[0]={} — mutated via arrayB because they share the same object", arrayA[0]);

        // Null — only reference types can be null.
        // Assigning null to a primitive is a compile error.
        String nullable = null;
        log.debug("String nullable={} — reference types can be null; int cannot", nullable);

        return """
            Q9 — Two main type groups:

            PRIMITIVES (8 types):
              byte (8-bit), short (16-bit), int (32-bit), long (64-bit)
              float (32-bit IEEE 754), double (64-bit IEEE 754)
              char (16-bit Unicode), boolean

              ✓ Stored by VALUE — on the stack, or inlined inside objects
              ✓ No nullability — cannot be null
              ✓ No methods — no .equals(), no .hashCode()
              ✓ Lower memory, no GC overhead

            REFERENCE TYPES (everything else):
              All classes (String, Object, your own classes)
              Interfaces, arrays, enums, records

              ✓ Variable stores a REFERENCE (pointer) to a heap object
              ✓ Can be null
              ✓ Have methods — equals(), hashCode(), toString()…
              ✓ Participate in polymorphism, generics, Collections

            // JUNIOR NOTE: Primitives live on the stack when they are local variables.
            // When a primitive is a field of an object, it lives inside that object on the heap.
            // The "stack vs heap" split is about local variables vs object fields,
            // not strictly about primitive vs reference.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q10 — Comparison: == vs equals
    // ─────────────────────────────────────────────────────────────────────────

    public String runComparisonDemo() {
        log.debug("=== DATA TYPES: comparison (Q10) ===");

        // ── Primitives: == compares values ───────────────────────────────────
        int x = 5, y = 5;
        log.debug("int x==y: {} — primitive == compares VALUES", x == y);

        // ── References: == compares memory addresses ─────────────────────────
        String s1 = new String("hello"); // explicitly creates a new heap object
        String s2 = new String("hello"); // another new heap object
        log.debug("String ==  : {} — different objects even though content is equal", s1 == s2);
        log.debug("String .equals: {} — equals() compares content", s1.equals(s2));

        // ── String literals use the pool ─────────────────────────────────────
        String lit1 = "world";
        String lit2 = "world"; // same pool entry — same reference!
        log.debug("String literals ==: {} — both point to the same pool object", lit1 == lit2);

        // ── Wrapper caching trap ─────────────────────────────────────────────
        // JUNIOR NOTE: Integer caches values from -128 to 127.
        // Autoboxed integers in that range return the SAME cached object.
        // Outside that range a new object is created → == returns false.
        Integer cached1 = 100;  // within cache range → same object
        Integer cached2 = 100;
        Integer big1 = 200;     // outside cache range → new object each time
        Integer big2 = 200;
        log.debug("Integer 100 ==: {} (cached → same object)", cached1 == cached2);
        log.debug("Integer 200 ==: {} (outside cache → different objects)", big1 == big2);

        return String.format("""
            Q10 — How to compare primitives and objects:

            PRIMITIVES — always use ==:
              int x=5, y=5;  x==y → %s  (compares the VALUES directly)

            REFERENCES — == vs equals():
              new String("hello") == new String("hello")  → %s  (different heap addresses)
              new String("hello").equals(new String("hello")) → %s  (compares content)

            STRING POOL (literals):
              "world" == "world"  → %s  (compiler interns literals → same object)

            INTEGER CACHE TRAP:
              Integer 100 == Integer 100  → %s  (cached -128..127 → same object)
              Integer 200 == Integer 200  → %s  (outside cache → new objects)

            RULE OF THUMB:
              Compare primitives with ==.
              Compare objects with .equals().
              NEVER use == to compare Strings or wrapper types unless you
              explicitly need reference identity.
            """,
            x == y, s1 == s2, s1.equals(s2), lit1 == lit2,
            cached1 == cached2, big1 == big2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q11 — Pass-by-value
    // ─────────────────────────────────────────────────────────────────────────

    public String runPassByValueDemo() {
        log.debug("=== DATA TYPES: pass-by-value (Q11) ===");

        // ── Primitive: the value is COPIED into the parameter ─────────────────
        int num = 10;
        incrementPrimitive(num);                     // passes a COPY of 10
        log.debug("num after incrementPrimitive: {} — unchanged", num);

        // ── Reference: the REFERENCE is COPIED ───────────────────────────────
        // The method receives its own copy of the pointer.
        // It CAN mutate the object the pointer points to.
        // It CANNOT make the caller's variable point to a different object.
        int[] arr = {1, 2, 3};
        mutateArray(arr);                             // copy of reference passed
        log.debug("arr[0] after mutateArray: {} — object IS mutated", arr[0]);

        int[] arr2 = {1, 2, 3};
        reassignArray(arr2);                          // copy of reference passed
        log.debug("arr2[0] after reassignArray: {} — caller's arr2 unchanged", arr2[0]);

        return """
            Q11 — Java is ALWAYS pass-by-value:

            PRIMITIVES:
              void increment(int n) { n++; }
              int x = 10;
              increment(x);
              // x is still 10 — only the copy inside the method changed

            REFERENCES (the tricky part):
              The REFERENCE (pointer) is passed by value — a copy of the pointer.
              → You CAN mutate the object the pointer points to.
              → You CANNOT reassign the caller's variable to a different object.

              void mutate(int[] a) { a[0] = 999; }   // mutates the shared object ✓
              void reassign(int[] a) { a = new int[]{0}; }  // only local copy affected ✗

            SUMMARY:
              Java passes a copy of the value.
              For references that copy is a copy of the address.
              "Pass-by-reference" would mean the called method could change
              WHICH object the caller's variable points to. Java never does that.
            """;
    }

    private void incrementPrimitive(int n) { n++; }

    private void mutateArray(int[] a) { a[0] = 999; }

    private void reassignArray(int[] a) { a = new int[]{0, 0, 0}; }

    // ─────────────────────────────────────────────────────────────────────────
    // Q12 — Object o = new Object(); what is stored where?
    // ─────────────────────────────────────────────────────────────────────────

    public String runObjectOnHeapDemo() {
        log.debug("=== DATA TYPES: object on heap (Q12) ===");

        // Object o = new Object();
        //   ┌──────────────────────────────────────────────────────────┐
        //   │ STACK frame (this method)                                │
        //   │   o  →  [reference / pointer]  →────────────────────┐   │
        //   └────────────────────────────────────────────────────--|───┘
        //                                                          │
        //   ┌──────────────────────────────────────────────────────▼───┐
        //   │ HEAP                                                      │
        //   │   Object instance:                                        │
        //   │     mark word (GC state, identity hashCode, lock state)  │
        //   │     klass pointer (→ Object.class metadata in Metaspace)  │
        //   └───────────────────────────────────────────────────────────┘

        Object o = new Object();
        log.debug("o is a reference on the stack pointing to an Object on the heap");
        log.debug("Identity hashCode (from mark word): {}", System.identityHashCode(o));

        // When o goes out of scope, the reference on the stack disappears.
        // The heap object becomes unreachable and eligible for GC.

        return """
            Q12 — Object o = new Object():

            WHAT IS STORED IN 'o':
              o is a local variable in the current stack frame.
              It holds a REFERENCE (a memory address / pointer) —
              typically 4 bytes (compressed oops) or 8 bytes (uncompressed).
              NOT the object itself — just a pointer to it.

            WHERE DOES new Object() LIVE:
              On the HEAP — specifically in the Eden region of the Young Generation
              (with G1GC / other modern collectors).
              The heap is shared across all threads.

            ANATOMY OF A HEAP OBJECT:
              ┌──────────────────────────────────────────────────┐
              │ Object header (16 bytes on 64-bit JVM)           │
              │   mark word  : GC age, identity hashCode, lock   │
              │   klass ptr  : → class metadata in Metaspace      │
              ├──────────────────────────────────────────────────┤
              │ Instance fields (zero for plain Object)           │
              └──────────────────────────────────────────────────┘

            LIFECYCLE:
              1. new Object()  → JVM allocates on heap, zeros fields, runs constructor
              2. o goes out of scope → reference disappears from stack
              3. Object has no more references → eligible for GC
              4. GC runs → object reclaimed, memory returned to pool

            // JUNIOR NOTE: Local primitives (int, boolean…) live on the stack.
            // All objects, including arrays, always live on the heap.
            // The stack variable holds only the reference, never the object itself.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q13 — Boxing / unboxing
    // ─────────────────────────────────────────────────────────────────────────

    public String runBoxingDemo() {
        log.debug("=== DATA TYPES: boxing/unboxing (Q13) ===");

        // ── Autoboxing: int → Integer ─────────────────────────────────────────
        // JUNIOR NOTE: The compiler inserts Integer.valueOf(42) for you.
        // Integer.valueOf() uses a cache for -128..127, so no new allocation there.
        int primitive = 42;
        Integer boxed = primitive;   // compiler: Integer.valueOf(42)
        log.debug("Autoboxed: int {} → Integer {}", primitive, boxed);

        // ── Unboxing: Integer → int ───────────────────────────────────────────
        // The compiler inserts boxed.intValue() for you.
        int unboxed = boxed;          // compiler: boxed.intValue()
        log.debug("Unboxed: Integer {} → int {}", boxed, unboxed);

        // ── NullPointerException from unboxing null ───────────────────────────
        // JUNIOR NOTE: This is the most common boxing trap in production.
        // If a method returns Integer and can return null, unboxing it to int
        // will throw NullPointerException — not a NullPointerException on the method call
        // but on the invisible .intValue() the compiler inserted.
        Integer nullableInt = null;
        try {
            int willThrow = nullableInt; // compiler: nullableInt.intValue() → NPE!
        } catch (NullPointerException e) {
            log.warn("NullPointerException from unboxing null Integer → int");
        }

        // ── Performance cost: boxing in a tight loop ──────────────────────────
        // Each autoboxed value outside the cache is a heap allocation + GC pressure.
        long sumBoxed = 0L;
        long start = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            Long l = (long) i;   // allocates a new Long object each iteration (outside cache mostly)
            sumBoxed += l;
        }
        long boxedTime = System.nanoTime() - start;

        long sumPrimitive = 0L;
        start = System.nanoTime();
        for (int i = 0; i < 100_000; i++) {
            long l = i;          // no allocation — pure stack value
            sumPrimitive += l;
        }
        long primitiveTime = System.nanoTime() - start;

        log.debug("Loop sum check: boxed={} primitive={}", sumBoxed, sumPrimitive);
        log.debug("Boxed loop: {}µs  Primitive loop: {}µs", boxedTime / 1000, primitiveTime / 1000);

        return String.format("""
            Q13 — Boxing and unboxing:

            AUTOBOXING (primitive → wrapper):
              int i = 42;
              Integer boxed = i;   // compiler inserts: Integer.valueOf(42)
              valueOf() uses a cache for -128..127 — no heap allocation in that range.

            UNBOXING (wrapper → primitive):
              int unboxed = boxed;  // compiler inserts: boxed.intValue()

            WRAPPER TYPE PAIRS:
              byte→Byte, short→Short, int→Integer, long→Long
              float→Float, double→Double, char→Character, boolean→Boolean

            THE NULL TRAP:
              Integer nullable = null;
              int x = nullable;   // NPE! compiler inserts nullable.intValue()
              This is invisible — the NPE looks like it's on the assignment line.

            PERFORMANCE (100k iterations):
              Long loop (boxing)    : %d µs
              long loop (primitive) : %d µs
              Boxing creates heap objects → GC pressure. Avoid in tight loops.

            WHEN BOXING IS NECESSARY:
              Collections only accept objects — List<Integer>, not List<int>.
              Generic type parameters must be reference types.
              Nullable fields where absence of a value is meaningful.
            """, boxedTime / 1000, primitiveTime / 1000);
    }
}
