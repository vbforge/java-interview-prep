# 05 — Collections — HashMap

- > **Questions covered:** Q34–Q44
- > **Demo:** [demo-03-collections](README.md)
- > **Sections that can't be skipped** per recruiter screen ✓

---

## Q34 — Describe HashMap

**Short answer**

**HashMap** is a hash table-based implementation of the `Map` interface. It stores key-value pairs and provides O(1) average-time performance for `get()` and `put()` operations. It allows one `null` key and multiple `null` values.

**In depth**

**Key characteristics:**
- **No ordering:** Does not guarantee any order of elements
- **Not thread-safe:** Not synchronized; use `Collections.synchronizedMap()` or `ConcurrentHashMap` for thread safety
- **Null support:** Allows one `null` key and many `null` values
- **Performance:** O(1) average time for `put()`, `get()`, `remove()`
- **Load factor:** Default 0.75, controls when to resize
- **Initial capacity:** Default 16 buckets

```java
// Basic HashMap usage
Map<String, Integer> map = new HashMap<>();
map.put("Apple", 100);
map.put("Banana", 200);
map.put("Cherry", 300);

Integer value = map.get("Banana");  // 200
boolean exists = map.containsKey("Apple");  // true
map.remove("Cherry");
```

> **// JUNIOR NOTE:** `HashMap` is not thread-safe. If multiple threads access a `HashMap` concurrently and at least one modifies it structurally, you'll get `ConcurrentModificationException`. Use `ConcurrentHashMap` for thread-safe scenarios.

---

## Q35 — How is HashMap implemented?

**Short answer**

`HashMap` is implemented as an **array of linked lists** (or trees for high collision) called **buckets**. Each bucket stores entries that have the same hash code. In Java 8+, when a bucket exceeds 8 entries, it's converted to a **balanced tree** (TreeNode) for better performance.

**In depth**

**Internal structure:**

```
HashMap (Java 8+) — Internal Structure

Node[] table = new Node[16]

[0] → null
[1] → Node(key=Bob, val=25) → Node(key=Alice, val=30)
[2] → null
[3] → Node(key=Charlie, val=40)
...
[15] → null

Node structure:
  int hash
  K key
  V value
  Node next → next node
```

**HashMap implementation details:**
- **Entry (Node):** Stores key, value, hash, and next pointer
- **Buckets:** Array where each index is a bucket
- **Collision handling:** Linked list (or tree) within each bucket
- **Tree threshold:** If a bucket has ≥ 8 nodes, it becomes a tree (TREEIFY_THRESHOLD = 8)
- **Untreeify threshold:** If a bucket has ≤ 6 nodes, it becomes a linked list (UNTREEIFY_THRESHOLD = 6)
- **Minimum tree capacity:** 64 (MIN_TREEIFY_CAPACITY)

```java
// Simplified HashMap put logic
public V put(K key, V value) {
    int hash = hash(key);
    int index = (n - 1) & hash;  // n = table.length
    
    Node node = table[index];
    if (node == null) {
        table[index] = new Node(hash, key, value, null);
    } else {
        // Traverse linked list or tree
        while (node != null) {
            if (node.hash == hash && node.key.equals(key)) {
                V oldValue = node.value;
                node.value = value;
                return oldValue;
            }
            node = node.next;
        }
        // Add new node to bucket
    }
}
```

> **// JUNIOR NOTE:** Since Java 8, when a bucket has many collisions, it uses a **tree** (balanced red-black tree) instead of a linked list. This improves worst-case performance from O(n) to O(log n). This is why `HashMap` is so efficient even with many collisions.

---

## Q36 — What is a hash code?

**Short answer**

A **hash code** is an `int` value returned by the `hashCode()` method. It is used to efficiently locate objects in hash-based collections like `HashMap`, `HashSet`, and `Hashtable`.

**In depth**

**hashCode() contract:**
1. **Consistency:** During one execution, the same object must consistently return the same hash code (unless modified)
2. **Equals consistency:** If two objects are equal (`equals()` returns `true`), they must have the **same** hash code
3. **Different objects:** Can have the same hash code (collision), but better distribution improves performance

