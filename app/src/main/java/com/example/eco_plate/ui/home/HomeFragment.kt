package com.example.eco_plate.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.eco_plate.R
import com.example.eco_plate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecycler()

        return root
    }

    private fun setupRecycler() {
        val categories = listOf(
            Category("Vegetables", R.drawable.ic_launcher_foreground),
            Category("Fruits", R.drawable.ic_launcher_foreground),
            Category("Bread", R.drawable.ic_launcher_foreground),
            Category("Sweets", R.drawable.ic_launcher_foreground),
            Category("Meats", R.drawable.ic_launcher_foreground),
            Category("Drinks", R.drawable.ic_launcher_foreground)
        )

        val adapter = CategoryAdapter(categories) { /* handle click later */ }
        binding.rvCategories.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategories.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}