package com.vbforge.collections.hashmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Q34 — Describe HashMap.
 * Q35 — How is it implemented?
 * Q36 — What is a hash code?
 * Q37 — Hash code normalization (spreading / masking).
 * Q38 — How is hashCode related to equals?
 * Q39 — Why does that matter for a hash map?
 * Q40 — Data structure in a bucket when there is a collision.
 * Q41 — What are capacity and load factor? What is the default load factor?
 * Q42 — Time complexity of lookup and insertion.
 * Q43 — In what order do you get elements if you iterate the map? Why?
 *
 * KEY POINTS:
 *
 *  HashMap is backed by an array of "buckets" (Node<K,V>[]).
 *  Bucket index = (n-1) & hash(key)  where n = current capacity (power of 2).
 *  Each bucket holds a linked list; at 8+ entries it converts to a red-black tree.
 *  Default capacity = 16, load factor = 0.75 → resize at 12 entries.
 *  hashCode() determines the bucket; equals() resolves collisions within a bucket.
 *  Iteration order is undefined — determined by bucket index and insertion order within bucket.
 */
@Component
public class HashMapDemo {

    private static final Logger log = LoggerFactory.getLogger(HashMapDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q34, Q35 — Basic operations and internal structure
    // ─────────────────────────────────────────────────────────────────────────

    public String runBasicsDemo() {
        log.debug("=== HASHMAP DEMO: basics (Q34, Q35) ===");

        // JUNIOR NOTE: HashMap is NOT thread-safe. Use ConcurrentHashMap for
        // concurrent access, or Collections.synchronizedMap() as a simpler wrapper.
        Map<String, Integer> map = new HashMap<>();

        // put() — O(1) average
        // 1. compute hash(key)
        // 2. mask to bucket index: (capacity-1) & hash
        // 3. if bucket is empty: insert new Node
        // 4. if bucket has entries: traverse list/tree, replace if key equals, else append
        map.put("alice", 30);
        map.put("bob", 25);
        map.put("charlie", 35);
        log.debug("After 3 puts: {}", map);

        // get() — O(1) average
        // 1. compute hash(key)
        // 2. find bucket
        // 3. traverse bucket chain comparing with equals()
        Integer age = map.get("bob");
        log.debug("get('bob') = {} — O(1) average", age);

        // putIfAbsent / getOrDefault — common interview questions
        map.putIfAbsent("alice", 99); // alice already exists — value unchanged
        int daveAge = map.getOrDefault("dave", -1);
        log.debug("putIfAbsent('alice', 99): alice={} (unchanged)", map.get("alice"));
        log.debug("getOrDefault('dave', -1): {}", daveAge);

        // null key — HashMap allows exactly ONE null key (stored in bucket 0)
        map.put(null, 0);
        log.debug("null key allowed: get(null)={}", map.get(null));

        // null values — allowed, multiple
        map.put("empty", null);
        log.debug("null value allowed: get('empty')={}", map.get("empty"));

        return String.format("""
            HashMap basics:
              size=%d, contains 'bob'=%s, get('bob')=%d
              null key allowed: get(null)=%d
              
            Internal structure (Java 8+):
              Node<K,V>[] table  — array of bucket heads (initially null)
              Each Node: hash | key | value | next (pointer to next node in bucket)
              At 8 collisions in one bucket: linked list → TreeNode (red-black tree)
              At 6 entries after removal: TreeNode → linked list again
            """,
            map.size(), map.containsKey("bob"), map.get("bob"), map.get(null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q36, Q37 — hashCode and spreading
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shows how HashMap uses and spreads the raw hashCode to pick a bucket.
     *
     * The key formula (from HashMap source):
     *   static final int hash(Object key) {
     *       int h;
     *       return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
     *   }
     *   bucket_index = (capacity - 1) & hash(key)
     *
     * The XOR with h>>>16 is "spreading" — it mixes high bits into low bits
     * so that keys whose hashCodes differ only in high bits end up in different
     * buckets. Without this, many String keys would collide in a small map.
     */
    public String runHashCodeDemo() {
        log.debug("=== HASHMAP DEMO: hashCode and spreading (Q36, Q37) ===");

        int capacity = 16; // default initial capacity

        String[] keys = {"alice", "bob", "charlie", "dave", "eve"};
        StringBuilder sb = new StringBuilder("Key → hashCode → spread hash → bucket index:\n\n");

        for (String key : keys) {
            int rawHash   = key.hashCode();
            int spread    = rawHash ^ (rawHash >>> 16);  // HashMap's internal hash()
            int bucketIdx = (capacity - 1) & spread;     // (n-1) & hash

            log.debug("'{}': rawHash={} spread={} bucket[{}]", key, rawHash, spread, bucketIdx);
            sb.append(String.format("  '%-10s' rawHash=%-15d spread=%-15d bucket[%d]%n",
                key, rawHash, spread, bucketIdx));
        }

        sb.append("""
            
            WHY capacity is always a power of 2:
              bucket = (capacity - 1) & hash
              If capacity = 16: 16-1 = 15 = 0b00001111
              The & acts as a fast modulo — only the last 4 bits of hash are used.
              This is much faster than the % operator.
              Non-power-of-2 capacity would make bit-masking non-uniform.
            
            WHY the >>> 16 spread matters:
              Small maps (capacity=16) only use the lowest 4 bits of hashCode.
              Without spreading, two keys differing only in high bits land in the
              same bucket even though their full hashCodes are very different.
              Spreading XORs the top 16 bits into the bottom 16 → better distribution.
            """);

        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q38, Q39 — hashCode + equals contract
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates what happens when you break the hashCode/equals contract.
     *
     * CONTRACT:
     *   If a.equals(b) → a.hashCode() == b.hashCode()   (REQUIRED)
     *   If a.hashCode() == b.hashCode() → a.equals(b)   (NOT required — collision is OK)
     *
     * Breaking the contract makes HashMap silently misbehave —
     * one of the most common "Java gotcha" interview questions.
     */
    public String runEqualsContractDemo() {
        log.debug("=== HASHMAP DEMO: equals contract (Q38, Q39) ===");

        // ── Good key: equals and hashCode consistent ─────────────────────────
        Map<GoodKey, String> goodMap = new HashMap<>();
        GoodKey k1 = new GoodKey(1, "alice");
        GoodKey k2 = new GoodKey(1, "alice"); // logically equal to k1
        goodMap.put(k1, "first");
        String found = goodMap.get(k2); // should find "first"
        log.debug("GoodKey: k1.equals(k2)={}, hashCodes equal={}, get(k2)={}",
            k1.equals(k2), k1.hashCode() == k2.hashCode(), found);

        // ── Broken key: equals overridden but hashCode not ───────────────────
        // JUNIOR NOTE: This is the #1 HashMap bug in interviews.
        // Two objects are logically equal (equals returns true) but have
        // DIFFERENT hashCodes → they land in different buckets →
        // get() searches the wrong bucket and returns null.
        Map<BrokenKey, String> brokenMap = new HashMap<>();
        BrokenKey bk1 = new BrokenKey(1, "alice");
        BrokenKey bk2 = new BrokenKey(1, "alice");
        brokenMap.put(bk1, "first");
        String notFound = brokenMap.get(bk2); // null — wrong bucket!
        log.debug("BrokenKey: bk1.equals(bk2)={}, hashCodes equal={}, get(bk2)={}",
            bk1.equals(bk2), bk1.hashCode() == bk2.hashCode(), notFound);

        return String.format("""
            hashCode + equals contract demo:
            
            GoodKey (both overridden correctly):
              k1.equals(k2) = %s
              k1.hashCode() == k2.hashCode() = %s
              map.get(k2) = "%s"  ← found correctly
            
            BrokenKey (equals overridden, hashCode NOT):
              bk1.equals(bk2) = %s
              bk1.hashCode() == bk2.hashCode() = %s  ← DIFFERENT hashCodes!
              map.get(bk2) = "%s"  ← null! Wrong bucket searched
            
            THE CONTRACT:
              equals(b) → must have same hashCode.  (If you break this, get() returns null)
              Same hashCode does NOT mean equals.    (That's just a collision — handled by bucket chain)
            
            RULE: If you override equals(), ALWAYS override hashCode().
            IDEs and Lombok can generate both. Records generate both automatically.
            """,
            k1.equals(k2), k1.hashCode() == k2.hashCode(), found,
            bk1.equals(bk2), bk1.hashCode() == bk2.hashCode(), notFound);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q40 — Collision: bucket data structure
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Forces collisions by using a key with a deliberately bad hashCode
     * (always returns the same value → all keys in bucket 0).
     * Shows the linked-list → tree treeification at 8 entries.
     */
    public String runCollisionDemo() {
        log.debug("=== HASHMAP DEMO: collision structure (Q40) ===");

        Map<CollidingKey, String> map = new HashMap<>();
        // All CollidingKey instances return hashCode=42 → all land in same bucket
        for (int i = 0; i < 12; i++) {
            map.put(new CollidingKey(i), "value-" + i);
        }

        log.debug("Added 12 entries all with same hashCode. " +
            "Bucket structure: linked list until 8 entries, then red-black tree.");

        return """
            Collision demo — all 12 keys landed in the same bucket (hashCode=42):
            
            BUCKET STRUCTURE evolution:
              1–7 entries  → Node linked list:  Node → Node → Node → null
              8th entry    → TREEIFY_THRESHOLD reached → linked list converts to TreeNode (red-black tree)
              After tree   → O(log n) lookup instead of O(n) — worst case improved
              < 6 entries  → UNTREEIFY_THRESHOLD → tree converts back to linked list
            
            JAVA 8 improvement:
              Before Java 8: only linked list in buckets → O(n) worst case under hash flooding attack
              Java 8+: treeification at 8 → O(log n) worst case
            
            TREEIFY constants (from HashMap source):
              TREEIFY_THRESHOLD  = 8   (list → tree)
              UNTREEIFY_THRESHOLD = 6  (tree → list on removal)
              MIN_TREEIFY_CAPACITY = 64 (bucket treeify only if total capacity >= 64)
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q41 — Capacity and load factor
    // ─────────────────────────────────────────────────────────────────────────

    public String runCapacityDemo() {
        log.debug("=== HASHMAP DEMO: capacity and load factor (Q41) ===");

        // Default: capacity=16, loadFactor=0.75 → resize threshold = 16 * 0.75 = 12
        // When size exceeds threshold: capacity doubles, all entries rehashed
        Map<String, Integer> map = new HashMap<>(); // capacity=16

        // Pre-sized map — avoids rehashing if you know the expected size
        // Rule of thumb: initialCapacity = expectedSize / loadFactor + 1
        // For 100 entries: 100 / 0.75 + 1 ≈ 135
        Map<String, Integer> presized = new HashMap<>(135);

        for (int i = 0; i < 100; i++) {
            presized.put("key-" + i, i);
        }
        log.debug("Pre-sized map with 100 entries — no rehash occurred");

        return """
            Capacity and load factor:
            
            DEFAULT VALUES:
              initialCapacity = 16    (must always be a power of 2)
              loadFactor      = 0.75  (75% full → resize)
              threshold       = capacity * loadFactor = 16 * 0.75 = 12
            
            RESIZE PROCESS (when size > threshold):
              1. New array created at 2× capacity  (16 → 32 → 64 → ...)
              2. All existing entries rehashed and redistributed
              3. O(n) operation — called "rehash" or "resize"
              4. threshold updated: 32 * 0.75 = 24
            
            LOAD FACTOR TRADE-OFFS:
              Lower (e.g. 0.5) → fewer collisions, more memory, more frequent resizes
              Higher (e.g. 0.9) → more collisions, less memory, fewer resizes
              0.75 is the empirically balanced default.
            
            PRE-SIZING — avoid rehash if you know expected size:
              new HashMap<>(expectedSize / 0.75 + 1)
              e.g. for 100 entries: new HashMap<>(135)
              Guava: Maps.newHashMapWithExpectedSize(100)  — does this for you
            
            // JUNIOR NOTE: Every resize is O(n). If you add 1M entries to a
            // default HashMap it will resize ~17 times. Pre-size to avoid this.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q42 — Time complexity
    // ─────────────────────────────────────────────────────────────────────────

    public String runComplexityDemo() {
        log.debug("=== HASHMAP DEMO: complexity (Q42) ===");
        return """
            HashMap time complexity:
            
            Operation          Average    Worst case    When worst case happens
            ─────────────────────────────────────────────────────────────────────
            get(key)           O(1)       O(n)*         All keys in one bucket (pre-Java8)
            put(key, value)    O(1)       O(n)*         Same as above
            remove(key)        O(1)       O(n)*         Same as above
            containsKey(key)   O(1)       O(n)*         Same as above
            containsValue(v)   O(n)       O(n)          Must scan all buckets
            
            *Java 8+: worst case is O(log n) after treeification at 8 collisions.
            
            WHY O(1) average:
              With a good hash function and load factor 0.75, the probability of
              more than a few keys in any one bucket is very low. The expected
              chain length per bucket is < 1 → effectively constant time.
            
            WHEN get() degrades:
              1. All keys have the same hashCode (deliberately bad implementation)
              2. Hash flooding attack (malicious input; mitigated in Java by
                 randomised String hashing since Java 7u6)
              3. Load factor too high — too many collisions per bucket
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q43 — Iteration order
    // ─────────────────────────────────────────────────────────────────────────

    public String runIterationOrderDemo() {
        log.debug("=== HASHMAP DEMO: iteration order (Q43) ===");

        Map<String, Integer> hashMap = new HashMap<>();
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        Map<String, Integer> treeMap = new TreeMap<>();

        String[] keys = {"charlie", "alice", "eve", "bob", "dave"};
        for (String k : keys) {
            int v = k.length();
            hashMap.put(k, v);
            linkedHashMap.put(k, v);
            treeMap.put(k, v);
        }

        log.debug("Insertion order: {}", Arrays.toString(keys));
        log.debug("HashMap iteration:       {}", hashMap.keySet());
        log.debug("LinkedHashMap iteration: {}", linkedHashMap.keySet());
        log.debug("TreeMap iteration:       {}", treeMap.keySet());

        return String.format("""
            Iteration order comparison:
            
            Insertion order: %s
            
            HashMap       : %s
              → No guaranteed order. Determined by bucket index (hash & (capacity-1)).
                Order can change on resize. Never rely on it.
            
            LinkedHashMap : %s
              → Insertion order guaranteed.
                Maintains a doubly-linked list through all entries alongside the hash table.
                Slightly more memory and overhead than HashMap.
            
            TreeMap       : %s
              → Natural key order (Comparable) or custom Comparator.
                Backed by a red-black tree. get/put/remove all O(log n).
                Use when you need sorted keys or range operations (subMap, headMap, tailMap).
            
            // JUNIOR NOTE: The most common answer interviewers want:
            // "HashMap has no guaranteed iteration order because entries are
            //  stored by bucket index, and bucket assignment depends on hashCode
            //  and current capacity — not insertion order."
            """,
            Arrays.toString(keys), hashMap.keySet(), linkedHashMap.keySet(), treeMap.keySet());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner key classes for the contract demo
    // ─────────────────────────────────────────────────────────────────────────

    /** Correctly implements both equals and hashCode. */
    private static class GoodKey {
        final int id;
        final String name;

        GoodKey(int id, String name) { this.id = id; this.name = name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GoodKey g)) return false;
            return id == g.id && Objects.equals(name, g.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name); // consistent with equals
        }
    }

    /** Overrides equals but NOT hashCode — deliberately broken. */
    private static class BrokenKey {
        final int id;
        final String name;

        BrokenKey(int id, String name) { this.id = id; this.name = name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BrokenKey b)) return false;
            return id == b.id && Objects.equals(name, b.name);
        }

        // JUNIOR NOTE: hashCode NOT overridden → uses Object.hashCode() which
        // returns a value based on object identity (memory address).
        // Two BrokenKey(1,"alice") instances → different memory addresses →
        // different hashCodes → different buckets → get() returns null.
    }

    /** Always returns the same hashCode — forces all entries into one bucket. */
    private static class CollidingKey {
        final int value;
        CollidingKey(int value) { this.value = value; }

        @Override public int hashCode() { return 42; } // always same bucket!
        @Override public boolean equals(Object o) {
            return o instanceof CollidingKey c && c.value == this.value;
        }
    }
}
