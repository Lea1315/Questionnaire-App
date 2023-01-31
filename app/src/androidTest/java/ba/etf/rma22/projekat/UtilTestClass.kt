package ba.etf.rma22.projekat

import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import ba.etf.rma22.projekat.data.models.Anketa
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class UtilTestClass {
    companion object{
        fun hasItemCount(n: Int) = object : ViewAssertion {
            override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
                if (noViewFoundException != null) {
                    throw noViewFoundException
                }
                assertTrue("View nije tipa RecyclerView", view is RecyclerView)
                var rv: RecyclerView = view as RecyclerView
                ViewMatchers.assertThat(
                    "GetItemCount RecyclerView broj elementa: ",
                    rv.adapter?.itemCount,
                    CoreMatchers.`is`(n)
                )
            }

        }

        fun itemTest(id:Int, k: Anketa){
            Espresso.onView(ViewMatchers.withId(R.id.listaAnketa)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    CoreMatchers.allOf(
                        ViewMatchers.hasDescendant(ViewMatchers.withText(k.naziv)),
                        ViewMatchers.hasDescendant(ViewMatchers.withText(k.nazivIstrazivanja))
                    )
                )
            )
        }
        fun withTextColor(trazenaBoja: Int) = object:TypeSafeMatcher<View>(){
            override fun describeTo(description: Description) {
                description.appendText("Nema tekstPitanja zelene boje")
            }

            override fun matchesSafely(item: View): Boolean {
                if(!(item is TextView)) return false;
                return item.currentTextColor== trazenaBoja

            }

        }
        fun withBackground(trazenaBoja:Int) = object:TypeSafeMatcher<View>(){
            override fun describeTo(description: Description) {
                description.appendText("Nema pozadinu zelene boje")
            }

            override fun matchesSafely(item: View): Boolean {
                if(!(item.background is ColorDrawable)) return false;
                var boja = item.background as ColorDrawable
                return boja.color==trazenaBoja
            }

        }
    }
}