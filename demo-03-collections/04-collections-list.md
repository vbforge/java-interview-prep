# 04 — Collections — List

- > **Questions covered:** Q28–Q33
- > **Demo:** [demo-03-collections](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q28 — What kinds of collections exist in Java?

**Short answer**

Java collections framework includes three main types: **List** (ordered, allows duplicates), **Set** (no duplicates, unordered), and **Map** (key-value pairs). Each has multiple implementations with different performance characteristics.

**In depth**

| Interface | Characteristics | Common Implementations |
|-----------|-----------------|------------------------|
| **List** | Ordered, allows duplicates, index-based access | `ArrayList`, `LinkedList`, `Vector` |
| **Set** | No duplicates, no guaranteed order | `HashSet`, `TreeSet`, `LinkedHashSet` |
| **Queue** | FIFO (or priority) ordering | `LinkedList`, `PriorityQueue`, `ArrayDeque` |
| **Map** | Key-value pairs, unique keys | `HashMap`, `TreeMap`, `LinkedHashMap` |

```
Java Collections Framework Hierarchy

Iterable
    │
    Collection
        │
        ├── List
        │    ├── ArrayList
        │    ├── LinkedList
        │    └── Vector
        │
        ├── Set
        │    ├── HashSet
        │    ├── TreeSet
        │    └── LinkedHashSet
        │
        └── Queue
             ├── LinkedList
             └── PriorityQueue

            Map (separate hierarchy — does NOT extend Collection)
             ├── HashMap
             ├── TreeMap
             ├── LinkedHashMap
             ├── Hashtable
             └── ConcurrentHashMap
```

> **// JUNIOR NOTE:** `Map` does *not* extend `Collection` — it's a separate interface in the hierarchy. This is because maps store key-value pairs, not individual elements like collections do.

---

## Q29 — Describe List

**Short answer**

A **List** is an ordered collection that allows duplicate elements. It provides **index-based access** to elements, meaning you can get, set, insert, and remove elements by their position (0-based index).

**In depth**

**Key characteristics of List:**
- **Ordered** — elements are stored in the order they were inserted
- **Duplicates allowed** — can contain the same element multiple times
- **Index-based** — access elements by position: `get(index)`, `set(index, element)`
- **Positional access** — insert at specific position: `add(index, element)`
- **Search** — find position of element: `indexOf(element)`

```java
// List interface methods
List<String> list = new ArrayList<>();
list.add("Apple");          // [Apple]
list.add("Banana");         // [Apple, Banana]
list.add(1, "Cherry");      // [Apple, Cherry, Banana]
String fruit = list.get(1); // "Cherry"
list.set(0, "Apricot");     // [Apricot, Cherry, Banana]
int index = list.indexOf("Cherry"); // 1
list.remove(1);             // [Apricot, Banana]
```

> **// JUNIOR NOTE:** `List` is an *interface*, not a class. You can't instantiate `List` directly — you always use one of its implementations like `ArrayList` or `LinkedList`.

---

## Q30 — Time complexity of insertion and read/access

**Short answer**

**ArrayList:** Access/read is `O(1)`, insertion at end is `O(1)` (amortized), insertion at beginning/middle is `O(n)`.
**LinkedList:** Access/read is `O(n)`, insertion at beginning/end is `O(1)`, insertion at middle is `O(n)` (due to traversal).

**In depth**

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| **Get by index** | O(1) — direct array access | O(n) — traverse from head/tail |
| **Set by index** | O(1) — direct array assignment | O(n) — traverse then set |
| **Add at end** | O(1) amortized* | O(1) — add to tail |
| **Add at beginning** | O(n) — shift all elements | O(1) — add to head |
| **Add at middle** | O(n) — shift elements | O(n) — traverse to position |
| **Remove at end** | O(1) — remove last | O(1) — remove from tail |
| **Remove at beginning** | O(n) — shift all elements | O(1) — remove from head |
| **Remove at middle** | O(n) — shift elements | O(n) — traverse to position |
| **Search (indexOf)** | O(n) — linear scan | O(n) — linear scan |

**\*ArrayList add amortized O(1):** When the internal array is full, it grows by 50% (doubles in older versions). This resize operation is O(n) but happens infrequently, so the *amortized* cost is O(1).

```java
// ArrayList size management
ArrayList<String> list = new ArrayList<>();  // Initial capacity: 10
// When adding element 11, array grows to capacity 15 (50% increase in Java 8+)
```

> **// JUNIOR NOTE:** The amortized O(1) for `ArrayList.add()` is important to understand. While a single add might trigger a resize (O(n)), the *average* cost over many adds is O(1). This is why `ArrayList` is usually the default choice.

---

## Q31 — When is an array-based list better, when is a linked list better?

**Short answer**

**ArrayList** is better when you need frequent **random access** (reading by index) and most modifications are at the **end**.
**LinkedList** is better when you need frequent **insertions/deletions at the beginning** or when you're using it as a **queue/stack**.

**In depth**

**Choose ArrayList when:**
- You need fast `get()` and `set()` operations (O(1))
- You're mostly adding elements at the end
- You need to iterate over the list frequently (better cache locality)
- Memory overhead is a concern (less memory per element)
- You know the approximate size in advance (can set initial capacity)

**Choose LinkedList when:**
- You need frequent insertions/deletions at the beginning or middle
- You're using the list as a queue or stack (`addFirst()`, `removeFirst()`)
- You don't need random access (or it's rare)
- The list size changes dramatically and frequently

