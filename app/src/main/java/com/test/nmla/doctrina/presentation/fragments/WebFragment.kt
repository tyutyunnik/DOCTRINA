package com.test.nmla.doctrina.presentation.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.test.nmla.doctrina.R
import com.test.nmla.doctrina.databinding.FragmentWebBinding

class WebFragment : Fragment(R.layout.fragment_web) {
    private lateinit var binding: FragmentWebBinding

    private var link = ""
    private lateinit var buttonIdList: ArrayList<ImageButton>
    private var lastItemIndex = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWebBinding.bind(view)

        link = "https://www.google.com"
        buttonIdList = ArrayList()

        with(binding) {
            webView.settings.javaScriptEnabled = true
            webView.settings.setSupportZoom(true)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        view?.loadUrl(url)
                    }
                    return true
                }
            }
            webView.loadUrl(link)

            buttonIdList.add(flagBtnMenuWeb)

            backBtnWeb.setOnClickListener {
                if (webView.canGoBack()) {
                    if (buttonIdList.size != 0) {
                        lastItemIndex = buttonIdList.size - 1
                        if (buttonIdList[lastItemIndex - 1] == flagBtnMenuWeb) {
                            setInactiveIcons()
                            backBtnHistoryChange(flagBtnMenuWeb, R.drawable.flag_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == saveToBtnMenuWeb) {
                            setInactiveIcons()
                            backBtnHistoryChange(saveToBtnMenuWeb, R.drawable.save_to_yellow)
                            titleWeb.text = resources.getString(R.string.choose_a_le).uppercase()
                        } else if (buttonIdList[lastItemIndex - 1] == chatBtnMenuWeb) {
                            setInactiveIcons()
                            backBtnHistoryChange(chatBtnMenuWeb, R.drawable.talk_cloud_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == starBtnMenuWeb) {
                            setInactiveIcons()
                            backBtnHistoryChange(starBtnMenuWeb, R.drawable.star_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == settingsBtnMenuWeb) {
                            setInactiveIcons()
                            backBtnHistoryChange(settingsBtnMenuWeb, R.drawable.settings_yellow)
                            titleWeb.text = resources.getString(R.string.your_setting).uppercase()
                        }
                    }
                    webView.goBack()
                }
            }

            logOutLinearLayout.setOnClickListener {
                findNavController().navigate(R.id.action_webFragment_to_loginFragment)
            }

            flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
            flagBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    flagBtnMenuWeb, R.drawable.flag_yellow,
                    "https://www.google.com", ""
                )
            }

            saveToBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    saveToBtnMenuWeb, R.drawable.save_to_yellow,
                    "https://github.com", resources.getString(R.string.choose_a_le).uppercase()
                )
            }

            chatBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    chatBtnMenuWeb, R.drawable.talk_cloud_yellow,
                    "https://stackoverflow.com/", ""
                )
            }

            starBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    starBtnMenuWeb, R.drawable.star_yellow,
                    "https://developer.android.com/", ""
                )
            }

            settingsBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    settingsBtnMenuWeb, R.drawable.settings_yellow,
                    "https://developer.android.com/studio",
                    resources.getString(R.string.your_setting).uppercase()
                )
            }
        }
    }

    private fun setInactiveIcons() {
        with(binding) {
            flagBtnMenuWeb.setImageResource(R.drawable.flag)
            saveToBtnMenuWeb.setImageResource(R.drawable.save_to)
            chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud)
            starBtnMenuWeb.setImageResource(R.drawable.star)
            settingsBtnMenuWeb.setImageResource(R.drawable.settings)
        }
    }

    private fun backBtnHistoryChange(imageBtn: ImageButton, imageResource: Int) {
        imageBtn.setImageResource(imageResource)
        buttonIdList.removeAt(lastItemIndex)
        setTitleAndLogoutVisibility(imageBtn)
    }

    private fun setTitleAndLogoutVisibility(imageBtn: ImageButton) {
        with(binding) {
            if (imageBtn == settingsBtnMenuWeb || imageBtn == saveToBtnMenuWeb) {
                titleWeb.visibility = View.VISIBLE
            } else {
                titleWeb.visibility = View.GONE
            }
            if (imageBtn == settingsBtnMenuWeb) {
                logOutLinearLayout.visibility = View.VISIBLE
            } else {
                logOutLinearLayout.visibility = View.GONE
            }
        }
    }

    private fun changeBtnState(
        imageBtn: ImageButton, resourceId: Int, link: String, title: String
    ) {
        imageBtn.setImageResource(resourceId)
        with(binding) {
            webView.loadUrl(link)
            titleWeb.text = title
        }
        setTitleAndLogoutVisibility(imageBtn)
        buttonIdList.add(imageBtn)
    }
}