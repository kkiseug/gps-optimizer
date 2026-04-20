plugins {
    id("java")
    id("application")
    id("me.champeau.jmh") version "0.7.2"
}

group = "music"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("Main")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Source: https://mvnrepository.com/artifact/org.assertj/assertj-core
    testImplementation("org.assertj:assertj-core:3.27.7")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    threads.set(1)
    fork.set(1)
    warmupIterations.set(3)
    iterations.set(5)
    benchmarkMode.set(listOf("avgt"))
    timeUnit.set("ms")
}

tasks.test {
    useJUnitPlatform()
}
