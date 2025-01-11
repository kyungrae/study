import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "me.hama"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.apache.kafka:kafka-clients:2.7.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
