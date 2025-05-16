using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using DotnetSandbox.Models;

namespace DotnetSandbox.Controllers
{
    [ApiController]
    [Route("api")]
    public class CodeExecutionController : ControllerBase
    {
        private static readonly string BuildDir = Path.Combine(Directory.GetCurrentDirectory(), "build", "sandbox");
        private static readonly int ExecutionTimeout = int.Parse(Environment.GetEnvironmentVariable("EXECUTION_TIMEOUT") ?? "30");

        [HttpPost("execute")]
        public async Task<IActionResult> Execute([FromBody] CodeRequest request)
        {
            try
            {
                Directory.CreateDirectory(BuildDir);
                var projectDir = Path.Combine(BuildDir, Guid.NewGuid().ToString());
                Directory.CreateDirectory(projectDir);

                // Write the project file with a reference to Newtonsoft.Json
                await System.IO.File.WriteAllTextAsync(
                    Path.Combine(projectDir, "Program.csproj"),
                    CreateProjectFile()
                );

                // Write the source file as provided by the user
                await System.IO.File.WriteAllTextAsync(
                    Path.Combine(projectDir, "Program.cs"),
                    CreateSourceFile(request.Code)
                );

                var result = await CompileAndRun(projectDir);

                try
                {
                    Directory.Delete(projectDir, true);
                }
                catch { } // Ignore cleanup errors

                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new ExecutionResponse("Error", ex.Message));
            }
        }

        private async Task<ExecutionResponse> CompileAndRun(string projectDir)
        {
            var compileInfo = new ProcessStartInfo
            {
                FileName = "dotnet",
                Arguments = $"build -o {projectDir}/bin",
                WorkingDirectory = projectDir,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false
            };

            using (var compile = Process.Start(compileInfo))
            {
                if (compile == null)
                    return new ExecutionResponse("Error", "Compilation process failed to start");

                string compileOutput = await compile.StandardOutput.ReadToEndAsync();
                string compileError = await compile.StandardError.ReadToEndAsync();
                await compile.WaitForExitAsync();

                if (compile.ExitCode != 0)
                {
                    Console.WriteLine($"Debug - Full compile error: {compileError}");
                    var errorLines = compileOutput.Split('\n')
                        .Concat(compileError.Split('\n'))
                        .Where(line => !string.IsNullOrWhiteSpace(line))
                        .ToList();

                    return new ExecutionResponse("CompilationError",
                        errorLines.Any() ? string.Join("\n", errorLines) : "Unknown compilation error");
                }
            }

            var runInfo = new ProcessStartInfo
            {
                FileName = "dotnet",
                Arguments = $"{projectDir}/bin/Program.dll",
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                UseShellExecute = false
            };

            using var process = Process.Start(runInfo);
            if (process == null)
                return new ExecutionResponse("Error", "Runtime process failed to start");

            var outputTask = process.StandardOutput.ReadToEndAsync();
            var errorTask = process.StandardError.ReadToEndAsync();

            if (!process.WaitForExit(ExecutionTimeout * 1000))
            {
                process.Kill();
                return new ExecutionResponse("TimeoutError", $"Execution exceeded {ExecutionTimeout} seconds timeout");
            }

            var output = await outputTask;
            var error = await errorTask;

            return process.ExitCode == 0
                ? new ExecutionResponse("Success", output)
                : new ExecutionResponse("RuntimeError", error);
        }

        private string CreateProjectFile()
        {
            // Read package references from an environment variable.
            // Expected format: "Newtonsoft.Json:13.0.1,Another.Package:1.2.3"
            string packagesEnv = Environment.GetEnvironmentVariable("PREINSTALL_LIBS");
            string packageReferences = "";

            if (!string.IsNullOrEmpty(packagesEnv))
            {
                var packageEntries = packagesEnv.Split(',', StringSplitOptions.RemoveEmptyEntries);
                foreach (var entry in packageEntries)
                {
                    var parts = entry.Split(':', StringSplitOptions.RemoveEmptyEntries);
                    if (parts.Length == 2)
                    {
                        var packageName = parts[0].Trim();
                        var packageVersion = parts[1].Trim();
                        packageReferences += $"    <PackageReference Include=\"{packageName}\" Version=\"{packageVersion}\" />\n";
                    }
                }
            }

            // Construct the project file.
            return $@"<Project Sdk=""Microsoft.NET.Sdk"">
<PropertyGroup>
    <OutputType>Exe</OutputType>
    <TargetFramework>net8.0</TargetFramework>
    <ImplicitUsings>enable</ImplicitUsings>
    <Nullable>enable</Nullable>
</PropertyGroup>
<ItemGroup>
{packageReferences}
</ItemGroup>
</Project>";
        }


        private string CreateSourceFile(string code) =>
            // Simply return the full source file as provided by the user.
            code;
    }
}
