package org.oppia.android.app.mydownloads.downloads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.TopicActivity
import org.oppia.android.databinding.DownloadsFragmentBinding
import org.oppia.android.databinding.DownloadsSortbyBinding
import org.oppia.android.databinding.DownloadsTopicCardBinding
import javax.inject.Inject

/** The presenter for [DownloadsFragment]. */
@FragmentScope
class DownloadsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {

  private var internalProfileId: Int = -1
  private val sortByListIndexListener = fragment as SortByListIndexListener

  @Inject
  lateinit var downloadsViewModel: DownloadsViewModel
  private lateinit var downloadsRecyclerViewAdapter: BindableAdapter<DownloadsItemViewModel>
  private lateinit var binding: DownloadsFragmentBinding
  private var previousSortTypeIndex: Int = 0

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    previousSortTypeIndex: Int
  ): View {
    binding = DownloadsFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.previousSortTypeIndex = previousSortTypeIndex
    this.internalProfileId = internalProfileId

    binding.apply {
      this.lifecycleOwner = fragment
      this.viewModel = downloadsViewModel
    }

    binding.downloadsRecyclerView.apply {
      downloadsRecyclerViewAdapter = createRecyclerViewAdapter()
      adapter = downloadsRecyclerViewAdapter
    }
    downloadsViewModel.setInternalProfileId(internalProfileId)
    downloadsViewModel.setSortTypeIndex(previousSortTypeIndex)

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<DownloadsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<DownloadsItemViewModel, ViewType> { downloadsItemViewModel ->
        when (downloadsItemViewModel) {
          is DownloadsSortByViewModel -> ViewType.VIEW_TYPE_SORT_BY
          is DownloadsTopicViewModel -> ViewType.VIEW_TYPE_TOPIC
          else -> throw IllegalArgumentException(
            "Encountered unexpected view model: $downloadsItemViewModel"
          )
        }
      }.registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SORT_BY,
        inflateDataBinding = DownloadsSortbyBinding::inflate,
        setViewModel = this::bindDownloadsSortBy,
        transformViewModel = { it as DownloadsSortByViewModel }
      ).registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TOPIC,
        inflateDataBinding = DownloadsTopicCardBinding::inflate,
        setViewModel = this::bindDownloadsTopicCard,
        transformViewModel = { it as DownloadsTopicViewModel }
      ).build()
  }

  private enum class ViewType {
    VIEW_TYPE_SORT_BY,
    VIEW_TYPE_TOPIC
  }

  private fun bindDownloadsSortBy(
    binding: DownloadsSortbyBinding,
    viewModel: DownloadsSortByViewModel
  ) {
    binding.viewModel = viewModel
    val sortByItemsList = mutableListOf<String>()
    enumValues<SortByItems>().forEach {
      sortByItemsList.add(fragment.getString(it.value))
    }
    val sortItemAdapter = ArrayAdapter(
      fragment.requireContext(),
      R.layout.downloads_sortby_menu,
      sortByItemsList
    )
    binding.sortByMenu.inputType = 0
    binding.sortByMenu.setText(
      sortItemAdapter.getItem(previousSortTypeIndex).toString(),
      /* filter= */ false
    )
    binding.sortByMenu.setAdapter(sortItemAdapter)

    binding.sortByMenu.setOnItemClickListener { parent, _, position, _ ->
      if (previousSortTypeIndex != position) {
        val sortedDownloadsItemViewModel: MutableList<DownloadsItemViewModel> =
          when (SortByItems.values()[position]) {
            SortByItems.NEWEST -> {
              // TODO(#552): update it with the time stamp value in the list
              sortTopicDownloadSize()
            }
            SortByItems.ALPHABETICAL -> {
              sortTopicAlphabetically()
            }
            SortByItems.DOWNLOAD_SIZE -> {
              sortTopicDownloadSize()
            }
          }
        previousSortTypeIndex = position
        sortByListIndexListener.onSortByItemClicked(previousSortTypeIndex)
        binding.sortByMenu.setText(
          sortItemAdapter.getItem(position).toString(),
          /* filter= */ false
        )
        downloadsRecyclerViewAdapter.setData(sortedDownloadsItemViewModel)
      }
    }
  }

  private fun sortTopicAlphabetically(): MutableList<DownloadsItemViewModel> {
    val sortedTopicNameList = mutableListOf<String>()
    val downloadsItemViewModelList = downloadsViewModel.downloadsViewModelLiveData.value
    downloadsItemViewModelList?.forEach { downloadsItemViewModel ->
      if (downloadsItemViewModel is DownloadsTopicViewModel) {
        sortedTopicNameList.add(downloadsItemViewModel.topicSummary.name)
      }
    }
    sortedTopicNameList.sort()

    val sortedDownloadsItemViewModel = mutableListOf<DownloadsItemViewModel>()
    sortedDownloadsItemViewModel.add(downloadsItemViewModelList!![0])
    sortedTopicNameList.forEach { topicName ->
      downloadsItemViewModelList.forEach { downloadsItemViewModel ->
        if (downloadsItemViewModel is DownloadsTopicViewModel &&
          topicName == downloadsItemViewModel.topicSummary.name
        ) {
          sortedDownloadsItemViewModel.add(downloadsItemViewModel)
        }
      }
    }
    return sortedDownloadsItemViewModel
  }

  private fun sortTopicDownloadSize(): MutableList<DownloadsItemViewModel> {
    val sortedTopicSizeList = mutableListOf<Long>()
    val downloadsItemViewModelList = downloadsViewModel.downloadsViewModelLiveData.value
    downloadsItemViewModelList?.forEach { downloadsItemViewModel ->
      if (downloadsItemViewModel is DownloadsTopicViewModel) {
        sortedTopicSizeList.add(downloadsItemViewModel.topicSummary.diskSizeBytes)
      }
    }
    sortedTopicSizeList.sort()

    val sortedDownloadsItemViewModel = mutableListOf<DownloadsItemViewModel>()
    sortedDownloadsItemViewModel.add(downloadsItemViewModelList!![0])
    sortedTopicSizeList.forEach { topicSize ->
      downloadsItemViewModelList.forEach { downloadsItemViewModel ->
        if (downloadsItemViewModel is DownloadsTopicViewModel &&
          topicSize == downloadsItemViewModel.topicSummary.diskSizeBytes
        ) {
          sortedDownloadsItemViewModel.add(downloadsItemViewModel)
        }
      }
    }
    return sortedDownloadsItemViewModel
  }

  private fun bindDownloadsTopicCard(
    binding: DownloadsTopicCardBinding,
    viewModel: DownloadsTopicViewModel
  ) {
    binding.viewModel = viewModel

    var isDeleteExpanded = false
    binding.isDeleteExpanded = isDeleteExpanded

    binding.expandListIcon.setOnClickListener {
      isDeleteExpanded = !isDeleteExpanded
      binding.isDeleteExpanded = isDeleteExpanded
    }

    binding.deleteImageView.setOnClickListener {
      downloadsViewModel.profileLiveData.observe(
        fragment,
        Observer { profile ->
          when {
            profile.allowDownloadAccess -> {
              openDownloadTopicDeleteDialog(profile.allowDownloadAccess)
            }
            else -> {
              openAdminPinInputDialog(profile.allowDownloadAccess)
            }
          }
        }
      )
    }
  }

  private fun openAdminPinInputDialog(allowDownloadAccess: Boolean) {
    val adminPin = downloadsViewModel.adminPin
    val dialogFragment = DownloadsAccessDialogFragment
      .newInstance(adminPin, allowDownloadAccess)
    dialogFragment.showNow(
      fragment.childFragmentManager,
      DownloadsFragment.ADMIN_PIN_CONFIRMATION_DIALOG_TAG
    )
  }

  fun startTopicActivity(internalProfileId: Int, topicId: String) {
    activity.startActivity(
      TopicActivity.createTopicActivityIntent(
        activity,
        internalProfileId,
        topicId
      )
    )
  }

  fun openDownloadTopicDeleteDialog(allowDownloadAccess: Boolean) {
    val dialogFragment = DownloadsTopicDeleteDialogFragment
      .newInstance(internalProfileId, allowDownloadAccess)
    dialogFragment.showNow(
      fragment.childFragmentManager,
      DownloadsFragment.DELETE_DOWNLOAD_TOPIC_DIALOG_TAG
    )
  }
}
