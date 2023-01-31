package ba.etf.rma22.projekat

import android.view.View
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import org.hamcrest.Matcher


abstract class ViewPager2ScrollAction : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isDisplayed()
    }
    open class PageChange : IdlingResource {
        private var mCurrState = ViewPager2.SCROLL_STATE_IDLE
        private lateinit var mCallback:IdlingResource.ResourceCallback
        var mNeedsIdle:Boolean=false
        var callback: OnPageChangeCallback=object:OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                if (mCurrState == ViewPager.SCROLL_STATE_IDLE) {
                    if (mCallback != null) {
                        mCallback.onTransitionToIdle();
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {
                mCurrState = state
                if (mCurrState == ViewPager.SCROLL_STATE_IDLE) {
                    if (mCallback != null) {
                        mCallback.onTransitionToIdle()
                    }
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }
        }

        override fun getName(): String {
            return "View pager listener";
        }

        override fun isIdleNow(): Boolean {
            if (!mNeedsIdle) {
                return true;
            } else {
                return mCurrState == ViewPager2.SCROLL_STATE_IDLE;
            }
        }

        override fun registerIdleTransitionCallback(callbackRS: IdlingResource.ResourceCallback) {
            mCallback=callbackRS
        }


    }
    override fun perform(uiController: UiController?, view: View?) {
        var vp = view as ViewPager2
        var vpCB: PageChange = PageChange()
        vp.registerOnPageChangeCallback(vpCB.callback)


        try {
            // Register our listener as idling resource so that Espresso waits until the
            // wrapped action results in the view pager getting to the STATE_IDLE state
            IdlingRegistry.getInstance().register(vpCB as IdlingResource)
            uiController!!.loopMainThreadUntilIdle()
            performScroll(view as ViewPager2)
            uiController!!.loopMainThreadUntilIdle()
            vpCB.mNeedsIdle = true
            uiController!!.loopMainThreadUntilIdle()
            vpCB.mNeedsIdle = false
        } finally {
            // Unregister our idling resource
                IdlingRegistry.getInstance().unregister(vpCB)
            // And remove our tracker listener from ViewPager
            vp.unregisterOnPageChangeCallback(vpCB.callback)
        }
    }
    protected abstract fun performScroll(viewPager: ViewPager2?)
}