package com.teamproject2k.lintrulelibrary.utils

object BaseUtils {
    fun isTitleCase(string: String): Boolean {
        return string.first().isUpperCase()
    }
}