package ba.etf.rma22.projekat

import androidx.test.espresso.ViewAction
import androidx.viewpager2.widget.ViewPager2

class ViewPager2Actions {
    companion object{
        fun scrollToFirst():ViewAction{
            return object :ViewPager2ScrollAction(){
                override fun performScroll(viewPager: ViewPager2?) {
                    val size: Int = viewPager!!.adapter!!.itemCount
                    if (size > 0) {
                        viewPager!!.currentItem=0
                    }
                }

                override fun getDescription(): String {
                    return "ViewPager move to first page";
                }

            }
        }
        fun scrollToPosition(position:Int):ViewAction{
            return object :ViewPager2ScrollAction(){
                override fun performScroll(viewPager: ViewPager2?) {
                    val size: Int = viewPager!!.adapter!!.itemCount
                    if (size > 0 && position<size) {
                        viewPager!!.currentItem=position
                    }
                }

                override fun getDescription(): String {
                    return "ViewPager move to position: ${position}"
                }

            }
        }
        fun scrollToLast():ViewAction{
            return object :ViewPager2ScrollAction(){
                override fun performScroll(viewPager: ViewPager2?) {
                    val size: Int = viewPager!!.adapter!!.itemCount
                    if (size > 0) {
                        viewPager!!.currentItem=size-1
                    }
                }

                override fun getDescription(): String {
                    return "ViewPager move to last position"
                }

            }
        }

    }
}