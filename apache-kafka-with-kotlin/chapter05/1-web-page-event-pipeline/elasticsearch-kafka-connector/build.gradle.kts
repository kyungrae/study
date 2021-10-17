import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "me.hama"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.apache.kafka:connect-api:2.7.1")
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.9.2")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to "")
    }
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })
}
