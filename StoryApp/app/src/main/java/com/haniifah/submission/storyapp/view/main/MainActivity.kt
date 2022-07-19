package com.haniifah.submission.storyapp.view.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haniifah.submission.storyapp.R
import com.haniifah.submission.storyapp.api.ApiConfig
import com.haniifah.submission.storyapp.databinding.ActivityMainBinding
import com.haniifah.submission.storyapp.model.ListStoryItem
import com.haniifah.submission.storyapp.model.StoriesResponse
import com.haniifah.submission.storyapp.model.Story
import com.haniifah.submission.storyapp.model.UserPreference
import com.haniifah.submission.storyapp.view.ViewModelFactory
import com.haniifah.submission.storyapp.view.adapter.ListStoryAdapter
import com.haniifah.submission.storyapp.view.camera.CreateStoryActivity
import com.haniifah.submission.storyapp.view.welcome.WelcomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Story Activity"
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "Story"
        }
        supportActionBar?.setDisplayShowTitleEnabled(true)

        setupViewModel()
        setSwipeRefreshLayout()

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        getStories()

        binding.fabCreateStory.setOnClickListener {
            Intent(this, CreateStoryActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_language -> {
                Intent(Settings.ACTION_LOCALE_SETTINGS).also {
                    startActivity(it)
                }
            }
            R.id.menu_logout -> {
                mainViewModel.logout()
                Intent(this, WelcomeActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            when {
                user.isLogin -> {
                }
                else -> {
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun getStories() {
        showLoading(true)
        binding.swipeRefresh.isRefreshing = true

        mainViewModel.getUser().observe(this ) {
            if(it != null) {
                val client = ApiConfig.getApiService().getStories("Bearer " + it.token)
                client.enqueue(object: Callback<StoriesResponse> {
                    override fun onResponse(
                        call: Call<StoriesResponse>,
                        response: Response<StoriesResponse>
                    ) {
                        showLoading(false)
                        binding.apply {
                            swipeRefresh.isRefreshing = false
                        }

                        val responseBody = response.body()
                        Log.d(TAG, "onResponse: $responseBody")
                        if(response.isSuccessful && responseBody?.message == "Stories fetched successfully") {
                            setStoriesData(responseBody.listStory)
                            Toast.makeText(this@MainActivity, getString(R.string.success_load_stories), Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "onFailure1: ${response.message()}")
                            Toast.makeText(this@MainActivity, getString(R.string.fail_load_stories), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                        showLoading(false)
                        binding.apply {
                            swipeRefresh.isRefreshing = false
                        }

                        Log.e(TAG, "onFailure2: ${t.message}")
                        Toast.makeText(this@MainActivity, getString(R.string.fail_load_stories), Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

    }

    private fun setSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            getStories()
            showLoading(false)
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setStoriesData(items: List<ListStoryItem>) {
        val listStories = ArrayList<Story>()
        for(item in items) {
            val story = Story(
                item.name,
                item.photoUrl,
                item.description
            )
            listStories.add(story)
        }

        val adapter = ListStoryAdapter(listStories)
        binding.rvStories.adapter = adapter
    }

}