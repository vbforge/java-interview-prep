package com.vbforge.streams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo-04 — Streams and Lambdas
 *
 * After startup explore the demo via:
 *   GET /demo                            → index of all endpoints
 *
 *   GET /demo/streams/pipeline           → Q45     what a Stream is, source types
 *   GET /demo/streams/intermediate       → Q46     filter, map, sorted, distinct, peek
 *   GET /demo/streams/terminal           → Q46     collect, forEach, reduce, count, anyMatch
 *   GET /demo/streams/lazy               → Q46,47  lazy evaluation proof, reuse attempt
 *   GET /demo/streams/flatmap            → Q50     map vs flatMap — the key difference
 *   GET /demo/streams/collectors         → Q46     groupingBy, partitioningBy, joining, toMap
 *   GET /demo/streams/primitive          → Q49     IntStream, LongStream, DoubleStream
 *   GET /demo/optional/basics            → Q48     Optional as one-shot container
 *   GET /demo/optional/chaining          → Q48     map, flatMap, filter on Optional
 *   GET /demo/lambdas/basics             → Q51     lambda syntax, target types, compilation
 *   GET /demo/lambdas/functional         → Q52     Function, Predicate, Consumer, Supplier, UnaryOperator
 *   GET /demo/lambdas/method-refs        → Q51     four kinds of method references
 */
@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
