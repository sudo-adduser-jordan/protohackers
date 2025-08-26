
plugins {
    java
    application
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "8.3.9"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(libs.guava)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
application.mainClass = "server.Server"

tasks.named<Test>("test") {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
}