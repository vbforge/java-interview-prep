package com.vbforge.org.config;

import com.vbforge.org.datatypes.DataTypesDemo;
import com.vbforge.org.oop.OopDemo;
import com.vbforge.org.strings.StringsDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * All demo-02-data-type-strings-oop-generics REST endpoints.
 *
 * Base URL: http://localhost:8082/demo
 *
 * ┌────────────────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                            │ Q       │ What it shows                  │
 * ├────────────────────────────────────────────────────────────────────────────────┤
 * │ GET /demo/datatypes/primitives-vs-objects │ Q9  │ Two type groups, value vs ref│
 * │ GET /demo/datatypes/comparison      │ Q10     │ == vs equals, Integer cache    │
 * │ GET /demo/datatypes/pass-by-value   │ Q11     │ Pass-by-value for prim & ref   │
 * │ GET /demo/datatypes/object-on-heap  │ Q12     │ Stack ref → heap object        │
 * │ GET /demo/datatypes/boxing          │ Q13     │ Autoboxing, NPE trap, perf     │
 * │ GET /demo/strings/pool-vs-heap      │ Q14     │ Literal vs new String()        │
 * │ GET /demo/strings/equality          │ Q15     │ == vs equals for String        │
 * │ GET /demo/strings/intern            │ Q16     │ String.intern() and dedup      │
 * │ GET /demo/strings/immutability      │ Q17     │ Why String is immutable        │
 * │ GET /demo/oop/generics-intro        │ Q18     │ Generics purpose, type erasure │
 * │ GET /demo/oop/generics-examples     │ Q19     │ Box<T>, Pair<A,B>, bounded     │
 * │ GET /demo/oop/wildcards             │ Q20     │ extends/super wildcards, PECS  │
 * │ GET /demo/oop/abstract-vs-interface │ Q21     │ Abstract class vs interface    │
 * │ GET /demo/oop/inheritance-rules     │ Q22–Q24 │ Extends limit, ctor, modifiers │
 * │ GET /demo/oop/multiple-inheritance  │ Q25–Q27 │ Diamond problem, resolution    │
 * └────────────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DataTypesDemo dataTypesDemo;
    private final StringsDemo   stringsDemo;
    private final OopDemo       oopDemo;

    public DemoController(DataTypesDemo dataTypesDemo,
                          StringsDemo stringsDemo,
                          OopDemo oopDemo) {
        this.dataTypesDemo = dataTypesDemo;
        this.stringsDemo   = stringsDemo;
        this.oopDemo       = oopDemo;
    }

    // ── Data Types ───────────────────────────────────────────────────────────

    @GetMapping("/datatypes/primitives-vs-objects")
    public ResponseEntity<String> primitivesVsObjects() {
        return ResponseEntity.ok(dataTypesDemo.runPrimitivesVsObjectsDemo());
    }

    @GetMapping("/datatypes/comparison")
    public ResponseEntity<String> comparison() {
        return ResponseEntity.ok(dataTypesDemo.runComparisonDemo());
    }

    @GetMapping("/datatypes/pass-by-value")
    public ResponseEntity<String> passByValue() {
        return ResponseEntity.ok(dataTypesDemo.runPassByValueDemo());
    }

    @GetMapping("/datatypes/object-on-heap")
    public ResponseEntity<String> objectOnHeap() {
        return ResponseEntity.ok(dataTypesDemo.runObjectOnHeapDemo());
    }

    @GetMapping("/datatypes/boxing")
    public ResponseEntity<String> boxing() {
        return ResponseEntity.ok(dataTypesDemo.runBoxingDemo());
    }

    // ── Strings ──────────────────────────────────────────────────────────────

    @GetMapping("/strings/pool-vs-heap")
    public ResponseEntity<String> poolVsHeap() {
        return ResponseEntity.ok(stringsDemo.runPoolVsHeapDemo());
    }

    @GetMapping("/strings/equality")
    public ResponseEntity<String> stringEquality() {
        return ResponseEntity.ok(stringsDemo.runEqualityDemo());
    }

    @GetMapping("/strings/intern")
    public ResponseEntity<String> stringIntern() {
        return ResponseEntity.ok(stringsDemo.runInternDemo());
    }

    @GetMapping("/strings/immutability")
    public ResponseEntity<String> stringImmutability() {
        return ResponseEntity.ok(stringsDemo.runImmutabilityDemo());
    }

    // ── OOP / Generics ───────────────────────────────────────────────────────

    @GetMapping("/oop/generics-intro")
    public ResponseEntity<String> genericsIntro() {
        return ResponseEntity.ok(oopDemo.runGenericsIntroDemo());
    }

    @GetMapping("/oop/generics-examples")
    public ResponseEntity<String> genericsExamples() {
        return ResponseEntity.ok(oopDemo.runGenericsExamplesDemo());
    }

    @GetMapping("/oop/wildcards")
    public ResponseEntity<String> wildcards() {
        return ResponseEntity.ok(oopDemo.runWildcardsDemo());
    }

    @GetMapping("/oop/abstract-vs-interface")
    public ResponseEntity<String> abstractVsInterface() {
        return ResponseEntity.ok(oopDemo.runAbstractVsInterfaceDemo());
    }

    @GetMapping("/oop/inheritance-rules")
    public ResponseEntity<String> inheritanceRules() {
        return ResponseEntity.ok(oopDemo.runInheritanceRulesDemo());
    }

    @GetMapping("/oop/multiple-inheritance")
    public ResponseEntity<String> multipleInheritance() {
        return ResponseEntity.ok(oopDemo.runMultipleInheritanceDemo());
    }

    // ── Index ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("""
            demo-02-data-type-strings-oop-generics — available endpoints:

              DATA TYPES (Q9–Q13):
              GET /demo/datatypes/primitives-vs-objects   Two type groups: primitives vs references
              GET /demo/datatypes/comparison              == vs equals, Integer cache trap
              GET /demo/datatypes/pass-by-value           Always pass-by-value, even for references
              GET /demo/datatypes/object-on-heap          Stack reference → heap object anatomy
              GET /demo/datatypes/boxing                  Autoboxing, NPE from null unboxing, perf cost

              STRINGS (Q14–Q17):
              GET /demo/strings/pool-vs-heap              Literal interning vs new String() heap bypass
              GET /demo/strings/equality                  == vs equals, null-safe pattern
              GET /demo/strings/intern                    String.intern() and deduplication
              GET /demo/strings/immutability              Why immutable, StringBuilder, safe map key

              OOP & GENERICS (Q18–Q27):
              GET /demo/oop/generics-intro                What generics are, type erasure
              GET /demo/oop/generics-examples             Box<T>, Pair<A,B>, bounded T extends Number
              GET /demo/oop/wildcards                     PECS: extends=producer, super=consumer
              GET /demo/oop/abstract-vs-interface         State, constructor, multiple inheritance
              GET /demo/oop/inheritance-rules             extends limit, ctor rules, access modifiers
              GET /demo/oop/multiple-inheritance          Diamond problem and default method resolution
            """);
    }
}
