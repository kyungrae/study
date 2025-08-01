# Ⅲ. Derived Data

There is no one database that can satisfy all those different needs simultaneously.
We will examine the issue around integrating multiple different data systems, potentially with different data models and optimized for different access patterns, into one coherent application architecture.

On a high level, systems that store and process data can be grouped into two broad categories:

- System of record  
A system of record, also known as source of truth, holds the authoritative version  of your data.
- Derived data systems  
Data in a derived system is the result of taking some existing data from another system and transforming or processing it in some way. A classic example is a cache: data can be served from the cache if present, but if the cache doesn't contain what you need, you can fall back to the underlying database.

## 10. Batch Processing

Let's distinguish three different types of systems:

- Services (online systems)  
A service waits for a request or instruction from a client to arrive. When one is received, the service tries to handle it as quickly as possible and sends a response back.
- Batch processing systems (offline systems)  
A batch processing system takes a large amount of input data, runs a job to process it, and produces some output data.
- Stream processing systems (near-real-time systems)  
Stream processing is somewhere between online and offline processing.
Like a batch processing system, a stream processor consumes input and produces outputs.

Batch processing is an important building block in our quest to build reliable, scalable, and maintainable applications.

### Batch Processing with Unix Tools

Say you have a web server that appends a line to a log file every time it serves a request.
For example, using the nginx default access log format, one line of the log might look like this:

```log
216.84.210.78 - - [27/Feb/2015:17:55:11 ++0000] "GET /css/typograph.css HTTP/1.1" 200 3377 "http://martin.kleppmann.com/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.02214.115 Safari/537.36
```

#### Simple Log Analysis

```sh
cat /var/log/nginx/access.log |
  awk '{print $7}' |
  sort             |
  uniq -c          |
  sort -r -n       |
  head -n 5
```

It will process gigabytes of log files in a matter of seconds, and you can easily modify the analysis to suit your needs.
If the job's working set is larger than the available memory, the sorting approach has the advantage that it can make efficient use of disks.
It's the same principle as we discussed in SSTables and LSM-Trees: chunks of data can be sorted in memory and written out to disk as segment files, and then multiple sorted segments can be merged into a larger sorted file.

#### The Unix Philosophy

##### A uniform interface

In Unix, the interface is a file. A file is just an ordered sequence of bytes.
Because that is such a simple interface, many different things can be represented using the same interface: an actual file on the filesystem, a communication channel to another process (Unix socket, stdin, stdout), a device driver (/dev/audio or /dev/lp0).

##### Separation of logic and wiring

Another characteristic feature of Unix tools is their use of standard input and standard output.
This allows a shell user to wire up the input and output in whatever way they want; the program doesn't know or care where the input is coming from and where the output is going to.

### MapReduce and Distributed Filesystems

While Unix tools use stdin and stdout as input and output, MapReduce jobs read and write files on a distributed filesystem.
In Hadoop's implementation of MapReduce, that filesystem is called HDFS (Hadoop Distributed File System), an open source reimplementation of the Google File System (GFS).

HDFS is based on the shared-nothing principle.
HDFS consists of a daemon process running on each machine, exposing a network service that allows other nodes to access files stored on that machine.
A central server called the NameNode keeps track of which file blocks are stored on which machine.
Thus, HDFS conceptually creates one big filesystem that can use the space on the disks of all machines running the daemon.

#### MapReduce Job Execution

**MapReduce** is a programming framework with which you can write code to process large dataset in a distributed filesystem like HDFS.

- Mapper  
The mapper is called once for every input record, and its job is to extract the key and value from the input record.
For each input, it generate any number of key-value pairs.
It does not keep any state from one input record to the next, so each record is handled independently.
- Reducer  
The mapReduce framework takes the key-value pairs produced by the mappers, collects all the values belonging to the same key, and calls the reducer with an iterator over that collection of values.
The reducer can produce output records.

##### Distributed execution of MapReduce

The main difference from pipelines of Unix commands is that MapReduce can parallelize a computation across many machines, without you having to write code to explicitly handle the parallelism.

Its parallelization is based on partitioning: the input to a job is typically a directory in HDFS, and each file or file block within the input directory is considered to be a separate partition that can be processed by a separate map task.

