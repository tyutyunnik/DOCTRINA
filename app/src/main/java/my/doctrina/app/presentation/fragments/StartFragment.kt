package my.doctrina.app.presentation.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import my.doctrina.app.R
import my.doctrina.app.databinding.FragmentStartBinding

@AndroidEntryPoint
class StartFragment : Fragment(R.layout.fragment_start) {
    private lateinit var binding: FragmentStartBinding

    private lateinit var userPrefs: SharedPreferences

    private var accessToken = ""
    private var refreshToken = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStartBinding.bind(view)
        userPrefs =
            requireActivity().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        accessToken = userPrefs.getString("access_token", "").toString()
        refreshToken = userPrefs.getString("refresh_token", "").toString()

        if (accessToken == "" || refreshToken == "") {
            findNavController().navigate(R.id.action_startFragment_to_loginFragment)
        } else {
            findNavController().navigate(R.id.action_startFragment_to_animationFragment)
        }
    }
}