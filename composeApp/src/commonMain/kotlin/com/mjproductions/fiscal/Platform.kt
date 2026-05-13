package com.mjproductions.fiscal

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform