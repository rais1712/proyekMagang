package com.proyek.maganggsp.presentation.monitor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.proyek.maganggsp.presentation.monitor.tabs.BlockedLoketFragment
import com.proyek.maganggsp.presentation.monitor.tabs.FlaggedLoketFragment

class MonitorPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // Kita punya dua tab

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FlaggedLoketFragment() // Fragment untuk tab "Dipantau"
            1 -> BlockedLoketFragment() // Fragment untuk tab "Diblokir"
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}