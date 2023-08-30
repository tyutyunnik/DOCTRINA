package my.doctrina.app.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.doctrina.app.R
import my.doctrina.app.databinding.FragmentAnimationBinding

class AnimationFragment : Fragment(R.layout.fragment_animation) {
    private lateinit var binding: FragmentAnimationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimationBinding.bind(view)

        with(binding.anim) {
            playAnimation()
        }

        lifecycleScope.launch(context = Dispatchers.Main) {
            delay(2000)
            findNavController().navigate(R.id.action_animationFragment_to_webFragment)
        }
    }
}