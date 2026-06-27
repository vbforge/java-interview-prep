package com.vbforge.org.oop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Q18 — What are generics, and what are they for?
 * Q19 — Examples of generics in Java.
 * Q20 — extends / super (bounded wildcards, PECS).
 * Q21 — Difference between an abstract class and an interface.
 * Q22 — How many supertypes you can extend / implement.
 * Q23 — Presence of a constructor.
 * Q24 — Default access modifiers of abstract class and interface.
 * Q25 — Multiple inheritance with interfaces.
 * Q26 — What if two interface methods overlap (same signature from two interfaces)?
 * Q27 — What if two default methods in interfaces conflict?
 *
 * KEY POINTS:
 *
 *  Generics — compile-time type safety without casting. Type parameter <T> is erased
 *  at runtime (type erasure) — the bytecode uses Object internally.
 *
 *  Bounded wildcards follow PECS:
 *    Producer Extends  → <? extends T>  read from a source safely
 *    Consumer Super    → <? super T>    write into a target safely
 *
 *  Abstract class vs Interface:
 *    Abstract class — can have state (fields), constructors, any access modifiers,
 *                     at most ONE superclass (extends).
 *    Interface      — fields are implicitly public static final (constants),
 *                     methods public abstract by default, can have default/static methods
 *                     since Java 8, can have private methods since Java 9.
 *                     A class can implement MANY interfaces.
 *
 *  Diamond problem — if two interfaces declare a default method with the same signature,
 *  the implementing class MUST override it to resolve the ambiguity.
 */
@Component
public class OopDemo {

