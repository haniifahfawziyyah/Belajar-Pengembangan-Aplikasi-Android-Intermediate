package com.haniifah.submission.storyapp.api

import com.haniifah.submission.storyapp.model.FileUploadResponse
import com.haniifah.submission.storyapp.model.LoginResponse
import com.haniifah.submission.storyapp.model.SignupResponse
import com.haniifah.submission.storyapp.model.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun createAccount(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ) : Call<SignupResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ) : Call<LoginResponse>

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int? = 0,
        @Query("size") size: Int? = 10,
    ) : StoriesResponse

    @GET("stories")
    fun getStoriesLoc (
        @Header("Authorization") authToken: String,
        @Query("location") location: Int = 1,
    ): Call<StoriesResponse>

    @Multipart
    @POST("stories")
    fun uploadImage(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<FileUploadResponse>
}