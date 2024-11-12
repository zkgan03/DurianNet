package com.example.duriannet.presentation

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.ActivityMainBinding
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    //private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavController()
        setupBackPress()
        setupBottomNav()
        setupEventBus()
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.host) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    private fun setupEventBus() {

        lifecycleScope.launch {
            EventBus.events.collect { event ->
                when (event) {
                    is Event.Toast -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}