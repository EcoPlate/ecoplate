package com.example.eco_plate.ui

import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.eco_plate.WelcomeActivity
import com.example.eco_plate.WelcomeViewModel
import com.example.eco_plate.databinding.FragmentLoginBinding
import androidx.core.content.edit

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WelcomeViewModel by activityViewModels()

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var rememberCB: CheckBox
    private lateinit var signupBTN: Button
    private lateinit var loginBTN: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailET = binding.loginEmail
        passwordET = binding.loginPassword
        rememberCB = binding.checkBox
        signupBTN = binding.btnCreateAccount
        loginBTN = binding.btnLogin

        // Email
        viewModel.loginEmail.observe(viewLifecycleOwner) { loginEmail ->
            val current = emailET.text?.toString().orEmpty()
            if (current != loginEmail) emailET.setText(loginEmail)
        }
        emailET.doAfterTextChanged { text ->
            val emailString = text?.toString().orEmpty()
            if (viewModel.loginEmail.value != emailString) viewModel.setLoginEmail(emailString)
        }

        // Password
        viewModel.loginPassword.observe(viewLifecycleOwner) { loginPassword ->
            passwordET.setText(loginPassword)
        }
        passwordET.doAfterTextChanged { text ->
            val passwordString = text?.toString().orEmpty()
            if (viewModel.loginPassword.value != passwordString) viewModel.setLoginPassword(passwordString)
        }

        // Remember Me
        rememberCB.isChecked = viewModel.isRemembered
        rememberCB.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRememberMe(isChecked)
            if (!isChecked){
               requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE).edit {
                   remove("saved_email")
               }
            }
        }

        // Buttons
        loginBTN.setOnClickListener {
            // Need to confirm password is correct
            (requireActivity() as WelcomeActivity).goToMain()
        }

        signupBTN.setOnClickListener {
            (requireActivity() as WelcomeActivity).goToSignup()
        }

    }
}