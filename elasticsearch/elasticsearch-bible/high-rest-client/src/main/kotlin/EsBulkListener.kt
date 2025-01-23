package highclient

import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkProcessor
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse

class EsBulkListener : BulkProcessor.Listener {
    override fun beforeBulk(executionId: Long, request: BulkRequest) {
        println("beforeBulk")
    }

    override fun afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {
        if (!response.hasFailures()) {
            println("bulk success")
        } else {
            System.err.println("has failures")
            val failedItems = response.items
                .filter { it.isFailed }

            doSomeRecoveryJobs(request, failedItems)
        }
    }

    override fun afterBulk(executionId: Long, requset: BulkRequest, failure: Throwable) {
        System.err.println()
    }

    private fun doSomeRecoveryJobs(request: BulkRequest, failedItems: List<BulkItemResponse>) {

    }
}
