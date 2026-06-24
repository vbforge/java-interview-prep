package com.vbforge.collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo-03 — Collections
 *
 * After startup explore the demo via:
 *   GET /demo                           → index of all endpoints
 *
 *   GET /demo/list/arraylist            → Q29,Q30  ArrayList ops and complexity
 *   GET /demo/list/linkedlist           → Q29,Q30  LinkedList ops and complexity
 *   GET /demo/list/comparison           → Q31      when to use which
 *   GET /demo/list/vs-array             → Q32      list vs plain array
 *   GET /demo/list/iterator             → Q33      Iterator pattern
 *
 *   GET /demo/hashmap/basics            → Q34,Q35  put/get internals
 *   GET /demo/hashmap/hashcode          → Q36,Q37  hashCode, spreading
 *   GET /demo/hashmap/equals-contract   → Q38,Q39  hashCode + equals contract
 *   GET /demo/hashmap/collision         → Q40      bucket structure on collision
 *   GET /demo/hashmap/capacity          → Q41      capacity and load factor
 *   GET /demo/hashmap/complexity        → Q42      O(1) vs O(n) scenarios
 *   GET /demo/hashmap/iteration-order   → Q43      why iteration order is undefined
 *   GET /demo/hashmap/vs-treemap        → Q44      HashMap vs TreeMap
 */
@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
