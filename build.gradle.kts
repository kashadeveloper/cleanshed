plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
    kotlin("plugin.jpa") version "2.4.10"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "click.ufate"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_26
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.xerial:sqlite-jdbc:3.53.2.1")
    implementation("org.hibernate.orm:hibernate-community-dialects")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
