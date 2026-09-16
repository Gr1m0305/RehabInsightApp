package com.example.rehabinsight.data.network

object ServerConfig {

    const val DEFAULT_BASE_URL: String = "http://10.0.2.2:8000"

    @Volatile
    private var overrideBaseUrl: String? = null

    var baseUrl: String
        get() = overrideBaseUrl ?: DEFAULT_BASE_URL
        set(value) {
            overrideBaseUrl = value.trimEnd('/')
        }

    fun resetToDefault() {
        overrideBaseUrl = null
    }

    fun url(path: String): String {
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$baseUrl$cleanPath"
    }
}