```java
// String.hashCode() implementation
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;
        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];  // Polynomial hash
        }
        hash = h;
    }
    return h;
}

// Good practice: override both hashCode() and equals()
@Override
public int hashCode() {
    return Objects.hash(field1, field2, field3);
}

@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    MyClass other = (MyClass) obj;
    return Objects.equals(field1, other.field1) &&
           Objects.equals(field2, other.field2);
}
```

> **// JUNIOR NOTE:** **NEVER** override `equals()` without overriding `hashCode()`. If you do, your objects will break hash-based collections. The contract says: if `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` must also be true.

---

## Q37 — Hash code normalization (spreading / masking the hash)

**Short answer**

**Hash code spreading** improves the distribution of hash codes across buckets. It applies additional transformations to the original hash code to reduce collisions. **Masking** uses the `&` operator to map the hash to a valid bucket index.

**In depth**

**HashMap's hash() method:**
```java
// Java 8+ HashMap.hash() implementation
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

**Why spread the hash?**
- **Better distribution:** XOR-ing higher bits with lower bits spreads the hash evenly
- **Reduces collisions:** Especially important when the number of buckets is a power of two
- **Masking:** `(n - 1) & hash` where `n` is the array size (power of 2)

**Visual example:**

```
Hash Spreading & Bucket Index Masking

Key.hashCode()  0x12345678  0001 0010 0011 0100 0101 0110 0111 1000
                            └─────── high bits ───────┘ └─────── low bits ────────┘

XOR with (h >>> 16)

h >>> 16        0x00001234  0000 0000 0000 0000 0001 0010 0011 0100
                            └─────── zero bits ───────┘ └─────── high bits ───────┘

─────────────────────────────────────────────────────────────────────────────

Hash (spread)   0x1234444C  0001 0010 0011 0100 0100 0100 0100 1100
                            └─────── high bits ───────┘ └─────── spread bits ─────┘

Bucket index (masking)
  int index = (n - 1) & hash   // n=16 → n-1=15 → 0b1111
  Result: last 4 bits of spread hash = bucket index (0–15)
```

**Why power-of-two array size?**
- Using `2^n` size allows using `&` instead of `%` (modulo)
- `&` is much faster than modulo
- Ensures even distribution when combined with hash spreading

> **// JUNIOR NOTE:** The hash spreading is a clever optimization. Without it, objects with similar hash codes (especially consecutive integers) would collide more often. The spreading ensures that bits from all 32 bits of the hash are used, not just the lower bits.

---

## Q38 — How is hashCode related to equals?

**Short answer**

The relationship between `hashCode()` and `equals()` is defined by the contract: if two objects are **equal** according to `equals()`, they **must** have the **same** hash code. But objects with the same hash code may not be equal.

**In depth**

**The relationship visualized:**

```
Hash Code Buckets — equals() & hashCode() Contract

Bucket 100 → [ObjectA hash=100] → [ObjectB hash=100] → [ObjectC hash=100]

Rules:
  if ObjectA.equals(ObjectB) → true
    ⟹ ObjectA.hashCode() MUST equal ObjectB.hashCode()
    ⟹ They are stored in the SAME bucket

  if ObjectA.hashCode() == ObjectB.hashCode()
    ⟹ They are in the SAME bucket
    ⟹ equals() may still return false — this is a COLLISION
```

**HashMap uses both methods:**
1. **Find bucket:** `hashCode()` determines which bucket to search
2. **Find element:** `equals()` finds the exact key within the bucket

```java
// HashMap.get() simplified logic
public V get(Object key) {
    int hash = hash(key);
    int index = (n - 1) & hash;      // 1. Find bucket using hashCode()
    
    Node<K,V> node = table[index];
    while (node != null) {
        if (node.hash == hash && 
            (node.key == key || key.equals(node.key))) {  // 2. Find exact key using equals()
            return node.value;
        }
        node = node.next;
    }
    return null;
}
```

**Common mistakes:**
- ❌ Overriding `equals()` but not `hashCode()` → Breaks collections
- ❌ Returning constant hash code → All objects in same bucket (O(n) performance)
- ❌ Using mutable fields in hash calculation → Object becomes "lost" if modified

> **// JUNIOR NOTE:** This is the most common interview question about `HashMap`. Remember: **equals() implies same hashCode()**, but same hashCode() does NOT imply equals(). The first is a guarantee, the second is just a collision.

---

## Q39 — Why does that matter for a hash map?

**Short answer**

`HashMap` relies on both methods: `hashCode()` to find the correct bucket efficiently, and `equals()` to find the exact key within that bucket. If the contract is broken, you'll get unexpected behavior like duplicate keys, lost entries, or performance degradation.

**In depth**

**What happens if you break the contract:**

| Scenario | Result |
|----------|--------|
| `equals()` overridden, `hashCode()` not | Keys with same logical identity go to different buckets → duplicate keys |
| `hashCode()` returns constant value | All entries in one bucket → O(n) performance, defeats hash map |
| Using mutable fields in `hashCode()` | Key can't be found after mutation → memory leak |

```java
// ❌ WRONG: equals() but no hashCode()
class BadKey {
    private String id;
    
    @Override
    public boolean equals(Object obj) {
        return this.id.equals(((BadKey) obj).id);
    }
    // No hashCode() override → uses Object.hashCode()
}

Map<BadKey, String> map = new HashMap<>();
BadKey key1 = new BadKey("123");
BadKey key2 = new BadKey("123");  // Logically equal to key1
map.put(key1, "Value");
map.put(key2, "Duplicate");
// map now has TWO entries with the same logical key!
// key1 and key2 are in different buckets because hashCode() differs
```

> **// JUNIOR NOTE:** This is why you should always use `Objects.hash()` or `Objects.hashCode()` to implement `hashCode()` consistently with `equals()`. Also, **never** use mutable fields in `hashCode()` or `equals()` — it will cause keys to be lost forever!

---

## Q40 — Data structure in a bucket when there is a collision

**Short answer**

When multiple keys have the same bucket index (hash collision), the bucket stores a **linked list** of entries. In Java 8+, if the list grows to 8 or more entries, it's converted to a **balanced red-black tree** for better performance.

**In depth**

**Collision resolution:**

```
Bucket (array index 5) — Collision Linked List

┌──────────────────────────────────────────────────────────────┐
│ hash=100  |  key="Alice"  |  val=30  |  next ↓               │
├──────────────────────────────────────────────────────────────┤
│                              ↓                               │
├──────────────────────────────────────────────────────────────┤
│ hash=100  |  key="Bob"    |  val=25  |  next ↓               │
├──────────────────────────────────────────────────────────────┤
│                              ↓                               │
├──────────────────────────────────────────────────────────────┤
│ hash=100  |  key="Charlie"|  val=40  |  next ↓               │
├──────────────────────────────────────────────────────────────┤
│                              ↓                               │
└──────────────────────────────────────────────────────────────┘
                            null — end of linked list
```

**Java 8+ Tree conversion:**

| Condition | Behavior |
|-----------|----------|
| Bucket has 0-7 nodes | Linked list (O(n) worst-case) |
| Bucket has ≥ 8 nodes | Convert to red-black tree (O(log n) worst-case) |
| Bucket has ≤ 6 nodes | Convert back to linked list (UNTREEIFY_THRESHOLD) |

```java
// TreeNode (red-black tree node)
static final class TreeNode<K,V> extends Node<K,V> {
    TreeNode<K,V> parent;   // red-black tree links
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    TreeNode<K,V> prev;     // needed to unlink next upon deletion
    boolean red;
}
```

**Why tree conversion helps:**
- **Worst-case performance:** O(n) for linked list → O(log n) for tree
- **Protection against DOS attacks:** Malicious keys can cause many collisions
- **Java 8+ improvement:** Makes `HashMap` more robust

> **// JUNIOR NOTE:** The tree conversion is a Java 8+ optimization. Before Java 8, `HashMap` always used linked lists, which could lead to O(n) performance in worst-case scenarios. This is why it's important to have good `hashCode()` implementation.

---

## Q41 — What are capacity and load factor? What is the default load factor?

**Short answer**

**Capacity** is the number of buckets in the hash table. **Load factor** is the threshold that determines when to resize. Default load factor is **0.75**, and default initial capacity is **16**.

**In depth**

**Load factor formula:**
```
Resize threshold = capacity × load factor
Default: 16 × 0.75 = 12

