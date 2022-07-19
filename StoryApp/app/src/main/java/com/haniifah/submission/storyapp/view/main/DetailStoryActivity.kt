package com.haniifah.submission.storyapp.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.bumptech.glide.Glide
import com.haniifah.submission.storyapp.databinding.ActivityDetailStoryBinding
import com.haniifah.submission.storyapp.model.Story

class DetailStoryActivity : AppCompatActivity() {

    companion object {
        const val DETAIL_STORY = "detail_story"
    }

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<Story>(DETAIL_STORY) as Story
        Glide.with(this)
            .load(story.photo)
            .into(binding.imageStoryDetail)
        binding.tvUsernameDetail.text = story.user
        binding.tvDescriptionDetail.text = story.description
        binding.apply {
            if (supportActionBar != null) {
                (supportActionBar as ActionBar).title = story.user
            }
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}