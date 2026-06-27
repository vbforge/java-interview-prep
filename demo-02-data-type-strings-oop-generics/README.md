# demo-02 — Data Types & Strings, OOP, Inheritance & Generics

- > **Theory files:** [02-data-types-strings.md](02-data-types-strings.md) · [03-oop-generics.md](03-oop-generics.md)
- > **Return to root README:** [java-interview-prep README](../README.md)
- > **[GitHub Pages site](https://vbforge.github.io/java-interview-prep)**
- > **Questions covered:** Q9–Q27
- > **Port:** 8082

Standalone Spring Boot module. No database, no Docker required.


## How to run

```bash
cd demos/demo-02-data-type-strings-oop-generics
mvn spring-boot:run
```

Then open: `http://localhost:8082/demo`

---

## Endpoints

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/datatypes/primitives-vs-objects` | Q9 | Two type groups: primitives vs references, value vs reference semantics |
| `GET /demo/datatypes/comparison` | Q10 | `==` vs `.equals()`, Integer cache trap (-128..127) |
| `GET /demo/datatypes/pass-by-value` | Q11 | Always pass-by-value — primitive copy vs reference copy |
| `GET /demo/datatypes/object-on-heap` | Q12 | Stack variable holds a reference; object lives on the heap |
| `GET /demo/datatypes/boxing` | Q13 | Autoboxing / unboxing, NPE from null Integer, performance cost |
| `GET /demo/strings/pool-vs-heap` | Q14 | Literal interning vs `new String()` heap bypass, compile-time folding |
| `GET /demo/strings/equality` | Q15 | `==` vs `.equals()`, null-safe `"literal".equals(var)` pattern |
| `GET /demo/strings/intern` | Q16 | `String.intern()` mechanics and deduplication use case |
| `GET /demo/strings/immutability` | Q17 | Why String is immutable, consequences, `StringBuilder` alternative |
| `GET /demo/oop/generics-intro` | Q18 | What generics are, type erasure, raw types |
| `GET /demo/oop/generics-examples` | Q19 | `Box<T>`, `Pair<A,B>`, bounded `T extends Number`, generic methods |
| `GET /demo/oop/wildcards` | Q20 | PECS — `? extends` (producer/read), `? super` (consumer/write) |
| `GET /demo/oop/abstract-vs-interface` | Q21 | State, constructor, concrete methods, multiple inheritance |
| `GET /demo/oop/inheritance-rules` | Q22–Q24 | One class / many interfaces, constructor rules, access modifiers |
| `GET /demo/oop/multiple-inheritance` | Q25–Q27 | Diamond problem, same-signature methods, default method conflict resolution |

---

## Key things to observe in the logs

**`/demo/datatypes/comparison`** — the Integer cache trap:
```
Integer 100 == Integer 100  → true   (cached → same object)
Integer 200 == Integer 200  → false  (outside cache → different objects)
```

**`/demo/datatypes/pass-by-value`** — mutation vs reassignment:
```
num after incrementPrimitive: 10  — unchanged (copy of value passed)
arr[0] after mutateArray: 999     — object IS mutated (shared reference)
arr2[0] after reassignArray: 1    — caller's variable unchanged (reassign only affects local copy)
```

**`/demo/datatypes/boxing`** — NPE from null unboxing:
```
NullPointerException — unboxing null Integer into int
```

**`/demo/strings/pool-vs-heap`** — literal vs new String():
```
lit1 == lit2: true    — same pool object
heap1 == heap2: false — different heap objects
"hel"+"lo" == lit1: true  — compile-time constant folding
runtime concat == lit1: false — runtime concat is a new heap object
```

**`/demo/oop/wildcards`** — PECS in action:
```
sumExtends(ints)=6.0     — reads List<Integer> as List<? extends Number>
sumExtends(doubles)=7.5  — same method, different subtype
addIntegers into List<Number>: [10, 20, 30]
```

---

## Key concepts cheat sheet

```
PRIMITIVES vs REFERENCES:
  Primitive  — value stored directly on stack; 8 types; no null; no methods
  Reference  — pointer on stack → object on heap; nullable; has methods

COMPARISON:
  primitives → always use ==  (compares values)
  objects    → always use .equals()  (== compares addresses)
  Integer cache: -128..127 cached → == returns true; outside → == returns false

PASS-BY-VALUE:
  Java always passes a COPY — of the value (primitive) or of the reference (object).
  Mutating the object via a copied reference works.
  Reassigning the parameter inside the method does NOT affect the caller.

STRING:
  "literal"        → String Pool (interned automatically, shared)
  new String("x")  → always new heap object (avoid)
  String.intern()  → manual pool lookup/add
  Immutable because byte[] is private final — every "change" returns a new String
  Consequences: thread-safe, safe map key, pool possible, hashCode cached

GENERICS:
  Compile-time type safety; type erased to Object at runtime
  <T extends Foo>  — bounded parameter; only Foo and subtypes allowed
  <? extends T>    — wildcard: read-only producer (PECS: Extends = Producer)
  <? super T>      — wildcard: write-only consumer (PECS: Super = Consumer)

ABSTRACT CLASS vs INTERFACE:
  Abstract: state + constructor + any modifiers + ONE superclass
  Interface: constants + abstract/default/static methods + MANY implementations
  Diamond problem: two default methods with same signature → MUST override,
                   use InterfaceName.super.method() to delegate explicitly
```

---

## Project structure

```
demo-02-data-type-strings-oop-generics/
├── pom.xml
├── README.md
└── src/main/java/com/vbforge/demo02/
    ├── MainApp.java
    ├── config/
    │   └── DemoController.java          ← all endpoints
    ├── datatypes/
    │   └── DataTypesDemo.java           ← Q9–Q13
    ├── strings/
    │   └── StringsDemo.java             ← Q14–Q17
    └── oop/
        └── OopDemo.java                 ← Q18–Q27
```

---
