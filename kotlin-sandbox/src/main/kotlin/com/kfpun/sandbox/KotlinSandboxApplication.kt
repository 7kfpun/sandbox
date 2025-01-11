package com.kfpun.sandbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinSandboxApplication

fun main(args: Array<String>) {
    runApplication<KotlinSandboxApplication>(*args)
}
