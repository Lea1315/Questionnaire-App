package ba.etf.rma22.projekat

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.PositionAssertions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LayoutTest {
    @get:Rule
    val mainLayout = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun relativeLayoutPositionsPocetna(){
        Espresso.onView(withId(R.id.filterAnketa)).check(
            ViewAssertions.matches(
                isCompletelyDisplayed()
            )
        )
        Espresso.onView(withId(R.id.listaAnketa)).check(ViewAssertions.matches(isDisplayed()))

        Espresso.onView(withId(R.id.filterAnketa)).check(isCompletelyAbove(withId(R.id.listaAnketa)));

    }

    @Test
    fun relativeLayoutPositionsUpis(){
        Espresso.onView(withId(R.id.pager)).perform(ViewPager2Actions.scrollToPosition(1))
        Espresso.onView(withId(R.id.odabirGodina)).check(
            ViewAssertions.matches(
                isCompletelyDisplayed()
            )
        )
        Espresso.onView(withId(R.id.odabirIstrazivanja)).check(
            ViewAssertions.matches(
                isCompletelyDisplayed()
            )
        )
        Espresso.onView(withId(R.id.odabirGrupa)).check(ViewAssertions.matches(isCompletelyDisplayed()))
        Espresso.onView(withId(R.id.dodajIstrazivanjeDugme)).check(
            ViewAssertions.matches(
                isCompletelyDisplayed()
            )
        )

        Espresso.onView(withId(R.id.odabirGodina)).check(isCompletelyAbove(withId(R.id.odabirIstrazivanja)))
        Espresso.onView(withId(R.id.odabirIstrazivanja)).check(isCompletelyAbove(withId(R.id.odabirGrupa)))
        Espresso.onView(withId(R.id.odabirGrupa)).check(isCompletelyAbove(withId(R.id.dodajIstrazivanjeDugme)))

    }



}


