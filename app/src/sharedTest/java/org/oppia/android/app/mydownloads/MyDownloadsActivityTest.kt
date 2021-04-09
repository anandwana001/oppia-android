package org.oppia.android.app.mydownloads

import android.app.Application
import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.EspressoTestsMatchers.matchCurrentTabTitle
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [MyDownloadsActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = MyDownloadsActivityTest.TestApplication::class, qualifiers = "port-xxhdpi")
class MyDownloadsActivityTest {

  @Inject
  lateinit var context: Context

  @get:Rule
  val activityTestRule: ActivityTestRule<MyDownloadsActivity> = ActivityTestRule(
    MyDownloadsActivity::class.java,
    /* initialTouchMode= */ true,
    /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testMyDownloadsActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(
      MyDownloadsActivity.createMyDownloadsActivityIntent(
        context = this.context.applicationContext,
        profileId = 1,
        isFromNavigationDrawer = true
      )
    )
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.my_downloads_activity_title))
  }

  @Test
  fun testMyDownloadsActivity_toolbarTitle_isDisplayedSuccessfully() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          instanceOf(TextView::class.java),
          withParent(withId(R.id.my_downloads_activity_toolbar))
        )
      ).check(
        matches(
          withText(R.string.my_downloads_activity_title)
        )
      )
    }
  }

  @Test
  fun testMyDownloadsActivity_showsMyDownloadsFragmentWithMultipleTabs() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_container)).perform(click())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testMyDownloadsActivity_swipePage_hasSwipedPage() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_viewpager)).perform(swipeLeft())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              1
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsActivity_defaultTabIsDownloads_isSuccessful() {
    launch(MyDownloadsActivity::class.java).use {
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              0
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsActivity_clickOnDownloadsTab_showsDownloadsTabSelected() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          withText(MyDownloadsTab.getTabForPosition(0).name),
          isDescendantOfA(withId(R.id.my_downloads_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              0
            ).name
          )
        )
      )
    }
  }

  @Test
  fun testMyDownloadsActivity_clickOnUpdatesTab_showsUpdatesTabSelected() {
    launch(MyDownloadsActivity::class.java).use {
      onView(
        allOf(
          withText(R.string.tab_updates),
          isDescendantOfA(withId(R.id.my_downloads_tabs_container))
        )
      ).perform(click())
      onView(withId(R.id.my_downloads_tabs_container)).check(
        matches(
          matchCurrentTabTitle(
            MyDownloadsTab.getTabForPosition(
              1
            ).name
          )
        )
      )
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, MyDownloadsModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(myDownloadsActivityTest: MyDownloadsActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerMyDownloadsActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(myDownloadsActivityTest: MyDownloadsActivityTest) {
      component.inject(myDownloadsActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}