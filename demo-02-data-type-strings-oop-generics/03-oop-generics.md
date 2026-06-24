# 03 — OOP, Inheritance & Generics

- > **Questions covered:** Q18–Q27
- > **Demo:** [demo-02-data-type-strings-oop-generics](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q18 — What are generics, and what are they for?

**Short answer**

**Generics** enable types (classes and interfaces) to be parameters when defining classes, interfaces, and methods. They provide **compile-time type safety** and eliminate the need for explicit casting.

**In depth**

**What generics solve:**
- **Type safety:** Catch type errors at compile time, not runtime
- **No casting:** Remove explicit casts when retrieving elements
- **Code reusability:** Write code that works with different types

```java
// Without generics (pre-Java 5)
List list = new ArrayList();
list.add("hello");
String s = (String) list.get(0);  // Required explicit cast

// With generics
List<String> list = new ArrayList<>();
list.add("hello");
String s = list.get(0);  // No cast needed
```

**Generic classes and methods:**
```java
// Generic class
public class Box<T> {
    private T value;
    
    public void set(T value) { this.value = value; }
    public T get() { return value; }
}

// Generic method
public static <T> T getMiddle(T... a) {
    return a[a.length / 2];
}
```

**Type erasure:** Generic type information is **erased** at compile time. At runtime, generic types are replaced with their bounds or `Object`. This is why you can't use `instanceof` with generic types.

> **// JUNIOR NOTE:** Generics are compile-time only. At runtime, all generic types are erased. This is called **type erasure**. A `List<String>` and a `List<Integer>` are the same at runtime — both are just `List`.

---

## Q19 — Examples of generics in Java

**Short answer**

Common examples include collections (`List<String>`, `Map<String, Integer>`), `Optional<T>`, `CompletableFuture<T>`, and `Comparable<T>` interface.

**In depth**

**Collection framework (most common):**
```java
List<String> names = new ArrayList<>();
Map<String, Integer> scores = new HashMap<>();
Set<User> users = new HashSet<>();
```

**Java standard library generics:**
```java
// Optional
Optional<String> result = Optional.of("value");

// CompletableFuture
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 42);

// Comparable interface
public class Person implements Comparable<Person> {
    @Override
    public int compareTo(Person other) {
        return this.name.compareTo(other.name);
    }
}

// Custom generic class
public class Result<T, E> {
    private T value;
    private E error;
}
```

**Generic methods in utility classes:**
```java
// Collections utility class
public static <T> List<T> reverse(List<T> list) {
    List<T> reversed = new ArrayList<>(list);
    Collections.reverse(reversed);
    return reversed;
}
```

> **// JUNIOR NOTE:** The diamond operator `<>` (introduced in Java 7) allows the compiler to infer the generic type. Instead of `new ArrayList<String>()`, you can write `new ArrayList<>()`.

---

## Q20 — extends / super (in generic bounds and wildcards)

**Short answer**

`extends` (`? extends T`) defines an **upper bound** — the type must be a subtype of `T` (read-only). `super` (`? super T`) defines a **lower bound** — the type must be a supertype of `T` (write-only).

**In depth**

**The PECS principle:** *P*roducer *E*xtends, *C*onsumer *S*uper.

| Wildcard | Meaning | Use case | What you can do |
|----------|---------|----------|-----------------|
| `? extends T` | Type is `T` or any subclass | Reading data (Producer) | Can read as `T`, cannot write (except `null`) |
| `? super T` | Type is `T` or any superclass | Writing data (Consumer) | Can write `T`, cannot read as `T` (only `Object`) |
| `?` (unbounded) | Any type | Type-agnostic operations | Can only read as `Object` |

```java
// ? extends T — Producer (read-only)
public void processNumbers(List<? extends Number> numbers) {
    // Can read as Number
    for (Number n : numbers) {
        System.out.println(n.doubleValue());
    }
    // Cannot add elements (except null)
    // numbers.add(new Integer(5)); // Compile error!
}

// ? super T — Consumer (write-only)
public void addIntegers(List<? super Integer> list) {
    list.add(42);        // Can add Integer
    list.add(100);       // Can add Integer
    // Cannot read as Integer
    // Integer i = list.get(0); // Compile error!
    Object obj = list.get(0);  // Can only read as Object
}

// Real-world example: Collections.copy
public static <T> void copy(List<? super T> dest, List<? extends T> src) {
    // src: Producer (read) → ? extends T
    // dest: Consumer (write) → ? super T
    for (int i = 0; i < src.size(); i++) {
        dest.set(i, src.get(i));
    }
}
```

**Type bounds on generic type parameters:**
```java
// T must extend Number
public class Calculator<T extends Number> {
    private T value;
    
    public double doubleValue() {
        return value.doubleValue();  // Safe because T extends Number
    }
}
```

> **// JUNIOR NOTE:** Remember **PECS**: *P*roducer *E*xtends, *C*onsumer *S*uper. If you're *reading* from a generic structure, use `extends`. If you're *writing* to it, use `super`.

---

## Q21 — Difference between an abstract class and an interface

**Short answer**

An **abstract class** can have state (fields), constructors, and partially implemented methods. An **interface** (before Java 8) was purely a contract with abstract methods. Since Java 8, interfaces can have `default` and `static` methods.

**In depth**

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| **State (fields)** | ✅ Can have instance fields | ❌ Only `static final` constants (before Java 9+) |
| **Constructors** | ✅ Can have constructors | ❌ Cannot have constructors |
| **Inheritance** | Single inheritance only | Multiple inheritance (can implement multiple) |
| **Method implementations** | ✅ Can have concrete methods | ✅ Since Java 8: `default` and `static` methods |
| **Access modifiers** | All access levels | Public (implicitly), private methods since Java 9 |
| **When to use** | "Is-a" relationship, shared state | "Can-do" relationship, contract, multiple roles |

```java
// Abstract class — can have state
public abstract class Animal {
    protected String name;  // State
    
    public Animal(String name) {  // Constructor
        this.name = name;
    }
    
    public String getName() {  // Concrete method
        return name;
    }
    
    public abstract void speak();  // Abstract method
}

// Interface — contract only
public interface Flyable {
    void fly();  // Abstract method (implicitly public)
    
    default void land() {  // Default method (Java 8+)
        System.out.println("Landing...");
    }
    
    static void help() {  // Static method (Java 8+)
        System.out.println("Flyable interface");
    }
}
```

> **// JUNIOR NOTE:** With Java 8+, the line between abstract classes and interfaces has blurred. The key difference remains: **abstract classes can have state**, interfaces cannot (except constants). Choose abstract class when you need shared state; choose interface when you need multiple inheritance of behavior.

---

## Q22 — How many supertypes can you extend / implement

**Short answer**

A class can **extend only one** class (single inheritance) but can **implement multiple** interfaces. An interface can **extend multiple** interfaces.

**In depth**

```java
// Class: one extends, multiple implements
public class Bird extends Animal 
                  implements Flyable, Singable, Breedable {
    // Can extend ONE class
    // Can implement MANY interfaces
}

// Interface: can extend multiple interfaces
public interface Lifeform extends Organism, Movable, Reproducible {
    // Can extend MANY interfaces
}

// Class with generic bounds: multiple bounds
public class CustomList<T extends Serializable & Comparable<T>> {
    // T must extend Serializable AND implement Comparable
}
```

**Why single inheritance for classes?**
- **Diamond problem:** Multiple inheritance would create ambiguity if two superclasses have the same method
- **Complexity:** Single inheritance keeps the object model simpler
- **State conflict:** Multiple superclasses could have conflicting field definitions

**Why multiple inheritance for interfaces?**
- Interfaces have no state (before Java 9), so no field conflicts
- Default methods can cause conflicts, but they must be resolved explicitly
- Interfaces represent *capabilities*, not *categories*

> **// JUNIOR NOTE:** This is why interfaces are so powerful in Java — they allow a class to fulfill multiple roles (e.g., a class can be both `Runnable` and `Comparable` and `Serializable`) while maintaining a single inheritance hierarchy.

---

## Q23 — Presence of a constructor

**Short answer**

**Abstract classes** have constructors (called when a concrete subclass is instantiated). **Interfaces** cannot have constructors.

**In depth**

```java
// Abstract class with constructor
public abstract class Vehicle {
    private String model;
    
    public Vehicle(String model) {  // Constructor exists
        this.model = model;
    }
    
    public String getModel() {
        return model;
    }
}

// Concrete subclass MUST call super constructor
public class Car extends Vehicle {
    public Car(String model) {
        super(model);  // Must call super constructor
    }
}

// Interface — NO constructor
public interface Drivable {
    // Cannot have a constructor
    void drive();
}
```

**Why abstract classes need constructors:**
- They can have fields that need initialization
- The constructor is called when the *concrete* subclass is instantiated
- It ensures that the abstract class's state is properly initialized

**Why interfaces don't have constructors:**
- Interfaces cannot have state (fields), so no initialization is needed
- Interfaces are pure contracts — they don't represent objects themselves
- You cannot instantiate an interface directly

> **// JUNIOR NOTE:** If you don't define any constructor in an abstract class, Java provides a default no-argument constructor. The subclass can call it implicitly with `super()`.

---

## Q24 — Default access modifiers of abstract class and interface

**Short answer**

**Abstract class:** Members have `default` (package-private) access if no modifier is specified. **Interface:** All members are implicitly `public` (methods and fields).

**In depth**

| Member type | Abstract Class (default) | Interface (default) |
|-------------|--------------------------|---------------------|
| **Fields** | `default` (package-private) | `public static final` (implicitly) |
| **Methods** | `default` (package-private) | `public abstract` (implicitly) or `public default` for default methods |
| **Constructors** | `default` (package-private) if no constructor defined | Not applicable |
| **Nested classes** | `default` (package-private) | `public static` (implicitly) |

```java
// Abstract class - default (package-private) access
abstract class AbstractExample {
    String field = "default access";  // package-private
    
    void method() { }  // package-private
    
    abstract void abstractMethod();  // package-private
}

// Interface - all public by default
interface InterfaceExample {
    String FIELD = "public static final";  // public static final
    
    void method();  // public abstract
    
    default void defaultMethod() { }  // public default
    
    static void staticMethod() { }  // public static
}
```

**Access modifiers in interfaces (Java 9+):**
- **Java 7 and earlier:** All methods are `public abstract`, all fields are `public static final`
- **Java 8:** Added `default` and `static` methods (still `public`)
- **Java 9:** Added `private` methods in interfaces (for code reuse in default methods)

> **// JUNIOR NOTE:** In an interface, you never need to write `public`, `abstract`, `static`, or `final` — they're all implicit. But many developers write them explicitly for clarity.

---

## Q25 — Multiple inheritance with interfaces

**Short answer**

Java supports **multiple inheritance of type** through interfaces. A class can implement multiple interfaces, and an interface can extend multiple interfaces.

**In depth**

```java
// Multiple interface inheritance (type)
interface Flyable {
    void fly();
}

interface Swimmable {
    void swim();
}

// Interface extending multiple interfaces
interface Amphibious extends Flyable, Swimmable {
    // Combines both interfaces
}

// Class implementing multiple interfaces
class Duck implements Flyable, Swimmable {
    @Override
    public void fly() {
        System.out.println("Duck flying");
    }
    
    @Override
    public void swim() {
        System.out.println("Duck swimming");
    }
}
```

**Benefits of multiple interface inheritance:**
- **Flexibility:** A class can fulfill multiple roles (`Runnable`, `Comparable`, `Serializable`)
- **Composition:** Interfaces can be combined to create more powerful abstractions
- **No diamond problem:** Interfaces don't have state or method implementations (except default methods)

> **// JUNIOR NOTE:** Java doesn't support multiple inheritance of *implementation* (extending multiple classes) to avoid the diamond problem. But multiple inheritance of *type* (interfaces) is fully supported and is a key feature of the language.

---

## Q26 — What if two interface methods "overlap" (same signature from two interfaces)?

**Short answer**

If two interfaces have methods with the **same signature**, the class implementing them only needs to **implement one method** that satisfies both interfaces.

**In depth**

```java
interface InterfaceA {
    void doSomething();
}

interface InterfaceB {
    void doSomething();  // Same signature
}

// Class implements both — one method satisfies both interfaces
class Implementation implements InterfaceA, InterfaceB {
    @Override
    public void doSomething() {
        // Single implementation satisfies both interfaces
        System.out.println("Doing something");
    }
}
```

**What if methods have different return types?**
- If the return types are **identical** → fine, one method satisfies both
- If the return types are **covariant** (one is a subtype of the other) → fine, one method satisfies both
- If the return types are **incompatible** → compile error

```java
interface InterfaceA {
    Number getValue();
}

interface InterfaceB {
    Integer getValue();  // Covariant return (Integer extends Number)
}

// This works — Integer satisfies both Number and Integer
class Implementation implements InterfaceA, InterfaceB {
    @Override
    public Integer getValue() {
        return 42;
    }
}
```

> **// JUNIOR NOTE:** This is a benefit of interface-based design — a single implementation can satisfy multiple contracts. This reduces code duplication and makes the system more cohesive.

---

## Q27 — What if two default methods in interfaces conflict?

**Short answer**

If a class implements two interfaces that both provide `default` methods with the **same signature**, the class must **override** the method to resolve the conflict. The compiler forces explicit resolution.

**In depth**

```java
interface InterfaceA {
    default void hello() {
        System.out.println("Hello from A");
    }
}

interface InterfaceB {
    default void hello() {
        System.out.println("Hello from B");
    }
}

// ❌ Compile error: Class must override the conflicting method
class Conflicting implements InterfaceA, InterfaceB {
    // Must override hello()
}

// ✅ Resolution: override and specify which behavior to use
class Resolved implements InterfaceA, InterfaceB {
    @Override
    public void hello() {
        // Option 1: Use one of the default implementations
        InterfaceA.super.hello();  // Calls InterfaceA's default
        
        // Option 2: Provide completely new implementation
        System.out.println("Hello from resolved class");
    }
}
```

**Resolution rules (from most specific to least):**
1. **Class method wins** — explicit override in the class takes precedence
2. **Most specific interface** — if one interface extends the other, the sub-interface wins
3. **Explicit resolution** — if equal specificity, class must override

```java
// Specificity example: one interface extends another
interface Parent {
    default void hello() {
        System.out.println("Hello from Parent");
    }
}

interface Child extends Parent {
    default void hello() {
        System.out.println("Hello from Child");
    }
}

// Child's default wins (more specific)
class Implementation implements Child {
    // No override needed — Child's hello() is used
}
```

> **// JUNIOR NOTE:** This is the "diamond problem" that default methods introduce. Java solves it by forcing explicit resolution when there's a conflict. This is one reason why interfaces with default methods should be used carefully — they can create complex inheritance hierarchies.

---

## Quick-reference cheat sheet

```
Generics     → type parameters for compile-time safety
             → List<String>, Map<K,V>, Optional<T>
             → Type erasure: generic info removed at runtime

Wildcards    → ? extends T (Producer → read)
             → ? super T   (Consumer → write)
             → PECS: Producer Extends, Consumer Super

Abstract     → can have state, constructors, concrete methods
Class        → single inheritance only

Interface    → contract, multiple inheritance of type
             → default methods (Java 8+)
             → static methods (Java 8+)
             → private methods (Java 9+)

Inheritance  → Class: ONE extends, MANY implements
             → Interface: MANY extends
             → Default method conflicts: MUST override

Access       → Abstract class: default = package-private
             → Interface: all = public (implicitly)
```

---

## Bonus Q & A

**Q1: What is the difference between a generic class and a generic method?**

**Q2: What is type erasure and why does it exist?**

**Q3: What is the difference between `List<String>` and `List<?>`?**

**Q4: What is the difference between `List<Object>` and `List<?>`?**

**Q5: Can you create an instance of a generic type? (e.g., `new T()`)**

**Q6: What is the difference between `ArrayList` and `ArrayList<?>`?**

**Q7: What is the difference between `? extends T` and `T extends SomeClass`?**

**Q8: What is the difference between `abstract` and `interface`?**

**Q9: What is the difference between `default` and `static` methods in interfaces?**

**Q10: What is the diamond problem and how does Java handle it?**

---
