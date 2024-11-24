package com.example.spaTi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomNavigationViewUser: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUserMenu()
        userMenuListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Code provide by https://github.com/Gavalencia12/Mobile_Application, granted permission to me to use in this project
    private fun setupUserMenu() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationViewUser = findViewById(R.id.bottom_navigation_user)
        NavigationUI.setupWithNavController(bottomNavigationViewUser, navController)

        bottomNavigationViewUser.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeMenuSearch -> {
                    navController.navigate(R.id.searchTagsFragment)
                    true
                }
                R.id.homeMenuHome -> {
                    navController.navigate(R.id.userHomeFragment)
                    true
                }
                R.id.homeMenuAppointments -> {
                    navController.navigate(R.id.appointmentsFragment)
                    true
                }
                R.id.homeMenuProfile -> {
                    navController.navigate(R.id.myProfileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun userMenuListener() {
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            when (destination.id) {
                R.id.searchTagsFragment, R.id.userHomeFragment, R.id.appointmentsFragment, R.id.myProfileFragment -> {
                    hideAllBottomNavigation()
                    showUserBottomNavigation()
                }
                else -> hideAllBottomNavigation()
            }
        }
    }

    private fun showUserBottomNavigation() {
        bottomNavigationViewUser.visibility = View.VISIBLE
    }


    private fun hideAllBottomNavigation() {
        bottomNavigationViewUser.visibility = View.GONE
        bottomNavigationViewUser.menu.findItem(R.id.homeMenuHome)?.isChecked = true
    }
}
