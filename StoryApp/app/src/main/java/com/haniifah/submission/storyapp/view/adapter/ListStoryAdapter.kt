package com.haniifah.submission.storyapp.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.haniifah.submission.storyapp.databinding.ItemRowStoryBinding
import com.haniifah.submission.storyapp.model.ListStoryItem
import com.haniifah.submission.storyapp.view.main.DetailStoryActivity

class ListStoryAdapter : PagingDataAdapter<ListStoryItem, ListStoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {
    private var itemClickCallback: OnItemClickCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoryAdapter.StoryViewHolder {
        val binding = ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    inner class StoryViewHolder(private val binding: ItemRowStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem) {
            binding.root.setOnClickListener {
                itemClickCallback?.itemClicked(story)
            }
            binding.apply {
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .apply(RequestOptions())
                    .into(imageStory)
                tvUsername.text = story.name
                tvDescription.text = story.description

                itemView.setOnClickListener{
                    val intent = Intent(itemView.context, DetailStoryActivity::class.java)
                    intent.putExtra("Story", story)

                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(imageStory, "img_story_detail_transition"),
                            Pair(tvUsername, "tv_username_detail_transition"),
                            Pair(tvDescription, "tv_description_detail_transition"),
                        )
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }

    fun setOnItemClickCallback(ItemClickCallback: OnItemClickCallback) {
        this.itemClickCallback = ItemClickCallback
    }

    interface OnItemClickCallback {
        fun itemClicked(story: ListStoryItem)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean { return oldItem.id == newItem.id }
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean { return oldItem == newItem }
        }
    }
}