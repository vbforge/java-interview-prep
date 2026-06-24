# 02 — Data types & Strings

- > **Questions covered:** Q9–Q17
- > **Demo:** [demo-02-data-type-strings-oop-generics](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q9 — The two main groups of data types

**Short answer**

Java has two main groups of data types: **primitive types** and **reference types**. Primitive types store values directly. Reference types store references (addresses) to objects on the heap.

**In depth**

| Group | What they store | Examples | Where they live |
|-------|-----------------|----------|-----------------|
| **Primitive types** | Actual values | `int`, `long`, `boolean`, `double`, `char`, `byte`, `short`, `float` | Stack (local) or Heap (fields) |
| **Reference types** | Memory address (pointer) | All classes, interfaces, arrays, enums | Reference on Stack, Object on Heap |

```java
int x = 42;              // x stores the VALUE 42 directly
String s = "hello";      // s stores a REFERENCE (address) to a String object
int[] arr = new int[10]; // arr stores a REFERENCE to an array object
```

**Primitive types are not objects** — they have no methods, no identity, no header. They are just bits in memory. That's why you can't call methods on them: `int x = 5; x.toString()` doesn't work directly (autoboxing would convert to `Integer` first).

> **// JUNIOR NOTE:** A common mistake is thinking "everything in Java is an object." Primitives are NOT objects. The phrase "Everything in Java is an object except primitives" is more accurate.

---

## Q10 — How to compare primitives and objects

**Short answer**

Primitives are compared using `==` (compares values). Objects are compared using `==` (compares references) OR `.equals()` (compares content/logical equality).

**In depth**

| Comparison | What it compares | When to use |
|------------|------------------|-------------|
| `==` with primitives | Values | Always correct for primitives |
| `==` with objects | References (memory addresses) | Checking if two references point to the exact same object |
| `.equals()` | Content / logical equality | Comparing object content (String, custom classes) |

```java
int a = 5;
int b = 5;
boolean result = (a == b);  // true — values are equal

String s1 = "hello";
String s2 = "hello";
boolean refsEqual = (s1 == s2);       // true (string pool, same reference)
boolean contentEqual = s1.equals(s2); // true (content is the same)

String s3 = new String("hello");
boolean refsEqual2 = (s1 == s3);      // false — different objects
boolean contentEqual2 = s1.equals(s3); // true — content is the same
```

**⚠️ Never use `==` to compare object content** — always use `.equals()` (or `Objects.equals()` for null-safety).

> **// JUNIOR NOTE:** `String` has its own `.equals()` implementation that compares characters. For custom classes, you must override `equals()` and `hashCode()` if you want content-based comparison.

---

## Q11 — Passing method arguments by value vs by reference

**Short answer**

Java is **always pass-by-value**. For primitives, the value is copied. For objects, the **reference** is copied (also pass-by-value, the reference value is copied).

**In depth**

**Pass-by-value for primitives:**
```java
void change(int x) {
    x = 100;  // only the copy changes
}

int num = 5;
change(num);
// num is still 5 — the original was not modified
```

**Pass-by-value for objects (reference copy):**
```java
void modify(Person p) {
    p.setName("Alice");  // modifies the object the reference points to
    p = new Person();    // reassigns the COPY of the reference
}

Person person = new Person("Bob");
modify(person);
// person still points to the original object, but its name is now "Alice"
// The reassignment inside the method did NOT affect the original reference
```

**Key insight:** When you pass an object reference, you're passing the *value of the reference*. That's why reassigning the parameter inside the method doesn't affect the original reference.

> **// JUNIOR NOTE:** Many juniors think Java is "pass-by-reference" for objects. That's incorrect. Java is **always** pass-by-value. The confusion comes from the fact that the reference value (memory address) is what gets copied, not the object itself.

---

## Q12 — Object o = new Object(); — What is stored in o? Where does new Object() live?

**Short answer**

`o` stores a **reference** (memory address) to the `Object` instance. The `new Object()` object lives on the **heap**.

**In depth**

```java
Object o = new Object();
```

This single line does two things:
1. **On the heap:** `new Object()` allocates memory for an `Object` instance — about 16 bytes (header) for an empty object (JVM-specific).
2. **On the stack:** The reference variable `o` is stored on the stack (if it's a local variable) and holds the memory address of that heap object.

```
Stack (current method frame)
┌─────────────────────────────────────────────┐
│       o → 0x7f3c2a1b4c (reference)          │
├─────────────────────────────────────────────┤
│       x = 42 (primitive)                    │
├─────────────────────────────────────────────┤
│       flag = true (primitive)               │
└─────────────────────────────────────────────┘
                    │
                    ▼
Heap
┌─────────────────────────────────────────────┐
│       Object @ 0x7f3c2a1b4c                 │
│        • header (mark word, class ptr)      │
│        • no fields (empty object)           │
├─────────────────────────────────────────────┤
│       String @ 0x7f3c2a1b5d                 │
│        • value → char[] @ 0x7f3c2a1b6e      │
│        • hash = 0 (cached)                  │
└─────────────────────────────────────────────┘
```

**If `o` is a field** (instance variable), the reference is stored inside the containing object on the heap, not on the stack.

> **// JUNIOR NOTE:** The most common mistake: "o is the object." No, o is the *reference* to the object. The object is on the heap. The reference is on the stack (or inside another object if it's a field).

---

## Q13 — Boxing / unboxing

**Short answer**

**Boxing** is converting a primitive to its corresponding wrapper class object. **Unboxing** is converting a wrapper object back to a primitive. This happens automatically in Java (autoboxing / autounboxing).

**In depth**

| Primitive | Wrapper Class |
|-----------|---------------|
| `int` | `Integer` |
| `long` | `Long` |
| `double` | `Double` |
| `boolean` | `Boolean` |
| `char` | `Character` |

```java
// Boxing (primitive → wrapper)
int primitive = 42;
Integer wrapped = Integer.valueOf(primitive);  // explicit boxing
Integer autoBoxed = primitive;                 // autoboxing (compiler does it)

// Unboxing (wrapper → primitive)
Integer wrappedNum = 100;
int unboxed = wrappedNum.intValue();           // explicit unboxing
int autoUnboxed = wrappedNum;                  // autounboxing

// Common autoboxing trap:
Integer a = 127;
Integer b = 127;
// a == b is true (Integer cache for -128 to 127)

Integer c = 128;
Integer d = 128;
// c == d is false (different objects)
// Use c.equals(d) instead!
```

**Cost of boxing:** Autoboxing creates objects on the heap, which adds GC pressure. In performance-critical code, use primitives instead of wrappers.

> **// JUNIOR NOTE:** The `Integer` cache (values -128 to 127) is a common interview trap. Always use `.equals()` for comparing wrapper objects, not `==`. Also, avoid autoboxing in tight loops — it creates unnecessary objects.

---

## Q14 — Is there a difference between String s1 = "string" and String s2 = new String("string")?

**Short answer**

**Yes.** `String s1 = "string"` uses the **string pool** (interned). `String s2 = new String("string")` creates a **new object** on the heap, bypassing the pool.

**In depth**

```java
String s1 = "hello";                     // Uses string pool
String s2 = new String("hello");         // Creates new object on heap
String s3 = "hello";                     // Reuses s1's object from pool

boolean isSameRef1 = (s1 == s2);  // false — different objects
boolean isSameRef2 = (s1 == s3);  // true — same object from pool
boolean isEqual = s1.equals(s2);  // true — content is the same
```

**The String Pool (String Intern Pool):**
- Stored in the heap (since Java 7+, before that in PermGen)
- Used to save memory by reusing `String` literals
- When you use `""` literal, the JVM checks if the string already exists in the pool
- If it exists, the reference is reused; if not, a new string is added to the pool

**String objects created with `new`:**
- Always create a new `String` object on the heap
- Do NOT use the pool (unless you call `.intern()`)
- Usually unnecessary — prefer `""` literals

> **// JUNIOR NOTE:** In modern Java, you should almost never use `new String("...")`. Use the literal syntax `"..."` instead. The only exception is when you explicitly need a new object (rare). The compiler and JVM optimize string literals automatically.

---

## Q15 — s1 == s2 vs s1.equals(s2)

**Short answer**

`==` compares **references** (memory addresses). `.equals()` compares **content** (logical equality). For strings, always use `.equals()` unless you specifically need reference equality.

**In depth**

```java
String s1 = "hello";
String s2 = new String("hello");
String s3 = "hello";

// Reference equality (memory addresses)
s1 == s2   // false — different objects
s1 == s3   // true — same object from string pool

// Content equality
s1.equals(s2)   // true — both contain "hello"
s1.equals(s3)   // true — both contain "hello"

// Null-safe alternative
Objects.equals(s1, s2)   // true, also handles nulls
```

**When `==` with objects makes sense:**
- Comparing enum constants: `if (status == Status.ACTIVE)`
- Checking if two references point to the same object instance
- Comparing primitive values (not objects)

**When `.equals()` is required:**
- String content comparison
- Custom objects where logical equality matters
- Any time you care about values, not memory addresses

> **// JUNIOR NOTE:** The most common junior mistake in interviews: using `==` to compare strings. Always explain that `==` compares references and `.equals()` compares content. For strings, always use `.equals()`!

---

## Q16 — String.intern()

**Short answer**

`String.intern()` adds the string to the **string pool** if it's not already there, and returns a reference to the pooled string. It can be used to manually pool strings to save memory.

**In depth**

```java
String s1 = new String("hello");
String s2 = s1.intern();      // Adds "hello" to pool (if not already there)
String s3 = "hello";          // Uses the pooled string

s1 == s2   // false — s1 is heap object, s2 is pooled
s2 == s3   // true — both from pool
```

**When to use `intern()`:**
- **Memory optimization:** If you have many duplicate strings, interning can save memory
- **Performance:** `==` comparison of interned strings is faster than `.equals()`
- **When to avoid:** If you intern too many unique strings, you can cause memory issues (the pool is not unlimited)

**How the string pool works:**
- Before Java 7: Pool was in PermGen (fixed size, could cause OutOfMemoryError)
- Java 7+: Pool is on the regular heap, can grow dynamically
- Interned strings are garbage-collected (unlike before Java 7)

> **// JUNIOR NOTE:** Modern applications rarely need to call `intern()` manually. The JVM handles string pooling for literals automatically. In most cases, explicitly calling `intern()` is unnecessary and can even hurt performance if overused.

---

## Q17 — Are String objects mutable or immutable?

**Short answer**

**Immutable.** Once a `String` object is created, its value cannot be changed. Any method that "modifies" a string (like `toUpperCase()`, `concat()`, `replace()`) returns a **new** `String` object.

**In depth**

```java
String s = "hello";
String upper = s.toUpperCase();

// s is still "hello"
// upper is "HELLO" (new object)

s = s.concat(" world");  // s now points to a NEW string "hello world"
                         // The original "hello" string is unchanged and may be GC'd
```

**Why strings are immutable:**
- **Security:** Strings are used in class loading, file paths, passwords, etc. Immutability prevents modification
- **String pool:** Reusing string literals safely requires immutability
- **Thread safety:** Strings can be shared freely between threads
- **Hash caching:** String hash code is computed once and cached
- **Performance:** Many string operations are optimized (e.g., substring shares char array)

**Alternatives for mutable strings:**
- `StringBuilder` — not thread-safe, fastest for concatenation in loops
- `StringBuffer` — thread-safe, slower (synchronized methods)

```java
// Use StringBuilder for heavy modifications
StringBuilder sb = new StringBuilder("hello");
sb.append(" world");
String result = sb.toString();  // "hello world" — no intermediate objects created
```

> **// JUNIOR NOTE:** A common interview question: "How many objects are created by `String s = new String("abc")`?" Answer: At least two — one in the pool (`"abc"`) and one on the heap (the `new` object). This is why you should avoid `new String("...")`.

---

## Quick-reference cheat sheet

```
Primitives   → 8 types: byte, short, int, long, float, double, char, boolean
             → stored on stack (local) or heap (fields)
             → compared with == (value comparison)

References   → classes, interfaces, arrays, enums
             → reference on stack, object on heap
             → compared with == (reference) or .equals() (content)

Passing      → ALWAYS pass-by-value in Java
             → primitives: value copied
             → objects: reference value copied (not the object)

String pool  → stores string literals for reuse
             → s1 = "hello" → pool
             → new String("hello") → heap (bypasses pool)
             → .intern() → returns pooled reference

String       → IMMUTABLE (any modification creates a new object)
             → use StringBuilder for mutable strings

Boxing       → primitive → wrapper (e.g., int → Integer)
Unboxing     → wrapper → primitive (e.g., Integer → int)
             → automatic (autoboxing/autounboxing)
             → watch out for Integer cache (-128 to 127)
```

---

## Bonus Q & A

**Q1: What is the difference between `String`, `StringBuilder`, and `StringBuffer`?**

**Q2: What is the default value of a primitive type vs a reference type?**

**Q3: What is the difference between `int` and `Integer`?**

**Q4: What is the difference between `==` and `equals()` for `String`?**

**Q5: What happens when you concatenate strings with `+` in a loop?**

**Q6: What is the difference between `String.valueOf()` and `toString()`?**

**Q7: What is the difference between `parseInt()` and `valueOf()`?**

**Q8: What is the difference between `"abc"` and `new String("abc")`?**

**Q9: What is the difference between `trim()` and `strip()` in Java 11+?**

**Q10: What is the difference between `char` and `Character`?**

---

