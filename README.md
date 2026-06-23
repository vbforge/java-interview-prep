# java-interview-prep

[GitHub Pages site](https://vbforge.github.io/java-interview-prep)

Structured preparation for Java backend interviews — 89 questions mapped to 10 theory files and 7 runnable Spring Boot demos. 
Built as a junior→middle transition reference.

> Based on a real first-round recruitment agency screen covering all required sections:
> Java memory model · Collections · Concurrency · Spring · JPA · Databases.
- **[original questions file (pdf)](docs/assets/interview-agency-questions.pdf)**
- **[numbering questions file (md)](docs/assets/numbering-questions-readme.md)**

---

## Repository structure

```
java-interview-prep/
│
├── README.md                                          # this file
├── docs/                                              # GitHub Pages source (Q&A reference - answers)
│   ├── assets
│   │   ├── numbering-questions-readme.md              # Questions list readme
│   │   └── interview-agency-questions.pdf             # Original document with questions list
│   ├── 01-jvm-memory.html                             # Q1–Q8
│   ├── 02-data-types-strings.html                     # Q9–Q17
│   ├── 03-oop-generics.html                           # Q18–Q27
│   ├── 04-collections-list.html                       # Q28–Q33
│   ├── 05-collections-hashmap.html                    # Q34–Q44
│   ├── 06-streams-lambdas.html                        # Q45–Q52
│   ├── 07-concurrency.html                            # Q53–Q63
│   ├── 08-spring-core-web.html                        # Q64–Q77
│   ├── 09-jpa-databases.html                          # Q78–Q86
│   ├── 10-design-patterns.html                        # Q87–Q89
│   ├── index.html                                     # Start / Home page 
│   └── numbering-questions.html                       # Questions list
│                   
├── demo-01-jvm-memory/                                # Runnable Spring Boot / Java projects
├── demo-02-collections/
├── demo-03-streams/
├── demo-04-concurrency/
├── demo-05-spring-core/
├── demo-06-spring-data/
└── demo-07-patterns/

```

---

## Projects across this repo

Each demo is a standalone Maven module — no shared parent, no cross-module dependencies. Copy any one folder and it runs on its own.

| Module                                     | Covers                                                        | Stack                                |
|--------------------------------------------|---------------------------------------------------------------|--------------------------------------|
| [demo-01-jvm-memory](demo-01-jvm-memory)   | GC logging, heap regions, object lifecycle                    | Java 21, G1GC flags                  |
| [demo-02-collections](demo-02-collections) | HashMap collision, TreeMap ordering, Iterator                 | Java 21                              |
| [demo-03-streams](demo-03-streams)         | Stream pipelines, flatMap, collectors, lazy eval              | Java 21                              |
| [demo-04-concurrency](demo-04-concurrency) | Race condition demo, CAS vs synchronized, ReentrantLock       | Java 21                              |
| [demo-05-spring-core](demo-05-spring-core) | Bean scopes, lifecycle hooks, DI types, circular dep          | Spring Boot 3, port 8085             |
| [demo-06-spring-data](demo-06-spring-data) | EAGER/LAZY fetch, N+1 fix, optimistic/pessimistic lock, index | Spring Boot 3, PostgreSQL, port 8086 |
| [demo-07-patterns](demo-07-patterns)       | Singleton, Factory, Proxy, Strategy in Spring context         | Spring Boot 3, port 8087             |

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
- `// JUNIOR NOTE:` inline comments throughout

---

## Progress tracking

The **[GitHub Page](https://vbforge.github.io/java-interview-prep)** has a live [question tracker with checkboxes](https://vbforge.github.io/java-interview-prep/#section-tracker) for all 89 questions, 
per-section progress bars, and the [interactive scorecard](https://vbforge.github.io/java-interview-prep/#section-scorecard).

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

