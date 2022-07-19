package com.haniifah.submission.storyapp.data

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.haniifah.submission.storyapp.api.ApiService
import com.haniifah.submission.storyapp.model.ListStoryItem

class ListStoryRepository(private val apiService: ApiService) {
    fun getAllStory(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { StoryPagingSource(apiService) }
        ).liveData
    }
}