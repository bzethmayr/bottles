plugins {
    id("java")
}

group = "net.bzethmayr"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bzethmayr.fungu:fungu:1.5.7")

    testImplementation("io.github.bzethmayr.fungu:fungu-test:1.2.12")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}