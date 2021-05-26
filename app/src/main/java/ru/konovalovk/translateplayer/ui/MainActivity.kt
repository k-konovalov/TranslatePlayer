package ru.konovalovk.translateplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import ru.konovalovk.translateplayer.R

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    val TAG = this::class.java.simpleName
    val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    val navigationView by lazy {findViewById<NavigationView>(R.id.navigationView).apply {
        setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected
            menu.forEach { it.isChecked = false }
            menuItem.isChecked = true
            drawerLayout.close()
            true
        }
    }}
    private val navController by lazy { findNavController(R.id.flMain) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupNav()
        navigationView
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, ask it.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2);
        }
    }

    fun setupNav(){
        findViewById<FragmentContainerView>(R.id.flMain).doOnLayout {
            //val appBarConfiguration = AppBarConfiguration(navController.graph, findViewById<DrawerLayout>(R.id.drawer_layout))
            //setupActionBarWithNavController(navController, appBarConfiguration)
            navigationView.setupWithNavController(navController)
            NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(drawerLayout) || super.onSupportNavigateUp()
    }
}