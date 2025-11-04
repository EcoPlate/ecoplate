package com.example.eco_plate

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.eco_plate.ui.LoginFragment
import com.example.eco_plate.ui.SignupFragment
import com.example.eco_plate.ui.WelcomeFragment

class WelcomeActivity : AppCompatActivity() {

    private lateinit var fragmentManager: FragmentManager
    private val viewModel: WelcomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(viewModel.hasSavedLogin){
            goToMain()
            return
        }

        setContentView(R.layout.activity_welcome)
        fragmentManager = supportFragmentManager

        showFragment(WELCOME) { WelcomeFragment() }
    }

    fun goToMain(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        return
    }

    fun goToLogin(){
        showFragment(LOGIN) { LoginFragment() }
    }

    fun goToSignup(){
        showFragment(SIGNUP) { SignupFragment() }
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