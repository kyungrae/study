package highclient

import org.apache.http.HttpHost
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.BackoffPolicy
import org.elasticsearch.action.bulk.BulkProcessor
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.ByteSizeValue
import org.elasticsearch.core.TimeValue
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.xcontent.XContentType

fun main() {
    val highLevelClient = restHighLevelClientWithoutTls()

    getSampleData(highLevelClient)
    searchSample(highLevelClient)
    bulkSample(highLevelClient)

    val bulkProcessor = buildBulkProcessor(highLevelClient)
    bulkProcessorSample(bulkProcessor)

    Thread.sleep(10000L)
    highLevelClient.close()
}

fun restHighLevelClientWithoutTls(): RestHighLevelClient {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
    restClientBuilder.setRequestConfigCallback {
        it.setConnectTimeout(5000)
            .setSocketTimeout(70000)
    }
    return RestHighLevelClient(restClientBuilder)
}

fun getSampleData(highLevelClient: RestHighLevelClient) {
    val getRequest = GetRequest()
        .index("my-index-01")
        .id("document-id-01")
        .routing("abc123")

    val getResponse = highLevelClient.get(getRequest, RequestOptions.DEFAULT)
    println(getResponse.sourceAsMap)
}

fun searchSample(highLevelClient: RestHighLevelClient) {
    val queryBuilder = QueryBuilders.boolQuery()
        .must(TermQueryBuilder("fieldOne", "hello"))
        .should(MatchQueryBuilder("fieldTwo", "hello world").operator(Operator.AND))
        .should(RangeQueryBuilder("fieldThree").gte(100).lte(200))
        .minimumShouldMatch(1)

    val searchSourceBuilder = SearchSourceBuilder()
        .from(0)
        .size(10)
        .query(queryBuilder)

    val searchRequest = SearchRequest()
        .indices("my-index-01", "my-index-01")
        .routing("abc123")
        .source(searchSourceBuilder)

    val searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT)
    val searchHits = searchResponse.hits
    val totalHits = searchHits.totalHits
    println("totalHits: $totalHits")
    println(searchHits.hits.map { it.sourceAsMap })
}

fun bulkSample(highLevelClient: RestHighLevelClient) {
    val bulkRequest = BulkRequest()
    bulkRequest.add(
        UpdateRequest()
            .index("my-index-01")
            .id("document-id-01")
            .routing("abc123")
            .doc(mapOf("hello" to "elasticsearch"))
    )

    val bulkResponse = highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT)
    if (bulkResponse.hasFailures()) {
        System.err.println(bulkResponse.buildFailureMessage())
    }
}

fun buildBulkProcessor(client: RestHighLevelClient): BulkProcessor {
    val bulkAsync = { request: BulkRequest, listener: ActionListener<BulkResponse> ->
        client.bulkAsync(request, RequestOptions.DEFAULT, listener)
        Unit
    }

    return BulkProcessor.builder(bulkAsync, EsBulkListener(), "myBulkProcessorName")
        .setBulkActions(50000)
        .setBulkSize(ByteSizeValue.ofMb(50L))
        .setFlushInterval(TimeValue.timeValueMillis(5000L))
        .setConcurrentRequests(1)
        .setBackoffPolicy(BackoffPolicy.exponentialBackoff())
        .build()
}

fun bulkProcessorSample(bulkProcessor: BulkProcessor) {
    val source = mapOf(
        "hello" to "world",
        "world" to 123
    )

    val indexRequest = IndexRequest("my-index-01")
        .id("document-id-01")
        .routing("abc123")
        .source(source, XContentType.JSON)

    bulkProcessor.add(indexRequest)
}
