package com.haniifah.submission.storyapp.di

import com.haniifah.submission.storyapp.api.ApiConfig
import com.haniifah.submission.storyapp.data.ListStoryRepository

object Injection {
    fun provideRepository(): ListStoryRepository {
        val apiService = ApiConfig.getApiService()
        return ListStoryRepository(apiService)
    }
}