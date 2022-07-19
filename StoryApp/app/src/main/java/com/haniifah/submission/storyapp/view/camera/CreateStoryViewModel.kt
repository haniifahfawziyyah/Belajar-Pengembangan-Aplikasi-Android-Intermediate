package com.haniifah.submission.storyapp.view.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.haniifah.submission.storyapp.model.User
import com.haniifah.submission.storyapp.model.UserPreference

class CreateStoryViewModel(private val pref: UserPreference) : ViewModel() {
    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }
}