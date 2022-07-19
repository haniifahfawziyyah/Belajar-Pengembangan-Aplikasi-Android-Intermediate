package com.haniifah.submission.storyapp.view.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haniifah.submission.storyapp.model.User
import com.haniifah.submission.storyapp.model.UserPreference
import kotlinx.coroutines.launch

class SignupViewModel(private val pref: UserPreference) : ViewModel() {
    fun saveUser(user: User) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }
}