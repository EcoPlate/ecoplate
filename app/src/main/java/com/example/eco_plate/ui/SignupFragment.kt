package com.example.eco_plate.ui

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.eco_plate.WelcomeActivity
import com.example.eco_plate.WelcomeViewModel
import com.example.eco_plate.databinding.FragmentSignupBinding

// TODO
// Need to implement password match
// need to pass new account to backend
// need to ensure button doesn't succeed without email and matching passwords
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WelcomeViewModel by activityViewModels()

    private lateinit var emailET : EditText
    private lateinit var passwordET : EditText
    private lateinit var confirmPasswordET: EditText
    private lateinit var accountTypeSpinner: Spinner
    private lateinit var createAccountBTN : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        _binding = FragmentSignupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailET = binding.signupEmail
        passwordET = binding.signupPassword1
        confirmPasswordET = binding.signupPassword2
        accountTypeSpinner = binding.signupSpinner
        createAccountBTN = binding.btnSignup

        // Email
        viewModel.loginEmail.observe(viewLifecycleOwner) { loginEmail ->
            val current = emailET.text?.toString().orEmpty()
            if (current != loginEmail) emailET.setText(loginEmail)
        }
        emailET.doAfterTextChanged { text ->
            val emailString = text?.toString().orEmpty()
            if (viewModel.loginEmail.value != emailString) viewModel.setLoginEmail(emailString)
        }

        passwordET.doAfterTextChanged { text ->
            val passwordString = text?.toString().orEmpty()
            if (viewModel.loginPassword.value != passwordString) viewModel.setLoginPassword(passwordString)
        }

        passwordET.doAfterTextChanged { text ->
            val passwordString = text?.toString().orEmpty()
            if (viewModel.loginPassword.value != passwordString) viewModel.setLoginPassword(passwordString)
        }

        val accountTypes = listOf("Buyer", "Seller")
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            accountTypes
        )
        accountTypeSpinner.adapter = adapter
        viewModel.accountType.observe(viewLifecycleOwner) { accountType ->
            val position = accountTypes.indexOf(accountType)
            if (position >= 0 && accountTypeSpinner.selectedItemPosition != position) {
                accountTypeSpinner.setSelection(position)
            }
        }

        accountTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val selectedType = accountTypes[p2]
                if (viewModel.accountType.value != selectedType) {
                    viewModel.setAccountType(selectedType)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        createAccountBTN.setOnClickListener {
            viewModel.signup()
        }

        viewModel.signupResult.observe(viewLifecycleOwner){ result ->
            if (result.success) {
                (requireActivity() as WelcomeActivity).goToMain()
            } else {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}