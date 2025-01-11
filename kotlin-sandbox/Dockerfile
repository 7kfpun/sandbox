FROM eclipse-temurin:21-jdk

WORKDIR /app

# Install Kotlin 1.9.22
RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl -L "https://github.com/JetBrains/kotlin/releases/download/v2.1.10-RC/kotlin-compiler-2.1.10-RC.zip" -o kotlin.zip && \
    unzip kotlin.zip -d /opt && \
    rm kotlin.zip

ENV PATH="/opt/kotlinc/bin:${PATH}"

COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x ./gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon

RUN useradd -m -U sandbox
RUN chown -R sandbox:sandbox /app

USER sandbox

ENV SPRING_DEVTOOLS_REMOTE_SECRET=mysecret
CMD ["java", "-jar", "build/libs/app.jar"]
