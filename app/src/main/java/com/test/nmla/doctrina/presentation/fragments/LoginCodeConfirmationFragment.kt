package com.test.nmla.doctrina.presentation.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.test.nmla.doctrina.R
import com.test.nmla.doctrina.data.api.DOCApi
import com.test.nmla.doctrina.data.api.ServiceBuilder
import com.test.nmla.doctrina.data.api.request.SignInConfirmRequest
import com.test.nmla.doctrina.data.api.response.SignInConfirmResponse
import com.test.nmla.doctrina.databinding.FragmentLoginCodeConfirmationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginCodeConfirmationFragment : Fragment(R.layout.fragment_login_code_confirmation) {
    private lateinit var binding: FragmentLoginCodeConfirmationBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var emailSP = ""
    private var imageUrlSP = ""

    private var code = ""

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginCodeConfirmationBinding.bind(view)
        sharedPreferences =
            requireActivity().getSharedPreferences("sharedP", AppCompatActivity.MODE_PRIVATE)
        emailSP = sharedPreferences.getString("email", "").toString()
        imageUrlSP = sharedPreferences.getString("imageUrl", "").toString()

        with(binding) {

            Glide.with(requireContext()).load(imageUrlSP).into(frontImage)

            textChangeListenerToEditText(firstNumber)
            textChangeListenerToEditText(secondNumber)
            textChangeListenerToEditText(thirdNumber)
            textChangeListenerToEditText(fourthNumber)
            textChangeListenerToEditText(fifthNumber)

            message.text = "paste the code sent to \n$emailSP"

            nextLinearLayout.setOnClickListener {
                getCodeNumbers()
                newUserCodeVerification(emailSP, code)
            }

            changeBtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun textChangeListenerToEditText(number: AppCompatEditText) {
        number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                DrawableCompat.setTint(
                    number.background,
                    ContextCompat.getColor(requireContext(), R.color.yellow)
                )
                with(binding) {
                    nextTV.setTextColor(Color.parseColor("#EEE919"))
                    arrowRight.setImageResource(R.drawable.arrow_right_yellow)
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun getCodeNumbers() {
        code = with(binding) {
            firstNumber.text.toString() +
                    secondNumber.text.toString() +
                    thirdNumber.text.toString() +
                    fourthNumber.text.toString() +
                    fifthNumber.text.toString()
        }
    }

    private fun newUserCodeVerification(email: String, code: String) {
        val signInConfirmRequest = SignInConfirmRequest(email, code)

        val response = ServiceBuilder.buildService(DOCApi::class.java)
        response.signInConfirm(signInConfirmRequest).enqueue(
            object : Callback<SignInConfirmResponse> {
                override fun onResponse(
                    call: Call<SignInConfirmResponse>,
                    response: Response<SignInConfirmResponse>
                ) {
                    if (response.isSuccessful) {
                        findNavController().navigate(R.id.action_loginCodeConfirmationFragment_to_animationFragment)
                    } else {
                        with(binding) {
                            message.setText(R.string.please_enter_code)
                            message.setTextColor(Color.parseColor("#FF1818"))
                        }
                    }
                }

                override fun onFailure(call: Call<SignInConfirmResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}