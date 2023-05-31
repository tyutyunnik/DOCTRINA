package com.test.nmla.doctrina.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.test.nmla.doctrina.R
import com.test.nmla.doctrina.databinding.FragmentAnimationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnimationFragment : Fragment(R.layout.fragment_animation) {
    private lateinit var binding: FragmentAnimationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimationBinding.bind(view)

//        AppInitializer.getInstance(requireContext())
//            .initializeComponent(RiveInitializer::class.java)

        lifecycleScope.launch(context = Dispatchers.Main) {
            delay(2000)
            findNavController().navigate(R.id.action_animationFragment_to_webFragment)
        }
//        Rive.init(requireContext())
    }
}