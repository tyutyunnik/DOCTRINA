package com.test.nmla.doctrina.presentation.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.test.nmla.doctrina.R
import com.test.nmla.doctrina.data.api.DOCApi
import com.test.nmla.doctrina.data.api.ServiceBuilder
import com.test.nmla.doctrina.data.api.request.SignInRequest
import com.test.nmla.doctrina.data.api.response.SignInResponse
import com.test.nmla.doctrina.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var emailValidate: String
    private var emailSP = ""
    private var imageUrl = "https://my.doctrina.app/mobile.png"
    private var imageUrlSP = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        sharedPreferences =
            requireActivity().getSharedPreferences("sharedP", AppCompatActivity.MODE_PRIVATE)
        emailSP = sharedPreferences.getString("email", "").toString()
        imageUrlSP = sharedPreferences.getString("imageUrl", "").toString()

        sharedPreferences.edit().putString("imageUrl", imageUrl).apply()

        with(binding) {

            Glide.with(requireContext()).load(imageUrl).into(frontImage)

            email.hint = resources.getString(R.string.your_email_g).uppercase();
            email.filters = arrayOf<InputFilter>(AllCaps())
            email.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    emailRemoveBtn.visibility = View.VISIBLE
                    line.setBackgroundColor(Color.parseColor("#EEE919"))
                    nextTV.setTextColor(Color.parseColor("#EEE919"))
                    arrowRight.setImageResource(R.drawable.arrow_right_yellow)
                    message.visibility = View.VISIBLE
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
            emailRemoveBtn.setOnClickListener {
                email.setText("")
                line.setBackgroundColor(Color.parseColor("#FFFFFF"))
                nextTV.setTextColor(Color.parseColor("#FFFFFF"))
                arrowRight.setImageResource(R.drawable.arrow_right)
            }

            nextLinearLayout.setOnClickListener {
                emailValidate = email.text.toString().lowercase().trim()

                if (emailValidate.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailValidate)
                        .matches()
                ) {
                    sharedPreferences.edit().putString("email", emailValidate).apply()
                    userEmailVerification(emailValidate)
                } else {
                    showIncorrectEmailMessage()
                }
            }
        }

    }

    private fun userEmailVerification(email: String) {
        val signInRequest = SignInRequest(email)

        val response = ServiceBuilder.buildService(DOCApi::class.java)
        response.signIn(signInRequest).enqueue(
            object : Callback<SignInResponse> {
                override fun onResponse(
                    call: Call<SignInResponse>,
                    response: Response<SignInResponse>
                ) {
                    if (response.isSuccessful) {
                        findNavController().navigate(R.id.action_loginFragment_to_loginCodeConfirmationFragment)
                    } else {
                        showIncorrectEmailMessage()
                    }
                }

                override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun showIncorrectEmailMessage() {
        with(binding) {
            message.setText(R.string.please_enter)
            message.setTextColor(Color.parseColor("#FF1818"))
        }
    }
}