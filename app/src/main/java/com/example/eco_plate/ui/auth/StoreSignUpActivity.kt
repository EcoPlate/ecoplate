package com.example.eco_plate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.eco_plate.R
import com.example.eco_plate.StoreActivity
import com.example.eco_plate.databinding.ActivityStoreSignUpBinding
import com.example.eco_plate.utils.Resource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreSignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreSignUpBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var placesClient: PlacesClient? = null
    private var placesAdapter: PlacesAutoCompleteAdapter? = null
    
    // Store coordinates from Places selection
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    // Canadian provinces
    private val provinces = arrayOf(
        "Alberta", "British Columbia", "Manitoba", "New Brunswick",
        "Newfoundland and Labrador", "Nova Scotia", "Ontario",
        "Prince Edward Island", "Quebec", "Saskatchewan",
        "Northwest Territories", "Nunavut", "Yukon"
    )
    
    // Province abbreviation mapping
    private val provinceAbbreviations = mapOf(
        "AB" to "Alberta",
        "BC" to "British Columbia",
        "MB" to "Manitoba",
        "NB" to "New Brunswick",
        "NL" to "Newfoundland and Labrador",
        "NS" to "Nova Scotia",
        "ON" to "Ontario",
        "PE" to "Prince Edward Island",
        "QC" to "Quebec",
        "SK" to "Saskatchewan",
        "NT" to "Northwest Territories",
        "NU" to "Nunavut",
        "YT" to "Yukon"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePlaces()
        setupProvinceDropdown()
        setupAddressAutocomplete()
        observeViewModel()
        setupClickListeners()
    }

    private fun initializePlaces() {
        try {
            // Initialize Places SDK if not already initialized
            if (!Places.isInitialized()) {
                val apiKey = getPlacesApiKey()
                if (apiKey.isNotEmpty()) {
                    Places.initialize(applicationContext, apiKey)
                }
            }
            
            if (Places.isInitialized()) {
                placesClient = Places.createClient(this)
            }
        } catch (e: Exception) {
            Log.e("StoreSignUp", "Failed to initialize Places", e)
        }
    }

    private fun getPlacesApiKey(): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            Log.e("StoreSignUp", "Failed to get Places API key", e)
            ""
        }
    }

    private fun setupAddressAutocomplete() {
        placesClient?.let { client ->
            placesAdapter = PlacesAutoCompleteAdapter(this, client)
            binding.etStoreAddress.setAdapter(placesAdapter)
            
            // Handle selection from dropdown
            binding.etStoreAddress.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val prediction = placesAdapter?.getItem(position)
                prediction?.let { selectedPrediction ->
                    // Fetch place details to get full address components
                    fetchPlaceDetails(selectedPrediction.placeId)
                }
            }
            
            // Hide helper text when user starts typing
            binding.etStoreAddress.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.tilStoreAddress.helperText = "Type at least 3 characters"
                } else {
                    binding.tilStoreAddress.helperText = null
                }
            }
        }
        
        // If Places isn't available, just let them type manually
        if (placesClient == null) {
            binding.tilStoreAddress.helperText = null
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient?.fetchPlace(request)
            ?.addOnSuccessListener { response ->
                val place = response.place
                populateAddressFromPlace(place)
                placesAdapter?.resetSession()
            }
            ?.addOnFailureListener { exception ->
                Log.e("StoreSignUp", "Failed to fetch place details", exception)
                Toast.makeText(this, "Couldn't get full address. Please fill in remaining fields.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateAddressFromPlace(place: Place) {
        var streetNumber = ""
        var streetName = ""
        var city = ""
        var province = ""
        var postalCode = ""

        // Capture coordinates from the place
        place.latLng?.let { latLng ->
            selectedLatitude = latLng.latitude
            selectedLongitude = latLng.longitude
            Log.d("StoreSignUp", "Captured coordinates: $selectedLatitude, $selectedLongitude")
        }

        // Parse address components
        place.addressComponents?.asList()?.forEach { component ->
            when {
                component.types.contains("street_number") -> streetNumber = component.name
                component.types.contains("route") -> streetName = component.name
                component.types.contains("locality") -> city = component.name
                component.types.contains("administrative_area_level_1") -> {
                    // Convert province abbreviation to full name
                    province = provinceAbbreviations[component.shortName] ?: component.name
                }
                component.types.contains("postal_code") -> postalCode = component.name
            }
        }

        // Build street address
        val streetAddress = if (streetNumber.isNotEmpty() && streetName.isNotEmpty()) {
            "$streetNumber $streetName"
        } else if (streetName.isNotEmpty()) {
            streetName
        } else {
            // Fall back to the full address and try to extract just the street part
            place.address?.split(",")?.firstOrNull()?.trim() ?: ""
        }

        // Populate the fields
        binding.etStoreAddress.setText(streetAddress)
        binding.etStoreCity.setText(city)
        
        // Set province in dropdown
        if (province.isNotEmpty()) {
            binding.etStoreProvince.setText(province, false)
        }
        
        // Format postal code (Canadian format: A1A 1A1)
        if (postalCode.isNotEmpty()) {
            val formattedPostalCode = formatCanadianPostalCode(postalCode)
            binding.etStorePostalCode.setText(formattedPostalCode)
        }

        // Clear helper text after successful selection
        binding.tilStoreAddress.helperText = null
        
        // Dismiss the dropdown
        binding.etStoreAddress.dismissDropDown()
        
        Toast.makeText(this, "Address auto-filled!", Toast.LENGTH_SHORT).show()
    }

    private fun formatCanadianPostalCode(postalCode: String): String {
        val cleaned = postalCode.replace("\\s".toRegex(), "").uppercase()
        return if (cleaned.length == 6) {
            "${cleaned.substring(0, 3)} ${cleaned.substring(3)}"
        } else {
            postalCode.uppercase()
        }
    }

    private fun setupProvinceDropdown() {
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, provinces)
        binding.etStoreProvince.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            if (binding.btnSignUp.isEnabled) {
                performSignUp()
            }
        }

        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        
        binding.tvBackToSelection.setOnClickListener {
            finish()
        }
    }

    private fun performSignUp() {
        // Personal Information
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        
        // Store Information
        val storeName = binding.etStoreName.text.toString().trim()
        val storePhone = binding.etStorePhone.text.toString().trim()
        val storeDescription = binding.etStoreDescription.text.toString().trim()
        
        // Store Location
        val streetAddress = binding.etStoreAddress.text.toString().trim()
        val city = binding.etStoreCity.text.toString().trim()
        val province = binding.etStoreProvince.text.toString().trim()
        val postalCode = binding.etStorePostalCode.text.toString().trim().uppercase()

        if (validateInput(
                firstName, lastName, email, password, confirmPassword,
                storeName, storePhone, streetAddress, city, province, postalCode
            )) {
            // Combine address fields into a full address
            val fullAddress = buildFullAddress(streetAddress, city, province, postalCode)
            
            // First create the user account with STORE_OWNER role
            // Pass coordinates if available from Places selection
            Log.d("StoreSignUp", "Signing up with coordinates: $selectedLatitude, $selectedLongitude")
            authViewModel.signUpAsStoreOwner(
                email = email,
                password = password,
                firstName = firstName.ifEmpty { null },
                lastName = lastName.ifEmpty { null },
                phone = phone.ifEmpty { null },
                storeName = storeName,
                storeAddress = fullAddress,
                storePhone = storePhone,
                storeDescription = storeDescription.ifEmpty { null },
                storeCity = city,
                storeProvince = province,
                storePostalCode = postalCode,
                storeLatitude = selectedLatitude,
                storeLongitude = selectedLongitude
            )
        }
    }

    private fun buildFullAddress(street: String, city: String, province: String, postalCode: String): String {
        return "$street, $city, $province $postalCode, Canada"
    }

    private fun observeViewModel() {
        authViewModel.authState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Store account created successfully!", Toast.LENGTH_SHORT).show()
                    navigateToStoreMain()
                }
                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = when {
                        result.message?.contains("already exists", ignoreCase = true) == true -> 
                            "This email is already registered"
                        result.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection"
                        result.message?.contains("password", ignoreCase = true) == true ->
                            "Password requirements not met"
                        else -> result.message ?: "Sign up failed. Please try again"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        storeName: String,
        storePhone: String,
        streetAddress: String,
        city: String,
        province: String,
        postalCode: String
    ): Boolean {
        var isValid = true
        
        // Personal Information Validation
        if (firstName.isEmpty()) {
            binding.tilFirstName.error = "First name is required"
            isValid = false
        } else {
            binding.tilFirstName.error = null
        }
        
        if (lastName.isEmpty()) {
            binding.tilLastName.error = "Last name is required"
            isValid = false
        } else {
            binding.tilLastName.error = null
        }
        
        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Min 8 chars"
            isValid = false
        } else if (!password.matches(Regex(".*[A-Z].*"))) {
            binding.tilPassword.error = "Need uppercase"
            isValid = false
        } else if (!password.matches(Regex(".*[a-z].*"))) {
            binding.tilPassword.error = "Need lowercase"
            isValid = false
        } else if (!password.matches(Regex(".*[0-9].*"))) {
            binding.tilPassword.error = "Need number"
            isValid = false
        } else if (!password.matches(Regex(".*[@\$!%*?&].*"))) {
            binding.tilPassword.error = "Need special char"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        // Confirm password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Required"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "No match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        // Store Information Validation
        if (storeName.isEmpty()) {
            binding.tilStoreName.error = "Store name is required"
            isValid = false
        } else {
            binding.tilStoreName.error = null
        }
        
        if (storePhone.isEmpty()) {
            binding.tilStorePhone.error = "Store phone is required"
            isValid = false
        } else if (!android.util.Patterns.PHONE.matcher(storePhone).matches()) {
            binding.tilStorePhone.error = "Invalid phone format"
            isValid = false
        } else {
            binding.tilStorePhone.error = null
        }
        
        // Store Location Validation
        if (streetAddress.isEmpty()) {
            binding.tilStoreAddress.error = "Street address is required"
            isValid = false
        } else {
            binding.tilStoreAddress.error = null
        }
        
        if (city.isEmpty()) {
            binding.tilStoreCity.error = "City is required"
            isValid = false
        } else {
            binding.tilStoreCity.error = null
        }
        
        if (province.isEmpty()) {
            binding.tilStoreProvince.error = "Province is required"
            isValid = false
        } else if (!provinces.contains(province)) {
            binding.tilStoreProvince.error = "Select a valid province"
            isValid = false
        } else {
            binding.tilStoreProvince.error = null
        }
        
        // Canadian postal code validation (A1A 1A1 format)
        val postalCodeRegex = Regex("^[A-Z]\\d[A-Z]\\s?\\d[A-Z]\\d$", RegexOption.IGNORE_CASE)
        if (postalCode.isEmpty()) {
            binding.tilStorePostalCode.error = "Postal code is required"
            isValid = false
        } else if (!postalCodeRegex.matches(postalCode)) {
            binding.tilStorePostalCode.error = "Invalid format (e.g., V6B 1A1)"
            isValid = false
        } else {
            binding.tilStorePostalCode.error = null
        }
        
        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.loadingOverlay.isVisible = isLoading
        binding.btnSignUp.isEnabled = !isLoading
        binding.btnSignUp.alpha = if (isLoading) 0.6f else 1.0f
    }

    private fun navigateToStoreMain() {
        val intent = Intent(this, StoreActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
