package com.vbforge.collections.treemap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Q44 — Difference between HashMap and TreeMap.
 *
 * KEY POINTS:
 *
 *  HashMap   — hash table, O(1) average, no order guarantee.
 *  TreeMap   — red-black tree, O(log n) all ops, always sorted by key.
 *  TreeMap implements NavigableMap → gives firstKey, lastKey, subMap,
 *  headMap, tailMap, floorKey, ceilingKey — none of which HashMap has.
 *
 *  WHEN TO USE TREEMAP:
 *  - You need keys in sorted order (e.g. leaderboard, event timeline)
 *  - You need range queries: "all keys between A and B"
 *  - You need nearest-key lookups: floorKey, ceilingKey
 */
@Component
public class TreeMapDemo {

    private static final Logger log = LoggerFactory.getLogger(TreeMapDemo.class);

    public String runVsHashMapDemo() {
        log.debug("=== TREEMAP DEMO: HashMap vs TreeMap (Q44) ===");

        // ── HashMap — unsorted ───────────────────────────────────────────────
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("charlie", 3);
        hashMap.put("alice", 1);
        hashMap.put("eve", 5);
        hashMap.put("bob", 2);
        hashMap.put("dave", 4);
        log.debug("HashMap keySet: {}", hashMap.keySet()); // unpredictable order

        // ── TreeMap — always sorted by natural key order ─────────────────────
        TreeMap<String, Integer> treeMap = new TreeMap<>(hashMap); // copy from hashMap
        log.debug("TreeMap keySet: {}", treeMap.keySet()); // alphabetical

        // ── NavigableMap operations — only available on TreeMap ───────────────
        log.debug("firstKey()='{}' lastKey()='{}'", treeMap.firstKey(), treeMap.lastKey());

        // floorKey — greatest key ≤ given key
        String floor = treeMap.floorKey("bravo"); // "bob" (b < br)
        log.debug("floorKey('bravo') = '{}'", floor);

        // ceilingKey — smallest key ≥ given key
        String ceiling = treeMap.ceilingKey("bravo"); // "charlie"
        log.debug("ceilingKey('bravo') = '{}'", ceiling);

        // subMap — keys in range [from, to)
        NavigableMap<String, Integer> sub = treeMap.subMap("bob", true, "dave", true);
        log.debug("subMap('bob'...'dave') inclusive: {}", sub);

        // headMap — all keys < given key
        NavigableMap<String, Integer> head = treeMap.headMap("charlie", false);
        log.debug("headMap (< 'charlie'): {}", head);

        // ── Custom sort order with Comparator ─────────────────────────────────
        // JUNIOR NOTE: TreeMap takes a Comparator in its constructor.
        // This overrides the natural ordering for any key type — including
        // String, which by default sorts lexicographically.
        TreeMap<String, Integer> byLength = new TreeMap<>(
            Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder())
        );
        byLength.putAll(hashMap);
        log.debug("TreeMap sorted by key length then alpha: {}", byLength.keySet());

        return String.format("""
            HashMap vs TreeMap (Q44):
            
            HashMap  keySet: %s  ← undefined order
            TreeMap  keySet: %s  ← always sorted (natural order)
            By-length order: %s  ← custom Comparator
            
            NAVIGABLE MAP operations (TreeMap only):
              firstKey()             = '%s'
              lastKey()              = '%s'
              floorKey('bravo')      = '%s'   (greatest key ≤ 'bravo')
              ceilingKey('bravo')    = '%s'   (smallest key ≥ 'bravo')
              subMap('bob'..'dave')  = %s
              headMap(< 'charlie')   = %s
            
            COMPLEXITY COMPARISON:
              Operation     HashMap     TreeMap
              ─────────────────────────────────
              get           O(1) avg    O(log n)
              put           O(1) avg    O(log n)
              remove        O(1) avg    O(log n)
              iteration     O(n)        O(n) — but sorted
              firstKey      —           O(log n)
              subMap        —           O(log n)
            
            WHEN TO USE TREEMAP:
              ✓ Sorted output needed (leaderboard, timeline, index)
              ✓ Range queries: all keys between A and B
              ✓ Nearest-key lookups: what's the next scheduled event after now?
              ✗ Pure key→value lookups with no ordering needs → use HashMap (faster)
            """,
            hashMap.keySet(), treeMap.keySet(), byLength.keySet(),
            treeMap.firstKey(), treeMap.lastKey(), floor, ceiling, sub, head);
    }
}
