plugins {
    kotlin("jvm") version "2.2.0"
}

kotlin {
    compilerOptions {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(":api"))
    runtimeOnly(project(":impl"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}