package ba.etf.rma22.projekat.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    fragmentManager: FragmentManager, var fragments: MutableList<Fragment>, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun add(index: Int, fragment: Fragment) {
        fragments.add(index, fragment)
        notifyItemChanged(index)
    }

    fun addBulk(fragmenti: List<Fragment>) {
        fragmenti.forEachIndexed { index, fragment -> fragments.add(index, fragment) }
        notifyItemRangeChanged(0, fragmenti.size)
    }

    fun refreshFragment(index: Int, fragment: Fragment) {
        if (fragments[0] !is FragmentPitanje) {
            fragments[index] = fragment
            notifyItemChanged(index)
        }
    }

    fun remove(index: Int) {
        fragments.removeAt(index)
        notifyItemChanged(index)
    }

    fun removeAll() {
        fragments.clear()
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return fragments[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragments.find { it.hashCode().toLong() == itemId } != null
    }
}
