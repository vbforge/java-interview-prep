# 06 — Streams & Lambdas

- > **Questions covered:** Q45–Q52
- > **Demo:** [demo-04-streams](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q45 — In Java, what is a Stream?

**Short answer**

A **Stream** is a sequence of elements supporting sequential and parallel aggregate operations. It's not a data structure — it doesn't store data. Instead, it **carries data** from a source (collection, array, generator) through a pipeline of operations.

**In depth**

**Key characteristics of Streams:**
- **Not a data structure:** Doesn't store elements, only processes them
- **Functional:** Operations don't modify the source
- **Lazy:** Intermediate operations are not executed until a terminal operation is called
- **Single use:** A stream can only be consumed once
- **Pipelined:** Operations are chained together

```java
// Stream pipeline structure
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

List<String> result = names.stream()          // 1. Create stream from source
    .filter(name -> name.length() > 3)        // 2. Intermediate: filter
    .map(String::toUpperCase)                 // 3. Intermediate: transform
    .sorted()                                 // 4. Intermediate: sort
    .collect(Collectors.toList());            // 5. Terminal: collect to list

// result: [ALICE, CHARLIE, DAVID]
```

**Stream sources:**
- `Collection.stream()` — from any collection
- `Stream.of()` — from elements
- `Arrays.stream()` — from arrays
- `Stream.iterate()` / `Stream.generate()` — infinite streams
- `Files.lines()` — from files

```java
// Creating streams from different sources
Stream<Integer> s1 = Stream.of(1, 2, 3);
Stream<Integer> s2 = Arrays.stream(new Integer[]{1, 2, 3});
Stream<Integer> s3 = List.of(1, 2, 3).stream();
Stream<Double> s4 = Stream.generate(Math::random).limit(10);
Stream<Integer> s5 = Stream.iterate(0, n -> n + 2).limit(5);  // 0, 2, 4, 6, 8
```

> **// JUNIOR NOTE:** A common mistake is trying to reuse a Stream after a terminal operation. Streams are **single-use** — once you call a terminal operation, the stream is consumed and cannot be reused. You must create a new stream for each pipeline.

---

## Q46 — What is the difference between intermediate and terminal stream operations? Give two examples of each.

**Short answer**

**Intermediate operations** are **lazy** — they return a new stream and don't execute until a terminal operation is called. **Terminal operations** are **eager** — they execute the pipeline and produce a result or side effect.

**In depth**

| Type | Characteristics | Examples |
|------|-----------------|----------|
| **Intermediate** | Lazy, returns Stream, can be chained | `filter()`, `map()`, `flatMap()`, `sorted()`, `distinct()`, `limit()`, `skip()`, `peek()` |
| **Terminal** | Eager, doesn't return Stream, executes pipeline | `collect()`, `forEach()`, `count()`, `reduce()`, `findFirst()`, `anyMatch()`, `toList()` |

```java
// Intermediate operations (lazy) — nothing executes here
Stream<String> stream = names.stream()
    .filter(name -> {                     // Intermediate: filter
        System.out.println("Filtering: " + name);
        return name.length() > 3;
    })
    .map(name -> {                        // Intermediate: map
        System.out.println("Mapping: " + name);
        return name.toUpperCase();
    })
    .sorted();                            // Intermediate: sorted

// Nothing printed yet! Stream is just built, not executed.

// Terminal operation (eager) — executes the entire pipeline
List<String> result = stream.collect(Collectors.toList());
// Now all the operations execute!
```

**More examples:**
```java
// Intermediate examples
stream.filter(s -> s.startsWith("A"))    // Keep only elements starting with "A"
stream.map(String::length)               // Transform strings to their lengths
stream.distinct()                        // Remove duplicates
stream.skip(5)                           // Skip first 5 elements
stream.limit(10)                         // Limit to 10 elements

// Terminal examples
stream.forEach(System.out::println)      // Print each element
stream.count()                           // Count elements
stream.findFirst()                       // Get first element
stream.allMatch(s -> s.length() > 0)     // Check all elements match
stream.toList()                          // Collect to List (Java 16+)
```

> **// JUNIOR NOTE:** Remember **lazy evaluation** — intermediate operations don't execute until a terminal operation is called. This allows optimizations like **short-circuiting** (e.g., `findFirst()` stops processing as soon as it finds a match). This is why Streams are efficient.

---

## Q47 — After you call a terminal operation on a Stream, can you run another terminal operation on the same Stream reference?

**Short answer**

**No.** A Stream can only be **consumed once**. After a terminal operation is called, the stream is considered **consumed** and cannot be reused. Attempting to call another operation will throw `IllegalStateException`.

**In depth**

```java
Stream<String> stream = List.of("A", "B", "C").stream();

// Terminal operation #1
long count = stream.count();  // Consumes the stream
System.out.println(count);    // 3

// ❌ Attempt to reuse the stream — throws IllegalStateException
List<String> list = stream.collect(Collectors.toList());
// Exception: java.lang.IllegalStateException: stream has already been operated upon or closed
```

**Why streams are single-use:**
- **Stateful:** The stream maintains state about position, operations, etc.
- **Performance:** Allows optimizations and short-circuiting
- **Design:** Streams are designed as a pipeline, not a reusable data structure

**Correct approach — create a new stream:**
```java
// ✅ Always create a new stream for each pipeline
List<String> source = List.of("A", "B", "C");

long count = source.stream().count();
List<String> result = source.stream().map(String::toLowerCase).toList();

// Or use a Supplier to get a fresh stream each time
Supplier<Stream<String>> streamSupplier = () -> List.of("A", "B", "C").stream();
long count2 = streamSupplier.get().count();
List<String> result2 = streamSupplier.get().map(String::toLowerCase).toList();
```

> **// JUNIOR NOTE:** This is a common interview question! The answer is simple: **no**, you cannot reuse a stream. The stream is consumed after the terminal operation. Always create a new stream from the source if you need to perform multiple operations.

---

## Q48 — Does Optional have terminal/intermediate operations like a stream, or is it a one-shot container for zero or one value?

**Short answer**

`Optional` is a **one-shot container** for zero or one value. It has methods for working with the value (like `ifPresent()`, `map()`, `orElse()`) but does NOT have intermediate/terminal operations like a Stream. `Optional` is for **handling nulls**, not for processing sequences.

**In depth**

**Optional vs Stream:**

| Feature | Optional | Stream |
|---------|----------|--------|
| **Purpose** | Handle null, represent absence | Process sequences of data |
| **Values** | Zero or one | Zero or more |
| **Operations** | Map, flatMap, filter (return Optional) | Many intermediate operations (return Stream) |
| **Lazy evaluation** | No (eager) | Yes (lazy) |
| **Terminal operations** | orElse(), orElseGet(), ifPresent() | collect(), forEach(), count(), etc. |

```java
// Optional operations
Optional<String> optional = Optional.of("hello");

// 🔄 Map transforms the value if present
Optional<String> mapped = optional.map(String::toUpperCase);
// mapped = Optional[HELLO]

// ⚠️ Filter returns Optional — can be empty
Optional<String> filtered = optional.filter(s -> s.length() > 5);
// filtered = Optional.empty()

// 🔧 Terminal operations
String result1 = optional.orElse("default");           // "hello"
String result2 = optional.orElseGet(() -> "generated"); // "hello"
optional.ifPresent(System.out::println);               // prints "hello"

// 📦 Converting Optional to Stream (Java 9+)
Stream<String> stream = optional.stream();  // Stream with 0 or 1 element
```

**Optional is NOT a stream:**
- No intermediate operations that return a Stream
- No terminal operations like collect() or count()
- No lazy evaluation
- Purpose is null safety, not sequence processing

> **// JUNIOR NOTE:** `Optional` is not for general-purpose processing — it's for handling *one* nullable value. If you have a collection, use Stream. If you have a single value that might be null, use Optional. **Don't use Optional to wrap collections!** Use `Stream` directly.

---

## Q49 — Besides Stream<T>, which primitive-specialized stream types exist?

**Short answer**

**IntStream**, **LongStream**, and **DoubleStream**. These specializations avoid the overhead of boxing/unboxing and provide specialized operations like `sum()`, `average()`, and `range()`.

**In depth**

**Why primitive streams?**
- **Performance:** Avoid boxing/unboxing overhead
- **Memory:** Less memory usage
- **Specialized operations:** `sum()`, `average()`, `range()`, `summaryStatistics()`

| Primitive Stream | Boxed Alternative | Specialized Methods |
|------------------|-------------------|---------------------|
| `IntStream` | `Stream<Integer>` | `sum()`, `average()`, `range()`, `rangeClosed()` |
| `LongStream` | `Stream<Long>` | `sum()`, `average()`, `range()` |
| `DoubleStream` | `Stream<Double>` | `sum()`, `average()` |

```java
// Creating IntStream
IntStream intStream1 = IntStream.of(1, 2, 3, 4, 5);
IntStream intStream2 = IntStream.range(1, 10);        // 1-9 (exclusive end)
IntStream intStream3 = IntStream.rangeClosed(1, 10);  // 1-10 (inclusive end)
IntStream intStream4 = List.of(1, 2, 3).stream().mapToInt(Integer::intValue);

// Specialized operations
int sum = IntStream.range(1, 100).sum();                    // 4950
double avg = IntStream.of(1, 2, 3, 4).average().orElse(0); // 2.5
IntSummaryStatistics stats = IntStream.range(1, 10).summaryStatistics();
System.out.println(stats);  // IntSummaryStatistics{count=9, sum=45, min=1, average=5.0, max=9}

// Converting between primitive and boxed streams
IntStream ints = IntStream.range(1, 5);
Stream<Integer> boxed = ints.boxed();          // Boxed → Stream<Integer>
IntStream unboxed = boxed.mapToInt(i -> i);    // Unboxed → IntStream

// Performance comparison
// ❌ Avoid: boxing overhead
int sumBoxed = List.of(1, 2, 3, 4, 5)
    .stream()
    .mapToInt(Integer::intValue)  // Boxing → IntStream
    .sum();

// ✅ Better: use IntStream directly
int sumPrimitive = IntStream.rangeClosed(1, 5).sum();
```

> **// JUNIOR NOTE:** Always use primitive streams (`IntStream`, `LongStream`, `DoubleStream`) when working with primitives. It's more efficient and provides convenient methods like `sum()` and `average()`. This is especially important in performance-critical code.

---

## Q50 — Difference between map and flatMap on a stream

**Short answer**

**map()** transforms each element into **one** new element (1:1 mapping). **flatMap()** transforms each element into **zero or more** elements (1:N mapping) and flattens the result into a single stream.

**In depth**

```
map()     → 1 input → 1 output
flatMap() → 1 input → 0..N outputs (flattened)

Visual example:

map(String::toUpperCase):
["hello", "world"] → ["HELLO", "WORLD"]  (same size)

flatMap(String::toCharArray):
["hello", "world"] → ['h','e','l','l','o','w','o','r','l','d']  (different size)
```

```java
// map() — one-to-one
List<String> words = List.of("hello", "world");
List<Integer> lengths = words.stream()
    .map(String::length)        // "hello" → 5, "world" → 5
    .collect(Collectors.toList());
// lengths: [5, 5]

// flatMap() — one-to-many
List<String> words = List.of("hello", "world");
List<Character> chars = words.stream()
    .flatMap(word -> word.chars().mapToObj(c -> (char) c))  // "hello" → ['h','e','l','l','o']
    .collect(Collectors.toList());
// chars: ['h','e','l','l','o','w','o','r','l','d']

// Real-world example: handling nested collections
List<List<String>> nested = List.of(
    List.of("A", "B"),
    List.of("C", "D")
);

// ❌ map gives nested structure
List<Stream<String>> nestedStreams = nested.stream()
    .map(List::stream)           // List → Stream (nested)
    .collect(Collectors.toList());

// ✅ flatMap flattens the structure
List<String> flat = nested.stream()
    .flatMap(List::stream)        // Flattens to single stream
    .collect(Collectors.toList());
// flat: [A, B, C, D]

// Another example: handling Optional values
List<Optional<String>> optionals = List.of(
    Optional.of("A"),
    Optional.empty(),
    Optional.of("C")
);

// flatMap with Optional (Java 9+)
List<String> values = optionals.stream()
    .flatMap(Optional::stream)     // Filters out empty Optionals
    .collect(Collectors.toList());
// values: [A, C]
```

> **// JUNIOR NOTE:** Remember: `map()` is for transforming each element to exactly one new element. `flatMap()` is for transforming each element to a stream of elements, then flattening all those streams into one. A common use case for `flatMap()` is flattening nested collections.

---

## Q51 — What is a lambda expression in Java? What is it compiled to?

**Short answer**

A **lambda expression** is a concise way to represent an anonymous function. It provides a clear and concise way to implement **functional interfaces** (interfaces with a single abstract method). At compile time, lambdas are compiled to **synthetic methods** using `invokedynamic` bytecode instruction.

**In depth**

**Lambda syntax:**
```java
(parameters) -> { body }

// Examples:
() -> 42                                    // No parameters, returns 42
x -> x * 2                                  // Single parameter, returns x*2
(x, y) -> x + y                             // Two parameters, returns sum
(String s) -> s.length()                    // Explicit parameter type
() -> { System.out.println("Hello"); }      // Multiple statements
```

**What it compiles to:**
- **Not an anonymous inner class:** Lambdas are NOT compiled to anonymous classes
- **invokedynamic:** Uses the `invokedynamic` bytecode instruction (Java 7+)
- **Bootstrap method:** At runtime, the JVM creates a `CallSite` that points to the implementation
- **Method handles:** Uses `MethodHandle` for efficient invocation
- **No separate class files:** Doesn't create additional .class files like anonymous classes

```java
// Lambda
Runnable r = () -> System.out.println("Hello");

// Equivalent anonymous inner class (NOT how lambdas work!)
Runnable r2 = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// Lambda is syntactic sugar for functional interface implementation
Function<String, Integer> func = s -> s.length();  // Function<T,R>
Predicate<Integer> pred = i -> i > 5;              // Predicate<T>
Supplier<String> sup = () -> "Hello";              // Supplier<T>
```

**Method references — even more concise:**
```java
// Lambda
List<String> names = List.of("Alice", "Bob");
names.forEach(name -> System.out.println(name));

// Method reference (even shorter)
names.forEach(System.out::println);

// Types of method references:
Function<String, Integer> ref1 = String::length;         // Class::instanceMethod
Supplier<List<String>> ref2 = ArrayList::new;          // Class::new (constructor)
Predicate<String> ref3 = String.isEmpty();              // Instance::instanceMethod
Function<String, String> ref4 = String.toUpperCase;    // Class::instanceMethod
```

> **// JUNIOR NOTE:** Many juniors think lambdas are just "anonymous inner classes with shorter syntax." That's **incorrect**. Lambdas are much more efficient — they don't create extra class files and use `invokedynamic` for performance. This is why lambdas were introduced in Java 8 — to enable functional programming efficiently.

---

## Q52 — Standard functional interfaces

**Short answer**

Java provides built-in functional interfaces in the `java.util.function` package. The most common are `Function<T,R>`, `Predicate<T>`, `Consumer<T>`, `Supplier<T>`, `UnaryOperator<T>`, and `BinaryOperator<T>`.

**In depth**

| Interface | Method | Input | Output | Use Case |
|-----------|--------|-------|--------|----------|
| `Function<T,R>` | `R apply(T t)` | 1 | 1 | Transform T to R |
| `Predicate<T>` | `boolean test(T t)` | 1 | boolean | Test condition |
| `Consumer<T>` | `void accept(T t)` | 1 | void | Consume value |
| `Supplier<T>` | `T get()` | 0 | 1 | Supply value |
| `UnaryOperator<T>` | `T apply(T t)` | 1 | 1 | Operate on T |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | 2 | 1 | Combine T values |

```java
// Function — transform
Function<String, Integer> toLength = String::length;
Integer len = toLength.apply("hello");  // 5

// Predicate — test condition
Predicate<String> isLong = s -> s.length() > 3;
boolean result = isLong.test("hello");  // true

// Consumer — consume value (side effect)
Consumer<String> printer = System.out::println;
printer.accept("Hello");  // prints "Hello"

// Supplier — produce value
Supplier<Double> random = Math::random;
Double value = random.get();  // random number

// UnaryOperator — operate on same type
UnaryOperator<String> upper = String::toUpperCase;
String s = upper.apply("hello");  // "HELLO"

// BinaryOperator — combine two values
BinaryOperator<Integer> sum = (a, b) -> a + b;
Integer total = sum.apply(5, 3);  // 8
```

**Primitive-specialized functional interfaces:**

| Interface | Use Case |
|-----------|----------|
| `IntFunction<R>` | Takes int, returns R |
| `IntPredicate` | Takes int, returns boolean |
| `IntSupplier` | Returns int |
| `IntConsumer` | Consumes int |
| `ToIntFunction<T>` | Takes T, returns int |
| `IntUnaryOperator` | Takes int, returns int |

**Chaining functional interfaces:**
```java
// Function composition
Function<String, String> toUpper = String::toUpperCase;
Function<String, String> addExclamation = s -> s + "!";
Function<String, String> combined = toUpper.andThen(addExclamation);
String result = combined.apply("hello");  // "HELLO!"

// Predicate composition
Predicate<String> startsWithA = s -> s.startsWith("A");
Predicate<String> endsWithZ = s -> s.endsWith("z");
Predicate<String> startsAEndsZ = startsWithA.and(endsWithZ);
boolean test = startsAEndsZ.test("Alphabeta");  // false
```

> **// JUNIOR NOTE:** These functional interfaces are the foundation of Java's functional programming support. They're used everywhere in Streams, CompletableFuture, and many other APIs. Memorize the core ones: `Function`, `Predicate`, `Consumer`, and `Supplier`. The others are just variations.

---

## Quick-reference cheat sheet

```
Stream:
  - Sequence of elements, not a data structure
  - Lazy evaluation (intermediate operations)
  - Single use (cannot reuse after terminal)
  - Functional (doesn't modify source)

Intermediate Operations (lazy):
  filter(), map(), flatMap(), sorted(), distinct()
  limit(), skip(), peek()

Terminal Operations (eager):
  collect(), forEach(), count(), reduce()
  findFirst(), findAny(), anyMatch(), allMatch()

map() vs flatMap():
  map()     → 1 input → 1 output (one-to-one)
  flatMap() → 1 input → 0..N outputs (one-to-many, flattened)

Primitive Streams:
  IntStream, LongStream, DoubleStream
  Specialized: sum(), average(), range(), summaryStatistics()

Lambda:
  (parameters) -> { body }
  Compiled using invokedynamic (not anonymous class!)
  Method references: Class::method, Class::new

Functional Interfaces (java.util.function):
  Function<T,R>   → apply(T) → R
  Predicate<T>    → test(T) → boolean
  Consumer<T>     → accept(T) → void
  Supplier<T>     → get() → T
  UnaryOperator<T> → apply(T) → T
  BinaryOperator<T> → apply(T, T) → T
```

---

## Bonus Q & A

**Q1: What is the difference between `Stream` and `Collection`?**

**Q2: What is the difference between `forEach()` and `peek()`?**

**Q3: What is the difference between `findFirst()` and `findAny()`?**

**Q4: What is the difference between `reduce()` and `collect()`?**

**Q5: What is the difference between `map()` and `flatMap()`?**

**Q6: What is the difference between `filter()` and `distinct()`?**

**Q7: What is the difference between `sorted()` and `unordered()`?**

**Q8: What is the difference between `parallelStream()` and `stream()`?**

**Q9: What is the difference between `limit()` and `skip()`?**

**Q10: What is the difference between `allMatch()`, `anyMatch()`, and `noneMatch()`?**

---