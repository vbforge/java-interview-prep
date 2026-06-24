# Java Interview Mastery Repository

## Numbered Questions (Concrete Task IDs)

### Java Memory Model (Q1-Q9)
- **Q1:** What kinds of memory exist in Java? What is the difference between them? Which is used when?
- **Q2:** What is the stack for, and what is stored in it?
- **Q3:** What regions is the heap divided into?
- **Q4:** Explain garbage collection in Java.
- **Q5:** Which garbage collectors do you know?
- **Q6:** How does the collector know an object has become garbage?
- **Q7:** Do primitives need to be collected?
- **Q8:** How does G1 work?

### Data Types (Q9-Q13)
- **Q9:** The two main groups of data types
- **Q10:** How to compare primitives and objects
- **Q11:** Passing method arguments by value vs by reference
- **Q12:** Object o = new Object(); — What is stored in o? Where does new Object() live?
- **Q13:** Boxing/unboxing

### Strings in Java (Q14-Q17)
- **Q14:** Is there a difference between String s1 = "string" and String s2 = new String("string")?
- **Q15:** s1 == s2 vs s1.equals(s2)
- **Q16:** String.intern()
- **Q17:** Are String objects mutable or immutable?

### Inheritance and Generics (Q18-Q21)
- **Q18:** What are generics, and what are they for?
- **Q19:** Examples of generics in Java
- **Q20:** extends / super (in generic bounds and wildcards)

### Abstract Classes and Interfaces (Q21-Q27)
- **Q21:** Difference between an abstract class and an interface
- **Q22:** How many supertypes you can extend / implement
- **Q23:** Presence of a constructor
- **Q24:** Default access modifiers of abstract class and interface
- **Q25:** Multiple inheritance with interfaces
- **Q26:** What if two interface methods "overlap" (same signature from two interfaces)?
- **Q27:** What if two default methods in interfaces conflict?

### Collections – General (Q28-Q34)
- **Q28:** What kinds of collections exist in Java?
- **Q29:** Describe List.
- **Q30:** Time complexity of insertion and read/access.
- **Q31:** When is an array-based list better, when is a linked list better?
- **Q32:** How does a list differ from a plain array?
- **Q33:** What is an iterator?

### HashMap (Q34-Q43)
- **Q34:** Describe HashMap.
- **Q35:** How is it implemented?
- **Q36:** What is a hash code?
- **Q37:** Hash code normalization (spreading / masking the hash).
- **Q38:** How is it related to equals?
- **Q39:** Why does that matter for a hash map?
- **Q40:** Data structure in a bucket when there is a collision.
- **Q41:** What are capacity and load factor? What is the default load factor?
- **Q42:** Time complexity of lookup and insertion.
- **Q43:** In what order do you get elements if you iterate the map? Why?
- **Q44:** Difference between HashMap and TreeMap.

### Streams (Q45-Q52)
- **Q45:** In Java, what is a Stream?
- **Q46:** What is the difference between intermediate and terminal stream operations? Give two examples of each.
- **Q47:** After you call a terminal operation on a Stream, can you run another terminal operation on the same Stream reference?
- **Q48:** Does Optional have terminal/intermediate operations like a stream, or is it a one-shot container for zero or one value?
- **Q49:** Besides Stream<T>, which primitive-specialized stream types exist?
- **Q50:** Difference between map and flatMap on a stream
- **Q51:** What is a lambda expression in Java? What is it compiled to (functional interface, syntactic sugar)?
- **Q52:** Standard functional interfaces

### Concurrency (Q53-Q61)
- **Q53:** How do you safely share mutable state (field, collection, counter) across threads?
- **Q54:** What goes wrong if two threads read–modify–write a shared variable without coordination?
- **Q55:** What is a data race? Is the outcome always obviously wrong?
- **Q56:** Why doesn't making a field volatile alone make i++ atomic?
- **Q57:** How does CAS in AtomicInteger differ from i++ under synchronized?
- **Q58:** When prefer atomics vs locks / synchronized?
- **Q59:** ReentrantLock — What does reentrant mean here?
- **Q60:** Why must unlock run in finally?
- **Q61:** What do synchronized and ReentrantLock share regarding mutual exclusion?
- **Q62:** What can ReentrantLock do that synchronized cannot (interruptible wait, tryLock, timed lock, multiple conditions)?
- **Q63:** Can the same thread re-enter synchronized on the same monitor — why?

