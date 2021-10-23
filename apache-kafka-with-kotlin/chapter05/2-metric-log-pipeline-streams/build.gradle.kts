import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "me.hama"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("com.google.code.gson:gson:2.8.0")
    implementation("org.apache.kafka:kafka-streams:2.7.1")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
