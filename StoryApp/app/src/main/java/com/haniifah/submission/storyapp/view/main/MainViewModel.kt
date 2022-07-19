package com.haniifah.submission.storyapp.view.main

import android.content.ContentValues
import android.os.Build
import android.service.controls.ControlsProviderService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.haniifah.submission.storyapp.api.ApiConfig
import com.haniifah.submission.storyapp.model.ListStoryItem
import com.haniifah.submission.storyapp.model.StoriesResponse
import com.haniifah.submission.storyapp.model.User
import com.haniifah.submission.storyapp.model.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference) : ViewModel() {

    val listStory = MutableLiveData<List<ListStoryItem>>()

    fun getUser(): LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun getStory(): LiveData<List<ListStoryItem>> { return listStory }
    fun setListStory(authToken: String) {
        val client = ApiConfig.getApiService().getStoriesLoc(authToken)
        client.enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(call: Call<StoriesResponse>, response: Response<StoriesResponse>) {
                if (response.isSuccessful) {
                    listStory.postValue(response.body()?.listStory)
                } else {
                    Log.e(ContentValues.TAG, "onFailure: ${response.message()}")
                }
            }

            @RequiresApi(Build.VERSION_CODES.R)
            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                Log.e(ControlsProviderService.TAG, "Failure: ${t.message}")
            }
        })
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }

}