```mermaid
---
title: Leader-based replication
---
flowchart LR
  subgraph map[ ]
    subgraph map1[Map task 1]
      direction LR
      m1@{ shape: cyl, label: "m 1" } --> m1mapper[Mapper]
      m1mapper --> m1record1[m1, r1]
      m1mapper --> m1record2[m1, r2]
    end
    subgraph map2[Map task 2]
      direction LR
      m2@{ shape: cyl, label: "m 2" } --> m2mapper[Mapper]
      m2mapper --> m2record1[m2, r1]
      m2mapper --> m2record2[m2, r2]
    end
  end
  subgraph reduce[ ]
    subgraph reduce1[Reduce task 1]
      direction LR
      r1record1[m1, r1] --> r1merge[merge]
      r1record2[m2, r1] --> r1merge
      r1merge[merge] --> r1reducer[Reduce] --> r1@{ shape: cyl, label: "r 1" }
    end
    subgraph reduce2[Reduce task 2]
      direction LR
      r2record1[m1, r2] --> r2merge[merge]
      r2record2[m2, r2] --> r2merge
      r2merge[merge] --> r2reducer[Reduce] --> r2@{ shape: cyl, label: "r2" }
    end
  end
  m1record1 --> r1record1
  m1record2 --> r2record1
  m2record1 --> r1record2
  m2record2 --> r2record2
```

To ensure that all key-value pairs with the same key end up at the same reducer, the framework use a hash of the key to determine which reduce task should receive a particular key-value pair.
Whenever a mapper finishes reading its input file and writing its sorted output files, the MapReduce scheduler notifies the reducers that they can start fetching the output files from that mapper.
The Reducers connect to each of the mappers and download the files of sorted key-value pairs for their partition.
The process of partitioning by reduer, sorting, and copying data partitions from mappers to reducer is known as the shuffle.

##### MapReduce workflows

It is very common for MapReduce jobs to be chained together into workflows, such that the output of one job becomes the input to the next job.
The Hadoop MapReduce framework does not have any particular support for workflows, so this chaining is done implicitly by directory name: the first job must be configured to writes its output to a designated directory in HDFS, and the second job must be configured to read that same directory name as its input.
From the MapReduce framework's point of view, they are two independent jobs.

A batch job's output is only considered valid when the job has completed successfully.
Therefore, one job in a workflow can only start when the prior jobs have completed successfully.
To handle these dependencies between job executions, various workflow schedulers for Hadoop have been developed.

#### Reduce-Side Joins and Grouping

In many datasets it is common for one record to have an association with another record: a foreign key in a relational meodel, a document reference in a document model, or an edge in a graph model.
A join is necessary whenever you have some code that needs to access records on both sides of that association.

When we talk about joins in the context of batch processing, we mean resolving all ocurrences of some assocication within a dataset.

##### Example: analysis of user activity events

On the left is a log of events describing the things that logged-in users did on a website, and on the right is a database of users.
An analytics task may need to correlate user activity with user profile information: for example, if the profile contains the user's age or date of birth, the system could determine which pages are most popular with which age groups.

![A join between a log of user activity events and a database of user profiles](./images/user_activtiy.png)

The simplest implementation of this join would go over the activity events one by one and query the user database for every user ID it encounters.
But it would most likely suffer from poor performance: the processing throughput would be limited by the round-trip time to the database server, and running large number of queries in parallel could easily overwhelm the database.
Moreover, querying a remote database would mean that the batch job becomes nondeterministic, because the data in the remote database might change.

Thus, a better approach would be to take a copy of the user database and to put it in the same distributed filesystem as the log of user activity events.

##### Sort-merge joins

When the MapReduce framework partitions the mapper output by key and then sorts the key-value pairs, the effect is that all the activity events and the user record with the same user ID become adjacent to each other in the reducer input.
The MapReduce job can even arrange the records to be sorted such that the reducer always sees the record from the user database first, followed by the activity events in timestamp order—this technique is known as a secondary sort.

