package my.doctrina.app.presentation.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import my.doctrina.app.R
import my.doctrina.app.data.api.DOCApi
import my.doctrina.app.data.api.ServiceBuilder
import my.doctrina.app.data.api.request.SignInConfirmRequest
import my.doctrina.app.data.api.response.SignInConfirmResponse
import my.doctrina.app.data.repository.SharedPreferencesRepository
import my.doctrina.app.databinding.FragmentLoginCodeConfirmationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginCodeConfirmationFragment : Fragment(R.layout.fragment_login_code_confirmation) {
    private lateinit var binding: FragmentLoginCodeConfirmationBinding
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    private var emailSP = ""
    private var imageUrlSP = ""

    private var code = ""

    private lateinit var codeNumberEditTextList: ArrayList<AppCompatEditText>

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginCodeConfirmationBinding.bind(view)

        sharedPreferencesRepository = SharedPreferencesRepository(requireContext())

        emailSP = sharedPreferencesRepository.getEmail()
        imageUrlSP = sharedPreferencesRepository.getImageUrl("imageUrl", "")
        codeNumberEditTextList = ArrayList()

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

    override fun onResume() {
        super.onResume()
        with(binding) {
            setInactiveMode(firstNumber)
            setInactiveMode(secondNumber)
            setInactiveMode(thirdNumber)
            setInactiveMode(fourthNumber)
            setInactiveMode(fifthNumber)
        }
    }

    private fun setInactiveMode(number: AppCompatEditText) {
        if (!number.isFocused || number.isFocused && number.text?.isEmpty() == true
        ) {
            DrawableCompat.setTint(
                number.background,
                ContextCompat.getColor(requireContext(), R.color.white)
            )
            with(binding) {
                nextTV.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                arrowRight.setImageResource(R.drawable.arrow_right)
            }
        }
    }

    private fun textChangeListenerToEditText(number: AppCompatEditText) {
        codeNumberEditTextList.add(number)
        number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                DrawableCompat.setTint(
                    number.background,
                    ContextCompat.getColor(requireContext(), R.color.yellow)
                )
                with(binding) {
                    nextTV.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.yellow
                        )
                    )
                    arrowRight.setImageResource(R.drawable.arrow_right_yellow)
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                setCodeEditTextFocus(p0, number)
            }

        })
    }

    private fun setCodeEditTextFocus(c: Editable?, editText: AppCompatEditText) {
        val index = codeNumberEditTextList.indexOf(editText)
        if (c != null) {
            if (c.length == 1) {
                if (index != codeNumberEditTextList.size - 1) {
                    codeNumberEditTextList[index + 1].requestFocus()
                } else {
                    editText.clearFocus()
                }
            }
        }
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
                        sharedPreferencesRepository.getUserData(response.body())
                        findNavController().navigate(R.id.action_loginCodeConfirmationFragment_to_animationFragment)
                    } else {
                        with(binding) {
                            message.setText(R.string.please_enter_code)
                            message.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
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


