package com.proyek.maganggsp.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyek.maganggsp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
    }

    private fun setupNavigation() {
        // Find views
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup BottomNavigation dengan NavController
        bottomNavigation.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        // Custom back button behavior: selalu kembali ke HomeFragment dulu
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                // Jika sudah di HomeFragment, keluar aplikasi
                super.onBackPressed()
            }
            else -> {
                // Jika di fragment lain, kembali ke HomeFragment
                navController.navigate(R.id.homeFragment)
            }
        }
    }
}