```mermaid
flowchart LR
  subgraph mapper[" "]
    subgraph activity["User activity mapper"]
      direction LR
      activityfile["user 104 loaded URL /x
        user 173 loaded URL /y
        user 104 loaded URL /z"] --> amapper["mapper"]
      amapper --> arecord1["104->url:/x
        104->url:/z"]
      amapper --> arecord2["173->url:/y"]
    end
    subgraph user["User database mapper"]
      direction LR
      userfile["{id:103,birth:1947-12-18}
        {id:104,birth:1989-01-30}"] --> umapper["mapper"]
      umapper --> urecord1["104->dob:1989
        104->url:/z"]
      umapper --> urecord2["103->dob:1947"]
    end
  end
  subgraph reducer[" "]
    subgraph reducer1["Reducer partition 1"]
      direction LR
      r1input["104->
        dob:1989
        url:/x
        url:/z"] --> r1reducer["reducer"] --> r1output["{url:/x,dob:1989}
          {url: /z,dob:1989}"]
    end
    subgraph reducer2["Reducer partition 2"]
      direction LR
      r2input["..."] --> r2reducer["..."] --> r2output["..."]
    end
  end
  arecord1 --> r1input
  arecord2 --> r2input
  urecord1 --> r1input
  urecord2 --> r2input
```

Since the reducer processes all of the records for a particular user ID in one go, it only needs to keep one user record in memory at any one time, and it never needs to make any requests over the network.
This algorithm is known as a sort-merge join.

#### Map-Side Joins

The reduce-side approach has the advantage that you do not need to make any assumptions about the input data.
However, the downside is that all that sorting, copying to reducers, and merging of reducer inputs can be quite expensive.

If you can make certain assumptions about your input data, it is possible to make joins faster by using a so-called map-side join.
This approach uses a cut-down MapReuce job in which there are no reducers and no sorting.

- Broadcast hash joins  
The simplest way of performing a map-side join applies in the case where a large dataset is joined with a small dataset.
In particular, the small dataset needs to be small enough that it can be loaded entirely into memory in each of the mappers.
- Partitioned hash joins  
If the inputs to the map-side join are partitioned in the same way, then the hash join approach can be applied to each partition independently.
This approach works if both of the joni's inputs have the same number of partitions, with records assigned to partitions based on the same key and the same hash function.
- Map-side merge joins  
Another variant of a map-side join applied if the input datasets are not only partitioned in the same way, but also sorted based on the same key.

#### The Output of Batch Workflows

- Building search indexes
- Key-value stores as batch process output(classifier and recommendation systems)

#### Comparing Hadoop to Distributed Databases

Msassively parallel processing databases focus on parallel execution of analytic SQL queries on a cluster of machines, while the combination of MapReduce and a distributed filesystem provides something much more like a general-purpose operating system that can run arbitrary programs.

### Beyond MapReduce

#### Materialization of Intermiediate State

The files on the distributed filesystem are simply intermediate state: a means of passing data from one job to the next.
MapReduce's approach of fully materializing intermiediate state has downsides compared to Unix pipes:

- Skew or varying load on different machines means that a job often has a few straggler tasks that take much longer to complete than the others.
- Mappers are often redundant.
- Storing intermediate state in a distributed filesystem means those files are replicated across several nodes.

##### Dataflow engines

New execution engines  for distributed batch computations handle an entire workflow as one job, rather than breaking it up into independent subjobs.

Unlike in MapReduce, these functions need not take the strict roles of alternating map and reduce, but instead can be assembled in more flexible wasy.
We call these functions operators.

## 11. Stream Processing

The problem with daily batch processes is that changes in the input are only reflected in the output a day later.
To reduce the delay, we can run the processing more frequently—say, processing a second's worth of data at the end of every second—or even continuously, abandoning the fixed time slices entirely and simply processing every event as it happens.
That is the idea behind stream processing.

### Transmitting Event Streams

In principle, a file or database is sufficient to connect producers and consumers: a producer writes every event that it generates to the datastore, and each consumer periodically polls the datastore to check for events that have appeared since it last ran.

However, when moving toward continual processing with low delays, polling becomes expensive if the datastore is not designed for this kind of usage.
The more often you poll, the lower the percentage of requests that return new events, and thus the higher the overheads become.
Instead, it is better for consumers to be notified when new events appear.

#### Message Systems

