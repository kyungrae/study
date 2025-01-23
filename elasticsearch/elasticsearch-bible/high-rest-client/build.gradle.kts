plugins {
    kotlin("jvm")
}

group = "me"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation("org.elasticsearch:elasticsearch:8.4.2")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:8.4.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
