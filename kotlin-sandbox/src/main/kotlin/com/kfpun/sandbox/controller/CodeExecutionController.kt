package com.kfpun.sandbox.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.*
import java.util.concurrent.*

@RestController
@RequestMapping("/api")
class CodeExecutionController {
   companion object {
       private const val BUILD_DIR = "build/sandbox"
       private const val DEPS_DIR = "build/dependencies"
   }

   @Value("\${KOTLIN_EXECUTION_TIMEOUT:30}")
   private var executionTimeout: Int = 30

   data class CodeRequest(val code: String)
   data class ExecutionResponse(val status: String, val output: String?)

   @PostMapping("/execute") 
   fun executeCode(@RequestBody request: CodeRequest): ResponseEntity<ExecutionResponse> {
       return try {
           val buildDir = File(BUILD_DIR).apply { mkdirs() }
           
           val depsDir = File(DEPS_DIR)
           if (!depsDir.exists()) {
               Runtime.getRuntime().exec("./gradlew copyDependencies").waitFor()
           }

           val sourceFile = File(buildDir, "Main.kt")
           sourceFile.writeText(request.code)
           
           val classpath = buildClasspath()
           
           try {
               compileCode(sourceFile, classpath)
           } catch (e: CompilationException) {
               return ResponseEntity.badRequest()
                   .body(ExecutionResponse("Compilation failed", e.message))
           }

           val output = executeCompiledCode(buildDir, classpath)
           ResponseEntity.ok(ExecutionResponse("Success", output))

       } catch (e: Exception) {
           ResponseEntity.badRequest()
               .body(ExecutionResponse("Error: ${e.message}", null))
       }
   }

   private fun buildClasspath(): String {
       return File(DEPS_DIR)
           .takeIf { it.exists() && it.isDirectory }
           ?.listFiles { _, name -> name.endsWith(".jar") }
           ?.joinToString(File.pathSeparator) { it.absolutePath }
           ?: ""
   }

   private fun compileCode(sourceFile: File, classpath: String): Boolean {
       val compileProcess = ProcessBuilder(
           "kotlinc",
           sourceFile.absolutePath, 
           "-d", BUILD_DIR,
           "-cp", classpath
       ).redirectErrorStream(true)
        .start()

       if (!compileProcess.waitFor(executionTimeout.toLong(), TimeUnit.SECONDS)) {
           throw TimeoutException("Compilation timed out")
       }

       val output = compileProcess.inputStream.bufferedReader().readText()
       
       if (compileProcess.exitValue() != 0) {
           throw CompilationException(output)
       }

       return true
   }

   private fun executeCompiledCode(buildDir: File, classpath: String): String {
       val executor = Executors.newSingleThreadExecutor()

       return try {
           val future = executor.submit(Callable {
               val process = ProcessBuilder(
                   "kotlin",
                   "-cp", 
                   "${buildDir.absolutePath}${File.pathSeparator}$classpath",
                   "MainKt"
               ).redirectErrorStream(true)
                .start()

               val output = process.inputStream.bufferedReader().readText()

               if (!process.waitFor(executionTimeout.toLong(), TimeUnit.SECONDS)) {
                   throw TimeoutException("Execution timed out")
               }

               if (process.exitValue() != 0) {
                   throw RuntimeException(output)
               }

               output
           })

           future.get(executionTimeout.toLong(), TimeUnit.SECONDS)
       } catch (e: TimeoutException) {
           throw Exception("Execution timed out after $executionTimeout seconds")
       } finally {
           executor.shutdownNow()
       }
   }

   private class CompilationException(message: String) : RuntimeException(message)
}
