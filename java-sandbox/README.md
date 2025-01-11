# Java Code Sandbox

A Spring Boot-based sandbox environment for executing Java code with process isolation.

## Features

- Execute Java code via HTTP API
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
docker build -t java-sandbox .
docker run -p 8081:8080 -v $(pwd)/src:/app/src java-sandbox
```

## Environment Variables

`.env.example`:
```env
# CORS origins (comma separated)
ALLOWED_ORIGINS=http://localhost:3000

# Execution timeout in seconds
JAVA_EXECUTION_TIMEOUT=30
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
        └── java/
            └── com/
                └── kfpun/
                    └── sandbox/
                        ├── JavaSandboxApplication.java 
                        ├── config/
                        │   └── WebConfig.java
                        └── controller/
                            └── CodeExecutionController.java
```

## API Usage

```bash
POST http://localhost:8081/api/execute
Content-Type: application/json

{
    "code": "public class Main { 
        public static void main(String[] args) {
            System.out.println(\"Hello World\");
        }
    }"
}
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
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // Add your dependencies here
    implementation("org.apache.commons:commons-math3:3.6.1")
}
```

## License

MIT License
