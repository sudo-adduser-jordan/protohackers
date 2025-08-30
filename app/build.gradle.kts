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
    implementation(libs.guava)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
 }

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
application.mainClass = "server.Server"

tasks.named<Test>("test") { // all
    useJUnitPlatform()
    reports {
        junitXml.required = false
        html.required = false
    }
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.register<Test>("test0") {
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("server/d0/*")
    reports {
        junitXml.required.set(false)
        html.required.set(false)
    }
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}


tasks.register<Test>("test1") {
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("server/d1/*")
    reports {
        junitXml.required.set(false)
        html.required.set(false)
    }
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.register<Test>("test2") {
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("server/d2/*")
    reports {
        junitXml.required.set(false)
        html.required.set(false)
    }
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.register<JavaExec>("d0") {
    group = "application"
    description = "Run server.d0.Server"
    mainClass.set("server.d0.Server")
    classpath = sourceSets.main.get().runtimeClasspath
}


tasks.register<JavaExec>("d1") {
    group = "application"
    description = "Run server.d1.Server"
    mainClass.set("server.d1.Server")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("d2") {
    group = "application"
    description = "Run server.d2.Server"
    mainClass.set("server.d2.Server")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("buildFatJar") {
    archiveFileName.set("my-application-all.jar")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "server.Server"
    }
}

tasks.named("build") {
    dependsOn("buildFatJar")
}

