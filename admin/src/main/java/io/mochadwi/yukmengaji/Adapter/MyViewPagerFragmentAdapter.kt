package io.mochadwi.yukmengaji.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.mochadwi.yukmengaji.Class.Category
import io.mochadwi.yukmengaji.PostFragment

class MyViewPagerFragmentAdapter(
    fragmentActivity: FragmentActivity,
    private val userId: String,
    private val items: List<Category>
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return PostFragment.newInstance(userId, items[position].name)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
