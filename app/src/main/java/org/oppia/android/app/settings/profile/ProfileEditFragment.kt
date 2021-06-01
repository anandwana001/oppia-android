package org.oppia.android.app.settings.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** Fragment that contains Profile Edit Screen. */
class ProfileEditFragment : InjectableFragment() {
  @Inject
  lateinit var profileEditFragmentPresenter: ProfileEditFragmentPresenter

  companion object {
    fun newInstance(
      isMultipane: Boolean = false,
      internalProfileId: Int
    ): ProfileEditFragment {
      val args = Bundle()
      args.putBoolean(IS_MULTIPANE_EXTRA_KEY, isMultipane)
      args.putInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, internalProfileId)
      val fragment = ProfileEditFragment()
      fragment.arguments = args
      return fragment
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected variables to be passed to ProfileEditFragment"
    }
    val isMultipane = args.getBoolean(IS_MULTIPANE_EXTRA_KEY)
    val internalProfileId = args.getInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY)
    return profileEditFragmentPresenter.handleOnCreateView(
      inflater,
      container,
      isMultipane,
      internalProfileId
    )
  }
}
