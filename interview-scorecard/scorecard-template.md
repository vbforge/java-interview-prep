# Java Interview Scorecard

**Candidate:** _________________  
**Interviewer:** _________________  
**Date:** _________________  
**Overall Rating:** _____ / 5

---

## Rating Scale

| Score | Meaning |
|-------|---------|
| 5 | Absolute star — exceeds expectations, deep understanding |
| 4 | Excellent overall — minor gaps only |
| 3 | Meets requirement — solid, hire for junior/mid level |
| 2 | Doesn't meet requirement, but has bright spots |
| 1 | Not good enough — significant gaps |

---

## Section 1: JVM Memory Model (Q1–Q8)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q1 | JVM architecture — memory areas | ___ | |
| Q2 | Heap vs Stack — differences | ___ | |
| Q3 | GC roots and reachability | ___ | |
| Q4 | Generational GC — Young/Old gen | ___ | |
| Q5 | G1GC — regions, humongous objects | ___ | |
| Q6 | Heap dump analysis scenarios | ___ | |
| Q7 | OutOfMemoryError types | ___ | |
| Q8 | Object lifecycle in memory | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 2: Data Types & Strings (Q9–Q17)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q9 | Primitives vs wrappers — performance | ___ | |
| Q10 | Autoboxing — when it happens | ___ | |
| Q11 | String immutability benefits | ___ | |
| Q12 | String pool — intern() behavior | ___ | |
| Q13 | StringBuilder vs StringBuffer | ___ | |
| Q14 | BigDecimal use cases | ___ | |
| Q15 | equals() and hashCode() contract | ___ | |
| Q16 | instanceof vs getClass() | ___ | |
| Q17 | Switch expressions (Java 14+) | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 3: OOP & Generics (Q18–Q27)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q18 | Inheritance vs composition | ___ | |
| Q19 | Abstract class vs interface (Java 8/17+) | ___ | |
| Q20 | Overriding rules — covariant returns | ___ | |
| Q21 | Polymorphism — virtual method invocation | ___ | |
| Q22 | Default methods — diamond problem | ___ | |
| Q23 | Generics — type erasure | ___ | |
| Q24 | Wildcards — extends vs super (PECS) | ___ | |
| Q25 | Type bounds — multiple bounds | ___ | |
| Q26 | Generic methods vs generic classes | ___ | |
| Q27 | Reflection with generics — limitations | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 4: Collections — List (Q28–Q33)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q28 | ArrayList internal — growth factor | ___ | |
| Q29 | LinkedList vs ArrayList — trade-offs | ___ | |
| Q30 | Vector — legacy status | ___ | |
| Q31 | Iterator fail-fast behavior | ___ | |
| Q32 | ConcurrentModificationException | ___ | |
| Q33 | List performance — add/get/remove | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 5: Collections — HashMap (Q34–Q44)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q34 | HashMap put() internals | ___ | |
| Q35 | hashCode() role — bucket distribution | ___ | |
| Q36 | Collision resolution — linked list → tree | ___ | |
| Q37 | Load factor and resizing | ___ | |
| Q38 | Treeify threshold (TREEIFY_THRESHOLD) | ___ | |
| Q39 | ConcurrentHashMap — segment locks | ___ | |
| Q40 | LinkedHashMap — insertion/access order | ___ | |
| Q41 | TreeMap — Red-Black tree | ___ | |
| Q42 | EnumMap performance | ___ | |
| Q43 | IdentityHashMap use case | ___ | |
| Q44 | WeakHashMap — reference queues | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 6: Streams & Lambdas (Q45–Q52)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q45 | Stream pipeline — source → ops → terminal | ___ | |
| Q46 | Intermediate vs terminal operations | ___ | |
| Q47 | Lazy evaluation benefit | ___ | |
| Q48 | flatMap use case | ___ | |
| Q49 | Collectors — groupingBy, partitioningBy | ___ | |
| Q50 | Optional — purpose and pitfalls | ___ | |
| Q51 | Method references (4 types) | ___ | |
| Q52 | Functional interfaces — @FunctionalInterface | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 7: Concurrency (Q53–Q63)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q53 | Thread lifecycle — states | ___ | |
| Q54 | synchronized — method vs block | ___ | |
| Q55 | volatile — visibility guarantee | ___ | |
| Q56 | CAS — compare-and-swap | ___ | |
| Q57 | Atomic classes (AtomicInteger) | ___ | |
| Q58 | ReentrantLock vs synchronized | ___ | |
| Q59 | Condition — await/signal | ___ | |
| Q60 | Thread pools — Executors framework | ___ | |
| Q61 | BlockingQueue implementations | ___ | |
| Q62 | CompletableFuture — async pipeline | ___ | |
| Q63 | Deadlock — detection and prevention | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 8: Spring Core & Web (Q64–Q77)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q64 | IoC — inversion of control | ___ | |
| Q65 | DI — constructor vs setter vs field | ___ | |
| Q66 | Bean scopes — singleton vs prototype | ___ | |
| Q67 | @Component vs @Bean | ___ | |
| Q68 | ApplicationContext — post-processors | ___ | |
| Q69 | @Autowired — wiring rules | ___ | |
| Q70 | Circular dependency — solutions | ___ | |
| Q71 | @Transactional — proxy mechanism | ___ | |
| Q72 | HTTP methods — GET, POST, PUT, DELETE | ___ | |
| Q73 | @RestController vs @Controller | ___ | |
| Q74 | Request parameters — @RequestParam vs @PathVariable | ___ | |
| Q75 | ResponseEntity — customizing response | ___ | |
| Q76 | Exception handling — @ControllerAdvice | ___ | |
| Q77 | CORS — @CrossOrigin, global config | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 9: JPA & Databases (Q78–Q86)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q78 | JPA vs Hibernate | ___ | |
| Q79 | Entity lifecycle — managed, detached | ___ | |
| Q80 | Relationships — @OneToMany, @ManyToOne | ___ | |
| Q81 | EAGER vs LAZY fetching | ___ | |
| Q82 | N+1 problem — cause and fix (@EntityGraph) | ___ | |
| Q83 | Optimistic locking — @Version | ___ | |
| Q84 | Pessimistic locking — LockModeType | ___ | |
| Q85 | Database indexes — structure, when to use | ___ | |
| Q86 | JPQL vs native queries | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Section 10: Design Patterns (Q87–Q89)

| Question | Topic | Rating (1–5) | Notes |
|----------|-------|--------------|-------|
| Q87 | GoF groups — creational, structural, behavioral | ___ | |
| Q88 | Spring patterns — Proxy, Singleton, Factory | ___ | |
| Q89 | Observer pattern — event/listener in Spring | ___ | |

**Section rating:** _____ / 5  
**Weak spots:**  

---

## Overall Summary

| Section | Rating |
|---------|--------|
| JVM Memory | _____ |
| Data Types & Strings | _____ |
| OOP & Generics | _____ |
| Collections — List | _____ |
| Collections — HashMap | _____ |
| Streams & Lambdas | _____ |
| Concurrency | _____ |
| Spring Core & Web | _____ |
| JPA & Databases | _____ |
| Design Patterns | _____ |

**Average:** _____ / 5  

---

## Interviewer Notes

**Strengths:**  




**Areas for improvement:**  




**Red flags (if any):**  




**Hire recommendation:** ☐ Yes ☐ No ☐ On hold  

---

## Candidate Self-Assessment (optional)

**What went well:**  




**What would you do differently:**  




**Topics to review before next round:**  




---

*Template based on recruitment agency screen — rating scale and question mapping aligned with theory files Q1–Q89*

---