package com.example.eco_plate

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WelcomeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    private var _loginEmail = MutableLiveData<String>("")
    private var _loginPassword = MutableLiveData<String>("")
    private var _confirmPassword = MutableLiveData<String>("")

    val loginEmail: LiveData<String> get() = _loginEmail
    val loginPassword: LiveData<String> get() = _loginPassword
    val confirmPassword: LiveData<String> get() = _confirmPassword
    val accountType = MutableLiveData<String>()
    val isRemembered: Boolean get() = preferences.getBoolean("remember_me", false)
    val hasSavedLogin: Boolean get() = isRemembered && !getSavedEmail().isNullOrBlank()

    init{
        if(isRemembered){
            val savedEmail = preferences.getString("saved_email", "") ?: ""
            _loginEmail.value = savedEmail
        }
    }
    fun setRememberMe(remember: Boolean) {
        preferences.edit { putBoolean("remember_me", remember) }
    }

    fun getSavedEmail(): String? = preferences.getString("saved_email", null)

    fun setLoginEmail(value: String) {
        _loginEmail.value = value

        if (isRemembered){
           preferences.edit { putString("saved_email", value) }
        }
    }

    fun setLoginPassword(value: String) {
        _loginPassword.value = value
    }

    fun setAccountType(value: String) {
        accountType.value = value
    }
}