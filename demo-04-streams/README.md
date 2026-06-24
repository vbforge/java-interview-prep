# demo-03 — Streams & Lambdas

- > **Theory file:** [06-streams-lambdas.md](06-streams-lambdas.md)
- > **Return to root README:** [java-interview-prep README](../README.md)
- > **[GitHub Pages site](https://vbforge.github.io/java-interview-prep)**
- > **Questions covered:** Q45–Q52
- > **Port:** 8084

Standalone Spring Boot module. No database, no Docker required.

---

## How to run

```bash
cd demos/demo-04-streams
mvn spring-boot:run
```

Then open: `http://localhost:8084/demo`

---

## Endpoints

### Streams — Q45–Q50

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/streams/pipeline` | Q45 | Stream source types, three-part pipeline anatomy |
| `GET /demo/streams/intermediate` | Q46 | `filter`, `map`, `sorted`, `distinct`, `peek`, `limit`, `skip` |
| `GET /demo/streams/terminal` | Q46 | `collect`, `reduce`, `count`, `anyMatch`, `findFirst`, `min`/`max` |
| `GET /demo/streams/lazy` | Q46, Q47 | Lazy evaluation proof via `peek` + `IllegalStateException` on reuse |
| `GET /demo/streams/flatmap` | Q50 | `map` vs `flatMap` — nested lists, sentence→words, Optional |
| `GET /demo/streams/collectors` | Q46 | `groupingBy`, `partitioningBy`, `joining`, `toMap`, `summarizingInt` |
| `GET /demo/streams/primitive` | Q49 | `IntStream`, `LongStream`, `DoubleStream`, boxing cost benchmark |

### Optional — Q48

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/optional/basics` | Q48 | `of`, `empty`, `ofNullable`, `orElse`, `orElseGet`, `ifPresent` |
| `GET /demo/optional/chaining` | Q48 | `map`, `flatMap`, `filter`, `or` — container vs stream distinction |

### Lambdas — Q51–Q52

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/lambdas/basics` | Q51 | Syntax forms, compilation to `invokedynamic`, capture rules |
| `GET /demo/lambdas/functional` | Q52 | `Function`, `Predicate`, `Consumer`, `Supplier`, `Bi*`, `UnaryOperator` |
| `GET /demo/lambdas/method-refs` | Q51 | Four kinds: static, instance on instance, instance on type, constructor |

---

## Key things to watch in the logs

**`/demo/streams/lazy`** — the most important log output in this demo:
```
Building pipeline... (no output yet — intermediate ops are lazy)
Pipeline built. Now calling terminal op collect()...
  [source] alice
  [after filter] alice       ← peek fires NOW, not when pipeline was built
  [after map] ALICE
  [source] bob               ← next element through the full pipeline
...
Pipeline complete.
```
This proves vertical (element-by-element) processing and lazy evaluation.

**`/demo/streams/lazy`** also shows:
```
Stream reuse: IllegalStateException: stream has already been operated upon or closed
```

**`/demo/optional/chaining`** — watch how empty short-circuits:
```
flatMap findEmail('vlad')         = Optional[vlad@vbforge.com]
flatMap findEmail('unknown-user') = Optional.empty
```

---

## Key concepts cheat sheet

```
STREAM PIPELINE:
  Source → intermediate ops (lazy, 0..n) → terminal op (eager, exactly 1)
  Stream is single-use — IllegalStateException on second terminal call

INTERMEDIATE (lazy, return Stream):
  filter(Predicate)     keep matching elements
  map(Function)         1:1 transform
  flatMap(Function)     1:N transform + flatten one level
  sorted()              natural or Comparator order
  distinct()            removes duplicates (uses equals/hashCode)
  peek(Consumer)        debug only — side effect mid-pipeline
  limit(n) / skip(n)    size control — limit is short-circuit

TERMINAL (eager, consume stream):
  collect(Collector)    materialise — toList(), toSet(), toMap(), groupingBy()
  forEach(Consumer)     side effect, no return
  reduce(identity, op)  fold to single value
  count()               long
  findFirst()           Optional<T>, short-circuit
  anyMatch / allMatch / noneMatch   boolean, short-circuit

PRIMITIVE STREAMS (no boxing):
  IntStream, LongStream, DoubleStream
  Extra: sum(), average(), summaryStatistics(), range(), rangeClosed()
  Convert: .mapToInt(fn) / .boxed() / .mapToObj(fn)

map vs flatMap:
  map(f)     → [f(a), f(b)]         same count, different type
  flatMap(f) → [f(a)..., f(b)...]   flattened, count can change

OPTIONAL:
  Container for 0 or 1 value. NOT a stream.
  map/flatMap/filter transform the value, short-circuit on empty.
  Use only as method return type — not as field, param, or in collections.

FUNCTIONAL INTERFACES (core four):
  Function<T,R>   apply()   T → R
  Predicate<T>    test()    T → boolean
  Consumer<T>     accept()  T → void
  Supplier<T>     get()     () → T
```

---

## Project structure

```
demo-04-streams/
├── pom.xml
├── README.md
└── src/main/java/com/vbforge/streams/
    ├── MainApp.java
    ├── config/
    │   └── DemoController.java       ← all endpoints
    ├── streams/
    │   └── StreamDemo.java           ← Q45–Q50
    ├── optional/
    │   └── OptionalDemo.java         ← Q48
    └── lambdas/
        └── LambdaDemo.java           ← Q51–Q52
```

---