A direct communication channel like a Unix pipe or TCP connection between producer and consumer would be a simple way of implementing a messaging system.
However, most messaging systems expand on this basic model.
In particular, Unix pipes and TCP connect exactly one sender with one recipient, whereas a messaging system allows multiple producer nodes to send messages to the same topic and allows multiple consumer nodes to receive messages in a topic

##### Direct messaging from producers to consumers

A number of messaging systems use direct network communication between producers and consumers without going via intermediary nodes:

- If the consumer exposes a service on the network, producers can make a direct HTTP or RPC request to push messages to the consumer.
This is the idea behind webhooks, a pattern in which a callback URL of one service is registered with another service, and it makes a request to that URL whenever an event occurs.

If a consumer is offline, it may miss messages that were sent while it is unreachable.
Some protocols allow the producer to retry failed message deliveries.

##### Message brokers

A widely used alternative is to send messages via a message broker, which is essentially a kind of database that is optimized for handling message streams.
It runs as a server, with producers and consumers connecting to it as clients.
Producers write messages to the broker, and consumers receive them by reading them from the broker.

By centralizing the data in the broker, these systems can more easily tolerate clients that come and go, and the question of durability is moved to the broker instead.
Some message brokers only keep messages in memory, while others write them to disk so that they are not lost in case of a broker crash.
Faced with slow consumers, they generally allow unbounded queuing.

###### Multiple consumers

- Load balancing  
Each message is delivered to one of the consumers, so the consumers can share the work of processing the messages in the topic.
The broker may assign messages to consumers arbitrarily.
- Fan-out  
Each message is delivered to all of the consumers.
Fan-out allows several independent consumers to each "tune in" to the same broadcast of messages, without affecting each other.

![load balancing vs fan-out](./images/load_balancing_vs_fan-out.png)

##### Acknowledgments and redelivery

Consumers may crash at any time, so it could happen that a broker delivers a message to a consumer but the consumer never processes it, or only partially processes it before crashing.
In order to ensure that the message is not lost, message brokers use acknowledgements: a client must explicitly tell the broker when it has finished processing a message so that the broker can remove it from the queue.

If the connection to a client is closed or times out without the broker receiving an acknowledgment, it assumes that the message was not processed, and therefore it delivers the message again to another consumer.

#### Partitioned Logs

A key feature of batch processes is that you can run them repeatedly, experimenting with the processing steps, without risk of damaging the input.
This is not the cas with AMQP/JMS-style messaging: receiving a message is destructive if the acknowledgment causes it to be deleted from the broker, so you cannot run the same consumer again and expect to get the same result.

Why can we not have a hybrid, combining the durable storage approach of databases with the low-latency notification facilities of messaging? This is the idea behind log-based message brokers.

##### Using logs for message storage

In order to scale to higher throughput than a single disk can offer, the log can be partitioned.
Different partitions can then be hosted on different machines, making each partition a separate log that can be read and written independently from other partitions.
A topic can then be defined as a group of partitions that all carry messages of the same type.

Within each partition, the broker assigns a monotonically increasing sequence number, of offset, to every message.
Such sequence number makes sense because a partition is append-only, so the messages within a partition are totally ordered.
There is no ordering guarantee across different partitions.

![offset by partition](./images/offset-by-partition.png)

##### Logs compared to traditional messaging

The log-based approach trivially supports fan-out messaging, because several consumers can independently read the log without affecting each other—reading a message does not delete it from the log.
To achieve load balancing across a group of consumers, instead of assigning individual messages to consumer clients, the broker can assign entire partitions to nodes in the consumer group.

Each client then consumes all the messages in the partitions it has been assigned.
Typically, when a consumer has been assigned a log partition, it reads the messages in the partition sequentially, in a straightforward single-threaded manner.

- The number of nodes sharing the work of consuming a topic can be at most the number of log partitions in that topic, because messages within the same partition are delivered to the same node.
- If a single message is slow to process, it holds up the processing of subsequent messages in that partition.

Thus in situations where messages may be expensive to process and you want to parallelize processing on a message-by-message basis, and where message ordering is not so important, the JMS/AMQP style of message broker is preferable.
On the other hand, in situations with high message throughput, where each message is fast to process and where message ordering is important, the log-based approach works very well.

##### Consumer offsets

##### Disk space usage

### Databases and Streams

### Processing Streams

## 12. The Future of Data Systems
