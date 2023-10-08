package com.example.lintrulelibrary.utils

object BaseUtils {
    fun isTitleCase(string: String): Boolean {
        return string.first().isUpperCase()
    }
}