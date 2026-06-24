package com.vbforge.collections.config;

import com.vbforge.collections.hashmap.HashMapDemo;
import com.vbforge.collections.list.ListDemo;
import com.vbforge.collections.treemap.TreeMapDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All demo-03-collections REST endpoints.
 *
 * Base URL: http://localhost:8083/demo
 *
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                          │ Q      │ What it shows               │
 * ├──────────────────────────────────────────────────────────────────────────┤
 * │ GET /demo/list/arraylist          │ Q29,30 │ ArrayList ops + complexity  │
 * │ GET /demo/list/linkedlist         │ Q29,30 │ LinkedList ops + complexity │
 * │ GET /demo/list/comparison         │ Q31    │ benchmark: append vs head   │
 * │ GET /demo/list/vs-array           │ Q32    │ List vs plain array         │
 * │ GET /demo/list/iterator           │ Q33    │ Iterator, CME, ListIterator │
 * │ GET /demo/hashmap/basics          │ Q34,35 │ put/get, null key/value     │
 * │ GET /demo/hashmap/hashcode        │ Q36,37 │ hash spreading, bucket idx  │
 * │ GET /demo/hashmap/equals-contract │ Q38,39 │ broken vs correct contract  │
 * │ GET /demo/hashmap/collision       │ Q40    │ list → tree treeification   │
 * │ GET /demo/hashmap/capacity        │ Q41    │ load factor, resize, presiz │
 * │ GET /demo/hashmap/complexity      │ Q42    │ O(1) avg vs O(log n) worst  │
 * │ GET /demo/hashmap/iteration-order │ Q43    │ HashMap vs LHM vs TreeMap   │
 * │ GET /demo/hashmap/vs-treemap      │ Q44    │ NavigableMap ops, comparator│
 * └──────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final ListDemo listDemo;
    private final HashMapDemo hashMapDemo;
    private final TreeMapDemo treeMapDemo;

    public DemoController(ListDemo listDemo,
                          HashMapDemo hashMapDemo,
                          TreeMapDemo treeMapDemo) {
        this.listDemo    = listDemo;
        this.hashMapDemo = hashMapDemo;
        this.treeMapDemo = treeMapDemo;
    }

    // ── List ─────────────────────────────────────────────────────────────────

    @GetMapping("/list/arraylist")
    public ResponseEntity<String> listArrayList() {
        return ResponseEntity.ok(listDemo.runArrayListDemo());
    }

    @GetMapping("/list/linkedlist")
    public ResponseEntity<String> listLinkedList() {
        return ResponseEntity.ok(listDemo.runLinkedListDemo());
    }

    /** @param n number of elements for the benchmark (default 50_000) */
    @GetMapping("/list/comparison")
    public ResponseEntity<String> listComparison(@RequestParam(defaultValue = "50000") int n) {
        return ResponseEntity.ok(listDemo.runComparisonDemo(n));
    }

    @GetMapping("/list/vs-array")
    public ResponseEntity<String> listVsArray() {
        return ResponseEntity.ok(listDemo.runVsArrayDemo());
    }

    @GetMapping("/list/iterator")
    public ResponseEntity<String> listIterator() {
        return ResponseEntity.ok(listDemo.runIteratorDemo());
    }

    // ── HashMap ───────────────────────────────────────────────────────────────

    @GetMapping("/hashmap/basics")
    public ResponseEntity<String> hashMapBasics() {
        return ResponseEntity.ok(hashMapDemo.runBasicsDemo());
    }

    @GetMapping("/hashmap/hashcode")
    public ResponseEntity<String> hashMapHashCode() {
        return ResponseEntity.ok(hashMapDemo.runHashCodeDemo());
    }

    @GetMapping("/hashmap/equals-contract")
    public ResponseEntity<String> hashMapEqualsContract() {
        return ResponseEntity.ok(hashMapDemo.runEqualsContractDemo());
    }

    @GetMapping("/hashmap/collision")
    public ResponseEntity<String> hashMapCollision() {
        return ResponseEntity.ok(hashMapDemo.runCollisionDemo());
    }

    @GetMapping("/hashmap/capacity")
    public ResponseEntity<String> hashMapCapacity() {
        return ResponseEntity.ok(hashMapDemo.runCapacityDemo());
    }

    @GetMapping("/hashmap/complexity")
    public ResponseEntity<String> hashMapComplexity() {
        return ResponseEntity.ok(hashMapDemo.runComplexityDemo());
    }

    @GetMapping("/hashmap/iteration-order")
    public ResponseEntity<String> hashMapIterationOrder() {
        return ResponseEntity.ok(hashMapDemo.runIterationOrderDemo());
    }

    @GetMapping("/hashmap/vs-treemap")
    public ResponseEntity<String> hashMapVsTreeMap() {
        return ResponseEntity.ok(treeMapDemo.runVsHashMapDemo());
    }

    // ── Index ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("""
            demo-03-collections — available endpoints:
            
              LIST (Q28–Q33):
              GET /demo/list/arraylist            ArrayList ops and time complexity
              GET /demo/list/linkedlist           LinkedList ops and time complexity
              GET /demo/list/comparison?n=50000   Benchmark: append vs head insert
              GET /demo/list/vs-array             List vs plain array differences
              GET /demo/list/iterator             Iterator, ConcurrentModificationException
            
              HASHMAP (Q34–Q44):
              GET /demo/hashmap/basics            put/get, null key/value, thread safety
              GET /demo/hashmap/hashcode          hashCode spreading, bucket index formula
              GET /demo/hashmap/equals-contract   broken vs correct hashCode+equals
              GET /demo/hashmap/collision         treeification at 8 collisions
              GET /demo/hashmap/capacity          load factor, resize, pre-sizing tip
              GET /demo/hashmap/complexity        O(1) avg vs O(log n) worst case
              GET /demo/hashmap/iteration-order   HashMap vs LinkedHashMap vs TreeMap
              GET /demo/hashmap/vs-treemap        NavigableMap ops, custom Comparator
            """);
    }
}
