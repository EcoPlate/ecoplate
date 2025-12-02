package com.example.eco_plate.ui.auth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.example.eco_plate.R
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlacesAutoCompleteAdapter(
    context: Context,
    private val placesClient: PlacesClient
) : ArrayAdapter<AutocompletePrediction>(context, R.layout.item_place_suggestion), Filterable {

    private var predictions: List<AutocompletePrediction> = emptyList()
    private var sessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    override fun getCount(): Int = predictions.size

    override fun getItem(position: Int): AutocompletePrediction? = predictions.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_place_suggestion, parent, false)
        
        val prediction = getItem(position)
        
        val tvPrimaryText = view.findViewById<TextView>(R.id.tvPrimaryText)
        val tvSecondaryText = view.findViewById<TextView>(R.id.tvSecondaryText)
        
        tvPrimaryText.text = prediction?.getPrimaryText(null)?.toString() ?: ""
        tvSecondaryText.text = prediction?.getSecondaryText(null)?.toString() ?: ""
        
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                
                if (constraint.isNullOrEmpty() || constraint.length < 3) {
                    results.values = emptyList<AutocompletePrediction>()
                    results.count = 0
                    return results
                }
                
                // Fetch predictions synchronously for the filter
                val newPredictions = runBlocking {
                    fetchPredictions(constraint.toString())
                }
                
                results.values = newPredictions
                results.count = newPredictions.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                predictions = (results?.values as? List<AutocompletePrediction>) ?: emptyList()
                if (predictions.isNotEmpty()) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as? AutocompletePrediction)?.getPrimaryText(null) ?: ""
            }
        }
    }

    private suspend fun fetchPredictions(query: String): List<AutocompletePrediction> {
        return suspendCancellableCoroutine { continuation ->
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries(listOf("CA")) // Canada only
                .setTypesFilter(listOf("address"))
                .setSessionToken(sessionToken)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    continuation.resume(response.autocompletePredictions)
                }
                .addOnFailureListener { exception ->
                    continuation.resume(emptyList())
                }
        }
    }

    fun resetSession() {
        sessionToken = AutocompleteSessionToken.newInstance()
    }
}

