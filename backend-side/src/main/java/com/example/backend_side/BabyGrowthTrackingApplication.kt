package com.example.backend_side

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class BabyGrowthTrackingApplication

fun main(args: Array<String>) {
    runApplication<BabyGrowthTrackingApplication>(*args)
}