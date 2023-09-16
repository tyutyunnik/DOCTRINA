package my.doctrina.app.presentation.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import my.doctrina.app.R
import my.doctrina.app.databinding.FragmentAnimationBinding
import my.doctrina.app.presentation.viewmodels.AnimationViewModel

class AnimationFragment : Fragment(R.layout.fragment_animation) {
    private lateinit var binding: FragmentAnimationBinding
    private val viewModel: AnimationViewModel by viewModels()
    private lateinit var userPrefs: SharedPreferences
    private var accessExpired = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAnimationBinding.bind(view)
        userPrefs =
            requireActivity().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        accessExpired = userPrefs.getInt("access_expired", 0)

        with(binding.anim) {
            playAnimation()
        }

        lifecycleScope.launch(context = Dispatchers.Main) {
            delay(2000)
            if (viewModel.isUserTokenExpired(accessExpired)) {
                userPrefs.edit().putString("access_token", "").apply()
                findNavController().navigate(R.id.action_animationFragment_to_startFragment)
            } else {
                findNavController().navigate(R.id.action_animationFragment_to_webFragment)
            }
        }
    }
}