package com.example.connect.common

data class ResponseState<out T>(val status: RequestStatusEnum, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): ResponseState<T> {
            return ResponseState(RequestStatusEnum.SUCCESS, data, null)
        }

        fun <T> loading(data: T? = null): ResponseState<T> {
            return ResponseState(RequestStatusEnum.LOADING, data, null)
        }

        fun <T> error(msg: String, data: T? = null): ResponseState<T> {
            return ResponseState(RequestStatusEnum.EXCEPTION, data, msg)
        }

        fun <T> none(): ResponseState<T> {
            return ResponseState(RequestStatusEnum.NONE, null, null)
        }
    }
}