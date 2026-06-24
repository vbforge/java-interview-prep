package com.vbforge.streams.config;

import com.vbforge.streams.lambdas.LambdaDemo;
import com.vbforge.streams.optional.OptionalDemo;
import com.vbforge.streams.streams.StreamDemo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All demo-04-streams REST endpoints.
 *
 * Base URL: http://localhost:8084/demo
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ Endpoint                       │ Q      │ What it shows                 │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ GET /demo/streams/pipeline     │ Q45    │ source types, full pipeline   │
 * │ GET /demo/streams/intermediate │ Q46    │ filter,map,sorted,peek,limit  │
 * │ GET /demo/streams/terminal     │ Q46    │ collect,reduce,count,anyMatch │
 * │ GET /demo/streams/lazy         │ Q46,47 │ lazy proof + reuse ISE        │
 * │ GET /demo/streams/flatmap      │ Q50    │ map vs flatMap — key demo     │
 * │ GET /demo/streams/collectors   │ Q46    │ groupingBy, joining, toMap    │
 * │ GET /demo/streams/primitive    │ Q49    │ IntStream,LongStream,Double   │
 * │ GET /demo/optional/basics      │ Q48    │ of, empty, orElse, ifPresent │
 * │ GET /demo/optional/chaining    │ Q48    │ map, flatMap, filter, or      │
 * │ GET /demo/lambdas/basics       │ Q51    │ syntax, compilation, capture  │
 * │ GET /demo/lambdas/functional   │ Q52    │ Function,Predicate,Consumer.. │
 * │ GET /demo/lambdas/method-refs  │ Q51    │ 4 kinds of method references  │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    private final StreamDemo  streamDemo;
    private final OptionalDemo optionalDemo;
    private final LambdaDemo  lambdaDemo;

    public DemoController(StreamDemo streamDemo,
                          OptionalDemo optionalDemo,
                          LambdaDemo lambdaDemo) {
        this.streamDemo   = streamDemo;
        this.optionalDemo = optionalDemo;
        this.lambdaDemo   = lambdaDemo;
    }

    // ── Streams ───────────────────────────────────────────────────────────────

    @GetMapping("/streams/pipeline")
    public ResponseEntity<String> pipeline() {
        return ResponseEntity.ok(streamDemo.runPipelineDemo());
    }

    @GetMapping("/streams/intermediate")
    public ResponseEntity<String> intermediate() {
        return ResponseEntity.ok(streamDemo.runIntermediateOpsDemo());
    }

    @GetMapping("/streams/terminal")
    public ResponseEntity<String> terminal() {
        return ResponseEntity.ok(streamDemo.runTerminalOpsDemo());
    }

    @GetMapping("/streams/lazy")
    public ResponseEntity<String> lazy() {
        return ResponseEntity.ok(streamDemo.runLazyDemo());
    }

    @GetMapping("/streams/flatmap")
    public ResponseEntity<String> flatMap() {
        return ResponseEntity.ok(streamDemo.runFlatMapDemo());
    }

    @GetMapping("/streams/collectors")
    public ResponseEntity<String> collectors() {
        return ResponseEntity.ok(streamDemo.runCollectorsDemo());
    }

    @GetMapping("/streams/primitive")
    public ResponseEntity<String> primitive() {
        return ResponseEntity.ok(streamDemo.runPrimitiveStreamsDemo());
    }

    // ── Optional ─────────────────────────────────────────────────────────────

    @GetMapping("/optional/basics")
    public ResponseEntity<String> optionalBasics() {
        return ResponseEntity.ok(optionalDemo.runBasicsDemo());
    }

    @GetMapping("/optional/chaining")
    public ResponseEntity<String> optionalChaining() {
        return ResponseEntity.ok(optionalDemo.runChainingDemo());
    }

    // ── Lambdas ───────────────────────────────────────────────────────────────

    @GetMapping("/lambdas/basics")
    public ResponseEntity<String> lambdaBasics() {
        return ResponseEntity.ok(lambdaDemo.runBasicsDemo());
    }

    @GetMapping("/lambdas/functional")
    public ResponseEntity<String> lambdaFunctional() {
        return ResponseEntity.ok(lambdaDemo.runFunctionalInterfacesDemo());
    }

    @GetMapping("/lambdas/method-refs")
    public ResponseEntity<String> lambdaMethodRefs() {
        return ResponseEntity.ok(lambdaDemo.runMethodRefsDemo());
    }

    // ── Index ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("""
            demo-04-streams — available endpoints:
            
              STREAMS (Q45–Q50):
              GET /demo/streams/pipeline       Q45      source types, full pipeline anatomy
              GET /demo/streams/intermediate   Q46      filter, map, sorted, distinct, peek
              GET /demo/streams/terminal       Q46      collect, reduce, count, anyMatch, min/max
              GET /demo/streams/lazy           Q46,Q47  lazy proof via peek + reuse IllegalStateException
              GET /demo/streams/flatmap        Q50      map vs flatMap — the most-asked stream question
              GET /demo/streams/collectors     Q46      groupingBy, partitioningBy, joining, toMap
              GET /demo/streams/primitive      Q49      IntStream, LongStream, DoubleStream, boxing cost
            
              OPTIONAL (Q48):
              GET /demo/optional/basics        Q48      of, empty, ofNullable, orElse, ifPresent
              GET /demo/optional/chaining      Q48      map, flatMap, filter, or — container vs stream
            
              LAMBDAS (Q51–Q52):
              GET /demo/lambdas/basics         Q51      syntax forms, compilation, capture, @FunctionalInterface
              GET /demo/lambdas/functional     Q52      Function, Predicate, Consumer, Supplier, Bi-variants
              GET /demo/lambdas/method-refs    Q51      4 kinds: static, instance, arbitrary, constructor
            """);
    }
}
