# Ⅱ. Designing Your Application

## 5. Indexes

### Introduction to Indexes

#### Creating an Index

```js
db.users.createIndex({"username":1})
db.users.createIndex({"age": 1, "username": 1})
```

#### How MongoDB Selects an Index

When a query comes in, MongoDB looks at the query's shape.
The shape has to do with what fields are being searched on and additional information, such as whether or not there is sort.
Based on that information, the system identifies a set of candidate indexes that it might be able to use in satisfying the query.

Create candidate query plans, and run the query in parallel threads.
Choose fastest one. The Server maintains a cache of query plans.
A winning plan is stored in the cache for future use for queries of that shape.

#### Using Compound Indexes

- Keys for equality filters should appear first.
- Key used for sorting should appear before multivalue fields.
- Keys for multivalue filters should appear last.

#### Indexing Objects and Arrays

##### INDEXING EMBEDDED DOCS

Note that indexing the embedded document itself has very different behavior than indexing a field of that embedded document("field.field").
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
  Whether MongoDB was able to fulfill this query using an index (e.g., IXSCAN OR COLSCAN).
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

### Geospatial Indexes

MongoDB has two types of geospatial indexes: 2dsphere and 2d.
2dsphere indexes work with spherical geometries that model the surface of the earth based on the WGS84 datum.
This datum models the surface of the earth as an ablate spheroid, meaning that there is some flattening at the poles.

2dsphere allows you to specify geometries for points, lines, and polygons in the GeoJSON format.
A point is given by a two-element array, representing [longitude, latitude].
The field that we are naming, "loc" in this example, can be called anything, but the field names in the embedded object are specified by GeoJSON and cannot be changed.

```json
{
  "name": "Hudson River",
  "loc": {
    "type": "LineString",
    "coordinates": [[0,1],[0,2],[1,2]]
  }
}
```

You can create a geospatial index using the "2dsphere" type with createIndex:

```js
db.collections.createIndex({"loc": "2dsphere"})
```

#### Type of Geospatial Queries

There are three types of geospatial queries that you can perform: intersection, within, and nearness.
You specify what you're looking for as a GeoJSON object that look like {"$geometry": geoJsonDesc }

```js
var eastVillage = {
  "type": "Polygon",
  "coordinates": [
    [
      [ -73.93383000695911, 40.81949109558767 ],
      [ -73.93411701695138, 40.81955053491088 ]
    ]
  ]
}

// Find all documents that had a point in an area.
db.openStreetMap.find({"loc": { "$geoIntersects": {"$geometry": eastVillage}}})

// Query for that things that are completely contained in an area.
db.openStreetMap.find({"loc": { "$geoWithin": {"$geometry": eastVillage}}})

// Query for nearby locations.
db.openStreetMap.find({"loc": { "$near": {"$geometry": eastVillage}}})
```

#### Using Geospatial Indexes

```js
// Find the current neighborhood.
var neighborhood = db.neighborhoods.findOne({
  "geometry": {
    "$geoIntersects": {
      "$geometry": {
        "type": "Point",
        "coordinates": [-73.93414657, 40.83302903]
      }
    }
  }
})

// Find all restaurants in the neighborhood.
db.restaurants.find({"location": {"$geoWithin": {"$geometry":neighborhood.geometry}}})

// Find unsorted order restaurants within a distance.
// To convert kilometers to radians, the specified kilometers were divided by 6378.1.
db.restaurants.find({
  "location": {
    "$geoWithin": {
      "$centerSphere": [[-73.93414657, 40.83302903], 5/3963.2]
    }
  }
})


// Find sorted order restaurants within a km distance.
db.restaurants.find({
  "location": {
    "$nearSphere": {
      "$geometry": {
        "type": "Point",
        "coordinates":[-73.93414657,40.82302903]
      },
      "$maxDistance": 1000
    }
  }
})
```

#### Compound Goespatial Indexes

```js
db.openStreetMap.createIndex({"tags": 1, "location": "2dsphere"})
```

#### 2d Indexes

```js
db.hyrule.createIndex({"tile": "2d"})
```

2d indexes assume a perfectly flat surface.
Document should use a two-element array fro their "2d" indexed field
Do not use a 2d index if you plan to store GeoJSON data-they can only index points.

```json
{
  "name": "Water Temple",
  "tile": [32,22]
}
```

```js
db.hyrule.find({tile: {$geoWithin: {$box:[[10,10],[100,100]]}}})
db.hyrule.find({tile: {$geoWithin: {$center:[[-17,20.5],25]}}})
db.hyrule.find({tile: {$geoWithin: {$box:[[0,0],[3,6],[6,0]]}}})
```

### Indexes for Full Text Search

### Capped Collections

### Time-To-Live Indexes

### Sorting Files with GridFS

## 7. Introduction to the Aggregation Framework

## 8. Transactions

## 9. Application Design
