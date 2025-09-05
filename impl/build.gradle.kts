plugins {
    kotlin("jvm") version "2.2.0"
}

kotlin {
    compilerOptions {
        explicitApi()
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(":api"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}