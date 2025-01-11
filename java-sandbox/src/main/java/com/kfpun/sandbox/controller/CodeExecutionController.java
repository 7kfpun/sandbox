package com.kfpun.sandbox.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.tools.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api")
public class CodeExecutionController {

    private static final String BUILD_DIR = "build/sandbox";
    private static final String DEPS_DIR = "build/dependencies";

    @Value("${EXECUTION_TIMEOUT:30}")
    private int executionTimeout;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(@RequestBody CodeRequest request) {
        try {
            File buildDir = new File(BUILD_DIR);
            buildDir.mkdirs();

            File depsDir = new File(DEPS_DIR);
            if (!depsDir.exists()) {
                Process process = Runtime.getRuntime().exec("./gradlew copyDependencies");
                process.waitFor();
            }

            String packageName = extractPackageName(request.getCode());
            File sourceDir = createPackageDirectories(buildDir, packageName);

            String className = extractClassName(request.getCode());
            File sourceFile = createSourceFile(sourceDir, className, request.getCode());

            String classpath = buildClasspath();

            try {
                boolean compilationSuccess = compileCode(sourceFile, classpath);
            } catch (CompilationException e) {
                return ResponseEntity.badRequest()
                        .body(new ExecutionResponse("Compilation failed", e.getMessage()));
            }

            String output = executeCompiledCode(buildDir, packageName, className, classpath);

            return ResponseEntity.ok(new ExecutionResponse("Success", output));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ExecutionResponse("Error: " + e.getMessage(), null));
        }
    }

    private String extractPackageName(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("package ")) {
                return line.trim().replace("package ", "").replace(";", "").trim();
            }
        }
        return "";
    }

    private String extractClassName(String code) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.trim().contains("class ")) {
                String[] parts = line.trim().split("\\s+");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("class") && i + 1 < parts.length) {
                        return parts[i + 1];
                    }
                }
            }
        }
        return "Main";
    }

    private File createPackageDirectories(File buildDir, String packageName) {
        if (packageName.isEmpty()) {
            return buildDir;
        }
        File sourceDir = new File(buildDir, "src/main/java/" + packageName.replace('.', '/'));
        sourceDir.mkdirs();
        return sourceDir;
    }

    private File createSourceFile(File sourceDir, String className, String code) throws IOException {
        File sourceFile = new File(sourceDir, className + ".java");
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(code);
        }
        return sourceFile;
    }

    private String buildClasspath() {
        File depsDir = new File(DEPS_DIR);
        StringBuilder classpath = new StringBuilder();
        if (depsDir.exists() && depsDir.isDirectory()) {
            File[] files = depsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null) {
                for (File file : files) {
                    if (classpath.length() > 0) {
                        classpath.append(File.pathSeparator);
                    }
                    classpath.append(file.getAbsolutePath());
                }
            }
        }
        return classpath.toString();
    }

    private boolean compileCode(File sourceFile, String classpath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));

        List<String> options = Arrays.asList(
                "-classpath", classpath,
                "-d", BUILD_DIR);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits);

        boolean success = task.call();

        StringBuilder errorMessage = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            errorMessage.append(String.format("Line %d: %s\n",
                    diagnostic.getLineNumber(),
                    diagnostic.getMessage(null)));
        }

        try {
            fileManager.close();
        } catch (IOException e) {
            errorMessage.append("Error closing file manager: ").append(e.getMessage());
        }

        if (!success) {
            throw new CompilationException(errorMessage.toString());
        }

        return success;
    }

    private String executeCompiledCode(File buildDir, String packageName, String className, String classpath)
            throws Exception {
        String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "-cp",
                    buildDir.getAbsolutePath() + File.pathSeparator + classpath,
                    fullClassName);
            Process process = pb.start();

            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            outputGobbler.start();
            errorGobbler.start();

            process.waitFor();

            return outputGobbler.getOutput() + errorGobbler.getOutput();
        });

        try {
            return future.get(executionTimeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new Exception("Execution timed out after " + executionTimeout + " seconds");
        } finally {
            executor.shutdownNow();
        }
    }

    private static class StreamGobbler extends Thread {
        private final InputStream is;
        private final StringBuilder output = new StringBuilder();

        StreamGobbler(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String getOutput() {
            return output.toString();
        }
    }

    private static class CompilationException extends RuntimeException {
        public CompilationException(String message) {
            super(message);
        }
    }
}

class CodeRequest {
    private String code;

    public CodeRequest() {
    }

    public CodeRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

class ExecutionResponse {
    private String status;
    private String output;

    public ExecutionResponse(String status, String output) {
        this.status = status;
        this.output = output;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
