# java-interview-prep

[GitHub Pages site](https://vbforge.github.io/java-interview-prep)

Structured preparation for Java backend interviews — 89 questions mapped to 10 theory files and 7 runnable Spring Boot demos. 
Built as a junior→middle transition reference.

> Based on a real first-round recruitment agency screen covering all required sections:
> Java memory model · Collections · Concurrency · Spring · JPA · Databases.
- **[original questions file (pdf)](first-round-interview-recruitment-agency-questions.pdf)**
- **[numbering questions file (md)](numbering-questions-readme.md)**

---

## Repository structure

```
java-interview-prep/
│
├── README.md
├── docs/                              # GitHub Pages source
│   └── index.md
│
├── theory/                            # Pure Q&A reference — no code, just answers
│   ├── 01-jvm-memory.md               # Q1–Q8
│   ├── 02-data-types-strings.md       # Q9–Q17
│   ├── 03-oop-generics.md             # Q18–Q27
│   ├── 04-collections-list.md         # Q28–Q33
│   ├── 05-collections-hashmap.md      # Q34–Q44
│   ├── 06-streams-lambdas.md          # Q45–Q52
│   ├── 07-concurrency.md              # Q53–Q63
│   ├── 08-spring-core-web.md          # Q64–Q77
│   ├── 09-jpa-databases.md            # Q78–Q86
│   └── 10-design-patterns.md          # Q87–Q89
│                   
├── demo-01-jvm-memory/                        # Runnable Spring Boot / Java projects
├── demo-02-collections/
├── demo-03-streams/
├── demo-04-concurrency/
├── demo-05-spring-core/
├── demo-06-spring-data/
├── demo-07-patterns/
│
└── interview-scorecard/
    └── scorecard-template.md          # 1–5 rating sheet from the original screen
```

**Two separate concerns, two separate folders:**
- `theory/` — 10 flat markdown files. Open one 10 minutes before an interview and read answers fast.
- `demos/` — runnable Spring Boot modules. One module per topic, fully self-contained.

---

## Theory files

| File | Topic | Questions |
|------|-------|-----------|
| [01-jvm-memory.md](helpers-files/theory/01-jvm-memory.md) | JVM memory, GC, G1 | Q1–Q8     |
| [02-data-types-strings.md](helpers-files/theory/02-data-types-strings.md) | Primitives, boxing, String pool | Q9–Q17    |
| [03-oop-generics.md](helpers-files/theory/03-oop-generics.md) | Inheritance, interfaces, generics | Q18–Q27   |
| [04-collections-list.md](helpers-files/theory/04-collections-list.md) | List, ArrayList, LinkedList, Iterator | Q28–Q33   |
| [05-collections-hashmap.md](helpers-files/theory/05-collections-hashmap.md) | HashMap internals, TreeMap | Q34–Q44   |
| [06-streams-lambdas.md](helpers-files/theory/06-streams-lambdas.md) | Streams, Optional, lambdas, functional interfaces | Q45–Q52   |
| [07-concurrency.md](helpers-files/theory/07-concurrency.md) | Threads, volatile, CAS, ReentrantLock | Q53–Q63   |
| [08-spring-core-web.md](helpers-files/theory/08-spring-core-web.md) | Beans, DI, scopes, HTTP, CORS | Q64–Q77   |
| [09-jpa-databases.md](helpers-files/theory/09-jpa-databases.md) | JPA, N+1, locking, indexes | Q78–Q86   |
| [10-design-patterns.md](helpers-files/theory/10-design-patterns.md) | GoF groups, Spring patterns | Q87–Q89   |

Each theory file follows the same structure:

```markdown
## Qn — Question text

**Short answer** (2–3 sentences, interview-ready)

**In depth** (explanation with code snippet where useful)

**// JUNIOR NOTE:** common mistake or trap to avoid

---
```

---

## Demos

Each demo is a standalone Maven module — no shared parent, no cross-module dependencies. Copy any one folder and it runs on its own.

| Module | Covers | Stack |
|--------|--------|-------|
| [demo-01-jvm-memory](demos/demo-01-jvm-memory) | GC logging, heap regions, object lifecycle | Java 21, G1GC flags |
| [demo-02-collections](demos/demo-02-collections) | HashMap collision, TreeMap ordering, Iterator | Java 21 |
| [demo-03-streams](demos/demo-03-streams) | Stream pipelines, flatMap, collectors, lazy eval | Java 21 |
| [demo-04-concurrency](demos/demo-04-concurrency) | Race condition demo, CAS vs synchronized, ReentrantLock | Java 21 |
| [demo-05-spring-core](demos/demo-05-spring-core) | Bean scopes, lifecycle hooks, DI types, circular dep | Spring Boot 3, port 8085 |
| [demo-06-spring-data](demos/demo-06-spring-data) | EAGER/LAZY fetch, N+1 fix, optimistic/pessimistic lock, index | Spring Boot 3, PostgreSQL, port 8086 |
| [demo-07-patterns](demos/demo-07-patterns) | Singleton, Factory, Proxy, Strategy in Spring context | Spring Boot 3, port 8087 |

### Running a demo

```bash
cd demos/demo-05-spring-core
mvn spring-boot:run
```

Demos that need a database ship with a `docker-compose.yml`:

```bash
cd demos/demo-06-spring-data
docker compose up -d
mvn spring-boot:run
```

### Demo conventions

Every demo follows the same layout to keep things predictable:

```
demo-0N-topic/
├── pom.xml
├── docker-compose.yml          # only where needed
├── README.md                   # what the demo shows and how to run it
├── src/main/java/com/vbforge/
│   └── MainApp.java            # @SpringBootApplication + @EnableKafka if needed
└── src/main/resources/
    └── application.yml
```

- Package namespace: `com.vbforge`
- No Spring Boot autoconfiguration for infrastructure beans — everything wired manually so the intent is visible
- `// JUNIOR NOTE:` inline comments throughout, same style as the Kafka series

---

## Interview scorecard

The file [interview-scorecard/scorecard-template.md](interview-scorecard/scorecard-template.md) is a markdown version of the original recruiter screen. Use it to:

- self-assess after each theory pass (rate yourself 1–5 per section)
- mock-interview with a peer (fill in as the "interviewer")
- track progress over time by saving dated copies

Rating scale from the original screen:

| Score | Meaning |
|-------|---------|
| 5 | Absolute star |
| 4 | Excellent overall |
| 3 | Meets requirement |
| 2 | Doesn't meet requirement, but has bright spots |
| 1 | Not good enough |

---

## Progress tracking

The [GitHub Pages site](https://vbforge.github.io/java-interview-prep) has a live question tracker with checkboxes for all 89 questions, per-section progress bars, and the interactive scorecard.

For local tracking, copy `interview-scorecard/scorecard-template.md` to `scorecard-YYYY-MM-DD.md` and fill it in after each full pass.

---

## Tech stack

| Layer | Choice |
|-------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Build | Maven (independent modules, no shared parent) |
| Database | PostgreSQL 16-alpine (Docker) |
| Containers | Docker Desktop / Docker Compose |

---

## Related repos

| Repo | What it covers |
|------|---------------|
| [spring-mastery](https://github.com/vbforge/spring-mastery) | Structured Spring learning with GitHub Pages index |
| [java-vbforge-core-deep-dive](https://github.com/vbforge/java-vbforge-core-deep-dive) | Java core modules 01–18: JVM, concurrency, reflection, testing |

---

## License

MIT
