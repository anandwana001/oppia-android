package org.oppia.android.app.mydownloads

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

const val INTERNAL_PROFILE_ID_SAVED_KEY = "MyDownloadsActivity.internal_profile_id"
const val IS_ALLOWED_DOWNLOAD_ACCESS_SAVED_KEY = "MyDownloadsActivity.is_allowed_download_access"

/** The presenter for [MyDownloadsActivity]. */
@ActivityScope
class MyDownloadsActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.my_downloads_activity)
    if (getMyDownloadsFragment() == null) {
      val myDownloadsFragment = MyDownloadsFragment()
      val args = Bundle()
      args.putInt(INTERNAL_PROFILE_ID_SAVED_KEY, internalProfileId)
      myDownloadsFragment.arguments = args
      activity.supportFragmentManager.beginTransaction().add(
        R.id.my_downloads_fragment_placeholder,
        myDownloadsFragment
      ).commitNow()
    }
  }

  private fun getMyDownloadsFragment(): MyDownloadsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.my_downloads_fragment_placeholder
      ) as MyDownloadsFragment?
  }
}
