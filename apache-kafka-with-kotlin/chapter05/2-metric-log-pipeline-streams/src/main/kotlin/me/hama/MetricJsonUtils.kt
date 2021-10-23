package me.hama

import com.google.gson.JsonParser

fun getTotalCpuPercent(value: String): Double =
    JsonParser().parse(value).asJsonObject["system"].asJsonObject["cpu"]
        .asJsonObject["total"].asJsonObject["pct"].asDouble

fun getMetricName(value: String): String =
    JsonParser().parse(value).asJsonObject["metricset"].asJsonObject["name"].asString

fun getHostTimeStamp(value: String): String {
    val objectValue = JsonParser().parse(value).asJsonObject
    val result = objectValue.getAsJsonObject("host")
    result.add("timestamp", objectValue["@timestamp"])
    return result.toString()
}
