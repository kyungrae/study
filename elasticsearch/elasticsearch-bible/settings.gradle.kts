plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "elasticsearch-bible"
include("low-rest-client")
include("high-rest-client")
include("new-rest-client")
