# Part Ⅰ. Foundations of Data Systems

## 1. Reliable, Scalable, and Maintainable Application

A data-intensive application is typically built from standard building blocks that provide commonly needed functionality.

- Store data so that they, or another application, can find it again later (database)
- Remember the result of an expensive operation, to speed up reads (cache)
- Allow users to search data by keyword or filter it in various ways (search index)
- Send a message to another process, to be handled asynchronously (stream processing)
- Periodically crunch a large amount of accumulated data (batch processing)

Most engineers wouldn't dream of writing a new data storage engine from scratch.
There are many database systems with different characteristics, because different applications have different requirements.
We will start by exploring the fundamentals of what we are trying to achieve: reliable, scalable, and maintainable data systems.

### Thinking About Data Systems

When you combine several tools in order to provide a service, the service's interface or application programming interface (API) usually hides those implementation details from clients.
Now you have essentially created a new, special-purpose data system from smaller, general-purpose components.
Your composite data system may provide certain guarantees: e.g., that the cache will be correctly invalidated or updated on writes so that outside clients see consistent results.
You are now not only an application developer, but also a data system designer.

```mermaid
flowchart
  user@{shape: circle, label: "User"} -- Client requests --> code1[Application code] -- First check if data is cached --> cache@{shape: cyl, label: "In-meory cache"}
  code1 -- Cache misses and writes --> database@{shape: cyl, label: "Primary database"}
  database -- Captures changes to data --> code2[Application code] -- Invalidate or update cache --> cache
  code1 -- Serach requests--> search@{shape: cyl, label: "Full-text index"}
  code2 -- Apply updates to searindex --> search
  code1 -- Asynchronous task --> queue@{ shape: das, label: "Message queue" } --> code3[Application code] --send email--> outside@{ shape: dbl-circ, label: "Outside world" }
```

### Reliability

The system should continue to work correctly (perform the correct function at the desired level of performance) even in the face of adversity(hard or soft-ware faults, and event human error).

- The application can perform the function that the user expected.
- It can tolerate the user making mistakes or using the software in unexpected ways.
- Its performance is good enough for the required use case, under the expected load and data volume.
- The system prevent unauthorized access and abuse.

The things that can go wrong are called _faults_, and system that anticipate faults and can cope with them are called _fault-tolerant_ or _resilient_. The former term is slightly misleading: it suggests that we would make a system tolerant of every possible kind of fault, which in reality is not feasible.
So it only makes sense to talk about tolerating certain types of faults.

A fault is usually defined as one component of the system deviating from its spec, whereas a failure is when the system as a whole stops providing the required service to the user.
It is impossible to reduce the probability of a fault to zero; therefore it is usually best to design fault-tolerance mechanisms that prevent faults from causing failures.

#### Hardware Faults

Hard disks crash, RAM becomes faulty, the power grid has a blackout, someone unplugs the wrong network cable.
Our first response is usually to add redundancy to the individual hardware components in order to reduce the failure rate of the system.

There is a move toward systems that can tolerate the loss of entire machines, by using software fault-tolerance techniques in preference or in addition to hardware redundancy.
Such systems also have operational advantages: a single-server system requires planned downtime if you need to reboot the machine, whereas a system that can tolerate machine failure can be patched one node at a time, without downtime of the entire system.

#### Software Errors

A runaway process that uses up some shared resource.
A service that the system depends on that slows down, becomes unresponsive, or starts returning corrupted responses.

#### Human Errors

Even when operators have the best intentions, humans are known to be unreliable.

Set up detailed and clear monitoring, such as performance metrics and error rates.
In other engineering this is referred to as telemetry.

### Scalability

Scalability is the term we use to describe a system's ability to cope with increased load.
Note that it is not a one-dimensional label that we can attach to a system.
Discussing scalabiltiy means considering questions like "If the system grows in a particular way, what are our options for coping with the growth?" and "How can we add computing resources to handle the additional load?"

#### Describing Load

Load can be described with a few numbers which we call load parameters.
The best choice of parameters depends on the architecture of your system: it may be requests per second to a web server, the ratio of reads to writes in a database, the number of simultaneously active users in chat room, the hit rate on a cache, the distribution of followers per user, or something else.

#### Describing Performance

Once you have described the load on your system, you can investigate what happens when the load increases.

- When you increase a load parameter and keep the system resource unchanged, how is the performance of your system affected?
- When you increase a load parameter, how much do you need to increase resources if you want to keep performance unchanged?

Both questions require performance numbers, performance numbers can vary and may be values such as response time, throughput, and more.

We need to think of response time not as a single value, but as a distribution of values.
High percentiles of response time(tail latencies) are important because they directly affect the user's experience of the service.

