plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.worldline"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //Database
    implementation("org.liquibase:liquibase-core")
    implementation("org.postgresql:postgresql:42.7.4")

    //Development tools
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    //API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    //Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Define custom source set for integration tests
sourceSets {
    create("integrationTest") {
        java.srcDir("src/test/java")
        resources.srcDir("src/test/resources")
    }
}

// Create the 'integrationTest' task to run only integration tests
val integrationTest by
tasks.registering(Test::class, fun Test.() {
    group = "verification"
    description = "Runs the integration tests"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

// Include tests that match the pattern "*IT"
    include("**/*IT.class")
})

tasks.withType<Test> {
    useJUnitPlatform()

    // Exclude tests that match the pattern "*IT" (integration tests)
    exclude("**/*IT.class")
}


tasks.bootBuildImage {
    imageName.set("worldline/taskboard:${project.version}")
}
