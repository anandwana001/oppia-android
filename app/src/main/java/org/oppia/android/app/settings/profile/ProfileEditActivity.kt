package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY = "ProfileEditActivity.profile_edit_profile_id"
const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.is_multipane"
const val IS_PROFILE_DELETION_DIALOG_VISIBLE_KEY =
  "ProfileEditActivity.is_profile_deletion_dialog_visible"

/** Activity that allows user to edit a profile. */
class ProfileEditActivity : InjectableFragment() {
  @Inject
  lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter

  private var profileListListener: ProfileListListener? = null

  companion object {
    fun createProfileEditActivity(
      context: Context,
      profileId: Int,
      isMultipane: Boolean = false
    ): ProfileEditActivity {
      val args = Bundle()
      args.putInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, profileId)
      args.putBoolean(IS_MULTIPANE_EXTRA_KEY, isMultipane)
      val fragment = ProfileEditActivity()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
    if (context is ProfileListActivity)
      profileListListener = context
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected variables to be passed to ProfileListFragment"
    }
    val isMultipane = args.getBoolean(IS_MULTIPANE_EXTRA_KEY)
    val internalProfileId = args.getInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, -1)
    return profileEditActivityPresenter.handleOnCreate(
      inflater,
      container,
      isMultipane,
      internalProfileId,
      profileListListener
    )
  }

  /*override fun onSupportNavigateUp(): Boolean {
    val isMultipane = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
    return false
  }

  override fun onBackPressed() {
    val isMultipane = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      val intent = Intent(this, ProfileListActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    }
  }*/

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    profileEditActivityPresenter.handleOnSaveInstanceState(outState)
  }
}