```java
// Good use of ArrayList: reading data, adding at end
List<User> users = new ArrayList<>();
users.add(new User("Alice"));  // Add at end → O(1)
users.add(new User("Bob"));    // Add at end → O(1)
User first = users.get(0);     // Random access → O(1)

// Good use of LinkedList: queue operations
Queue<Task> queue = new LinkedList<>();
queue.add(new Task());         // Add to tail → O(1)
Task task = queue.poll();      // Remove from head → O(1)
```

**Memory comparison:**
- **ArrayList:** Stores elements contiguously in an array. Overhead is minimal.
- **LinkedList:** Each element is a separate node with two pointers (prev/next). Much higher memory overhead (~40 bytes per element).

> **// JUNIOR NOTE:** In most real-world applications, `ArrayList` is the better choice. `LinkedList` is overused by juniors who think "linked list = better for insertion." But unless you're inserting at the beginning, `ArrayList` often performs better due to cache locality and lower memory overhead.

---

## Q32 — How does a list differ from a plain array?

**Short answer**

A **List** is a dynamic, resizable collection with utility methods, while an **array** is a fixed-size, primitive-level data structure. Lists are part of the Collections Framework and provide more functionality and flexibility.

**In depth**

| Feature | Array | List (ArrayList) |
|---------|-------|------------------|
| **Size** | Fixed — cannot change after creation | Dynamic — grows/shrinks automatically |
| **Type** | Can hold primitives and objects | Only objects (wrappers for primitives) |
| **Syntax** | `String[] arr = new String[10];` | `List<String> list = new ArrayList<>();` |
| **Access** | `arr[0]` (bracket notation) | `list.get(0)` (method call) |
| **Utility methods** | Minimal (`Arrays.toString()`, `Arrays.sort()`) | Rich (`add()`, `remove()`, `contains()`, `indexOf()`, etc.) |
| **Performance** | Fastest, lowest overhead | Slightly more overhead due to method calls |
| **Generics** | Limited (reified types, can't have generic arrays) | Full generic support |
| **Iteration** | Enhanced for loop, manual index | Enhanced for loop, iterator, stream, forEach |

```java
// Array — fixed size
String[] arr = new String[3];
arr[0] = "A";
arr[1] = "B";
arr[2] = "C";
// arr[3] = "D"; // ArrayIndexOutOfBoundsException

// List — dynamic size
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");
list.add("D");  // Automatically grows
list.remove(1); // Remove "B"
list.contains("A"); // true
```

**Converting between arrays and lists:**
```java
// Array → List
String[] arr = {"A", "B", "C"};
List<String> list = Arrays.asList(arr);  // Fixed-size view
List<String> mutableList = new ArrayList<>(Arrays.asList(arr));  // Full copy

// List → Array
List<String> list = new ArrayList<>();
String[] arr = list.toArray(new String[0]);  // Preferred approach
```

> **// JUNIOR NOTE:** `Arrays.asList()` returns a *fixed-size* list backed by the original array. You can't `add()` or `remove()` from it — that will throw `UnsupportedOperationException`. If you need a mutable list, wrap it in `new ArrayList<>()`.

---

## Q33 — What is an iterator?

**Short answer**

An **Iterator** is an object that enables traversal of a collection. It provides `hasNext()` to check if there are more elements, `next()` to get the next element, and `remove()` to remove the current element.

**In depth**

```java
// Basic Iterator usage
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

Iterator<String> iterator = list.iterator();
while (iterator.hasNext()) {
    String element = iterator.next();
    System.out.println(element);
}
```

**Key Iterator methods:**

| Method | Description | Throws |
|--------|-------------|--------|
| `hasNext()` | Returns `true` if there are more elements | — |
| `next()` | Returns the next element and advances | `NoSuchElementException` |
| `remove()` | Removes the last element returned by `next()` | `IllegalStateException` |

**Why use Iterator over enhanced for loop?**
- **Remove elements safely:** You can remove elements during iteration without `ConcurrentModificationException`
- **Multiple iterations:** You can have multiple iterators over the same collection
- **Generic collections:** Works with any `Collection` type

```java
// Safe removal with Iterator
List<String> names = new ArrayList<>();
names.add("Alice");
names.add("Bob");
names.add("Charlie");

Iterator<String> it = names.iterator();
while (it.hasNext()) {
    String name = it.next();
    if (name.startsWith("A")) {
        it.remove();  // Safe! No ConcurrentModificationException
    }
}
// names now contains: [Bob, Charlie]

// ❌ Wrong — causes ConcurrentModificationException
for (String name : names) {
    if (name.startsWith("A")) {
        names.remove(name);  // ConcurrentModificationException!
    }
}
```

**ListIterator — bidirectional iterator:**
```java
ListIterator<String> listIt = list.listIterator();
while (listIt.hasNext()) {
    String element = listIt.next();
    // Can also go backwards
}
while (listIt.hasPrevious()) {
    String element = listIt.previous();  // Traverse backwards
}
```

> **// JUNIOR NOTE:** The most common iterator mistake is trying to modify a collection while iterating over it with an enhanced for loop. Always use an `Iterator` (or `ListIterator`) when you need to remove elements during iteration. The enhanced for loop uses an iterator internally but doesn't expose the `remove()` method.

---

## Quick-reference cheat sheet

```
Collections:
  List    → Ordered, duplicates allowed, index-based
  Set     → No duplicates, unordered
  Map     → Key-value pairs (NOT a Collection)
  Queue   → FIFO/priority ordering

ArrayList vs LinkedList:
  ArrayList  → O(1) get, O(1) add at end (amortized)
             → Best for: random access, iteration, add at end
  LinkedList → O(1) add/remove at beginning/end
             → Best for: queue/stack, frequent head modifications
             → Worst for: random access (O(n))

Array vs List:
  Array    → Fixed size, can hold primitives, fastest
  List     → Dynamic size, utility methods, flexible

Iterator:
  hasNext() → check if more elements
  next()    → get next element
  remove()  → safely remove current element
  ListIterator → bidirectional iteration

⚠️ Always use Iterator.remove() to remove during iteration!
```

---

## Bonus Q & A

**Q1: What is the difference between `ArrayList` and `Vector`?**

**Q2: What is the difference between `List` and `Set`?**

**Q3: What is the difference between `List` and `Queue`?**

**Q4: What is `ArrayDeque` and when would you use it over `LinkedList`?**

**Q5: What is `CopyOnWriteArrayList` and when is it useful?**

**Q6: What is `Collections.synchronizedList()` and how does it differ from `CopyOnWriteArrayList`?**

**Q7: What is the difference between `fail-fast` and `fail-safe` iterators?**

**Q8: What happens when you call `list.remove(index)` vs `list.remove(Object)`?**

**Q9: What is `Arrays.asList()` and what are its limitations?**

**Q10: What is the difference between `list.toArray()` and `list.toArray(new T[0])`?**


---

