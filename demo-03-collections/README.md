# demo-03 — Collections

- > **Theory files:** [04-collections-list.md](04-collections-list.md) · [05-collections-hashmap.md](05-collections-hashmap.md)
- > **Return to root README:** [java-interview-prep README](../README.md)
- > **[GitHub Pages site](https://vbforge.github.io/java-interview-prep)**
- > **Questions covered:** Q28–Q44
- > **Port:** 8083

Standalone Spring Boot module. No database, no Docker required.

---

## How to run

```bash
cd demos/demo-03-collections
mvn spring-boot:run
```

Then open: `http://localhost:8083/demo`


---

## Endpoints

| Endpoint | Q | What it shows |
|----------|---|---------------|
| `GET /demo/list/arraylist` | Q29, Q30 | ArrayList operations with complexity explanation |
| `GET /demo/list/linkedlist` | Q29, Q30 | LinkedList operations with complexity explanation |
| `GET /demo/list/comparison?n=50000` | Q31 | Live benchmark: append vs head insert, random access |
| `GET /demo/list/vs-array` | Q32 | List vs plain array — sizing, generics, boxing |
| `GET /demo/list/iterator` | Q33 | Iterator, `ConcurrentModificationException`, `ListIterator` |
| `GET /demo/hashmap/basics` | Q34, Q35 | `put`/`get` flow, null key/value, thread safety note |
| `GET /demo/hashmap/hashcode` | Q36, Q37 | `hashCode`, XOR spread, bucket index formula |
| `GET /demo/hashmap/equals-contract` | Q38, Q39 | Broken vs correct `hashCode`+`equals` — the #1 HashMap bug |
| `GET /demo/hashmap/collision` | Q40 | `CollidingKey` demo, list → tree at 8 entries |
| `GET /demo/hashmap/capacity` | Q41 | Default capacity, load factor 0.75, resize, pre-sizing |
| `GET /demo/hashmap/complexity` | Q42 | O(1) average vs O(log n) worst case — when it degrades |
| `GET /demo/hashmap/iteration-order` | Q43 | HashMap vs LinkedHashMap vs TreeMap order comparison |
| `GET /demo/hashmap/vs-treemap` | Q44 | NavigableMap ops, subMap, floorKey, custom Comparator |

---

## Key things to observe in the logs

**`/demo/list/comparison`** — run with `?n=100000` and watch the log lines:
```
ArrayList  append 100k: ~Xms
LinkedList append 100k: ~Xms
ArrayList  get 10k:     ~Xµs   ← very fast
LinkedList get 1k:      ~Xµs   ← much slower per element
```

**`/demo/hashmap/equals-contract`** — the most important output in this demo:
```
BrokenKey: get(k2) = null   ← equals is true, but different hashCodes → wrong bucket
GoodKey:   get(k2) = "first" ← correct
```

**`/demo/hashmap/hashcode`** — shows the actual bucket index each key lands in:
```
'alice'  rawHash=93029210  spread=91605959  bucket[7]
'bob'    rawHash=97299  spread=97298  bucket[2]
```

---

## Key concepts cheat sheet

```
ArrayList internal:  Object[] array, default capacity=10, grows 1.5×
LinkedList internal: doubly-linked Node<E>{prev, item, next}

HashMap internal:    Node<K,V>[] table, capacity=16, loadFactor=0.75
  bucket index     = (capacity - 1) & (hashCode ^ hashCode >>> 16)
  on 8 collisions  → linked list converts to TreeNode (red-black tree)
  on resize        → capacity doubles, all entries rehashed

hashCode contract:
  a.equals(b) → MUST have a.hashCode() == b.hashCode()
  If you break this → HashMap silently returns null on get()

TreeMap:  red-black tree, O(log n), always sorted, NavigableMap
  use when: sorted keys, range queries (subMap/headMap/tailMap),
            nearest-key lookups (floorKey/ceilingKey)
```

---

## Project structure

```
demo-03-collections/
├── pom.xml
├── README.md
└── src/main/java/com/vbforge/collections/
    ├── MainApp.java
    ├── config/
    │   └── DemoController.java      ← all endpoints
    ├── list/
    │   └── ListDemo.java            ← Q28–Q33
    ├── hashmap/
    │   └── HashMapDemo.java         ← Q34–Q43
    └── treemap/
        └── TreeMapDemo.java         ← Q44
```

---