    private static final Logger log = LoggerFactory.getLogger(OopDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q18 — What are generics and what are they for?
    // ─────────────────────────────────────────────────────────────────────────

    public String runGenericsIntroDemo() {
        log.debug("=== OOP: generics intro (Q18) ===");

        // ── Without generics (pre-Java 5 style) ──────────────────────────────
        // Everything is Object → runtime ClassCastException risk.
        List rawList = new java.util.ArrayList();
        rawList.add("hello");
        rawList.add(42);          // compiles — no type check
        // String s = (String) rawList.get(1); // ClassCastException at runtime!
        log.debug("Raw list (no generics): any type accepted, casts required, runtime errors possible");

        // ── With generics ─────────────────────────────────────────────────────
        // JUNIOR NOTE: The type parameter <String> tells the compiler to:
        //   1. Reject non-String add() calls at compile time.
        //   2. Insert the cast automatically on get() — verified safe.
        //   3. Erase <String> to Object at bytecode level (type erasure).
        List<String> typedList = new java.util.ArrayList<>();
        typedList.add("hello");
        // typedList.add(42); // COMPILE ERROR — caught before runtime
        String s = typedList.get(0); // no cast needed — compiler inserts it
        log.debug("Typed list<String>: type-safe at compile time, s='{}'", s);

        // ── Generic method ────────────────────────────────────────────────────
        String first = firstOrDefault(List.of("alpha", "beta"), "none");
        Integer num   = firstOrDefault(List.of(10, 20), -1);
        log.debug("firstOrDefault: String='{}', Integer={}", first, num);

        return """
            Q18 — Generics:

            PURPOSE:
              Compile-time type safety — catch type errors before runtime.
              Eliminate manual casts — the compiler inserts them, verified safe.
              Enable reusable containers and algorithms over any type.

            WITHOUT GENERICS (raw types):
              List list = new ArrayList();
              list.add("hello");
              list.add(42);                    // compiles — no check
              String s = (String) list.get(1); // ClassCastException at runtime!

            WITH GENERICS:
              List<String> list = new ArrayList<>();
              list.add("hello");
              list.add(42);     // COMPILE ERROR — caught immediately
              String s = list.get(0); // no cast — compiler handles it

            TYPE ERASURE:
              Generics are a compile-time feature only.
              At runtime List<String> and List<Integer> are both just List (Object).
              You cannot do: if (list instanceof List<String>) — the <String> is erased.
              You CAN do:    if (list instanceof List<?>) — raw wildcard is ok.

            // JUNIOR NOTE: Type erasure exists for backward compatibility with
            // pre-generics bytecode. It means you cannot create generic arrays:
            // T[] arr = new T[10]; // compile error — T is unknown at runtime.
            """;
    }

    private <T> T firstOrDefault(List<T> list, T defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q19 — Examples of generics in Java
    // ─────────────────────────────────────────────────────────────────────────

    public String runGenericsExamplesDemo() {
        log.debug("=== OOP: generics examples (Q19) ===");

        // ── Generic class: Box<T> ─────────────────────────────────────────────
        Box<String>  strBox = new Box<>("interview prep");
        Box<Integer> intBox = new Box<>(2024);
        log.debug("Box<String>: {}", strBox.get());
        log.debug("Box<Integer>: {}", intBox.get());

        // ── Generic class: Pair<A, B> — two type parameters ───────────────────
        Pair<String, Integer> pair = new Pair<>("score", 100);
        log.debug("Pair<String,Integer>: {}={}", pair.first(), pair.second());

        // ── Bounded type parameter: <T extends Number> ────────────────────────
        // JUNIOR NOTE: T extends Number means T must be Number or a subtype.
        // This lets you call .doubleValue() on T — guaranteed to be available.
        log.debug("sum(List.of(1,2,3)) = {}", sum(List.of(1, 2, 3)));
        log.debug("sum(List.of(1.5, 2.5)) = {}", sum(List.of(1.5, 2.5)));

        // ── Standard library examples ─────────────────────────────────────────
        // Collections, Maps, Optional, Streams, Comparator — all generic
        java.util.Optional<String> opt = java.util.Optional.of("present");
        Function<Integer, String>  fn  = i -> "value-" + i;
        log.debug("Optional<String>: {}", opt.orElse("absent"));
        log.debug("Function<Integer,String>.apply(5): {}", fn.apply(5));

        return """
            Q19 — Generic examples:

            GENERIC CLASS — one type parameter:
              class Box<T> { private T value; ... }
              Box<String>  b1 = new Box<>("hello");
              Box<Integer> b2 = new Box<>(42);

            GENERIC CLASS — two type parameters:
              class Pair<A, B> { A first; B second; }
              Pair<String, Integer> p = new Pair<>("score", 100);

            BOUNDED TYPE PARAMETER — T extends Number:
              double sum(List<T extends Number> list)
              Constrains T to Number subtypes → .doubleValue() is safe to call.
              sum(List.of(1, 2, 3))     → 6.0
              sum(List.of(1.5, 2.5))    → 4.0

            GENERIC METHOD — type inferred at call site:
              <T> T firstOrDefault(List<T> list, T def)
              firstOrDefault(List.of("a","b"), "none")  → "a"
              firstOrDefault(List.of(10, 20), -1)       → 10

            STANDARD LIBRARY — everything you use daily is generic:
              List<E>, Map<K,V>, Optional<T>, Stream<T>,
              Comparator<T>, Function<T,R>, Supplier<T>, Consumer<T>
            """;
    }

    // Generic class with one type parameter
    private static class Box<T> {
        private final T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
        @Override public String toString() { return "Box[" + value + "]"; }
    }

    // Generic record with two type parameters (Java 16+)
    private record Pair<A, B>(A first, B second) {}

    // Bounded type parameter
    private <T extends Number> double sum(List<T> numbers) {
        return numbers.stream().mapToDouble(Number::doubleValue).sum();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q20 — extends / super wildcards (PECS)
    // ─────────────────────────────────────────────────────────────────────────

    public String runWildcardsDemo() {
        log.debug("=== OOP: wildcards / PECS (Q20) ===");

        List<Integer>  ints    = List.of(1, 2, 3);
        List<Double>   doubles = List.of(1.5, 2.5, 3.5);
        List<Number>   numbers = List.of(10, 20.5, 30);

        // ── Producer Extends: read from a source ──────────────────────────────
        // <? extends Number> accepts List<Integer>, List<Double>, List<Number>.
        // You can READ elements as Number safely.
        // You CANNOT write — the compiler doesn't know the exact subtype.
        log.debug("sumExtends(ints)={}    sumExtends(doubles)={}",
            sumExtends(ints), sumExtends(doubles));

        // ── Consumer Super: write into a target ───────────────────────────────
        // <? super Integer> accepts List<Integer>, List<Number>, List<Object>.
        // You can WRITE Integer (or subtypes) safely.
        // You can only READ as Object — exact type unknown.
        List<Number> target = new java.util.ArrayList<>();
        addIntegers(target, List.of(10, 20, 30));
        log.debug("addIntegers into List<Number>: {}", target);

        // ── Unbounded wildcard <?> ─────────────────────────────────────────────
        // JUNIOR NOTE: <?> means "List of unknown type".
        // You can read elements as Object; you cannot add anything (except null).
        // Useful when you only need to call methods that don't depend on the type.
        log.debug("printSize(ints)={}  printSize(numbers)={}", ints.size(), numbers.size());

        return """
            Q20 — extends / super wildcards (PECS):

            PECS = Producer Extends, Consumer Super

            PRODUCER EXTENDS — <? extends T>   "I will READ from this":
              double sumExtends(List<? extends Number> list)
              Accepts: List<Integer>, List<Double>, List<Number>
              ✓ Can read elements as Number (the upper bound)
              ✗ Cannot add — compiler doesn't know exact subtype
              Use when: the list is a DATA SOURCE you iterate over.

            CONSUMER SUPER — <? super T>       "I will WRITE into this":
              void addIntegers(List<? super Integer> list, List<Integer> source)
              Accepts: List<Integer>, List<Number>, List<Object>
              ✓ Can write Integer (or any subtype of Integer)
              ✗ Can only read as Object — exact type unknown
              Use when: the list is a DESTINATION you add items to.

            UNBOUNDED — <?>                    "I don't care about the type":
              void printSize(List<?> list)
              ✓ Can read elements as Object
              ✗ Cannot add anything (except null)
              Use when: you only need methods that don't depend on the element type.

            MEMORY AID:
              ? extends T → you GET things out  (like a vending machine)
              ? super T   → you PUT things in   (like a rubbish bin)

            // JUNIOR NOTE: Collections.copy(dest, src) is the textbook PECS example:
            // void copy(List<? super T> dest, List<? extends T> src)
            //   dest is a Consumer (super) — we write into it.
            //   src  is a Producer (extends) — we read from it.
            """;
    }

    private double sumExtends(List<? extends Number> list) {
        return list.stream().mapToDouble(Number::doubleValue).sum();
    }

    private void addIntegers(List<? super Integer> target, List<Integer> source) {
        target.addAll(source);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q21 — Abstract class vs Interface
    // ─────────────────────────────────────────────────────────────────────────

    public String runAbstractVsInterfaceDemo() {
        log.debug("=== OOP: abstract class vs interface (Q21) ===");

        // ── Demonstrate abstract class ────────────────────────────────────────
        // JUNIOR NOTE: Abstract class can have:
        //   - Instance fields (state)
        //   - Constructor (called by subclass via super())
        //   - Concrete methods (inherited as-is)
        //   - Abstract methods (must be overridden)
        //   - Any access modifier on members
        Animal dog = new Dog("Rex");
        Animal cat = new Cat("Whiskers");
        log.debug("dog.describe(): {}", dog.describe());
        log.debug("cat.describe(): {}", cat.describe());
        log.debug("dog.sound(): {}", dog.sound());
        log.debug("cat.sound(): {}", cat.sound());

        // ── Demonstrate interface ─────────────────────────────────────────────
        // Interface defines a contract — what the implementor CAN DO.
        // A class can implement many interfaces (multiple capabilities).
        Flyable plane = new Airplane();
        Flyable bird  = new Bird();
        Swimmable duck = new Duck();
        log.debug("plane.fly(): {}", plane.fly());
        log.debug("bird.fly(): {}", bird.fly());
        log.debug("duck.swim(): {}", duck.swim());
        // Duck implements both Flyable and Swimmable — multiple capabilities
        log.debug("duck.fly(): {}", ((Flyable) duck).fly());

        return """
            Q21 — Abstract class vs Interface:

            ABSTRACT CLASS:
              • Can have instance fields (state)
              • Has a constructor (invoked by subclass via super())
              • Can mix abstract + concrete methods
              • Members can have any access modifier (private, protected, public)
              • A class can extend only ONE abstract class

              Use when:
                → Sharing common state and/or implementation among related classes.
                → You have a "is-a" relationship with a base concept.
                → e.g. Animal (has name, has breathe()) with abstract sound()

            INTERFACE:
              • Fields are implicitly public static final (constants only)
              • Methods are public abstract by default
              • Can have default methods (Java 8+) — optional override
              • Can have static methods (Java 8+)
              • Can have private methods (Java 9+) — reuse inside the interface
              • NO constructor — cannot hold instance state
              • A class can implement MANY interfaces

              Use when:
                → Defining a capability / contract (Comparable, Runnable, Serializable)
                → Enabling multiple inheritance of behaviour
                → e.g. Flyable, Swimmable — orthogonal capabilities

            QUICK COMPARISON:
              Feature              Abstract class    Interface
              ─────────────────────────────────────────────────
              Instance fields      ✓                 ✗ (only constants)
              Constructor          ✓                 ✗
              Multiple inheritance ✗ (one only)      ✓ (many)
              Access modifiers     any               public only
              State                ✓                 ✗
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q22, Q23, Q24 — Inheritance rules, constructors, access modifiers
    // ─────────────────────────────────────────────────────────────────────────

    public String runInheritanceRulesDemo() {
        log.debug("=== OOP: inheritance rules (Q22, Q23, Q24) ===");

        // Q22 — A class can extend ONE class but implement MANY interfaces
        log.debug("Duck extends Bird (1 class) and implements Flyable, Swimmable (2 interfaces)");

        // Q23 — Interface has no constructor; abstract class has one
        // new Animal("test"); // compile error — abstract class cannot be instantiated directly
        // Interface fields are always public static final even without the keywords
        log.debug("Flyable.DEFAULT_ALTITUDE={}", Flyable.DEFAULT_ALTITUDE);

        // Q24 — Interface members: public abstract (methods), public static final (fields)
        // Abstract class members: any modifier — private, protected, package-private, public
        log.debug("Abstract class can have protected/private members; interface cannot");

        return """
            Q22 — How many supertypes can you extend / implement?

              extends  → exactly ONE class (abstract or concrete)
              implements → UNLIMITED interfaces
              class Duck extends Bird implements Flyable, Swimmable { ... }

            Q23 — Constructors:

              Abstract class:
                HAS a constructor — called by the subclass with super(...).
                Cannot be instantiated directly (new Animal() → compile error).
                Constructor runs normally when a subclass instance is created.

              Interface:
                NO constructor — has no instance state to initialise.
                Fields are constants (public static final), not instance fields.
                Cannot be instantiated at all.

            Q24 — Default access modifiers:

              Abstract class members:  whatever you write — default is package-private.
                private, protected, public, or package-private all valid.

              Interface members:
                Methods  → public abstract   (even without those keywords)
                Fields   → public static final (even without those keywords)
                Default methods → public (since Java 8)
                Private methods → private (since Java 9, for internal reuse only)

            // JUNIOR NOTE: Writing "public abstract" in an interface method is
            // redundant but not wrong — the compiler adds them implicitly.
            // Writing "public static final" on an interface field is also redundant.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q25, Q26, Q27 — Multiple inheritance, same signature, diamond default methods
    // ─────────────────────────────────────────────────────────────────────────

    public String runMultipleInheritanceDemo() {
        log.debug("=== OOP: multiple inheritance & diamond (Q25, Q26, Q27) ===");

        // Q25 — A class CAN implement multiple interfaces → multiple inheritance of TYPE
        Duck duck = new Duck();
        log.debug("Duck implements Flyable AND Swimmable simultaneously");

        // Q26 — Two interfaces with SAME abstract method signature → NO conflict
        // The class provides ONE implementation that satisfies both contracts.
        log.debug("Duck.fly() satisfies both Flyable.fly() and any other interface with fly()");

        // Q27 — Two interfaces with SAME DEFAULT method → MUST override to resolve
        // DiamondResolved shows the pattern
        DiamondResolved resolved = new DiamondResolved();
        log.debug("DiamondResolved.greet(): {}", resolved.greet());
        log.debug("DiamondResolved calls Left explicitly: {}", resolved.greetFromLeft());

        return """
            Q25 — Multiple inheritance with interfaces:

              A class CAN implement multiple interfaces.
              This gives multiple inheritance of TYPE (polymorphism) and
              multiple inheritance of BEHAVIOUR (via default methods).

              class Duck extends Bird implements Flyable, Swimmable { ... }
              Flyable  f = new Duck();   // Duck IS-A Flyable
              Swimmable s = new Duck();  // Duck IS-A Swimmable

            Q26 — Two interfaces with same abstract method signature:

              interface A { void move(); }
              interface B { void move(); }
              class C implements A, B {
                  @Override
                  public void move() { ... }  // ONE impl satisfies BOTH contracts
              }
              No problem — the compiler just requires the class to provide the method.
              It doesn't matter that two interfaces declare it.

            Q27 — Two interfaces with SAME DEFAULT method — the diamond problem:

              interface Left  { default String greet() { return "Left";  } }
              interface Right { default String greet() { return "Right"; } }

              class Broken implements Left, Right { }  // COMPILE ERROR
              // "class Broken inherits unrelated defaults for greet()"

              RESOLUTION — the class MUST override:
              class DiamondResolved implements Left, Right {
                  @Override
                  public String greet() {
                      return Left.super.greet() + " + " + Right.super.greet();
                      // or pick one: return Left.super.greet();
                  }
              }
              InterfaceName.super.method() explicitly calls a specific default.

            PRIORITY RULES (when no override):
              1. Class / superclass method always wins over interface default.
              2. More specific interface wins over less specific (subinterface wins).
              3. If still ambiguous → compile error, must override.

            // JUNIOR NOTE: The diamond problem only arises with DEFAULT methods.
            // Abstract methods with the same signature from two interfaces
            // are not a conflict — the implementing class just provides one impl.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner types used across the demos
    // ─────────────────────────────────────────────────────────────────────────

    // Q21 — Abstract class hierarchy
    private static abstract class Animal {
        private final String name; // instance field — not allowed in interface

        Animal(String name) { this.name = name; } // constructor

        // Concrete method — inherited by all subclasses
        String describe() { return getClass().getSimpleName() + " named " + name; }

        // Abstract method — each subclass must provide its own implementation
        abstract String sound();
    }

    private static class Dog extends Animal {
        Dog(String name) { super(name); }
        @Override String sound() { return "Woof!"; }
    }

    private static class Cat extends Animal {
        Cat(String name) { super(name); }
        @Override String sound() { return "Meow!"; }
    }

    // Q21, Q22 — Interfaces (capabilities, not hierarchy)
    private interface Flyable {
        // JUNIOR NOTE: This constant is implicitly public static final.
        int DEFAULT_ALTITUDE = 1000;

        String fly(); // implicitly public abstract

        // Default method — optional override (Java 8+)
        default String landingGear() { return "gear deployed"; }
    }

    private interface Swimmable {
        String swim();
    }

    private static class Airplane implements Flyable {
        @Override public String fly() { return "Airplane cruising at 35,000 ft"; }
    }

    private static class Bird extends Animal implements Flyable {
        Bird() { super("bird"); }
        @Override public String sound() { return "Tweet!"; }
        @Override public String fly()   { return "Bird flapping wings"; }
    }

    // Q22 — Duck: extends one class, implements two interfaces
    private static class Duck extends Bird implements Flyable, Swimmable {
        Duck() { super(); }
        @Override public String fly()  { return "Duck flying low"; }
        @Override public String swim() { return "Duck paddling on water"; }
        @Override public String sound() { return "Quack!"; }
    }

    // Q27 — Diamond problem and resolution
    private interface LeftGreeter  { default String greet() { return "Hello from Left";  } }
    private interface RightGreeter { default String greet() { return "Hello from Right"; } }

    private static class DiamondResolved implements LeftGreeter, RightGreeter {
        // MUST override — compiler error without this
        @Override
        public String greet() {
            // Explicitly delegate to one or both using InterfaceName.super.method()
            return LeftGreeter.super.greet() + " + " + RightGreeter.super.greet();
        }
        String greetFromLeft() { return LeftGreeter.super.greet(); }
    }
}