### Maintainability

Over time, many different people will work on the system, and they should all be able to work on it productively.

- Operability  
  Make it easy for operations teams to keep the system running smoothly.
- Simplicity  
  Make it easy for new engineers to understand the system, by removing as much as possible from the system.
- Evolvability  
  Make it easy for engineers to make changes to the system in the future, adapting it for unanticipated use cases as requirements change.

## 2. Data Models and Query Languages

Data models are perhaps the most important part of developing software, because they have such a profound effect: not only on how the software is written, but also on how we think about the problem that we are solving.

### Relational Models Versus Document Models

#### Relational Models

- Advantages
  - Efficiently represents relationships like many-to-one and many-to-many using joins and foreign keys.
  - Facilitates easy normalization, helping to reduce data redundancy.
- Disadvantages
  - The mismatch between object-oriented programming and the relational model (impedance mismatch) requires additional effort to manage.
  - Joins involving foreign keys may result in frequent disk random access, leading to poor performance when handling many joins.

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
But more importantly, it also hides implementation details of the database engine.
Declarative languages often lend themselves to parallel execution.

#### MapReduce

MapReduce is a fairly low-level programming model for distributed execution on a cluster of machines.

### Graph-Like Data Model

As the connection within your data becomes more complex, it becomes more natural to start modeling your data as a graph.
Graph model are not limited to such homogeneous data: an equally powerful use of graphs is to provide a consistent way of storing completely different types of objects in a single datastore.

#### Property Graphs

1. Any vertex can have an edge connecting it with any other vertex. There is no schema that restricts which kinds of things can or cannot be associated.
2. Given any vertex, you can efficiently find both its incoming and its outgoing edges, and thus traverse the graph.
3. By using different labels for different kinds of relationships, you can store several different kinds of information in a single graph, while still maintaining a clean data model.

#### The Cypher Query Language

Cypher is a declarative query language for property graphs.

```CYPHER
CREATE
  (NAmerica:Location {name:'North America', type:'continent'}),
  (USA:Location {name:'United States', type:'country' }),
  (Idaho:Location {name:'Idaho', type:'state' }),
  (Lucy:Person {name:'Lucy'}),
  (Idaho) -[:WITHIN]-> (USA) -[:WITHIN]-> (NAmerica),
  (Lucy) -[:BORN_IN]-> (Idaho)
```

```CYPHER
MATCH
  (person) -[:BORN_IN]-> () -[:WITHIN*0..]-> (us:Location {name:'United States'}),
  (person) -[:LIVES_IN]-> () -[:WITHIN*0..]-> (us:Location {name:'Europe'})
RETURN person.name
```

#### Graph Queries in SQL

```SQL
WITH RECURSIVE
  in_usa(vertex_id) AS (
      SELECT vertex_id FROM vertices WHERE properties->>'name' = 'United States'
    UNION
      SELECT edges.tail_vertex FROM edges
        JOIN in_usa ON edges.head_vertex = in_usa.vertex_id
        WHERE edges.label = 'within'
  ),

  in_europe(vertex_id) AS (
      SELECT vertex_id FROM vertices WHERE properties->>'name' = 'Europe'
    UNION
      SELECT edges.tail_vertex FROM edges
        JOIN in_usa ON edges.head_vertex = in_europe.vertex_id
        WHERE edges.label = 'within'
  ),
  born_in_usa(vertex_id) AS (
      SELECT edges.tail_vertex FROM edges
        JOIN in_usa ON edges.head_vertex = in_usa.vertex_id
        WHERE edges.label = 'born_in'
  ),
  born_in_europe(vertex_id) AS (
      SELECT edges.tail_vertex FROM edges
        JOIN in_europe ON edges.head_vertex = in_europe.vertex_id
        WHERE edges.label = 'lives_in'
  )

SELECT vertices.properties->>'name'
FROM vertices
JOIN born_in_usa ON vertices.vertex_id = born_in_usa.vertex_id
JOIN born_in_usa ON vertices.vertex_id = lives_in_europe.vertex_id
```

## 3. Storage and Retrieval

You do need to select a storage engine that is appropriate for your application, from the many that are available.
In order to tune a storage engine to perform well on your kind of workload, you need to have a rought idea of what the storage engine is doing under the hood.

### Data Structures That Power your Database

The general idea behind indexes is to keep some additional metadata on the side, which acts as a signpost and helps you to locate the data you want.

#### Hash Indexes

Keep an in-memory hash map where every key is mapped to a byte offset in the data file—
the the location at which the value can be found.

![hash index](./images/hash_index.png)

