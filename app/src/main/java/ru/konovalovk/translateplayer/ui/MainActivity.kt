package ru.konovalovk.translateplayer.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import ru.konovalovk.translateplayer.R


class MainActivity : AppCompatActivity(R.layout.activity_main) {
    val TAG = this::class.java.simpleName
    private val navController by lazy { findNavController(R.id.flMain) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        findViewById<FragmentContainerView>(R.id.flMain).doOnLayout {
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
    }
}