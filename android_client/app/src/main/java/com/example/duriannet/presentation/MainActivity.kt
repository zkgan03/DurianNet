package com.example.duriannet.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.duriannet.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBackPress()
        setupBottomNav()
    }

    private fun setupBottomNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(binding.host.id) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController!!)
    }

    private fun setupBackPress() {
        onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (navController?.currentDestination?.id == navController?.graph?.startDestinationId) {

                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
                            // (https://issuetracker.google.com/issues/139738913)
                            finishAfterTransition()
                        } else {
                            finish()
                        }

                    } else {
                        navController?.navigateUp()
                    }
                }
            })
    }

}