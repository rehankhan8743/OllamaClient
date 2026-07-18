package com.rehan.ollamaclient.data.remote

import com.rehan.ollamaclient.data.remote.model.OpenAIModelResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface OpenAICompatService {
    @GET("models")
    @Headers("Content-Type: application/json")
    suspend fun listModels(
        @Header("Authorization") auth: String? = null
    ): OpenAIModelResponse
}
