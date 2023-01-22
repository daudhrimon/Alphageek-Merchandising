package com.gdm.alphageek.view.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.gdm.alphageek.databinding.ActivitySplashScreenBinding
import com.gdm.alphageek.utils.SharedPref
import com.gdm.alphageek.view.ui.auth.LoginActivity
import com.gdm.alphageek.view.ui.welcome_screen.WelcomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.versionNameTv.text = "Version ${this.packageManager.getPackageInfo(this.packageName, 0).versionName}"

        // SharedPref initialize
        SharedPref.init(this)


        lifecycleScope.launch {
            // 3 sec delay
            delay(2000)

            if (SharedPref.getUserID().isEmpty()) {
                startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreen, WelcomeActivity::class.java))
            }

            finishAffinity()
        }
    }
}