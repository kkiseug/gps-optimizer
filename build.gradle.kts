plugins {
    id("java-library")
    id("application")
    id("me.champeau.jmh") version "0.7.2"
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp") version "0.1.1"
}

group = "io.github.kkiseug"
version = "0.1.0"

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

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
    testImplementation("org.assertj:assertj-core:3.27.7")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("GpsOptimizer")
                description.set("A high-performance Java library for GPS track optimization (outlier removal, Kalman smoothing, simplification).")
                url.set("https://github.com/kkiseug/gps-optimizer")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("kkiseug")
                        name.set("kkiseug")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/kkiseug/gps-optimizer.git")
                    developerConnection.set("scm:git:ssh://github.com/kkiseug/gps-optimizer.git")
                    url.set("https://github.com/kkiseug/gps-optimizer")
                }
            }
        }
    }
}

nmcp {
    centralPortal {
        username = project.findProperty("centralUsername")?.toString() ?: ""
        password = project.findProperty("centralPassword")?.toString() ?: ""
        publishingType = "AUTOMATIC"
    }
}

signing {
    sign(publishing.publications["mavenJava"])
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
