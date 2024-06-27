package com.example.heartattack

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            val destination = intent.getIntExtra("destination", R.id.nav_tentang)
            navigateToFragment(destination)
            navigationView.setCheckedItem(destination)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        navigateToFragment(item.itemId)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.nav_tentang -> FragmentAbout()
            R.id.nav_dataset -> FragmentDataset()
            R.id.nav_fitur -> FragmentFitur()
            R.id.nav_model -> FragmentModel()
            R.id.nav_simulasimodel -> FragmentSimulasi()
            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, it).commit()
            toolbar.title = getString(when (itemId) {
                R.id.nav_tentang -> R.string.nav_about_title
                R.id.nav_dataset -> R.string.nav_dataset_title
                R.id.nav_fitur -> R.string.nav_features_title
                R.id.nav_model -> R.string.nav_model_title
                R.id.nav_simulasimodel -> R.string.nav_modelsimulation_title
                else -> R.string.app_name
            })
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
