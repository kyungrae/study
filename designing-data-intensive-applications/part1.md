# Part 1. Foundations of Data Systems

## 1. Reliabile, Scalable, and Maintainable Application

### Reliablity

The system should continue to work correctly (perform the correct function at the desired level of performance) even in the face of adversity(hard or soft-ware faults, and event human error)

- The application can perform the function that the user expected.
- It can tolerate the user making mistakes or using the software in unexpected ways.
- Its performance is good enough for the required use case, under the expected load and data volume.
- The system prevent unauthorized access adn abuse.

### Scalability

As the system grows(in data volume, traffic volume, or complexity), there should be reasonable ways of dealing with that growth.

#### Describing Load

The best choice of paramters depends on the architecture of your system.

- Requests per second
- Ratio of reads to writes
- The number of simultaneously active users in chat room
- The hit rate on a cache
- The distribution of followers per user

#### Describing Performance

Once you have described the load on your system, you can investigate what happens when the load increase.

- When you increase a load parameter and keep the system resource unchanged, how is the performance of your system affected?
- When you increase a load parameter, how much do you need to increase resource if you want to keep performance unchanged?

Both questions require performance numbers, performance numbers can vary and may be values such as response time, throughput, and more.

We need to think of response time not as a single value, but as a distribution of values that you can measure.
The  mean is not a very good metric if you want to know your "typical" response time, because it doesn't tell you how many users actually experienced that delay.
Usually it is better to use ***percentiles***.
High percentiles of response time, also known as tail latencies, are important because they directly affect user's experience of the service.

When generating load artificially in order to test the scalability of system, the load generating client needs to keep sending requests independently of the response time(queueing delay).

### Maintainability

Over time, many different people will work on the system(engineering and operations, both maintaining current behavior and adapting the system to new use case),
and they should all be able to work on in productively

## 2. Data Models and Query Languages

### Relational Models Versus Document Models

#### Relational Models

- Advantages
  - Efficiently represents relationships like many-to-one and many-to-many using joins and foreign keys.
  - Facilitates easy normalization, helping to reduce data redundancy.
  - Optimizers handle efficient execution plans, reducing the need for manual optimization by developers.
- Disadvantages
  - The mismatch between object-oriented programming and the relational model (impedance mismatch) requires additional effort to manage.
  - Joins involving foreign keys may result in frequent disk random access, leading to poor performance when handling many joins (locality issue).

#### Document Models

- Advantages
  - Can easily represent unstructured data formats like JSON.
  - Good read performance, as a single query can fetch most of the required data due to data locality.
  - Flexible schema, making it easier to manage items with varying structures within a collection.
- Disadvantages
  - Weaker support for joins, making it difficult to represent relationships like many-to-one and many-to-many.
  - Denormalization can lead to issues of data duplication and inconsistency.

### Query Languages for Data

A declarative query language is attractive because it is typically more concise and easier to work with than an imperative API.
But more importantly, it is also hides implementation details of the database engine.
Declarative language often lend themselves to parallel execution.

#### MapReduce

MapReduce is a fairly low-level programming model for distributed execution on a cluster of machines.

### Graph-Like Data Model

As the connection within your data become more complex, it becomes more natural to start modeling your data as a graph.

#### Property Graphs

1. Any vertex can have an edge connecting it with any other vertex. There is no schema that restricts which kinds of things can or cannot be associated.
2. Given any vertex, you can efficiently find both its incoming and its outgoing edges, and thus traverse the graph
3. By using different labels for different kinds of relationships, you can store several different kinds of information in a single graph, while still maintaining a clean data model.

#### The Cypher Query Language

Cypher is a declarative query language for property graph.

```CYPHER
CREATE
  (NAmerica:Location {name:'North America', type:'continent'}),
  (USA:Location {name:'United States', type:'country' }),
  (Idaho:Location {name:'Idaho', type:'state' }),
  (Lucy:Person {name:'Lucy' }),
  (Idaho) -[:WITHIN]-> (USA) -[:WITHIN]-> (NAmerica),
  (Lucy) -[:BORN_IN]-> (Idaho)
```

```CYPHER
MATCH
  (person) -[:BORN_IN]-> () -[:WITHIN*0..]-> (us:Location {name:'United States'}),
  (person) -[:LIVES_IN]-> () -[:WITHIN*0..]-> (us:Location {name:'Europe'})
RETURN person.name
```

#### Graph Query in SQL

