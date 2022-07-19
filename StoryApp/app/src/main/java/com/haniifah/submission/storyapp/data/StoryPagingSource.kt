package com.haniifah.submission.storyapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.haniifah.submission.storyapp.api.ApiService
import com.haniifah.submission.storyapp.di.CompanionObject
import com.haniifah.submission.storyapp.di.CompanionObject.INITIAL_PAGE_INDEX
import com.haniifah.submission.storyapp.model.ListStoryItem

class StoryPagingSource (private val apiService: ApiService) : PagingSource<Int, ListStoryItem>() {

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getStories(
                CompanionObject.token,
                page,
                params.loadSize)
            val data = responseData.listStory
            LoadResult.Page(
                data = data,
                prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}