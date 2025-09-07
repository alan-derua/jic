plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    compilerOptions {
        explicitApi()
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(":api"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")

    implementation("org.ow2.asm:asm:9.8")
    implementation("org.ow2.asm:asm-commons:9.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}