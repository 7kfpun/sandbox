# Kotlin Code Sandbox

A Spring Boot-based sandbox environment for executing Kotlin code with process isolation.

## Features

- Execute Kotlin code via HTTP API  
- Process isolation per request
- Memory safety
- Hot-reload during development
- CORS support
- Configurable timeout

## Setup

1. Configure environment:
```bash
cp .env.example .env
```

2. Build and run:
```bash 
gradle wrapper
docker build -t kotlin-sandbox .
docker run -p 8082:8080 -v $(pwd)/src:/app/src kotlin-sandbox
```

## Environment Variables

`.env.example`:
```env
# CORS origins (comma separated)
ALLOWED_ORIGINS=http://localhost:3000

# Execution timeout in seconds  
KOTLIN_EXECUTION_TIMEOUT=30
```

## Project Structure
```
.
├── .env.example
├── .env
├── Dockerfile
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        └── kotlin/
            └── com/
                └── kfpun/
                    └── sandbox/
                        ├── KotlinSandboxApplication.kt
                        ├── config/
                        │   └── WebConfig.kt
                        └── controller/
                            └── CodeExecutionController.kt
```

## API Usage

```bash
curl -X POST http://localhost:8082/api/execute \
-H "Content-Type: application/json" \
-d '{
    "code": "fun main() { println(\"Hello World\") }"
}'

curl -X POST http://localhost:8082/api/execute \
-H "Content-Type: application/json" \
-d '{
    "code": "import java.time.LocalDate\nimport java.time.format.DateTimeFormatter\n\nfun main() {\n    val date = LocalDate.now()\n    println(\"Current date: ${date.format(DateTimeFormatter.ISO_DATE)}\")}"
}'
```

Response:
```json
{
    "status": "Success|Error",
    "output": "string"  
}
```

## Dependencies 

Add custom dependencies in `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Add your dependencies here
}
```

## License

MIT License
