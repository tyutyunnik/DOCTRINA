package my.doctrina.app.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import my.doctrina.app.R
import my.doctrina.app.databinding.FragmentWebBinding


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

                override fun onPageFinished(view: WebView?, url: String?) {
                    swipeRefreshLayout.isRefreshing = false
                    if (url != null) {
                        link = url
                    }
                    super.onPageFinished(view, url)
                }
            }

            setLayoutVisibilitySettings(webView, noInternetLayout)
            setWebViewLoadingMode()

            swipeRefreshLayout.setOnRefreshListener {
                setWebViewLoadingMode()
            }

            refreshLayout.setOnClickListener {
                setWebViewLoadingMode()
            }

            buttonIdList.add(flagBtnMenuWeb)

            backBtnWeb.setOnClickListener {
                setBackButtonsSettings()
            }

            logOutLinearLayout.setOnClickListener {
                findNavController().navigate(R.id.action_webFragment_to_loginFragment)
            }

            flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
            flagBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    flagBtnMenuWeb, R.drawable.flag_yellow,
                    "https://www.google.com"
                )
            }

            saveToBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    saveToBtnMenuWeb, R.drawable.save_to_yellow,
                    "https://github.com"
                )
            }

            chatBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    chatBtnMenuWeb, R.drawable.talk_cloud_yellow,
                    "https://stackoverflow.com/"
                )
            }

            starBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    starBtnMenuWeb, R.drawable.star_yellow,
                    "https://developer.android.com/"
                )
            }

            settingsBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    settingsBtnMenuWeb, R.drawable.settings_yellow,
                    "https://developer.android.com/studio"
                )
            }
        }
        onBackPressed()
    }

    private fun setWebViewLoadingMode() {
        with(binding) {
            setLayoutVisibilitySettings(webView, noInternetLayout)
            if (isNetworkAvailable(requireContext())) {
                setWebViewActive(webView)
            } else {
                noInternetLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun setWebViewActive(view: WebView) {
        view.apply {
            visibility = View.VISIBLE
            loadUrl(link)
        }
    }

    private fun setLayoutVisibilitySettings(
        webView: WebView,
        noInternetLayout: LinearLayoutCompat
    ) {
        webView.visibility = View.GONE
        noInternetLayout.visibility = View.GONE
    }

    private fun setBackButtonsSettings() {
        with(binding) {
            if (webView.canGoBack()) {
                if (buttonIdList.size != 0) {
                    lastItemIndex = buttonIdList.size - 1
                    if (buttonIdList[lastItemIndex - 1] == flagBtnMenuWeb) {
                        setInactiveIcons()
                        backBtnHistoryChange(flagBtnMenuWeb, R.drawable.flag_yellow)
                    } else if (buttonIdList[lastItemIndex - 1] == saveToBtnMenuWeb) {
                        setInactiveIcons()
                        backBtnHistoryChange(saveToBtnMenuWeb, R.drawable.save_to_yellow)
                    } else if (buttonIdList[lastItemIndex - 1] == chatBtnMenuWeb) {
                        setInactiveIcons()
                        backBtnHistoryChange(chatBtnMenuWeb, R.drawable.talk_cloud_yellow)
                    } else if (buttonIdList[lastItemIndex - 1] == starBtnMenuWeb) {
                        setInactiveIcons()
                        backBtnHistoryChange(starBtnMenuWeb, R.drawable.star_yellow)
                    } else if (buttonIdList[lastItemIndex - 1] == settingsBtnMenuWeb) {
                        setInactiveIcons()
                        backBtnHistoryChange(settingsBtnMenuWeb, R.drawable.settings_yellow)
                    }
                }
                webView.goBack()
            }
        }

    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    setBackButtonsSettings()
                }
            }
        )
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
        setLogoutVisibility(imageBtn)
    }

    private fun setLogoutVisibility(imageBtn: ImageButton) {
        with(binding) {
            if (imageBtn == settingsBtnMenuWeb) {
                logOutLinearLayout.visibility = View.VISIBLE
            } else {
                logOutLinearLayout.visibility = View.GONE
            }
        }
    }

    private fun changeBtnState(
        imageBtn: ImageButton, resourceId: Int, link: String
    ) {
        imageBtn.setImageResource(resourceId)
        with(binding) {
            webView.loadUrl(link)
        }
        setLogoutVisibility(imageBtn)
        buttonIdList.add(imageBtn)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        return result
    }
}