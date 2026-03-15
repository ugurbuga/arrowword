package com.ugurbuga.arrowword

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform