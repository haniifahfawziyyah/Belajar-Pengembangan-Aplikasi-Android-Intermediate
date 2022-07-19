package com.haniifah.submission.storyapp.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.haniifah.submission.storyapp.di.Injection
import com.haniifah.submission.storyapp.model.UserPreference
import com.haniifah.submission.storyapp.view.camera.CreateStoryViewModel
import com.haniifah.submission.storyapp.view.login.LoginViewModel
import com.haniifah.submission.storyapp.view.main.MainViewModel
import com.haniifah.submission.storyapp.view.main.StoryViewModel

class ViewModelFactory(private val pref: UserPreference) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref) as T
            }
            modelClass.isAssignableFrom(StoryViewModel::class.java) -> {
                StoryViewModel(Injection.provideRepository()) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(CreateStoryViewModel::class.java) -> {
                CreateStoryViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}