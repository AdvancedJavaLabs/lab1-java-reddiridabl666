plugins {
    kotlin("jvm") version "1.9.20"
    java
    application
    id("io.freefair.lombok") version "9.0.0-rc2"
}

group = "org.itmo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.openjdk.jcstress:jcstress-core:0.16")
    testAnnotationProcessor("org.openjdk.jcstress:jcstress-core:0.16")


    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

// JCStress runner task: runs JCStress tests located on the test runtime classpath
// Use: ./gradlew jcstress [-PjcstressArgs="-v -m quick"]
tasks.register<JavaExec>("jcstress") {
    group = "verification"
    description = "Run JCStress stress tests"
    mainClass.set("org.openjdk.jcstress.Main")
    classpath = sourceSets.test.get().runtimeClasspath
    dependsOn("testClasses")

    val argsProp = project.findProperty("jcstressArgs") as String?
    if (!argsProp.isNullOrBlank()) {
        args = argsProp.split("\\s+".toRegex())
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()

      minHeapSize = "512m"
  maxHeapSize = "8000m"
    
    testLogging {
        events("started", "passed", "skipped", "failed")
        // if (project.hasProperty("verbose")) {
            showStandardStreams = true
        // }
        setExceptionFormat("full")
    }
}
