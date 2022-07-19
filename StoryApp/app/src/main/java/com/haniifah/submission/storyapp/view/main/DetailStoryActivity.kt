package com.haniifah.submission.storyapp.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.haniifah.submission.storyapp.databinding.ActivityDetailStoryBinding
import com.haniifah.submission.storyapp.model.ListStoryItem

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<ListStoryItem>("Story") as ListStoryItem
        Glide.with(applicationContext)
            .load(story.photoUrl)
            .apply(RequestOptions())
            .into(binding.imageStoryDetail)
        binding.tvUsernameDetail.text = story.name
        binding.tvDescriptionDetail.text = story.description

        binding.apply {
            if (supportActionBar != null) {
                (supportActionBar as ActionBar).title = story.name
            }
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}