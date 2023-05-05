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
    private var element = 0
    private var buttonState = "white"

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
                // TODO: отследить последовательность(математика)
                if (webView.canGoBack()) {
                    if (buttonIdList.size != 0) {
                        lastItemIndex = buttonIdList.size - 1
                        if (buttonIdList[lastItemIndex - 1] == flagBtnMenuWeb) {
                            setInactiveIcons()
                            flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == saveToBtnMenuWeb) {
                            setInactiveIcons()
                            saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == chatBtnMenuWeb) {
                            setInactiveIcons()
                            chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == starBtnMenuWeb) {
                            setInactiveIcons()
                            starBtnMenuWeb.setImageResource(R.drawable.star_yellow)
                        } else if (buttonIdList[lastItemIndex - 1] == settingsBtnMenuWeb) {
                            setInactiveIcons()
                            settingsBtnMenuWeb.setImageResource(R.drawable.settings_yellow)
                        }
                    }
                    webView.goBack()

//                    if (index != 0) {
//                        element = buttonIdList[index - 1]
//                        when (element) {
//                            0 -> {
//                                setInactiveIcons()
//                                flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
//                            }
//                            1 -> {
//                                setInactiveIcons()
//                                saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
//                            }
//                            2 -> {
//                                setInactiveIcons()
//                                chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud_yellow)
//                            }
//                            3 -> {
//                                setInactiveIcons()
//                                starBtnMenuWeb.setImageResource(R.drawable.star_yellow)
//                            }
//                            4 -> {
//                                setInactiveIcons()
//                                settingsBtnMenuWeb.setImageResource(R.drawable.settings_yellow)
//                            }
//                        }
//                    }
                }
            }

            logOutLinearLayout.setOnClickListener {
                findNavController().navigate(R.id.action_webFragment_to_loginFragment)
            }

            flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
            flagBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
                link = "https://www.google.com"
//                index = 0
                webView.loadUrl(link)
                titleWeb.visibility = View.GONE
                logOutLinearLayout.visibility = View.GONE
                buttonIdList.add(flagBtnMenuWeb)
            }

            saveToBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
                link = "https://github.com"
//                index += 1
                webView.loadUrl(link)
                titleWeb.visibility = View.VISIBLE
                titleWeb.text = resources.getString(R.string.choose_a_le).uppercase()
                logOutLinearLayout.visibility = View.GONE
                buttonIdList.add(saveToBtnMenuWeb)
            }

            chatBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud_yellow)
                link = "https://stackoverflow.com/"
//                index = 2
                webView.loadUrl(link)
                titleWeb.visibility = View.GONE
                logOutLinearLayout.visibility = View.GONE
                buttonIdList.add(chatBtnMenuWeb)
            }

            starBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                starBtnMenuWeb.setImageResource(R.drawable.star_yellow)
                link = "https://developer.android.com/"
//                index = 3
                webView.loadUrl(link)
                titleWeb.visibility = View.GONE
                logOutLinearLayout.visibility = View.GONE
                buttonIdList.add(starBtnMenuWeb)
            }

            settingsBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                settingsBtnMenuWeb.setImageResource(R.drawable.settings_yellow)
                link = "https://developer.android.com/studio"
//                index = 4
                webView.loadUrl(link)
                titleWeb.visibility = View.VISIBLE
                titleWeb.text = resources.getString(R.string.your_setting).uppercase()
                logOutLinearLayout.visibility = View.VISIBLE
                buttonIdList.add(settingsBtnMenuWeb)
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
}