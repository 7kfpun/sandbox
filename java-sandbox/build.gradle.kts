plugins {
   java
   id("org.springframework.boot") version "3.2.0"
   id("io.spring.dependency-management") version "1.1.4"
}

group = "com.kfpun.sandbox"
version = "0.0.1-SNAPSHOT"

java {
   sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
   mavenCentral()
}

dependencies {
   implementation("org.springframework.boot:spring-boot-starter-web")
   developmentOnly("org.springframework.boot:spring-boot-devtools")

   // Add your dependencies here
   implementation("org.apache.commons:commons-math3:3.6.1")
   implementation("org.apache.commons:commons-lang3:3.14.0")
}

tasks.bootJar {
   archiveFileName.set("app.jar")
   enabled = true
}

tasks.jar {
   enabled = false
}

tasks.register<Copy>("copyDependencies") {
   from(configurations.runtimeClasspath)
   into("build/dependencies")
}
