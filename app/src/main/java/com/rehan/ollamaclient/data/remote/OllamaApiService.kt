package com.rehan.ollamaclient.data.remote

import com.rehan.ollamaclient.data.remote.model.OllamaDeleteRequest
import com.rehan.ollamaclient.data.remote.model.OllamaTagsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OllamaApiService {
    @GET("api/tags")
    suspend fun getTags(): OllamaTagsResponse

    @POST("api/delete")
    suspend fun deleteModel(@Body request: OllamaDeleteRequest)
}
