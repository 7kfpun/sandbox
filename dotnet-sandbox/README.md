# .NET Code Sandbox

A .NET-based sandbox for executing C# code with process isolation and hot-reload. This sandbox dynamically compiles and executes user-supplied code—whether it’s a complete C# file or a simple snippet—by generating a custom project file per execution. Additional package references can be defined via an environment variable.

## Setup

1. **Configure Environment Variables:**
   Copy the provided `.env.example` to `.env` and update as needed. For example, you can define:
   ```env
   ALLOWED_ORIGINS=http://localhost:3000
   EXECUTION_TIMEOUT=30
   PACKAGES=Newtonsoft.Json:13.0.1,Serilog:2.12.0
   ```
   The `PACKAGES` variable is a comma-separated list of package references (in the format `PackageName:Version`) that will be injected into the sandbox project file.

2. **Build and Run with Docker:**
   ```bash
   docker-compose up --build
   ```

## Project Structure

```
dotnet-sandbox/
├── docker-compose.yml
├── Dockerfile
├── .env.example
├── .env
└── DotnetSandbox/
    ├── Program.cs
    ├── Controllers/
    │   └── CodeExecutionController.cs
    ├── Models/
    │   ├── CodeRequest.cs
    │   └── ExecutionResponse.cs
    └── DotnetSandbox.csproj
```

## How It Works

- **Dynamic Project Generation:**
  Each execution request creates a temporary project with its own `Program.csproj`. This project file is generated dynamically using the `CreateProjectFile()` method, which reads the `PACKAGES` environment variable to include any desired package references.

- **Code Input Flexibility:**
  The sandbox accepts two types of input:
  - **Complete Source Files:** If the user code includes a `class` or `namespace` declaration, it is assumed to be complete and is compiled as provided.
  - **Simple Code Snippets:** If the code is a snippet (for example, just a single line like `Console.WriteLine("Hello World");`), the sandbox will automatically wrap it in a complete `Program` class with a `Main` method and default using directives.

- **Global Usings for Packages:**
  A separate file (`GlobalUsings.cs`) is created to define global using directives (e.g. `global using Newtonsoft.Json;`) so that configured packages are available without requiring the user to include them in their code.

- **Process Isolation:**
  Each request compiles and runs in an isolated process with configurable execution timeout and memory safety between requests.

## API Usage

### Simple Code Snippet

If you send a simple snippet, the sandbox wraps it in a complete C# program:

```bash
curl -X POST http://localhost:8084/api/execute \
-H "Content-Type: application/json" \
-d '{
    "code": "Console.WriteLine(\"Hello World\");"
}'
```

### Complete Source File Example Using Popular .NET Libraries

This example demonstrates a complete source file that uses both **Newtonsoft.Json** and **Serilog**. (Make sure you have defined the PACKAGES environment variable accordingly, for example: `PACKAGES=Newtonsoft.Json:13.0.1,Serilog:2.12.0`)

```bash
curl -X POST http://localhost:8084/api/execute \
-H "Content-Type: application/json" \
-d '{
    "code": "using Newtonsoft.Json;\nusing Serilog;\nusing System;\n\npublic class Program\n{\n    public static void Main()\n    {\n        // Configure Serilog to write to the console\n        Log.Logger = new LoggerConfiguration().WriteTo.Console().CreateLogger();\n        Log.Information(\"Hello from Serilog!\");\n        \n        // Serialize an object to JSON using Newtonsoft.Json\n        var json = JsonConvert.SerializeObject(new { message = \"Hello World\" });\n        Console.WriteLine(json);\n    }\n}\n"
}'
```

## Environment Variables

Define the following in your `.env` file:

```env
ALLOWED_ORIGINS=http://localhost:3000
EXECUTION_TIMEOUT=30
PACKAGES=Newtonsoft.Json:13.0.1,Serilog:2.12.0
```

- **ALLOWED_ORIGINS:** The origins allowed for CORS.
- **EXECUTION_TIMEOUT:** The maximum time (in seconds) allowed for code execution.
- **PACKAGES:** A comma-separated list of package references in the format `PackageName:Version`. This controls which packages are available to the executed code.

## Features

- **Dynamic Code Execution:** Supports both full source files and code snippets.
- **Process Isolation:** Each request is executed in a separate process.
- **Hot-Reload:** Changes during development trigger automatic recompilation.
- **Configurable Dependencies:** Use the `PACKAGES` environment variable to control package references.
- **Memory Safety:** Ensures isolation of memory between requests.
- **Execution Timeout:** Configurable timeout to prevent runaway executions.
- **Global Usings:** Automatically provides global using directives for configured packages.

## Error Handling

The API responds with JSON in the following format:

```json
{
    "status": "Success|CompilationError|RuntimeError|TimeoutError|Error",
    "output": "Execution output or error message"
}
```
