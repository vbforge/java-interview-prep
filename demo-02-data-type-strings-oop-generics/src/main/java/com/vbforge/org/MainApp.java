package com.vbforge.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo-02 — Data Types, Strings, OOP and Generics
 *
 * After startup explore the demo via:
 *   GET /demo                               → index of all endpoints
 *
 *   GET /demo/datatypes/primitives-vs-objects  → Q9   two main type groups
 *   GET /demo/datatypes/comparison             → Q10  == vs equals for primitives and objects
 *   GET /demo/datatypes/pass-by-value          → Q11  pass-by-value for primitives and references
 *   GET /demo/datatypes/object-on-heap         → Q12  where new Object() lives
 *   GET /demo/datatypes/boxing                 → Q13  autoboxing / unboxing, Integer cache
 *
 *   GET /demo/strings/pool-vs-heap             → Q14  literal vs new String()
 *   GET /demo/strings/equality                 → Q15  == vs equals for String
 *   GET /demo/strings/intern                   → Q16  String.intern()
 *   GET /demo/strings/immutability             → Q17  why String is immutable
 *
 *   GET /demo/oop/generics-intro               → Q18  what are generics and why
 *   GET /demo/oop/generics-examples            → Q19  examples: Box<T>, Pair<A,B>, bounded
 *   GET /demo/oop/wildcards                    → Q20  extends / super wildcards (PECS)
 *   GET /demo/oop/abstract-vs-interface        → Q21  abstract class vs interface
 *   GET /demo/oop/inheritance-rules            → Q22,Q23,Q24  extends limit, constructors, modifiers
 *   GET /demo/oop/multiple-inheritance         → Q25,Q26,Q27  default methods, diamond problem
 */

@SpringBootApplication
public class MainApp {

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

}
