plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.kfpun"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Add your dependencies here
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")

   // KodyPay
   implementation("com.kodypay.grpc:kody-clientsdk-java:0.0.9")
   implementation("io.grpc:grpc-netty-shaded:1.58.0")
   implementation("io.grpc:grpc-protobuf:1.58.0")
   implementation("io.grpc:grpc-stub:1.58.0")
}

tasks.bootJar {
    archiveFileName.set("app.jar")
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath)
    into("build/dependencies")
}
