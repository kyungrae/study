import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester
import co.elastic.clients.elasticsearch.core.GetRequest
import co.elastic.clients.elasticsearch.core.IndexRequest
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation
import co.elastic.clients.elasticsearch.core.bulk.CreateOperation
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation
import co.elastic.clients.elasticsearch.core.bulk.UpdateAction
import co.elastic.clients.elasticsearch.core.bulk.UpdateOperation
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

fun main() {
    val client = buildJavaClient()

    indexExample(client)
    getExample(client)

    bulkExampleOne(client)
    bulkExampleTwo(client)

    searchExample(client)

    bulkIngesterExample(client)

    client._transport().close()
}

private fun buildJavaClient(): ElasticsearchClient {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
    val lowLevelRestClient = restClientBuilder.build()
    val mapper = jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false)
        .build()

    val transport = RestClientTransport(lowLevelRestClient, JacksonJsonpMapper(mapper))

    return ElasticsearchClient(transport)
}

private fun indexExample(client: ElasticsearchClient) {
    val indexResponse = client.index { builder: IndexRequest.Builder<MyIndexClass> ->
        builder
            .index("my-index")
            .id("my-id-1")
            .routing("my-routing-1")
            .document(MyIndexClass("hello", 1L, ZonedDateTime.now(ZoneOffset.UTC)))
    }

    val result = indexResponse.result()
    println(result.name)
}

private fun getExample(client: ElasticsearchClient) {
    val getRequest = GetRequest.Builder()
        .index("my-index")
        .id("my-id-1")
        .routing("my-routing-1")
        .build()

    val response = client.get(getRequest, MyIndexClass::class.java)
    println(response.source())
}

private fun bulkExampleOne(client: ElasticsearchClient) {
    val createOperation = CreateOperation.Builder<MyIndexClass>()
        .index("my-index")
        .id("my-id-2")
        .routing("my-routing-3")
        .document(MyIndexClass("world", 2L, ZonedDateTime.now(ZoneOffset.UTC)))
        .build()

    val indexOperation = IndexOperation.Builder<MyIndexClass>()
        .index("my-index")
        .id("my-id-3")
        .routing("my-routing-3")
        .document(MyIndexClass("world", 4L, ZonedDateTime.now(ZoneOffset.UTC)))
        .build()

    val updateAction = UpdateAction.Builder<MyIndexClass, MyPartialIndexClass>()
        .doc(MyPartialIndexClass("world updated"))
        .build()

    val updateOperation = UpdateOperation.Builder<MyIndexClass, MyPartialIndexClass>()
        .index("my-index")
        .id("my-id-1")
        .routing("my-routing-1")
        .action(updateAction)
        .build()

    val bulkOpOne = BulkOperation.Builder().create(createOperation).build()
    val bulkOpTwo = BulkOperation.Builder().index(indexOperation).build()
    val bulkOpThree = BulkOperation.Builder().update(updateOperation).build()

    val operations = listOf<BulkOperation>(bulkOpOne, bulkOpTwo, bulkOpThree)
    val bulkResponse = client.bulk { it.operations(operations) }

    for (item in bulkResponse.items())
        println("result: ${item.result()}, error: ${item.error()}")
}

private fun bulkExampleTwo(client: ElasticsearchClient) {
    val bulkResponse = client.bulk { _0 -> _0
        .operations { _1 -> _1
            .index { _2: IndexOperation.Builder<MyIndexClass> -> _2
                .index("my-index")
                .id("my-id-4")
                .routing("my-routing-4")
                .document(MyIndexClass("world", 4L, ZonedDateTime.now(ZoneOffset.UTC)))
            }
        }
        .operations { _1 -> _1
            .update { _2: UpdateOperation.Builder<MyIndexClass, MyPartialIndexClass> -> _2
                .index("my-index")
                .id("my-id-2")
                .routing("my-routing-2")
                .action { _3 -> _3
                    .doc(MyPartialIndexClass("world updated"))
                }
            }
        }
    }

    for(item in bulkResponse.items()) {
        println("result: ${item.result()}, error: ${item.error()}")
    }
}

private fun searchExample(client: ElasticsearchClient) {
    val response = client.search({ builder -> builder
        .index("my-index")
        .from(0)
        .size(10)
        .query { query -> query
            .term { term -> term
                .field("fieldOne")
                .value { value -> value.stringValue("world") }
            }
        }
    }, MyIndexClass::class.java)

    for (hit in response.hits().hits()) {
        println(hit.source())
    }
}

private fun bulkIngesterExample(client: ElasticsearchClient) {
    val listener = BulkIngesterListener<String>()

    val ingester = BulkIngester.of<String> {
        it.client(client)
            .maxOperations(200)
            .maxConcurrentRequests(1)
            .maxSize(5242880L)
            .flushInterval(5L, TimeUnit.SECONDS)
            .listener(listener)
    }

    for (number in 0L until 1100L) {
        val bulkOperation = BulkOperation.of { bulkBuilder ->
            bulkBuilder.index { indexOpBuilder: IndexOperation.Builder<MyIndexClass> ->
                indexOpBuilder
                    .index("my-index")
                    .id("my-id-$number")
                    .routing("my-routing-$number")
                    .document(MyIndexClass("world", number, ZonedDateTime.now(ZoneOffset.UTC)))
            }
        }

        ingester.add(bulkOperation)
    }

    println("[${LocalDateTime.now()} sleep 10 seconds ...")
    Thread.sleep(10000L)

    for(number in 1100L until 1200L) {
        val bulkOperation = BulkOperation.of { bulkBuilder ->
            bulkBuilder.index { indexOpBuilder: IndexOperation.Builder<MyIndexClass> ->
                indexOpBuilder
                    .index("my-index")
                    .id("my-id-$number")
                    .routing("my-routing-$number")
                    .document(MyIndexClass("world", number, ZonedDateTime.now(ZoneOffset.UTC)))
            }
        }

        ingester.add(bulkOperation)
    }

    println("[${LocalDateTime.now()} sleep 10 seconds ...")
    Thread.sleep(10000L)

    ingester.close()
}
