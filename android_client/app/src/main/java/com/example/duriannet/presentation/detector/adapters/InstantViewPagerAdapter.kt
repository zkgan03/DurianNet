package com.example.duriannet.presentation.detector.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.duriannet.presentation.detector.fragments.instant_detect.InstantCameraFragment
import com.example.duriannet.presentation.detector.fragments.instant_detect.InstantGalleryFragment

class InstantViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2 // We have two fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InstantCameraFragment()
            1 -> InstantGalleryFragment()
            else -> InstantCameraFragment()
        }
    }
}