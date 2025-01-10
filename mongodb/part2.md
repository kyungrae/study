# Ⅱ. Designing Your Application

## 5. Indexes

### Introduction to Indexes

#### Creating an Index

```js
db.users.createIndex({"username":1})
db.users.createIndex({"age": 1, "username": 1})
```

MongoDB can't sort documents when documents are bigger than 32MB.
It will just error out.

#### How MongoDB Selects an Index

When a query comes in, MongoDB looks at the query's shape.
The shape has to do with what fields are being searched on and additional information, such as whether or not there is sort.
Based on that information, the system identifies a set of candidate indexes that it might be able to use in satisfying the query.

Create candidate query plans, and run the query in parallel threads.
Choose fastest one.
The Server maintains a cache of query plans.
A winning plan is stored in the cache for future use for queries of that shape.

#### Using Compound Indexes

- Keys for equality filters should appear first.
- Key used for sorting should appear before multivalue filter.
- Keys for multivalue filters should appear last.

#### Indexing Objects and Arrays

##### INDEXING EMBEDDED DOCS

Note that indexing the embedded document itself ahs very different behavior than indexing a field of that embedded document("field.field").
Indexing the entire subdocument will only help queries that are querying for the entire subdocument.

##### INDEXING ARRAYS

Indexing an array creates an index entry for each element of the array, so if a post had 20 comments, it would have 20 index entries.

### explain Output

We can use the explain command to see what MongoDB is doing when it executes the query.

```js
db.find().explain("executionStats")
```

- isMultiKey  
  If this query used a multikey index
- nReturned  
  The number of documents returned by the query.
- totalDocsExamined  
  The number of times MongoDB had to follow an index pointer to the actual document on disk.
- totalKeysExamined
  The number of index entries looked at, if an index was used.
  If this was a table scan, it is the number of documents examined.
- stage  
  Whether MongoDB was able to fulfill this query using an index
- needYield  
  The number of times this query yielded to allow a write request to proceed.
- executionTimeMillis  
  The number of milliseconds it took the database to execute the query.
- indexBounds  
  A description of how the index was used, giving ranges of the index traversed.

### Type of Indexes

#### Unique Indexes

Unique index guarantee that each value will appear at most once in the index.

```js
db.users.createIndex({"firstname": 1},{"unique": true})
```

#### Partial Indexes

Partial indexes are only created on a subset of the data.

```js
db.users.createIndex(
    {"email": 1},
    {"unique": true, "partialFilterExpression": {email: {$exist: true}}}
)
```

## 6. Special Index and Collection Type

## 7. Introduction to the Aggregation Framework

## 8. Transactions

## 9. Application Design
