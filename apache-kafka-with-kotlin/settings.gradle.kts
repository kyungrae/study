rootProject.name = "apache-kafka-with-kotlin"

include(":chapter03:4-client")
include(":chapter03:5-streams")
include(":chapter03:6-connect:1-source")
include(":chapter03:6-connect:2-sink")
include(":chapter04:3-kafka-consumer")
include(":chapter04:4-spring-kafka")
include(
    ":chapter05:1-web-page-event-pipeline:spring-boot-web-kafka-producer",
    ":chapter05:1-web-page-event-pipeline:hdfs-kafka-consumer",
    ":chapter05:1-web-page-event-pipeline:elasticsearch-kafka-connector"
)
include(":chapter05:2-metric-log-pipeline-streams")
