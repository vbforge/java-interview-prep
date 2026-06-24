package com.vbforge.streams.streams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Q45 — In Java, what is a Stream?
 * Q46 — Intermediate vs terminal operations — two examples of each.
 * Q47 — After a terminal op, can you reuse the same Stream reference?
 * Q49 — Primitive-specialized stream types besides Stream<T>.
 * Q50 — Difference between map and flatMap.
 */
@Component
public class StreamDemo {

    private static final Logger log = LoggerFactory.getLogger(StreamDemo.class);

    // Sample data used across demos
    private static final List<String> NAMES = List.of(
        "alice", "bob", "charlie", "dave", "eve", "frank", "grace"
    );

    private static final List<List<String>> NESTED = List.of(
        List.of("alpha", "beta"),
        List.of("gamma"),
        List.of("delta", "epsilon", "zeta")
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Q45 — What is a Stream?
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A Stream is a lazily-evaluated pipeline of operations over a sequence
     * of elements. It is NOT a data structure — it does not store elements.
     * It pulls elements from a source (collection, array, generator, I/O)
     * and processes them through a chain of operations.
     *
     * THREE parts of every stream pipeline:
     *   Source → intermediate ops (0..n) → terminal op (exactly 1)
     */
    public String runPipelineDemo() {
        log.debug("=== STREAM DEMO: pipeline (Q45) ===");

        // ── Source types ─────────────────────────────────────────────────────
        Stream<String> fromCollection = NAMES.stream();
        Stream<String> fromArray      = Arrays.stream(new String[]{"x", "y", "z"});
        Stream<String> fromOf         = Stream.of("a", "b", "c");
        Stream<String> fromGenerate   = Stream.generate(() -> "ping").limit(3);
        Stream<Integer> fromIterate   = Stream.iterate(0, n -> n + 2).limit(5); // 0,2,4,6,8

        log.debug("fromCollection first: {}", fromCollection.findFirst().orElse("none"));
        log.debug("fromArray count: {}", fromArray.count());
        log.debug("fromOf:  {}", fromOf.collect(Collectors.toList()));
        log.debug("fromGenerate: {}", fromGenerate.collect(Collectors.toList()));
        log.debug("fromIterate (evens): {}", fromIterate.collect(Collectors.toList()));

        // ── A real pipeline: source → filter → map → collect ──────────────────
        List<String> result = NAMES.stream()           // source
            .filter(n -> n.length() > 3)               // intermediate — keep names > 3 chars
            .map(String::toUpperCase)                  // intermediate — transform
            .sorted()                                  // intermediate — sort alphabetically
            .collect(Collectors.toList());             // terminal — materialise to List

        log.debug("Pipeline result (len>3, uppercase, sorted): {}", result);

        return String.format("""
            Stream pipeline demo:
            
            A Stream has THREE parts:
              1. SOURCE       — collection, array, Stream.of(), Stream.generate(), Stream.iterate()
              2. INTERMEDIATE — filter, map, flatMap, sorted, distinct, peek, limit, skip
                                These are LAZY — they don't execute until a terminal op is called.
              3. TERMINAL     — collect, forEach, reduce, count, findFirst, anyMatch, toList
                                Exactly ONE terminal op per stream. Triggers execution of the pipeline.
            
            WHAT A STREAM IS NOT:
              ✗ Not a data structure — it doesn't store elements
              ✗ Not reusable — once a terminal op runs, the stream is consumed
              ✗ Not always sequential — .parallelStream() processes in parallel
            
            Result of filter(len>3) → map(toUpperCase) → sorted():
              %s
            """, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q46 — Intermediate operations
    // ─────────────────────────────────────────────────────────────────────────

    public String runIntermediateOpsDemo() {
        log.debug("=== STREAM DEMO: intermediate ops (Q46) ===");

        List<Integer> numbers = List.of(5, 3, 8, 1, 5, 9, 2, 8, 7, 3);

        // filter — keeps elements matching the predicate
        List<Integer> filtered = numbers.stream()
            .filter(n -> n > 4)
            .collect(Collectors.toList());
        log.debug("filter(>4): {}", filtered);

        // map — transforms each element (one-to-one)
        List<String> mapped = numbers.stream()
            .map(n -> "num:" + n)
            .collect(Collectors.toList());
        log.debug("map(n -> 'num:'+n): {}", mapped);

        // sorted — natural order; sorted(Comparator) for custom
        List<Integer> sorted = numbers.stream()
            .sorted()
            .collect(Collectors.toList());
        log.debug("sorted(): {}", sorted);

        // distinct — removes duplicates using equals/hashCode
        List<Integer> distinct = numbers.stream()
            .distinct()
            .collect(Collectors.toList());
        log.debug("distinct(): {}", distinct);

        // limit / skip — size control
        List<Integer> limited = numbers.stream().sorted().limit(3).collect(Collectors.toList());
        List<Integer> skipped = numbers.stream().sorted().skip(7).collect(Collectors.toList());
        log.debug("sorted().limit(3): {}", limited);
        log.debug("sorted().skip(7):  {}", skipped);

        // peek — inspect elements mid-pipeline without consuming; useful for debugging
        // JUNIOR NOTE: peek is for debugging only. Never use it for side effects
        // in production — its execution is not guaranteed in all stream optimisations.
        List<Integer> peeked = numbers.stream()
            .filter(n -> n > 4)
            .peek(n -> log.debug("  peek after filter: {}", n))
            .map(n -> n * 10)
            .collect(Collectors.toList());
        log.debug("peek result: {}", peeked);

        return String.format("""
            Intermediate operations (all LAZY — execute only when terminal is called):
            
            Source: %s
            
            filter(n > 4)       → %s
            map(n -> 'num:'+n)  → %s
            sorted()            → %s
            distinct()          → %s
            limit(3)            → %s   (after sort)
            skip(7)             → %s   (after sort)
            
            KEY POINT: intermediate ops return a NEW Stream — they do NOT modify
            the original collection. NAMES.stream().filter(...) does not change NAMES.
            
            peek(n -> log(n)) — shows elements mid-pipeline in the logs above.
            Use only for debugging. Never for side effects in production.
            """,
            numbers, filtered, mapped, sorted, distinct, limited, skipped);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q46 — Terminal operations
    // ─────────────────────────────────────────────────────────────────────────

    public String runTerminalOpsDemo() {
        log.debug("=== STREAM DEMO: terminal ops (Q46) ===");

        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // collect — most flexible terminal op; materialises the stream
        List<Integer> evens = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());

        // toList() — Java 16+ shorthand, returns unmodifiable list
        List<Integer> odds = numbers.stream()
            .filter(n -> n % 2 != 0)
            .toList();

        // reduce — fold all elements into a single value
        // identity=0, accumulator=(a,b)->a+b
        int sum = numbers.stream()
            .reduce(0, Integer::sum);

        // count — terminal, returns long
        long count = numbers.stream().filter(n -> n > 5).count();

        // anyMatch / allMatch / noneMatch — short-circuit terminals
        boolean anyOver9  = numbers.stream().anyMatch(n -> n > 9);
        boolean allPositive = numbers.stream().allMatch(n -> n > 0);
        boolean noneNeg   = numbers.stream().noneMatch(n -> n < 0);

        // findFirst — returns Optional<T>, short-circuits after first match
        Optional<Integer> first = numbers.stream().filter(n -> n > 5).findFirst();

        // min / max
        Optional<Integer> max = numbers.stream().max(Integer::compareTo);
        Optional<Integer> min = numbers.stream().min(Integer::compareTo);

        // forEach — side-effect terminal; no return value
        log.debug("forEach evens: ");
        numbers.stream().filter(n -> n % 2 == 0).forEach(n -> log.debug("  {}", n));

        // groupingBy — collector that returns Map<K, List<V>>
        Map<Boolean, List<Integer>> partitioned = numbers.stream()
            .collect(Collectors.partitioningBy(n -> n % 2 == 0));

        return String.format("""
            Terminal operations (trigger pipeline execution, consume the stream):
            
            Source: %s
            
            collect(toList()) evens     → %s
            toList() odds               → %s   (Java 16+, unmodifiable)
            reduce(0, Integer::sum)     → %d
            count(n > 5)                → %d
            anyMatch(n > 9)             → %s
            allMatch(n > 0)             → %s
            noneMatch(n < 0)            → %s
            findFirst(n > 5)            → %s
            max()                       → %s
            min()                       → %s
            partitioningBy(even)        → %s
            
            SHORT-CIRCUIT terminals — stop processing as soon as answer is known:
              anyMatch, allMatch, noneMatch, findFirst, findAny, limit
              e.g. anyMatch on a 1M-element stream stops at the first match.
            """,
            numbers, evens, odds, sum, count,
            anyOver9, allPositive, noneNeg,
            first, max, min, partitioned);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q47 — Lazy evaluation + stream reuse
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Two things this demo proves:
     *
     * 1. LAZY — intermediate ops do NOTHING until a terminal op is called.
     *    The peek calls below will only print if a terminal op is appended.
     *
     * 2. SINGLE USE — once a terminal op runs, the Stream is consumed.
     *    Calling any operation on it again throws IllegalStateException.
     */
    public String runLazyDemo() {
        log.debug("=== STREAM DEMO: lazy evaluation (Q46, Q47) ===");

        // ── Lazy proof ────────────────────────────────────────────────────────
        log.debug("Building pipeline... (no output yet — intermediate ops are lazy)");

        // JUNIOR NOTE: nothing in the pipeline below runs yet.
        // peek() will not print a single line until .collect() is called.
        Stream<String> pipeline = NAMES.stream()
            .peek(n -> log.debug("  [source] {}", n))
            .filter(n -> n.length() > 3)
            .peek(n -> log.debug("  [after filter] {}", n))
            .map(String::toUpperCase)
            .peek(n -> log.debug("  [after map] {}", n));

        log.debug("Pipeline built. Now calling terminal op collect()...");
        List<String> result = pipeline.collect(Collectors.toList());
        log.debug("Pipeline complete. Result: {}", result);

        // ── Reuse attempt ─────────────────────────────────────────────────────
        // The stream above is now consumed. Trying to use it again throws ISE.
        String reuseResult;
        try {
            pipeline.count(); // IllegalStateException: stream has already been operated upon
            reuseResult = "no exception (unexpected)";
        } catch (IllegalStateException e) {
            reuseResult = "IllegalStateException: " + e.getMessage();
            log.warn("Stream reuse attempt: {}", reuseResult);
        }

        // ── Vertical (per-element) vs horizontal (per-op) slicing ─────────────
        // JUNIOR NOTE: Streams process VERTICALLY — element by element through
        // the full pipeline, NOT horizontally (all elements through filter,
        // then all through map). This enables short-circuit and lazy optimisation.
        log.debug("Demonstrating vertical (element-by-element) processing:");
        List<String> vertical = List.of("alpha","beta","gamma","delta").stream()
            .filter(s -> { log.debug("  filter: {}", s); return s.length() > 4; })
            .map(s -> { log.debug("  map:    {}", s); return s.toUpperCase(); })
            .collect(Collectors.toList());
        log.debug("vertical result: {}", vertical);

        return String.format("""
            Lazy evaluation and stream reuse:
            
            LAZY PROOF:
              Pipeline was built with 3 peek() calls.
              Nothing printed until .collect() was called.
              Check the DEBUG logs — peek output appears only after terminal.
            
            STREAM REUSE:
              After .collect(), calling .count() on same reference:
              → %s
              A Stream is a one-shot cursor. Create a new one from the source.
            
            VERTICAL PROCESSING (check logs):
              Stream processes element-by-element through the FULL pipeline,
              not all-elements-through-filter then all-through-map.
              This means: findFirst() on a 1M-element stream may touch only 1 element.
            
            Result: %s
            """, reuseResult, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q50 — map vs flatMap
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * THE most important distinction to nail in a streams interview.
     *
     * map     — one element in  → one element out  (1:1 transformation)
     * flatMap — one element in  → zero or more elements out (1:N + flatten)
     *
     * flatMap = map + flatten one level of nesting.
     */
    public String runFlatMapDemo() {
        log.debug("=== STREAM DEMO: map vs flatMap (Q50) ===");

        // ── map: one-to-one ──────────────────────────────────────────────────
        // Stream<List<String>> → Stream<List<String>> (nested structure preserved)
        List<List<String>> mappedNested = NESTED.stream()
            .map(list -> list)   // map returns Stream<List<String>> — still nested
            .collect(Collectors.toList());
        log.debug("map(identity): still nested — {}", mappedNested);

        // map works perfectly for 1:1 transformations
        List<Integer> lengths = NAMES.stream()
            .map(String::length)    // String → Integer (1:1)
            .collect(Collectors.toList());
        log.debug("map(String::length): {}", lengths);

        // ── flatMap: one-to-many + flatten ───────────────────────────────────
        // Stream<List<String>> → Stream<String> (one level of nesting removed)
        // Each List<String> is "flattened" into individual String elements.
        List<String> flat = NESTED.stream()
            .flatMap(Collection::stream)   // List<String> → Stream<String>, then merged
            .collect(Collectors.toList());
        log.debug("flatMap(Collection::stream): flat — {}", flat);

        // ── Real-world flatMap: split sentences into words ────────────────────
        List<String> sentences = List.of(
            "the quick brown fox",
            "jumps over the lazy dog"
        );

        // map would give Stream<String[]> — an array per sentence — not what we want
        // flatMap gives Stream<String> — every word as a flat stream
        List<String> words = sentences.stream()
            .flatMap(s -> Arrays.stream(s.split(" ")))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        log.debug("flatMap words (distinct, sorted): {}", words);

        // ── flatMap on Optional (Q48 preview) ────────────────────────────────
        // Optional.flatMap avoids Optional<Optional<T>> nesting
        Optional<String> name = Optional.of("  vlad  ");
        Optional<String> trimmed = name
            .map(String::trim)
            .filter(s -> !s.isEmpty());
        log.debug("Optional map+filter: {}", trimmed);

        return String.format("""
            map vs flatMap (Q50):
            
            map — transforms each element 1:1. Returns Stream<R>.
              NAMES.stream().map(String::length) → %s
              One String in → one Integer out.
            
            flatMap — transforms each element into a Stream<R>, then FLATTENS.
              NESTED (3 lists) → flatMap(Collection::stream) → %s
              Each List<String> becomes a stream; all streams merged into one.
            
            MENTAL MODEL:
              map(f)     →  [f(a), f(b), f(c)]          (same count, different type)
              flatMap(f) →  [f(a)..., f(b)..., f(c)...] (count changes, one level flatter)
            
            REAL-WORLD USE CASES for flatMap:
              • Flatten nested collections (orders → order lines)
              • Split strings into tokens (sentences → words)
              • Chain Optional-returning methods without Optional<Optional<T>>
              • Explode one record into multiple rows (denormalisation)
            
            Sentences → distinct sorted words:
              %s
            """, lengths, flat, words);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q46 — Collectors
    // ─────────────────────────────────────────────────────────────────────────

    public String runCollectorsDemo() {
        log.debug("=== STREAM DEMO: collectors (Q46) ===");

        List<String> words = List.of("apple","banana","avocado","blueberry","cherry","apricot");

        // groupingBy — Map<K, List<V>>
        Map<Character, List<String>> byFirstLetter = words.stream()
            .collect(Collectors.groupingBy(w -> w.charAt(0)));
        log.debug("groupingBy first letter: {}", byFirstLetter);

        // groupingBy with downstream collector
        Map<Character, Long> countByLetter = words.stream()
            .collect(Collectors.groupingBy(w -> w.charAt(0), Collectors.counting()));
        log.debug("groupingBy + counting: {}", countByLetter);

        // partitioningBy — Map<Boolean, List<V>> (exactly two groups)
        Map<Boolean, List<String>> partitioned = words.stream()
            .collect(Collectors.partitioningBy(w -> w.length() > 6));
        log.debug("partitioningBy(len>6): {}", partitioned);

        // joining — concatenate strings
        String joined     = words.stream().collect(Collectors.joining(", "));
        String joinedFull = words.stream().collect(Collectors.joining(", ", "[", "]"));
        log.debug("joining: {}", joined);
        log.debug("joining with prefix/suffix: {}", joinedFull);

        // toMap — Map<K, V> from stream elements
        // JUNIOR NOTE: toMap throws IllegalStateException on duplicate keys.
        // Use mergeFunction (third arg) to handle duplicates: (a,b)->b keeps the last.
        Map<String, Integer> wordLengths = words.stream()
            .collect(Collectors.toMap(w -> w, String::length));
        log.debug("toMap(word->length): {}", wordLengths);

        // summarizingInt — count, sum, min, max, average in one pass
        IntSummaryStatistics stats = words.stream()
            .collect(Collectors.summarizingInt(String::length));
        log.debug("summarizingInt: count={} sum={} min={} max={} avg={}",
            stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage());

        return String.format("""
            Collectors demo:
            
            Source: %s
            
            groupingBy(first letter)         → %s
            groupingBy + counting()          → %s
            partitioningBy(length > 6)       → %s
            joining(", ")                    → %s
            joining(", ", "[", "]")          → %s
            toMap(w -> w, String::length)    → %s
            summarizingInt(length)           → count=%d sum=%d min=%d max=%d avg=%.1f
            
            // JUNIOR NOTE: toMap() throws on duplicate keys.
            // Fix: .collect(toMap(k, v, (existing, replacement) -> replacement))
            """,
            words, byFirstLetter, countByLetter, partitioned,
            joined, joinedFull, wordLengths,
            stats.getCount(), stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q49 — Primitive-specialized streams
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Stream<Integer> boxes each int into an Integer object → GC pressure.
     * IntStream, LongStream, DoubleStream work with primitives directly →
     * no boxing, better performance for numeric pipelines.
     */
    public String runPrimitiveStreamsDemo() {
        log.debug("=== STREAM DEMO: primitive streams (Q49) ===");

        // IntStream.range — [start, end)
        int sumRange = IntStream.range(1, 11).sum(); // 1+2+...+10
        log.debug("IntStream.range(1,11).sum() = {}", sumRange);

        // IntStream.rangeClosed — [start, end]
        long countRange = IntStream.rangeClosed(1, 100).filter(n -> n % 3 == 0).count();
        log.debug("multiples of 3 in 1..100 = {}", countRange);

        // mapToInt — convert Stream<String> to IntStream (no boxing)
        int totalLength = NAMES.stream()
            .mapToInt(String::length)  // Stream<String> → IntStream
            .sum();
        log.debug("total char count of all names = {}", totalLength);

        // IntSummaryStatistics — count, sum, min, max, average in one pass
        IntSummaryStatistics stats = NAMES.stream()
            .mapToInt(String::length)
            .summaryStatistics();
        log.debug("name length stats: {}", stats);

        // boxed() — IntStream → Stream<Integer> when you need objects
        List<Integer> boxed = IntStream.range(1, 6)
            .boxed()                    // int → Integer
            .collect(Collectors.toList());
        log.debug("IntStream.range(1,6).boxed(): {}", boxed);

        // LongStream — for large numbers
        long factorial10 = LongStream.rangeClosed(1, 10).reduce(1L, Math::multiplyExact);
        log.debug("10! via LongStream = {}", factorial10);

        // DoubleStream — for floating point
        OptionalDouble avg = DoubleStream.of(1.5, 2.5, 3.5, 4.5).average();
        log.debug("DoubleStream average = {}", avg);

        // PERFORMANCE: Stream<Integer> vs IntStream
        long start = System.nanoTime();
        int sumBoxed = IntStream.range(0, 1_000_000)
            .boxed()                              // forces boxing
            .reduce(0, Integer::sum);
        long boxedTime = System.nanoTime() - start;

        start = System.nanoTime();
        int sumPrimitive = IntStream.range(0, 1_000_000).sum(); // no boxing
        long primitiveTime = System.nanoTime() - start;

        log.debug("Sum 1M: boxed={}ms primitive={}ms", boxedTime/1_000_000, primitiveTime/1_000_000);

        return String.format("""
            Primitive streams (Q49):
            
            THREE primitive stream types:
              IntStream    — int primitives  (no Integer boxing)
              LongStream   — long primitives
              DoubleStream — double primitives
            
            WHY they exist:
              Stream<Integer> boxes every int → Integer on the heap → GC pressure.
              IntStream works with int[] directly → zero boxing overhead.
            
            EXTRA METHODS (not on Stream<T>):
              .sum()                → total
              .average()            → OptionalDouble
              .summaryStatistics()  → count, sum, min, max, average in one pass
              .range(a, b)          → [a, b)  exclusive end
              .rangeClosed(a, b)    → [a, b]  inclusive end
              .boxed()              → IntStream → Stream<Integer> when needed
            
            CONVERSION:
              stream.mapToInt(fn)   → Stream<T>   → IntStream
              intStream.mapToObj(fn)→ IntStream   → Stream<T>
              intStream.boxed()     → IntStream   → Stream<Integer>
            
            RESULTS:
              IntStream.range(1,11).sum()          = %d
              multiples of 3 in 1..100             = %d
              total name length                    = %d
              name length stats                    = %s
              10! via LongStream                   = %d
              DoubleStream average                 = %s
              Sum 1M (boxed=%dms vs primitive=%dms)
            """,
            sumRange, countRange, totalLength, stats, factorial10, avg,
            boxedTime/1_000_000, primitiveTime/1_000_000);
    }
}