Break the log into segments of a certain size by closing a segment file when it reaches a certain size, and making subsequent writes to a new segment file.
We can then perform compaction on these segments.
**Compaction** means throwing away duplicate keys in the log, and keeping only the most recent update for each key.

![compaction](./images/compacation.png)

#### SStable and LSM tree

Require that the sequence of key-value pairs is sorted by key.
We call this format Sorted String Table (SSTable).
We also require that each key only appears once within each merged segment file (the compaction process ensure that).

- In-memory indexes can be sparse because of sorting.
- It uses a red-black tree or AVL tree(memtable) to maintain a sorted structure on disk.

##### Performance optimizations

- The LSM-tree algorithm can be slow when looking up keys that do not exist in the database. A Bloom filter is a memory-efficient data structure for approximating the contents of a set.
- There are strategies to determin the order and timing of how SSTables are compacted and merged.
  - In size-tiered compaction, newer and smaller SSTables are successively merged into older and larger SSTables.
  - In leveled compaction, the key range is split up into smaller SSTables and older data is moved into separatee "levels," which allows the compaction to proceed more incrementally and use less disk space.

#### B-Tree

The log-structured indexs we saw earlier break the database down into variable-size segments, typically serveral megabytes or more in size, and always write a segment seuentially.
B-trees break the database down into fixed-size blocks or pages and read or write one page at a time.

One page is designed as the root of the B-tree.
Each child is responsible for a continuous range of keys, and the keys between the references indicate where the boundaries between those ranges lie.

### Transaction Processing or Analytics?

Transaction processing just means allowing clients to make low-latency reads and writes—as opposed to batch processing jobs, which only run periodically.

| Property             | Transaction processing systems (OLTP)             | Analytics systems (OLAP)                  |
| -------------------- | ------------------------------------------------- | ----------------------------------------- |
| Main read pattern    | Small number of records per query, fetched by key | Aggregate over large number of records    |
| Main write pattern   | Random-access,low-latency writes from user input  | Bulk import(ETL) or event stream          |
| Primarily used by    | End user/customer, via web application            | Internal analyst, for decision support    |
| What data represents | Latest state of data(current point in time)       | History of events that happened over time |
| Dataset size         | Gigabytes to terabytes                            | Terabytes to petabytes                    |

This separate database was called a data warehouse.

#### Data warehousing

These OLTP systems are usually expected to be highly available and to process transactions with low latency, since they are often critical to the operation of the business.
Database administrators are usually reluctant to let business analysts run ad hoc analytic queries on an OLTP database, since those queries are often expensive, scanning large parts of the dataset, which can harm the performance of concurrently executing transactions.

```mermaid
flowchart
  u1@{ shape: sm-circ, label: "Small start" } --> Ecommerce
  u2@{ shape: sm-circ, label: "Small start" } --> Stock
  u3@{ shape: sm-circ, label: "Small start" } --> Vehicle
  subgraph Ecommerce[Ecommerce site]
    SalesDB[(Sales DB)]
  end
  subgraph Stock[Stock-keeping app]
    InventoryDB[(Inventory DB)]
  end
  subgraph Vehicle[Vehicle route planner]
    GeoDB[(Geo DB)]
  end

  subgraph Datawarehouse[ ]
    transform1[transform] --load--> DataWarehouse[(Data warehouse)]
    transform2[transform] --load--> DataWarehouse
    transform3[transform] --load--> DataWarehouse
  end
  SalesDB --extract--> transform1
  InventoryDB --extract--> transform2
  GeoDB --extract--> transform3
  u4@{ shape: sm-circ, label: "Small start" } --query--> DataWarehouse
```

A big advantage of using a separate data warehouse, rather than querying OLTP systems directly for analytics, is that the data warehouse can be optimized for analytic access patterns.

#### Stars and Snowflakes: Schemas for Analytics

Facts are captured as individual events, because this allows maximum flexibility of analysis later.
However, this means that the fact table can become extremely large.

Some of the columns in the fact table are attributes.
Other columns in the fact table are foreign key references to other tables, called dimension tables.
As each row in the fact table represents an event, the dimensions represent the who, what, where, when, how, and why of the event.

The name "star schema" comes from the fact that when the table relationships are visualized, the fact table is in the middle, surrounded by its dimension tables; the connections to these tables are like the rays of a star.

### Column-Oriented Storage

If you have trillions of rows and petabytes of data in your fact tables, storing and querying them efficiently becomes a challenging problem.
Although fact tables are often over 100 columns wide, a typical data warehouse query only accesses 4 or 5 of them at one time.

