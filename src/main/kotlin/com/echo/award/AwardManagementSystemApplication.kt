package com.echo.award

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class AwardManagementSystemApplication

fun main(args: Array<String>) {
    runApplication<AwardManagementSystemApplication>(*args)
}
