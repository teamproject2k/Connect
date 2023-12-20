package com.example.connect.domain.network_request_response

data class ResponseState<out T>(val status: RequestStatusEnum, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): ResponseState<T> {
            return ResponseState(RequestStatusEnum.Success, data, null)
        }

        fun <T> loading(data: T? = null): ResponseState<T> {
            return ResponseState(RequestStatusEnum.Loading, data, null)
        }

        fun <T> error(msg: String, data: T? = null): ResponseState<T> {
            return ResponseState(RequestStatusEnum.Exception, data, msg)
        }

        fun <T> none(): ResponseState<T> {
            return ResponseState(RequestStatusEnum.None, null, null)
        }
    }
}