The idea behind column-oriented storage is simple: don't store all the values from one row together, but store all the values from each column together instead.
If each column is stored in a separate file, a query only needs to read and parse those columns that are used in that query, which can save a lot of work.

#### Column-oriented storage and column families

Cassandra and HBase have a concept of column families, which they inherited from Bigtable.
However, it is very misleading to call them column-oriented: within each column family, they store all columns from a row together, along with a row key, and they do not use column compression.
Thus, the Bigtable model is still mostly row-oriented.

#### Aggregation: Data Cubes and Materialized Views

One way of creating such a cache is a materialized view.
In a relational data model, it is often defined like a standard (virtual) view: a table-like object whose contents are the results of some query.
The difference is that materialized view is an actual copy of the query results, written to disk, whereas a virtual view is just a shortcut for writing queries.

## 4. Encoding and Evolution

Application inevitably change over time.
We should aim to build systems that make it easy to adapt to change(Evolvability).

When a data format or schema changes, a corresponding change to application code often needs to happen.
However, code changes often cannot happen instanteously.
In order for the system to continue running smoothly, we need to maintain compatibility in both directions.

- Backward compatibility  
  Newer code can read data that was written by older code.
- Forward compatibility  
  Older code can read data that was written by newer code.

### Formats for Encoding Data

Program usually work with data in (at least) two different representations:

1. In memory, data is kept in objects, structs, lists, arrays, hash tables, trees, and so on.
   These data structures are optimized for efficient access and manipulation by the CPU (typically using pointers).
2. When you want to write data to a file or send it over the network, you have to encode it as some kind of self-contained sequence of bytes(for example, a JSON document).
   Since a pointer wouldn't make sense to any other process.

The translation from the in-memory representation to a byte sequence is called encoding, and the reverse is called decoding.

#### Language-Specific Formats

Programming language-specific encodings(Serializable, Marshal, pickle) are restricted to a single programming language and often fail to provide forward and backward compatibility.
Efficiency is also often an afterthought.

#### JSON, XML, and Binary Variants

Textual formats like JSON, XML, and CSV are widespread, and their compatibility depends on how you use them.
These formats are somewhat vague about datatypes, so you have to be careful with things like number and binary strings.

##### Binary encoding

Since they don't prescribe a schema, they need to include all the object field names within the encoded data.
Let's look at an example of MessagePack, a binary encoding for json.

```json
{
  "userName": "Martin",
  "favoriteNumber": 1337,
  "interests": ["daydream", "hacking"]
}
```

![message pack](./images/messagepack.png)

#### Thrift, Protocol Buffers

Both Thrift and Protocol Buffers require a schema for any data that is encoded.
To encode the data in json, you would describe the schema.

```thrift
struct Person {
  1: required string username,
  2: optional i64 favoriteNumber,
  3: optional list<string> interests
}
```

```proto
message Person {
  required string user_name = 1;
  optional int64 favorite_number = 2;
  repeated string interests = 3;
}
```

Thrift and Protocol Buffers each come with a code generation tool that takes a schema definition like the ones shown here, and produces classes that implement the schema in various programming languages.

The big difference compared to MessagePack is that there are no field name.
Instead, the encoded data contains field tags, which are number.

![Thrift](./images/thrift.png)
![Protocol buffer](./images/protocol_buffer.png)

#### Avro

```avro
record Person {
  string userName
  union { null, long } favoriteNumber = null;
  array<string> interests;
}
```

```json
{
  "type": "record",
  "name": "Person",
  "fields": [
    {
      "name": "userName",
      "type": "string"
    },
    {
      "name": "favoriteNumber",
      "type": ["null", "long"],
      "default": null
    },
    {
      "name": "interests",
      "type": {
        "type": "array",
        "items": "string"
      }
    }
  ]
}
```

![avro](./images/avro.png)

To parse the binary data, you go through the fields in the order that they appear in the schema and use the schema to tell you the datatype of each field.
This means that the binary data can only be decoded correctly if the code reading the data is using the exact same schema as the code that wrote the data.

Binary schema-driven formats like Thrift, Protocol Buffers, and Avro allow compact, efficient encoding with clearly defined forward and backward compatibility semantics.
The schemas can be useful for documentation and code generation in statically typed languages.
However, they have the downside that needs to be decoded before it is human-readable.

#### What is the writer's schema

- Large file with lots of records
- Database with individually written records
- Sending records over a network connection

### Modes of Dataflow

- Dataflow Through Databases  
  Forward compatibility is often required.
- Dataflow Through Services: REST and RPC  
  Backward compatibility is required on request and forward compatibility is required on response.
- Message-Passing Dataflow  
  Forward compatibility is required on the producer.
