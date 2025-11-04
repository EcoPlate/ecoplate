package com.example.eco_plate

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.eco_plate.ui.WelcomeFragment

class WelcomeActivity : AppCompatActivity() {

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var fragmentManager: FragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        fragmentManager = supportFragmentManager

        fragmentContainer = findViewById<FrameLayout>(R.id.welcome_fragment_container)

        showFragment(WELCOME) {
            WelcomeFragment()
        }
    }

    private fun showFragment(tag: String, createLambda: () -> Fragment) {
        val transactionManager = fragmentManager.beginTransaction()

        // first hide ALL fragments
        fragmentManager.fragments.forEach {
            transactionManager.hide(it)
        }

        // Show if it exists or add to manager
        val existing = fragmentManager.findFragmentByTag(tag)
        if (existing != null) {
            transactionManager.show(existing)
        } else {
            transactionManager.add(R.id.welcome_fragment_container, createLambda(), tag)
        }

        transactionManager.commit()
    }

    companion object {
        private const val WELCOME = "welcome"
        private const val LOGIN = "login"
        private const val SIGNUP = "signup"
    }
}


/*private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGetStarted.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }*/