This idea of variable-length traversal paths in a query can be expressed using something called recursive common table expressions (the WITH RECURSIVE syntax).

#### Triple-Stores and SPARQL

ALL information is stored in the form of very simple three-part statements: (subject, predicate, object)

## 3. Storage and Retrieval

### Data Structures That Power your Database

#### SStable and LSM tree

Their key idea is that they systematically turn random-access writes into sequential writes on disk, which enables higher write throughput due to the performance characteristics of hard drives and SSDs.

- SSTable require that the sequence of key-value pairs is sorted by key and that each key only appears once within each merged segment file(the compaction process already ensure that).
- In-memory index can be sparse because of sorting.
- It use red-black tree or AVL tree(memtable) to maintain a sorted structure on disk.
- The LSM-tree algrorithm can be slow when looking up keys that do not exist in the database. A Bloom filter is a memory-efficient data structure for approximating the contents of a set. It can tell you if a key does not appear in the database, and thus saves many unnecessary disk reads for nonexistent keys.

#### B-Tree

B-trees break the database down into fixed-size blocks or sequentially.
One page is designed as the root of the B-tree.
Each child is responsible for a continuous range of keys, and the keys between the refernces indicate where the boundaries between thoes ranges lie.
Eventaully we get down to a page containing individual keys(a leaf page), which either contains the value for each key inline or contains references to the pages where the value can be found.

### Transaction Processing or Analytics?

| Property | Transaction processing systems (OLTP) | Analytics systems (OLAP) |
|---|---|---|
| Main read pattern | Small number of records per query, feched by key | Aggregate over large number of records |
| Main write pattern | Random-access,low-latency writes from user input | Bulk import(ETL) or event stream |
| Primarily used by | End user/customer, via web application | Internal analyst, for decision support |
| What data represents | Latest state of data(current point in time) | History of events that happened over time |
| Dataset size | Gigabytes to terabytes | Terabytes to petabytes |

This separate database was called a data warehouse.

### Column-Oriented Storage

The idea behind column-oriented storage is simple: don't store all the values from one row together, but store all the values from each column together instead.
If each column is stored in a separate file, a query only needs to read and parse those columns that are used in that query, which can save a lot of work.

#### Column-oriented storage and column families

Cassandra and HBase have a concept of column families, which they inherited from Bigtable.
However, it is very misleading to call them column-oriented: within each column family, they store all columns from a row together, along with a row ky, and they do not use column compression.
Thus, the Bigtable model is still mostly row-oriented.

#### Aggregation: Data Cubes and Materalized Views

One way of creating such a cache is a materalized view.
In a relational data model, it is often defined like a standard (virtual) view: a table-like object whose content are the results of some query.
The difference is that materalized view is an actual copy of the query results, written to disk, whereas a virtual view is just a shortcut for writing queries.

## 4. Encoding and Evolution

- Backward compatibility  
  Newer code can read data that was written by older code.  
- Forward compatibility  
  Older code can read data that was written by newer code.

### Formats for Encoding Data

Program usually work with data in (at least) two different representations:

1. In memory, data is kept in objects, structs, list, array, hash table, trees, and so on.
These data structures are optimized for efficient access and manipulation by the CPU (typically using pointers).
2. When you want to write data to file or sent it over the network, you have to encode it as some kind of self-contained sequence of bytes(for example, a JSON document).
Since a pointer wouldn't make sense to any other process, this sequence-of-bytes representation looks quite different from the data structures that are normally used in memory.

The translation from the in-memory representation to a byte sequence is called encoding, and the reverse is called decoding.

#### Language-Specific Formats

Programming language-specific encodings(Serializable, Marshar, pickle) are restricted to a single programming language and often fail to provide forward and backward compatibility.

#### JSON, XML, and Binary Variants

Textual formats like JSON, XML, and CSV are widespread, and their compatibility depends on how you use them.
These formats are somewhat vague about datatypes, so you have to be careful with things like number and binary strings.

#### Thrift, Protocol Buffers and Avro

Binary schema-driven format like Thrift, Protocol Buffers, and Avro allow compact, efficient encoding with clearly defined forward and backwrad compatibility semantics.
The schemas can be useful for documentation and code generation in satically typed languages.
However, they have downside that needs to be decoded before its humna-readable.

### Modes of Dataflow

- Dataflow Through Datbases
- Dataflow Through Services: REST and RPC
- Message-Passing Dataflow
