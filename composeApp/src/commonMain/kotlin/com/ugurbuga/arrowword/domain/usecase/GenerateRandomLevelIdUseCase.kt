package com.ugurbuga.arrowword.domain.usecase

import kotlin.random.Random

class GenerateRandomLevelIdUseCase {
    fun generate(): String {
        val suffix = Random.nextLong().toString().replace("-", "")
        return "generated-" + suffix
    }
}