When the number of entries exceeds 12, HashMap resizes to:
New capacity = old capacity × 2 = 32
```

| Setting | Default | Description |
|---------|---------|-------------|
| **Initial capacity** | 16 | Number of buckets when created |
| **Load factor** | 0.75 | Threshold for resizing (75% full) |
| **Resize factor** | 2x | Doubles the capacity on resize |

**Choosing a custom load factor:**
- **Higher (e.g., 0.9):** Less memory, more collisions, slower operations
- **Lower (e.g., 0.5):** More memory, fewer collisions, faster operations
- **Default 0.75:** Good trade-off between time and space

```java
// Creating with custom settings
Map<String, Integer> map1 = new HashMap<>();  // capacity=16, load=0.75

Map<String, Integer> map2 = new HashMap<>(100);  // capacity=100, load=0.75

Map<String, Integer> map3 = new HashMap<>(100, 0.9f);  // capacity=100, load=0.9

// If you know the size in advance, set capacity to avoid resizing
// capacity = (expected size / load factor) + 1
int expectedSize = 1000;
int capacity = (int) (expectedSize / 0.75f) + 1;
Map<String, Integer> map4 = new HashMap<>(capacity);
```

> **// JUNIOR NOTE:** If you know the approximate size of your map, **set the initial capacity**. Resizing is expensive (rehashes all entries). For example, if you know you'll have 1000 entries, use `new HashMap<>(1334)` (1000/0.75 + 1) to avoid resizing.

---

## Q42 — Time complexity of lookup and insertion

**Short answer**

**Average case:** O(1) for both `put()` and `get()`. **Worst case:** O(log n) (Java 8+, tree) or O(n) (Java 7-, linked list) when many collisions occur.

**In depth**

| Operation | Average Case | Worst Case (Java 7) | Worst Case (Java 8+) |
|-----------|--------------|---------------------|----------------------|
| **put()** | O(1) | O(n) | O(log n) |
| **get()** | O(1) | O(n) | O(log n) |
| **remove()** | O(1) | O(n) | O(log n) |
| **containsKey()** | O(1) | O(n) | O(log n) |

**Why average is O(1):**
- **Good hash distribution:** Keys spread evenly across buckets
- **Low load factor:** Buckets remain mostly empty (0.75)
- **Constant time operations:** Hash → bucket index → find/insert

**Why worst case happens:**
- **Poor hashCode() implementation:** All keys go to same bucket
- **Malicious input:** Keys designed to cause collisions (DoS attack)
- **High load factor:** Many entries per bucket

```java
// How O(1) works:
HashMap<String, Integer> map = new HashMap<>();
map.put("key", 42);  // O(1): compute hash, find bucket, insert

Integer val = map.get("key");  // O(1): compute hash, find bucket, get value

// If hashCode() is bad:
class BadKey {
    @Override
    public int hashCode() {
        return 1;  // ALL keys go to bucket 1 → O(n)
    }
}
```

> **// JUNIOR NOTE:** The O(1) is *average*, not guaranteed. A good `hashCode()` implementation is critical for performance. In interviews, mention that the worst-case is O(log n) in modern Java due to tree conversion.

---

## Q43 — In what order do you get elements if you iterate the map? Why?

**Short answer**

`HashMap` does **not guarantee any order**. The iteration order is based on the internal array of buckets, which changes on resizing. If you need predictable order, use `LinkedHashMap` (insertion order) or `TreeMap` (natural order).

**In depth**

**Why order is unpredictable:**
- **Bucket order:** Iteration follows the array from index 0 to n-1
- **Collision order:** Within each bucket, it follows the linked list/tree
- **Resizing:** When capacity changes, entries are rehashed to new buckets

```java
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);
map.put("B", 2);
map.put("C", 3);
map.put("D", 4);

// Order might be: [B=2, A=1, D=4, C=3] (depends on hash codes)
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + "=" + entry.getValue());
}

