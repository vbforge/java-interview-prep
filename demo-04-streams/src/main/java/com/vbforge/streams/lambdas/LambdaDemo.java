package com.vbforge.streams.lambdas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Q51 — What is a lambda expression in Java? What is it compiled to?
 * Q52 — Standard functional interfaces.
 */
@Component
public class LambdaDemo {

    private static final Logger log = LoggerFactory.getLogger(LambdaDemo.class);

    // ─────────────────────────────────────────────────────────────────────────
    // Q51 — Lambda basics, syntax, compilation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A lambda expression is an anonymous function — a block of code with
     * parameters and a body, treated as a value (can be passed around).
     *
     * WHAT IT COMPILES TO:
     *   A lambda is syntactic sugar for an anonymous class implementing a
     *   @FunctionalInterface (an interface with exactly ONE abstract method).
     *   The compiler infers the target type from context.
     *   At runtime, lambdas use invokedynamic (not new anonymous class instances)
     *   via LambdaMetafactory — more efficient than anonymous classes.
     */
    public String runBasicsDemo() {
        log.debug("=== LAMBDA DEMO: basics (Q51) ===");

        // ── Syntax forms ──────────────────────────────────────────────────────

        // 1. No parameters
        Runnable noParams = () -> log.debug("  lambda: no params");
        noParams.run();

        // 2. One parameter — parentheses optional
        Consumer<String> oneParam = s -> log.debug("  lambda: one param → {}", s);
        oneParam.accept("hello");

        // 3. Two parameters
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        log.debug("  BiFunction add(3,4) = {}", add.apply(3, 4));

        // 4. Multi-line block body with explicit return
        Function<String, String> shout = s -> {
            String upper = s.toUpperCase();
            return upper + "!";
        };
        log.debug("  shout('hello') = {}", shout.apply("hello"));

        // ── What it compiles to (conceptually) ───────────────────────────────
        // This lambda:
        //   Predicate<String> p = s -> s.length() > 3;
        //
        // Is EQUIVALENT to this anonymous class (but more efficient at runtime):
        //   Predicate<String> p = new Predicate<String>() {
        //       @Override public boolean test(String s) { return s.length() > 3; }
        //   };
        //
        // At bytecode level: invokedynamic → LambdaMetafactory (NOT a new class file)
        Predicate<String> lambda = s -> s.length() > 3;
        Predicate<String> anon   = new Predicate<>() {
            @Override public boolean test(String s) { return s.length() > 3; }
        };
        log.debug("  lambda.test('hi') = {} — same as anon.test: {}", lambda.test("hi"), anon.test("hi"));

        // ── Variable capture — effectively final ─────────────────────────────
        // JUNIOR NOTE: A lambda can capture local variables from its enclosing scope,
        // but ONLY if they are effectively final (never reassigned after initialisation).
        String prefix = "Hello"; // effectively final — never reassigned
        Function<String, String> greet = name -> prefix + ", " + name + "!";
        log.debug("  captured prefix: {}", greet.apply("Vlad"));

        // This would NOT compile — 'prefix' would need to be effectively final:
        // prefix = "Hi";  // ← would make the lambda illegal

        // ── @FunctionalInterface ──────────────────────────────────────────────
        // An interface with exactly ONE abstract method.
        // The @FunctionalInterface annotation is optional but makes the compiler
        // enforce the single-abstract-method rule.
        MyTransformer upper = s -> s.toUpperCase(); // target type inferred
        log.debug("  MyTransformer: {}", upper.transform("vbforge"));

        return """
            Lambda basics (Q51):
            
            SYNTAX:
              ()        -> expr           no parameters
              x         -> expr           one parameter (parens optional)
              (x, y)    -> expr           two parameters
              (x, y)    -> { stmts; return val; }  block body
            
            WHAT IT COMPILES TO:
              A lambda is an instance of a @FunctionalInterface.
              The compiler infers the target type from the assignment context.
              At runtime: invokedynamic → LambdaMetafactory (NOT a new .class file per lambda)
              This is more efficient than anonymous inner classes.
            
            EFFECTIVELY FINAL CAPTURE:
              Lambdas can read local variables from enclosing scope.
              Those variables must be effectively final (never reassigned).
              Instance fields and static fields can be read AND written.
            
            @FunctionalInterface:
              Interface with exactly ONE abstract method.
              Can have default methods and static methods (those don't count).
              Examples: Runnable, Callable, Comparator, Predicate, Function, Consumer, Supplier
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q52 — Standard functional interfaces
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The java.util.function package contains 43 functional interfaces.
     * Interviewers expect you to know the four core ones and their variants.
     *
     * CORE FOUR:
     *   Function<T,R>    T → R       (transform)
     *   Predicate<T>     T → boolean (test/filter)
     *   Consumer<T>      T → void    (side effect)
     *   Supplier<T>      () → T      (produce)
     */
    public String runFunctionalInterfacesDemo() {
        log.debug("=== LAMBDA DEMO: functional interfaces (Q52) ===");

        // ── Function<T, R> — takes T, returns R ──────────────────────────────
        Function<String, Integer> strLen  = String::length;
        Function<Integer, String> intStr  = Object::toString;
        Function<String, String>  shout   = s -> s.toUpperCase() + "!";

        // andThen — compose: apply f then g  →  g(f(x))
        Function<String, String> lenThenStr = strLen.andThen(intStr);
        // compose — compose: apply g then f  →  f(g(x))
        log.debug("Function strLen('hello') = {}", strLen.apply("hello"));
        log.debug("Function andThen: 'hello' → length → string = '{}'", lenThenStr.apply("hello"));

        // ── Predicate<T> — takes T, returns boolean ───────────────────────────
        Predicate<String> longWord  = s -> s.length() > 5;
        Predicate<String> startsA   = s -> s.startsWith("a");

        // and / or / negate — compose predicates
        Predicate<String> longAndA  = longWord.and(startsA);
        Predicate<String> longOrA   = longWord.or(startsA);
        Predicate<String> notLong   = longWord.negate();

        List<String> words = List.of("apple", "avocado", "banana", "blueberry", "kiwi");
        log.debug("Predicate longWord:  {}", words.stream().filter(longWord).collect(Collectors.toList()));
        log.debug("Predicate longAndA:  {}", words.stream().filter(longAndA).collect(Collectors.toList()));
        log.debug("Predicate longOrA:   {}", words.stream().filter(longOrA).collect(Collectors.toList()));
        log.debug("Predicate notLong:   {}", words.stream().filter(notLong).collect(Collectors.toList()));

        // ── Consumer<T> — takes T, returns void (side effect) ────────────────
        Consumer<String> print  = s -> log.debug("  Consumer print: {}", s);
        Consumer<String> shoutPrint = s -> log.debug("  Consumer shout: {}!", s.toUpperCase());

        // andThen — chain consumers
        Consumer<String> both = print.andThen(shoutPrint);
        both.accept("vbforge");

        // ── Supplier<T> — takes nothing, returns T (lazy factory) ────────────
        Supplier<List<String>> listFactory = ArrayList::new; // method reference
        Supplier<Double>       random      = Math::random;
        Supplier<String>       greeting    = () -> "Hello, " + System.getProperty("user.name");

        List<String> newList = listFactory.get(); // creates a new ArrayList each call
        newList.add("from supplier");
        log.debug("Supplier listFactory: {}", newList);
        log.debug("Supplier random: {}", random.get());

        // ── Bi-variants — two input parameters ───────────────────────────────
        BiFunction<String, Integer, String>  repeat = String::repeat;
        BiPredicate<String, String>          starts = String::startsWith;
        BiConsumer<String, Integer>          logIt  = (s, n) -> log.debug("  BiConsumer: {} × {}", s, n);

        log.debug("BiFunction repeat('ab', 3) = '{}'", repeat.apply("ab", 3));
        log.debug("BiPredicate starts('avocado','av') = {}", starts.test("avocado", "av"));
        logIt.accept("hello", 42);

        // ── UnaryOperator<T> / BinaryOperator<T> ────────────────────────────
        // Specialisations of Function where input and output types are the same
        UnaryOperator<String>  trim    = String::trim;
        BinaryOperator<String> concat  = String::concat;
        BinaryOperator<Integer> maxInt = BinaryOperator.maxBy(Integer::compareTo);

        log.debug("UnaryOperator trim: '{}'", trim.apply("  hello  "));
        log.debug("BinaryOperator concat: '{}'", concat.apply("foo", "bar"));
        log.debug("BinaryOperator maxBy: {}", maxInt.apply(7, 42));

        return String.format("""
            Standard functional interfaces (Q52):
            
            CORE FOUR (java.util.function):
            ─────────────────────────────────────────────────────────────────
            Interface           Signature          Method    Use case
            ─────────────────────────────────────────────────────────────────
            Function<T,R>       T → R              apply()   transform / map
            Predicate<T>        T → boolean        test()    filter / condition
            Consumer<T>         T → void           accept()  side effect / forEach
            Supplier<T>         () → T             get()     factory / lazy value
            ─────────────────────────────────────────────────────────────────
            
            BI-VARIANTS (two inputs):
              BiFunction<T,U,R>   (T,U) → R
              BiPredicate<T,U>    (T,U) → boolean
              BiConsumer<T,U>     (T,U) → void
            
            OPERATOR VARIANTS (same type in and out):
              UnaryOperator<T>    T → T            (Function<T,T> specialisation)
              BinaryOperator<T>   (T,T) → T        (BiFunction<T,T,T> specialisation)
            
            COMPOSITION METHODS:
              Function  : andThen(g) = g(f(x))   compose(g) = f(g(x))
              Predicate : and(), or(), negate()
              Consumer  : andThen()  — chain side effects
            
            FILTER results on words %s:
              longWord (>5)       = %s
              longWord AND startsA = %s
              longWord OR startsA  = %s
              NOT longWord         = %s
            """,
            words,
            words.stream().filter(longWord).collect(Collectors.toList()),
            words.stream().filter(longAndA).collect(Collectors.toList()),
            words.stream().filter(longOrA).collect(Collectors.toList()),
            words.stream().filter(notLong).collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q51 — Four kinds of method references
    // ─────────────────────────────────────────────────────────────────────────

    public String runMethodRefsDemo() {
        log.debug("=== LAMBDA DEMO: method references (Q51) ===");

        List<String> names = List.of("alice", "bob", "charlie");

        // 1. Static method reference:      ClassName::staticMethod
        //    equivalent lambda: s -> Integer.parseInt(s)
        Function<String, Integer> parseInt = Integer::parseInt;
        log.debug("Static ref Integer::parseInt('42') = {}", parseInt.apply("42"));

        // 2. Instance method ref on specific instance:  instance::method
        //    equivalent lambda: s -> "prefix-".concat(s)
        String prefix = "prefix-";
        Function<String, String> addPrefix = prefix::concat;
        log.debug("Instance ref prefix::concat('abc') = '{}'", addPrefix.apply("abc"));

        // 3. Instance method ref on arbitrary instance:  ClassName::instanceMethod
        //    equivalent lambda: s -> s.toUpperCase()
        //    The first argument becomes the receiver.
        Function<String, String>   toUpper  = String::toUpperCase;
        Function<String, Integer>  length   = String::length;
        Predicate<String>          isEmpty  = String::isEmpty;
        log.debug("Arbitrary instance ref String::toUpperCase: {}",
            names.stream().map(toUpper).collect(Collectors.toList()));

        // 4. Constructor reference:         ClassName::new
        //    equivalent lambda: s -> new StringBuilder(s)
        Function<String, StringBuilder> sbFactory = StringBuilder::new;
        StringBuilder sb = sbFactory.apply("hello");
        log.debug("Constructor ref StringBuilder::new('hello') = '{}'", sb);

        // Practical use — constructor ref with Supplier
        Supplier<ArrayList<String>> listMaker = ArrayList::new;
        // Used by Collectors: Collectors.toCollection(ArrayList::new)
        List<String> collected = names.stream()
            .collect(Collectors.toCollection(ArrayList::new));
        log.debug("toCollection(ArrayList::new): {}", collected);

        return """
            Four kinds of method references (Q51):
            
            KIND                        SYNTAX                   EQUIVALENT LAMBDA
            ──────────────────────────────────────────────────────────────────────
            1. Static method            ClassName::staticMethod  (args) -> Cls.method(args)
               Integer::parseInt        String → Integer         s -> Integer.parseInt(s)
            
            2. Instance on instance     instance::method         (args) -> inst.method(args)
               "prefix-"::concat        String → String          s -> "prefix-".concat(s)
            
            3. Instance on arbitrary    ClassName::instanceMethod (obj, args) -> obj.method(args)
               String::toUpperCase      String → String          s -> s.toUpperCase()
               String::length           String → int             s -> s.length()
            
            4. Constructor              ClassName::new            (args) -> new Cls(args)
               ArrayList::new           () → ArrayList            () -> new ArrayList<>()
               StringBuilder::new       String → StringBuilder    s -> new StringBuilder(s)
            
            // JUNIOR NOTE: Method references are NOT faster than lambdas —
            // they compile to the same invokedynamic bytecode.
            // They are preferred for readability when the lambda just delegates.
            """;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Custom @FunctionalInterface used in basicsDemo
    // ─────────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface MyTransformer {
        String transform(String input);
        // Could have default/static methods — they don't break the single-abstract-method rule
        default String transformAndShout(String input) { return transform(input) + "!"; }
    }
}
