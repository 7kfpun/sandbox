FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Create sandbox user
RUN useradd -m -U sandbox

# Set ownership
RUN chown -R sandbox:sandbox /app

# Switch to non-root user
USER sandbox

# Enable devtools
ENV SPRING_DEVTOOLS_REMOTE_SECRET=mysecret
CMD ["java", "-jar", "-Dspring.devtools.restart.enabled=true", "build/libs/app.jar"]
