plugins {
    kotlin("jvm") version "2.2.0"
    application
    id("com.gradleup.shadow") version "9.1.0"
}

kotlin {
    compilerOptions {
        jvmToolchain(17)
    }
}

application {
    mainClass.set("com.github.alanderua.jic.cli.JicKt")
}

dependencies {
    implementation(project(":api"))
    runtimeOnly(project(":impl"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}