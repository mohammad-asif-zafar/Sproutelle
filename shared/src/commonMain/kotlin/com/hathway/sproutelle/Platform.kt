package com.hathway.sproutelle

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform