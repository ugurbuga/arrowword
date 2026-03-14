package com.ugurbuga.arrowword.domain.usecase

class GenerateRandomLevelIdUseCase {
    fun generate(): String {
        return "generated-" + System.currentTimeMillis().toString()
    }
}
