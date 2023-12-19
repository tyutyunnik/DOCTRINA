package my.doctrina.app.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import my.doctrina.app.R
import my.doctrina.app.data.api.DOCApi
import my.doctrina.app.data.api.ServiceBuilder
import my.doctrina.app.data.api.request.SignInRequest
import my.doctrina.app.data.api.response.SignInResponse
import my.doctrina.app.data.repository.SharedPreferencesRepository
import my.doctrina.app.databinding.FragmentLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

//    private val viewModel: LoginViewModel by viewModels()

    private lateinit var emailValidate: String

    private var imageUrl = "https://my.doctrina.app/mobile.png"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        sharedPreferencesRepository = SharedPreferencesRepository(requireContext())

        sharedPreferencesRepository.saveImageUrl("imageUrl", imageUrl)

        with(binding) {

            Glide.with(requireContext()).load(imageUrl).into(frontImage)

            email.apply {
                hint = resources.getString(R.string.your_email_g).uppercase()
                filters = arrayOf<InputFilter>(AllCaps())
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        emailRemoveBtn.visibility = View.VISIBLE
                        line.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.yellow
                            )
                        )
                        nextTV.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.yellow
                            )
                        )
                        arrowRight.setImageResource(R.drawable.arrow_right_yellow)
                        message.visibility = View.VISIBLE
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun afterTextChanged(p0: Editable?) {
                    }
                })
            }

            emailRemoveBtn.setOnClickListener {
                email.setText("")
                setInactiveMode()
            }

            nextLinearLayout.setOnClickListener {
                emailValidate = email.text.toString().lowercase().trim()

                if (emailValidate.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailValidate)
                        .matches()
                ) {

                    sharedPreferencesRepository.saveEmail("email", emailValidate)

                    userEmailVerification(emailValidate)
                } else {
                    showIncorrectEmailMessage()
                }
            }
        }

    }

    private fun setInactiveMode() {
        with(binding) {
            line.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            nextTV.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            arrowRight.setImageResource(R.drawable.arrow_right)
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            if (!email.isFocused || email.isFocused && email.text?.isEmpty() == true) {
                emailRemoveBtn.visibility = View.GONE
                setInactiveMode()
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
            message.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
        }
    }
}