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
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
application.mainClass = "protohackers.server.Server"



tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.register<Test>("test0") {
    include("protohackers/server/d0/*")
    group = "application"
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}


tasks.register<Test>("test1") {
    include("protohackers/server/d1/*")
    group = "application"
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.register<Test>("test2") {
    include("protohackers/server/d2/*")
    group = "application"
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.register<JavaExec>("d0") {
    group = "application"
    description = "Run protohackers.server.d0.Server"
    mainClass.set("protohackers.server.d0.Server")
    classpath = sourceSets.main.get().runtimeClasspath
}


tasks.register<JavaExec>("d1") {
    group = "application"
    description = "Run protohackers.server.d1.Server"
    mainClass.set("protohackers.server.d1.Server")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<JavaExec>("d2") {
    group = "application"
    description = "Run protohackers.server.d2.Server"
    mainClass.set("protohackers.server.d2.Server")
    classpath = sourceSets.main.get().runtimeClasspath
}

tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("buildFatJar") {
    archiveFileName.set("protohackers-all.jar")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "protohackers.server.Server"
    }
}

tasks.named("build") {
    dependsOn("buildFatJar")
}