### Spring Core (Q64-Q74)
- **Q64:** What is a Spring bean?
- **Q65:** Where are beans stored, and how do you retrieve them from the container?
- **Q66:** What ways exist to inject dependencies (constructor, setter, field, etc.)?
- **Q67:** Which bean scopes do you know (singleton, prototype, request, session, …)?
- **Q68:** What is @PostConstruct used for?
- **Q69:** What is @Qualifier used for?
- **Q70:** What is @Primary used for?
- **Q71:** What is @Order used for (ordered beans, @Configuration classes, filters, aspects)?
- **Q72:** Can Spring run code after the web application has finished starting up? (If yes, how?)
- **Q73:** What is a circular dependency between beans, and how can you resolve or avoid it?

### Spring Web (Q74-Q79)
- **Q74:** Which HTTP methods do you know?
- **Q75:** How do you understand cookies, HTTP headers, and HTTP session?
- **Q76:** What is CORS (Cross-Origin Resource Sharing)?
- **Q77:** What is idempotency (in the context of HTTP APIs)?

### Spring Data JPA / Hibernate (Q78-Q81)
- **Q78:** Hibernate fetch types (EAGER, LAZY)
- **Q79:** @OneToOne, @OneToMany, @ManyToMany
- **Q80:** N+1 problem

### Databases (Q81-Q87)
- **Q81:** What is optimistic locking? How is it implemented in databases?
- **Q82:** What is pessimistic locking? How is it implemented?
- **Q83:** Which of them would you prefer and in what cases?
- **Q84:** What is a DB index.
- **Q85:** What is the purpose of the index? Pros and cons? If no cons - why not create index for all possible combinations?
- **Q86:** Explain briefly how btree index works

### Design Patterns (Q87-Q89)
- **Q87:** Name the three main groups of design patterns.
- **Q88:** Name one pattern from each group.
- **Q89:** Which three patterns do you most often see in Spring?

---

## Project Grouping Strategy (**8 cohesive projects**)

| Module                                                                                 | Covers                                                        | Stack                                         |
|----------------------------------------------------------------------------------------|---------------------------------------------------------------|-----------------------------------------------|
| [demo-01-jvm-memory](../../demo-01-jvm-memory)                                         | GC logging, heap regions, object lifecycle                    | Spring Boot 3, Java 21, G1GC flags, port 8081 |
| [demo-02-data-type-strings-oop-generics](../../demo-02-data-type-strings-oop-generics) | Data types & Strings, OOP & Generics                          | Spring Boot 3, Java 21, port 8082             |
| [demo-03-collections](../../demo-03-collections)                                       | HashMap collision, TreeMap ordering, Iterator                 | Spring Boot 3, Java 21, port 8083             |
| [demo-04-streams](../../demo-04-streams)                                               | Stream pipelines, flatMap, collectors, lazy eval              | Spring Boot 3, Java 21, port 8084             |
| [demo-05-concurrency](../../demo-05-concurrency)                                       | Race condition demo, CAS vs synchronized, ReentrantLock       | Spring Boot 3, Java 21, port 8085             |
| [demo-06-spring-core](../../demo-06-spring-core)                                       | Bean scopes, lifecycle hooks, DI types, circular dep          | Spring Boot 3, port 8086                      |
| [demo-07-spring-data](../../demo-07-spring-data)                                       | EAGER/LAZY fetch, N+1 fix, optimistic/pessimistic lock, index | Spring Boot 3, PostgreSQL, port 8087          |
| [demo-08-patterns](../../demo-08-patterns)                                             | Singleton, Factory, Proxy, Strategy in Spring context         | Spring Boot 3, port 8088                      |


---

