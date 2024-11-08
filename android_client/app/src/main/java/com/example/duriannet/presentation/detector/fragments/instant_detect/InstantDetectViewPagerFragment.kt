package com.example.duriannet.presentation.detector.fragments.instant_detect

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.duriannet.R
import com.example.duriannet.databinding.BottomSheetDetectorSettingsBinding
import com.example.duriannet.databinding.FragmentInstantDetectViewPagerBinding
import com.example.duriannet.presentation.detector.adapters.InstantViewPagerAdapter
import com.example.duriannet.presentation.detector.view_models.DetectorViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator


class InstantDetectViewPagerFragment : Fragment() {

    private var _binding: FragmentInstantDetectViewPagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetectorViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentInstantDetectViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        setupBottomSheetDialog()
        setupViewPager()
        setupDetector()
    }

    private fun setupDetector() {
        // set default values
        viewModel.setConfidenceThreshold(resources.getString(R.string.default_confidence_threshold).toFloat())
        viewModel.setIouThreshold(resources.getString(R.string.default_iou_threshold).toFloat())
        viewModel.setMaxNumberDetection(resources.getString(R.string.default_max_number_detection).toInt())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "onDestroyView")
        _binding = null
    }


    private fun setupViewPager() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        viewPager.adapter = InstantViewPagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)

        // TODO: Consider to swipe between tabs
        viewPager.isUserInputEnabled = false // Disable swipe

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_title_camera)
                1 -> getString(R.string.tab_title_gallery)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
    }


    private fun setupActionBar() {

        val parentActivity = requireActivity() as AppCompatActivity

        parentActivity.setSupportActionBar(binding.toolbar)

        parentActivity.supportActionBar?.apply {
            title = "Instant Detection"
            setDisplayHomeAsUpEnabled(true)
        }

        // set the navigation icon to close the fragment
        binding.toolbar.setNavigationOnClickListener {
            val navController = findNavController()
            navController.navigateUp()
        }

        parentActivity.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.detector_menu, menu)
                    menu.findItem(R.id.action_settings).isVisible = true
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_settings -> {
                            bottomSheetSetting.show()
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

    }


    // Bottom Sheet Settings
    private lateinit var bottomSheetSetting: BottomSheetDialog
    private lateinit var bottomSheetBinding: BottomSheetDetectorSettingsBinding
    private fun setupBottomSheetDialog() {
        bottomSheetSetting = BottomSheetDialog(requireContext())

        val bottomSheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_detector_settings, null)
            .also { bottomSheetSetting.setContentView(it) }

        bottomSheetBinding = BottomSheetDetectorSettingsBinding.bind(bottomSheetView)

        // set up model selection dropdown list
        val modelAvailable = resources.getStringArray(R.array.detection_models)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_model_list, modelAvailable)
        bottomSheetBinding.actvModelSelection.setAdapter(adapter)
        bottomSheetBinding.actvModelSelection.setText(modelAvailable[0], false)
    }


    fun setOnModelSelectedListener(listener: (position: Int, id: Long) -> Unit) {
        bottomSheetBinding.actvModelSelection.setOnItemClickListener { _, _, position, id ->
            listener(position, id)
        }
    }

    fun setMaxNumberDetectionListener(listener: (Int) -> Unit) {
        bottomSheetBinding.sliderMaxNumOfDetection.addOnChangeListener { _, value, _ ->
            bottomSheetBinding.tvMaxNumOfDetection.text = value.toInt().toString()
            listener(value.toInt())
        }
    }

    fun setConfidenceThresholdListener(listener: (Float) -> Unit) {
        bottomSheetBinding.sliderConfidenceThreshold.addOnChangeListener { _, value, _ ->
            bottomSheetBinding.tvConfidenceThreshold.text = String.format("%.2f", value)
            listener(value)
        }
    }

    fun setIouThresholdListener(listener: (Float) -> Unit) {
        bottomSheetBinding.sliderIoUThreshold.addOnChangeListener { _, value, _ ->
            bottomSheetBinding.tvIoUThreshold.text = String.format("%.2f", value)
            listener(value)
        }
    }

    companion object {
        private const val TAG = "InstantDetectViewPagerFragment"
    }

}