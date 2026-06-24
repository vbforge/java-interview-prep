package com.vbforge.collections.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Q29 — Describe List.
 * Q30 — Time complexity of insertion and read/access.
 * Q31 — When is array-based list better, when is linked list better?
 * Q32 — How does a list differ from a plain array?
 * Q33 — What is an iterator?
 *
 * KEY POINTS:
 *
 *  ArrayList  — backed by Object[], O(1) random access, O(n) insert at middle,
 *               amortised O(1) append (doubles capacity on resize).
 *  LinkedList — doubly-linked nodes, O(1) insert/delete at head/tail,
 *               O(n) random access (must traverse from head or tail).
 *  Iterator   — unified traversal interface; removes the caller's dependency
 *               on the concrete collection type.
 *  Array      — fixed size, no generics, no Collections utility methods.
 */
@Component
public class ListDemo {

    private static final Logger log = LoggerFactory.getLogger(ListDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q29, Q30 — ArrayList operations and complexity
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates the most important ArrayList operations with their
     * real time complexities and the reason behind each.
     */
    public String runArrayListDemo() {
        log.debug("=== LIST DEMO: ArrayList (Q29, Q30) ===");

        // JUNIOR NOTE: ArrayList is backed by an Object[] array internally.
        // Default initial capacity is 10. When full it copies to a new array
        // that is 1.5× the previous size (since Java 6).
        List<String> list = new ArrayList<>();

        // ── append: amortised O(1) ───────────────────────────────────────────
        // Most appends write directly to the next free slot — O(1).
        // Occasional resize copies the entire array — O(n), but amortised
        // over all appends the cost per operation is O(1).
        list.add("alpha");
        list.add("beta");
        list.add("gamma");
        list.add("delta");
        log.debug("After 4 appends: {}", list);

        // ── random access: O(1) ─────────────────────────────────────────────
        // Backed by an array — index arithmetic is a single memory read.
        String element = list.get(2);
        log.debug("get(2) = '{}' — O(1) because array index arithmetic", element);

        // ── insert at index: O(n) ────────────────────────────────────────────
        // Everything at index >= 1 must shift one position to the right.
        // With 1M elements, inserting at index 0 shifts 1M elements → very slow.
        list.add(1, "INSERTED");
        log.debug("After add(1, INSERTED): {} — O(n) because of shifting", list);

        // ── remove by index: O(n) ────────────────────────────────────────────
        list.remove(1);
        log.debug("After remove(1): {} — O(n) because of shifting", list);

        // ── remove by value: O(n) ────────────────────────────────────────────
        // Must scan linearly to find the element, then shift.
        list.remove("gamma");
        log.debug("After remove('gamma'): {}", list);

        // ── contains: O(n) ───────────────────────────────────────────────────
        boolean has = list.contains("beta");
        log.debug("contains('beta') = {} — O(n) linear scan", has);

        // ── size: O(1) ───────────────────────────────────────────────────────
        log.debug("size() = {} — O(1), maintained as a field", list.size());

        return buildComplexityTable("ArrayList",
            new String[][]{
                {"add(e) — append",       "O(1) amortised", "Writes to next array slot; O(n) on resize but rare"},
                {"add(i,e) — insert",     "O(n)",           "Shifts all elements right of index i"},
                {"get(i)",                "O(1)",           "Array index arithmetic — single memory read"},
                {"remove(i)",             "O(n)",           "Shifts elements left after removal"},
                {"contains(e)",           "O(n)",           "Linear scan"},
                {"size()",                "O(1)",           "Maintained as a field"},
            });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q29, Q30 — LinkedList operations and complexity
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates LinkedList. Java's LinkedList is a DOUBLY-linked list —
     * each node holds a reference to both previous and next nodes.
     * This makes head/tail operations O(1) but random access O(n).
     */
    public String runLinkedListDemo() {
        log.debug("=== LIST DEMO: LinkedList (Q29, Q30) ===");

        // JUNIOR NOTE: LinkedList implements both List AND Deque.
        // Use it as a Deque (addFirst, addLast, peekFirst, pollLast) when
        // you need a queue or stack — not as a general-purpose list.
        LinkedList<String> list = new LinkedList<>();

        // ── addFirst / addLast: O(1) ─────────────────────────────────────────
        // Head and tail pointers are maintained — no traversal needed.
        list.addLast("B");
        list.addFirst("A");
        list.addLast("C");
        log.debug("After addFirst/addLast: {}", list);

        // ── get(i): O(n) ─────────────────────────────────────────────────────
        // Must traverse from head (or tail if i > size/2) — no array index shortcut.
        String middle = list.get(1);
        log.debug("get(1) = '{}' — O(n) because must traverse nodes", middle);

        // ── removeFirst / removeLast: O(1) ───────────────────────────────────
        String first = list.removeFirst();
        log.debug("removeFirst() = '{}' — O(1) head pointer update only", first);

        // ── insert in middle: O(n) to find + O(1) to link ────────────────────
        // Finding position is O(n); once found, relinking pointers is O(1).
        list.add(1, "INSERTED");
        log.debug("add(1, INSERTED): {} — O(n) to find, O(1) to relink", list);

        return buildComplexityTable("LinkedList",
            new String[][]{
                {"addFirst(e) / addLast(e)", "O(1)", "Head/tail pointer update only"},
                {"add(i, e) — middle",       "O(n)", "O(n) to find position, O(1) to relink"},
                {"get(i)",                   "O(n)", "Must traverse from head or tail"},
                {"removeFirst() / removeLast()", "O(1)", "Head/tail pointer update only"},
                {"remove(i) — middle",       "O(n)", "O(n) traversal + O(1) unlink"},
                {"contains(e)",              "O(n)", "Linear traversal"},
            });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q31 — When is each better?
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Benchmark: appending vs inserting at head.
     * Shows exactly why ArrayList wins for random access / append,
     * and why LinkedList can win for repeated head insertions.
     */
    public String runComparisonDemo(int n) {
        log.debug("=== LIST DEMO: comparison — n={} (Q31) ===", n);

        // ── ArrayList: append n elements ────────────────────────────────────
        List<Integer> arrayList = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) arrayList.add(i);
        long arrayListAppend = System.nanoTime() - start;

        // ── LinkedList: append n elements ────────────────────────────────────
        LinkedList<Integer> linkedList = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < n; i++) linkedList.add(i);
        long linkedListAppend = System.nanoTime() - start;

        // ── ArrayList: random access (10k reads) ─────────────────────────────
        start = System.nanoTime();
        for (int i = 0; i < 10_000; i++) arrayList.get(i % n);
        long arrayListGet = System.nanoTime() - start;

        // ── LinkedList: random access (10k reads) ────────────────────────────
        // JUNIOR NOTE: this is intentionally slow. Calling get(i) on a LinkedList
        // in a loop is an O(n²) algorithm — a classic performance trap.
        int safeN = Math.min(n, 1000); // limit to avoid too-long response
        start = System.nanoTime();
        for (int i = 0; i < safeN; i++) linkedList.get(i % safeN);
        long linkedListGet = System.nanoTime() - start;

        // ── LinkedList: insert at head (n inserts) ───────────────────────────
        LinkedList<Integer> llHead = new LinkedList<>();
        start = System.nanoTime();
        for (int i = 0; i < n; i++) llHead.addFirst(i);
        long linkedListHeadInsert = System.nanoTime() - start;

        // ── ArrayList: insert at head (n inserts) ────────────────────────────
        List<Integer> alHead = new ArrayList<>();
        start = System.nanoTime();
        for (int i = 0; i < Math.min(n, 10_000); i++) alHead.add(0, i); // capped — very slow
        long arrayListHeadInsert = System.nanoTime() - start;

        log.debug("Append {}x:   ArrayList={}ms  LinkedList={}ms", n, arrayListAppend/1_000_000, linkedListAppend/1_000_000);
        log.debug("get() 10k:    ArrayList={}µs  LinkedList={}µs ({}x reads capped)", arrayListGet/1_000, linkedListGet/1_000, safeN);
        log.debug("Head insert:  ArrayList(capped)={}ms  LinkedList={}ms", arrayListHeadInsert/1_000_000, linkedListHeadInsert/1_000_000);

        return String.format("""
            === Comparison: n=%d ===
            
            APPEND %d elements:
              ArrayList  : %d ms  ← amortised O(1) per append
              LinkedList : %d ms  ← O(1) per addLast but worse cache locality
            
            RANDOM ACCESS (get, %d reads):
              ArrayList  : %d µs  ← O(1) array index
              LinkedList : %d µs  ← O(n) traversal — up to %dx slower
            
            HEAD INSERT (%d inserts):
              ArrayList  : %d ms  ← O(n) shift per insert — capped at 10k
              LinkedList : %d ms  ← O(1) addFirst — clear winner
            
            WHEN TO USE WHICH:
              ArrayList  → default choice. Random access, iteration, append-heavy.
              LinkedList → frequent insert/delete at head or tail (queue/stack).
                           Almost never needed as a plain List in practice.
            """,
            n,
            n, arrayListAppend/1_000_000, linkedListAppend/1_000_000,
            safeN, arrayListGet/1_000, linkedListGet/1_000, linkedListGet/(arrayListGet+1),
            n, arrayListHeadInsert/1_000_000, linkedListHeadInsert/1_000_000);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q32 — List vs plain array
    // ─────────────────────────────────────────────────────────────────────────

    public String runVsArrayDemo() {
        log.debug("=== LIST DEMO: List vs array (Q32) ===");

        // ── Plain array ──────────────────────────────────────────────────────
        // Fixed size set at creation — cannot grow or shrink.
        int[] arr = new int[3];
        arr[0] = 10; arr[1] = 20; arr[2] = 30;
        // arr[3] = 40; // → ArrayIndexOutOfBoundsException — no dynamic resize

        // Arrays can hold primitives directly (no boxing overhead).
        // int[] uses ~4 bytes per element; Integer[] uses ~16 bytes per element.
        log.debug("Array: fixed size={}, holds primitives directly, no generics", arr.length);

        // ── ArrayList ────────────────────────────────────────────────────────
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
        list.add(40); // dynamic resize — no exception
        list.remove(Integer.valueOf(10)); // remove by value
        Collections.sort(list);
        log.debug("List: dynamic size={}, generics, Collections utility methods", list.size());

        // JUNIOR NOTE: Arrays.asList() returns a FIXED-SIZE list backed by the array.
        // You can SET elements but cannot ADD or REMOVE — it throws UnsupportedOperationException.
        // Use new ArrayList<>(Arrays.asList(...)) or List.of(...) + new ArrayList<>() for a mutable copy.
        List<String> fixed = Arrays.asList("a", "b", "c");
        try {
            fixed.add("d"); // UnsupportedOperationException!
        } catch (UnsupportedOperationException e) {
            log.debug("Arrays.asList() is fixed-size — add() throws UnsupportedOperationException");
        }

        return """
            List vs plain array:
            
            ARRAY (int[]):
              + Holds primitives — no boxing, best memory efficiency
              + Slightly faster for tight numeric loops (cache locality)
              - Fixed size — set at creation, cannot grow or shrink
              - No generics, no Collections utility methods
              - No built-in remove / contains / sort helpers
            
            LIST (ArrayList<E>):
              + Dynamic size — grows automatically on add()
              + Generics — type-safe, no casting
              + Full Collections API: sort, shuffle, binarySearch, frequency…
              + Implements Iterable — works with for-each and Stream
              - Elements are Objects — primitives get autoboxed (memory cost)
              - Slightly slower for raw numeric iteration vs int[]
            
            // JUNIOR NOTE: Arrays.asList() returns a fixed-size List —
            // you can set() but not add() or remove(). Use new ArrayList<>(list)
            // to get a truly mutable copy.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q33 — Iterator pattern
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Demonstrates the Iterator pattern and why it matters.
     *
     * KEY POINTS:
     *  - Iterator decouples traversal logic from the collection implementation.
     *  - The for-each loop is compiled to an Iterator under the hood.
     *  - ListIterator allows bidirectional traversal and in-place mutation.
     *  - Modifying a collection while iterating with a for-each throws
     *    ConcurrentModificationException — use iterator.remove() instead.
     */
    public String runIteratorDemo() {
        log.debug("=== LIST DEMO: Iterator (Q33) ===");

        List<String> list = new ArrayList<>(List.of("alpha", "beta", "gamma", "delta", "epsilon"));

        // ── for-each (syntactic sugar over Iterator) ─────────────────────────
        // Compiled by javac to: Iterator<String> it = list.iterator(); while(it.hasNext()){ ... }
        log.debug("for-each traversal (compiled to Iterator):");
        for (String s : list) {
            log.debug("  → {}", s);
        }

        // ── explicit Iterator ─────────────────────────────────────────────────
        // JUNIOR NOTE: Use iterator.remove() — NOT list.remove() — inside
        // an iterator loop. list.remove() during iteration causes
        // ConcurrentModificationException because it increments modCount
        // but the iterator's expectedModCount is stale.
        log.debug("Removing elements starting with 'b' via Iterator.remove():");
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
            if (s.startsWith("b")) {
                it.remove(); // safe — updates modCount and expectedModCount together
                log.debug("  removed: {}", s);
            }
        }
        log.debug("After safe removal: {}", list);

        // ── ConcurrentModificationException demo ─────────────────────────────
        List<String> list2 = new ArrayList<>(List.of("a","b","c"));
        try {
            for (String s : list2) {
                if (s.equals("b")) {
                    list2.remove(s); // WRONG — modifies list during iteration
                }
            }
        } catch (ConcurrentModificationException e) {
            log.warn("ConcurrentModificationException — never modify a collection inside for-each. Use iterator.remove()");
        }

        // ── ListIterator: bidirectional + in-place set ───────────────────────
        List<String> list3 = new ArrayList<>(List.of("one","two","three"));
        ListIterator<String> lit = list3.listIterator(list3.size()); // start at end
        log.debug("ListIterator traversal in reverse:");
        while (lit.hasPrevious()) {
            log.debug("  ← {}", lit.previous());
        }

        return """
            Iterator demo complete — see logs.
            
            THREE things to remember about Iterator:
            
            1. for-each IS an Iterator.
               javac compiles it to: Iterator<T> it = col.iterator(); while(it.hasNext()){ T x = it.next(); ... }
            
            2. NEVER call collection.remove() inside a for-each.
               Use iterator.remove() — it keeps modCount consistent.
               Calling collection.remove() throws ConcurrentModificationException.
            
            3. ListIterator extends Iterator with:
               - hasPrevious() / previous() — reverse traversal
               - set(e)  — replace current element in-place
               - add(e)  — insert at current position
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private String buildComplexityTable(String name, String[][] rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" — time complexity:\n\n");
        sb.append(String.format("  %-30s %-18s %s%n", "Operation", "Complexity", "Reason"));
        sb.append("  ").append("─".repeat(78)).append("\n");
        for (String[] r : rows) {
            sb.append(String.format("  %-30s %-18s %s%n", r[0], r[1], r[2]));
        }
        return sb.toString();
    }
}
