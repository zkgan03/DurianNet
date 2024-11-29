package com.example.duriannet.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavController()
        setupBackPress()
        setupEventBus()

        // Handle deep link when activity is created
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun setupNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHostFragment.navController

        // Link BottomNavigationView with NavController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Define mapping between menu item IDs and navigation destinations
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.durianProfileHome -> {
                    navController.navigate(R.id.durianProfileFragment)
                    true
                }
                R.id.chatbotHome -> {
                    navController.navigate(R.id.durianChatbotFragment)
                    true
                }
                R.id.userProfileHome -> {
                    navController.navigate(R.id.userProfileFragment)
                    true
                }
                else -> false
            }
        }

        // Hide BottomNavigationView for specific fragments
        val noBottomNavFragments = setOf(
            R.id.loginFragment,
            R.id.resetPasswordFragment,
            R.id.signUpFragment,
            R.id.forgetPasswordFragment,
            R.id.changePasswordFragment,
            R.id.editProfileFragment,
            R.id.favoriteDurianFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id in noBottomNavFragments) View.GONE else View.VISIBLE
        }
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

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Navigate back if possible; otherwise, finish the app
                if (!navController.popBackStack()) {
                    finish()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.pathSegments.contains("reset-password")) {
                val email = uri.getQueryParameter("email")
                val bundle = Bundle().apply {
                    putString("email", email ?: "")
                }
                navController.navigate(R.id.resetPasswordFragment, bundle)
            }
        }
    }
}


/*
package com.example.duriannet.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
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
    private lateinit var bottomNavController: NavController
    private lateinit var mainNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        */
/*setupNavControllers()*//*

        setupBackPress()
        setupBottomNav()
        setupEventBus()

        // Handle deep link when activity is created
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun setupNavController() {
        // Initialize NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHostFragment.navController

        // Set the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController)

        // Link BottomNavigationView with NavController
        binding.bottomNavigationView.setupWithNavController(navController)

        // Hide BottomNavigationView for specific fragments
        val noBottomNavFragments = setOf(
            R.id.loginFragment,
            R.id.resetPasswordFragment,
            R.id.signUpFragment,
            R.id.forgetPasswordFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id in noBottomNavFragments) View.GONE else View.VISIBLE
        }
    }

    */
/*private fun setupNavControllers() {
        // Initialize mainNavController for login flow
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        mainNavController = navHostFragment.navController

        // Initialize bottomNavController for bottom navigation flow
        val bottomNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.bottomNavHost) as NavHostFragment
        bottomNavController = bottomNavHostFragment.navController

        // Set the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(mainNavController)

        // Hide BottomNavigationView for certain fragments
        val noBottomNavFragments = setOf(
            R.id.loginFragment,
            R.id.resetPasswordFragment,
            R.id.signUpFragment,
            R.id.forgetPasswordFragment
        )
        mainNavController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id in noBottomNavFragments) View.GONE else View.VISIBLE
        }
    }*//*



    */
/*private fun setupNavControllers() {
        // Set up bottomNavController for bottom_bar_nav_graph
        val bottomNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.bottomNavHost) as NavHostFragment
        bottomNavController = bottomNavHostFragment.navController

        // Set up mainNavController for nav graph
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        mainNavController = navHostFragment.navController


        // Set the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        // Link the ActionBar with the bottomNavController by default
        //setupActionBarWithNavController(bottomNavController)
        setupActionBarWithNavController(mainNavController)


        // Hide the bottom navigation view for certain fragments
        val noBottomNavFragments = setOf(
            R.id.loginFragment,
            R.id.resetPasswordFragment,
            R.id.signUpFragment,
            R.id.forgetPasswordFragment
        )

        bottomNavController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in noBottomNavFragments) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }*//*


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
        binding.bottomNavigationView.setupWithNavController(bottomNavController)
    }

    */
/*private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomNavController.currentDestination?.id == bottomNavController.graph.startDestinationId) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        finishAfterTransition()
                    } else {
                        finish()
                    }
                } else {
                    bottomNavController.navigateUp()
                }
            }
        })
    }*//*


    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check which NavController is active
                val activeNavController = if (mainNavController.currentDestination != null) {
                    mainNavController
                } else {
                    bottomNavController
                }

                // If there's something in the back stack, navigate back
                if (!activeNavController.popBackStack()) {
                    finish() // Otherwise, close the app
                }
            }
        })
    }


    override fun onSupportNavigateUp(): Boolean {
        return bottomNavController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.pathSegments.contains("reset-password")) {
                val email = uri.getQueryParameter("email")
                val bundle = Bundle().apply {
                    putString("email", email ?: "")
                }
                mainNavController.navigate(R.id.resetPasswordFragment, bundle)
            }
        }
    }
}


*/
/*
package com.example.duriannet.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
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

        // Handle deep link when activity is created
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }


    private fun setupNavController() {
        *//*

*/
/*val navHostFragment = supportFragmentManager.findFragmentById(R.id.host) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)*//*
*/
/*


        // Set the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar) // This links the toolbar to the activity

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.host) as NavHostFragment
        navController = navHostFragment.navController

        // Link the ActionBar with the NavController
        setupActionBarWithNavController(navController)

        //

        // List of fragments that should NOT show the BottomNavigationView
        val noBottomNavFragments = setOf(
            R.id.loginFragment,
            R.id.resetPasswordFragment,
            R.id.signUpFragment,
            R.id.forgetPasswordFragment
        )

        // Control visibility of BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in noBottomNavFragments) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
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

    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.pathSegments.contains("reset-password")) {
                val email = uri.getQueryParameter("email")
                val bundle = Bundle().apply {
                    putString("email", email ?: "")
                }
                navController.navigate(R.id.resetPasswordFragment, bundle)
            }
        }
    }



}*/

