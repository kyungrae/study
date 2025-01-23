package lowclient

import org.apache.http.HttpHost
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseListener
import org.elasticsearch.client.RestClient
import java.lang.Exception

fun main() {
    val restClient = restClientWithoutTls()
    lowLevelClientSample(restClient)
    restClient.close()
}

fun restClientWithoutTls(): RestClient {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
    restClientBuilder.setRequestConfigCallback {
        it.setConnectTimeout(5000)
            .setSocketTimeout(70000)
    }

    return restClientBuilder.build()
}

fun lowLevelClientSample(lowLevelClient: RestClient) {
    // sync request
    val getSettingRequest = Request("GET", "/_cluster/settings")
    getSettingRequest.addParameters(mutableMapOf("pretty" to "true"))

    val getSettingResponse: Response = lowLevelClient.performRequest(getSettingRequest)
    printResponse(getSettingResponse)

    // async request
    val updateSettingRequest = Request("PUT", "/_cluster/settings")
    val requestBody = """
        {
            "transient": {
                "cluster.routing.allocation.enable": "all"
            }
        }
    """.trimIndent()
    updateSettingRequest.entity = NStringEntity(requestBody, ContentType.APPLICATION_JSON)

    val callable = lowLevelClient.performRequestAsync(updateSettingRequest, object : ResponseListener {
        override fun onSuccess(response: Response) {
            printResponse(response)
        }

        override fun onFailure(exception: Exception) {
            System.err.println(exception)
        }
    })

    // 필요하다면 요청 취소 가능
    Thread.sleep(1000L)
    callable.cancel()
    lowLevelClient.close()
}

fun printResponse(response: Response) {
    val statusCode = response.statusLine.statusCode
    val responseBody = EntityUtils.toString(response.entity, Charsets.UTF_8)
    println("statusCode: $statusCode, responseBody: $responseBody")
}