// If you need ordered map:
Map<String, Integer> ordered = new LinkedHashMap<>();  // Insertion order
Map<String, Integer> sorted = new TreeMap<>();          // Natural order
```

| Map Type | Iteration Order |
|----------|-----------------|
| `HashMap` | No guaranteed order |
| `LinkedHashMap` | Insertion order |
| `TreeMap` | Natural order (key's Comparable) |

> **// JUNIOR NOTE:** Don't rely on `HashMap` iteration order. If you need predictable order, use `LinkedHashMap` or `TreeMap`. Also note that `HashMap` is not sorted — keys are not kept in any logical order.

---

## Q44 — Difference between HashMap and TreeMap

**Short answer**

**HashMap** uses a hash table (O(1) average) and does not guarantee order. **TreeMap** uses a red-black tree (O(log n) operations) and maintains **sorted** order based on the natural ordering of keys or a custom comparator.

**In depth**

| Feature | HashMap | TreeMap |
|---------|---------|---------|
| **Implementation** | Array of buckets + trees | Red-black tree |
| **Ordering** | No guaranteed order | Sorted (natural or comparator) |
| **Null key** | Allows one `null` key | No `null` key (throws exception) |
| **Performance** | O(1) average, O(log n) worst | O(log n) always |
| **Memory** | Less overhead | More overhead (tree nodes) |
| **When to use** | Fast lookups, no order needed | Sorted iteration, range queries |

```java
// HashMap — fast but unordered
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put("Zebra", 10);
hashMap.put("Apple", 5);
hashMap.put("Banana", 7);
// Output order: unpredictable

// TreeMap — slower but sortedMap<String, Integer> treeMap = new TreeMap<>();
treeMap.put("Zebra", 10);
treeMap.put("Apple", 5);
treeMap.put("Banana", 7);
// Output order: [Apple=5, Banana=7, Zebra=10] (sorted)

// TreeMap with custom comparator (reverse order)
Map<String, Integer> reverseMap = new TreeMap<>(Comparator.reverseOrder());
reverseMap.put("Zebra", 10);
reverseMap.put("Apple", 5);
reverseMap.put("Banana", 7);
// Output: [Zebra=10, Banana=7, Apple=5]
```

**When to use TreeMap:**
- Need to iterate keys in sorted order
- Range queries: `subMap()`, `headMap()`, `tailMap()`
- Natural ordering of keys (e.g., strings, numbers)

**When to use HashMap:**
- Most common scenarios
- Performance-critical applications
- No ordering requirements

> **// JUNIOR NOTE:** If you need a map with predictable iteration order but don't need sorting, use `LinkedHashMap` instead of `TreeMap`. It provides O(1) performance (like `HashMap`) with insertion-order iteration.

---

## Quick-reference cheat sheet

```
HashMap:
  - Hash table implementation
  - O(1) average, O(log n) worst (Java 8+ tree)
  - No order guarantee
  - Allows one null key, many null values
  - Not thread-safe

HashCode & Equals Contract:
  equal objects → same hashCode (MUST)
  same hashCode → may or may not be equal (collision)
  ALWAYS override both together

Collision Handling:
  Java 7: linked list → O(n) worst
  Java 8+: linked list until 8 → tree → O(log n)

Capacity & Load Factor:
  Default capacity: 16
  Default load factor: 0.75
  Resize when: entries > capacity * load factor

HashMap vs TreeMap:
  HashMap  → O(1), unordered
  TreeMap  → O(log n), sorted (natural/comparator)

Common Mistakes:
  ❌ Only equals(), no hashCode()
  ❌ Mutable keys (lost entries)
  ❌ Using HashMap when order needed
  ❌ Not considering thread-safety
```

---

## Bonus Q & A

**Q1: What is the difference between `HashMap` and `Hashtable`?**

**Q2: What is `ConcurrentHashMap` and how does it differ from `HashMap`?**

**Q3: What is the difference between `ConcurrentHashMap` and `Collections.synchronizedMap()`?**

**Q4: What is `LinkedHashMap` and how is it different from `HashMap`?**

**Q5: What is the difference between `putIfAbsent()` and `computeIfAbsent()`?**

**Q6: What happens when you try to store a key that already exists in a `HashMap`?**

**Q7: What is `identityHashMap` and when would you use it?**

**Q8: What is the difference between `HashMap` and `WeakHashMap`?**

**Q9: Why does `HashMap` use power-of-two capacity and how is that related to hash masking?**

**Q10: What is the impact of a poor `hashCode()` implementation on `HashMap` performance?**

---

