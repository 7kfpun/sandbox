# Code Sandbox API

A secure sandbox environment for executing isolated code from frontend applications.

## Languages Supported

- [Python Sandbox](python-sandbox/README.md)
- [Java Sandbox](java-sandbox/README.md)
- [Kotlin Sandbox](kotlin-sandbox/README.md)

## Quick Start

### Environment Setup
```env
ALLOWED_ORIGINS=http://localhost:3000
EXECUTION_TIMEOUT=30
```

### Run Services
```bash
# Python
cd python-sandbox
docker build -t python-sandbox .
docker run -p 8080:8080 -v $(pwd):/app python-sandbox

# Java 
cd java-sandbox
docker build -t java-sandbox .
docker run -p 8081:8080 -v $(pwd)/src:/app/src java-sandbox

# Kotlin
cd kotlin-sandbox
docker build -t kotlin-sandbox .
docker run -p 8082:8080 -v $(pwd)/src:/app/src kotlin-sandbox
```

## API Endpoints

### Execute Code
```bash
POST http://{host}:{port}/api/execute
Content-Type: application/json

{
    "code": "string"
}
```

Response:
```json
{
    "status": "Success|Error",
    "output": "string"
}
```

## License

MIT License
