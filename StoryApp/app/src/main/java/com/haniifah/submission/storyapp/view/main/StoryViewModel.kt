package com.haniifah.submission.storyapp.view.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.haniifah.submission.storyapp.data.ListStoryRepository
import com.haniifah.submission.storyapp.di.Injection
import com.haniifah.submission.storyapp.model.ListStoryItem

class StoryViewModel(private val repository: ListStoryRepository) : ViewModel() {
    fun stories(): LiveData<PagingData<ListStoryItem>> = repository.getAllStory().cachedIn(viewModelScope)
}

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> { StoryViewModel(Injection.provideRepository()) as T }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
