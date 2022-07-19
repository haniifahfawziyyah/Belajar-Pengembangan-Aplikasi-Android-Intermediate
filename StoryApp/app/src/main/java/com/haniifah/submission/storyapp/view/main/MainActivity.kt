package com.haniifah.submission.storyapp.view.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.haniifah.submission.storyapp.R
import com.haniifah.submission.storyapp.databinding.ActivityMainBinding
import com.haniifah.submission.storyapp.di.CompanionObject
import com.haniifah.submission.storyapp.model.ListStoryItem
import com.haniifah.submission.storyapp.model.UserPreference
import com.haniifah.submission.storyapp.view.ViewModelFactory
import com.haniifah.submission.storyapp.view.adapter.ListStoryAdapter
import com.haniifah.submission.storyapp.view.adapter.LoadingStateAdapter
import com.haniifah.submission.storyapp.view.camera.CreateStoryActivity
import com.haniifah.submission.storyapp.view.maps.MapsActivity
import com.haniifah.submission.storyapp.view.welcome.WelcomeActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ListStoryAdapter

    // Map
    private var lStoryMap: ArrayList<LatLng>? = null
    private var lStoryMapName: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setSwipeRefreshLayout()

        if (supportActionBar != null) {
            (supportActionBar as ActionBar).title = "Story"
        }
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // List Story
        adapter = ListStoryAdapter()
        lStoryMap = ArrayList()
        lStoryMapName = ArrayList()
        adapter.setOnItemClickCallback(object : ListStoryAdapter.OnItemClickCallback {
            override fun itemClicked(story: ListStoryItem) {
                Intent(this@MainActivity, DetailStoryActivity::class.java).also {
                    it.putExtra(CompanionObject.EXTRA_PHOTO, story.photoUrl)
                    it.putExtra(CompanionObject.EXTRA_NAME, story.name)
                    it.putExtra(CompanionObject.EXTRA_DESC, story.description)
                    startActivity(it)
                }
            }
        })

        showLoading(true)
        binding.swipeRefresh.isRefreshing = true
        mainViewModel.getStory().observe(this) {
            if (it != null) {
                for (i in it.indices) {
                    lStoryMap!!.add(LatLng(it[i].lat, it[i].lon))
                    lStoryMapName!!.add(it[i].name)
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

    }

    private fun getToken(token: String) {
        binding.apply {
            if (token.isEmpty()) return
            showLoading(true)
            mainViewModel.setListStory(token)
        }
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(this, ViewModelFactory(UserPreference.getInstance(dataStore)))[MainViewModel::class.java]
        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                CompanionObject.token = "Bearer ${user.token}"
                binding.apply {
                    rvStories.layoutManager = LinearLayoutManager(this@MainActivity)
                    rvStories.adapter = adapter.withLoadStateFooter(
                        footer = LoadingStateAdapter {
                            adapter.retry()
                        }
                    )
                    getToken(CompanionObject.token)
                }

                // Paging
                storyViewModel.stories().observe(this) { story ->
                    adapter.submitData(lifecycle, story)
                }

                // Add Story
                binding.fabCreateStory.setOnClickListener { view ->
                    if (view.id == R.id.fab_create_story) {
                        startActivity(
                            Intent(this@MainActivity, CreateStoryActivity::class.java).apply {
                                putExtra(CompanionObject.EXTRA_TOKEN, CompanionObject.token)
                            }
                        )
                    }
                }
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    // Option Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        val list = menu.findItem(R.id.menu_list)
        list.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_maps -> {
                Intent(this@MainActivity, MapsActivity::class.java).also {
                    it.putExtra(CompanionObject.EXTRA_MAP, lStoryMap)
                    it.putExtra(CompanionObject.EXTRA_MAP_NAME, lStoryMapName)
                    startActivity(it)
                }
            }

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

    private fun setSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            setupViewModel()
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

}