package org.oppia.android.app.topic.lessons

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.LessonsChapterViewBinding
import org.oppia.android.databinding.TopicLessonsStorySummaryBinding
import org.oppia.android.databinding.TopicLessonsTitleBinding

// TODO(#216): Make use of generic data-binding-enabled RecyclerView adapter.

private const val VIEW_TYPE_TITLE_TEXT = 1
private const val VIEW_TYPE_STORY_ITEM = 2

/** Adapter to bind StorySummary to [RecyclerView] inside [TopicLessonsFragment]. */
class StorySummaryAdapter(
  private val itemList: MutableList<TopicLessonsItemViewModel>,
  private val expandedChapterListIndexListener: ExpandedChapterListIndexListener,
  private var currentExpandedChapterListIndex: Int?,
  private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      // TODO(#216): Generalize this binding to make adding future items easier.
      VIEW_TYPE_TITLE_TEXT -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicLessonsTitleBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        TopicPlayTitleViewHolder(binding)
      }
      VIEW_TYPE_STORY_ITEM -> {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
          TopicLessonsStorySummaryBinding.inflate(
            inflater,
            parent,
            /* attachToParent= */ false
          )
        StorySummaryViewHolder(binding)
      }
      else -> throw IllegalArgumentException("Invalid view type: $viewType")
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, i: Int) {
    when (holder.itemViewType) {
      VIEW_TYPE_TITLE_TEXT -> {
        (holder as TopicPlayTitleViewHolder).bind(
          itemList.count {
            it is StorySummaryViewModel
          },
          context
        )
      }
      VIEW_TYPE_STORY_ITEM -> {
        (holder as StorySummaryViewHolder).bind(itemList[i] as StorySummaryViewModel, i)
      }
      else -> throw IllegalArgumentException("Invalid item view type: ${holder.itemViewType}")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (itemList[position]) {
      is TopicLessonsTitleViewModel -> {
        VIEW_TYPE_TITLE_TEXT
      }
      is StorySummaryViewModel -> {
        VIEW_TYPE_STORY_ITEM
      }
      else -> throw IllegalArgumentException(
        "Invalid type of data $position with item ${itemList[position]}"
      )
    }
  }

  override fun getItemCount(): Int {
    return itemList.size
  }

  private class TopicPlayTitleViewHolder(
    private val binding: TopicLessonsTitleBinding
  ) : RecyclerView.ViewHolder(binding.root) {
    internal fun bind(size: Int, context: Context) {
      binding.topicPlayTextView.setText(
        context.resources.getQuantityText(
          R.plurals.story_play_heading,
          size
        )
      )
    }
  }

  inner class StorySummaryViewHolder(private val binding: TopicLessonsStorySummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    internal fun bind(storySummaryViewModel: StorySummaryViewModel, position: Int) {
      var isChapterListVisible = false
      if (currentExpandedChapterListIndex != null) {
        isChapterListVisible = currentExpandedChapterListIndex!! == position
      }
      binding.isListExpanded = isChapterListVisible
      binding.viewModel = storySummaryViewModel

      val chapterSummaries = storySummaryViewModel
        .storySummary.chapterList
      val completedChapterCount =
        chapterSummaries.map(ChapterSummary::getChapterPlayState)
          .filter {
            it == ChapterPlayState.COMPLETED
          }
          .size
      val storyPercentage: Int =
        (completedChapterCount * 100) / storySummaryViewModel.storySummary.chapterCount
      binding.storyPercentage = storyPercentage
      binding.storyProgressView.setStoryChapterDetails(
        storySummaryViewModel.storySummary.chapterCount,
        completedChapterCount
      )
      binding.topicPlayStoryDashedLineView.setLayerType(
        View.LAYER_TYPE_SOFTWARE,
        /* paint= */ null
      )
      binding.chapterRecyclerView.adapter = createRecyclerViewAdapter()

      binding.root.setOnClickListener {
        val previousIndex: Int? = currentExpandedChapterListIndex
        currentExpandedChapterListIndex =
          if (currentExpandedChapterListIndex != null &&
            currentExpandedChapterListIndex == position
          ) {
            null
          } else {
            position
          }
        expandedChapterListIndexListener.onExpandListIconClicked(currentExpandedChapterListIndex)
        if (previousIndex != null && currentExpandedChapterListIndex != null &&
          previousIndex == currentExpandedChapterListIndex
        ) {
          notifyItemChanged(currentExpandedChapterListIndex!!)
        } else {
          if (previousIndex != null) {
            notifyItemChanged(previousIndex)
          }
          if (currentExpandedChapterListIndex != null) {
            notifyItemChanged(currentExpandedChapterListIndex!!)
          }
        }
      }
    }

    private fun createRecyclerViewAdapter(): BindableAdapter<ChapterSummaryViewModel> {
      return BindableAdapter.SingleTypeBuilder
        .newBuilder<ChapterSummaryViewModel>()
        .registerViewDataBinderWithSameModelType(
          inflateDataBinding = LessonsChapterViewBinding::inflate,
          setViewModel = LessonsChapterViewBinding::setViewModel
        ).build()
    }
  }
}
