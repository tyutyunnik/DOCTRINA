package com.test.nmla.doctrina.presentation.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.test.nmla.doctrina.R
import com.test.nmla.doctrina.databinding.FragmentWebBinding

class WebFragment : Fragment(R.layout.fragment_web) {
    private lateinit var binding: FragmentWebBinding

    private var link = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWebBinding.bind(view)

        link = "https://www.google.com"

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


            backBtnWeb.setOnClickListener {
                if (webView.canGoBack()) {
                    webView.goBack()
                }
            }

            flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
            flagBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
                link = "https://www.google.com"
                webView.loadUrl(link)
                titleWeb.visibility = View.GONE
            }

            saveToBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
                link = "https://github.com"
                webView.loadUrl(link)
                titleWeb.visibility = View.VISIBLE
                titleWeb.text = resources.getString(R.string.choose_a_le).uppercase()
            }

            chatBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud_yellow)
                link = "https://stackoverflow.com/"
                webView.loadUrl(link)
                titleWeb.visibility = View.GONE
            }

            starBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                starBtnMenuWeb.setImageResource(R.drawable.star_yellow)
                link = "https://developer.android.com/"
                webView.loadUrl(link)
                titleWeb.visibility = View.GONE
            }

            settingsBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                settingsBtnMenuWeb.setImageResource(R.drawable.settings_yellow)
                link = "https://developer.android.com/studio"
                webView.loadUrl(link)
                titleWeb.visibility = View.VISIBLE
                titleWeb.text = resources.getString(R.string.your_setting).uppercase()
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