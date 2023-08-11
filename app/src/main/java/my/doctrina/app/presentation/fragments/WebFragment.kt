package my.doctrina.app.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import my.doctrina.app.R
import my.doctrina.app.data.*
import my.doctrina.app.databinding.FragmentWebBinding


class WebFragment : Fragment(R.layout.fragment_web) {
    private lateinit var binding: FragmentWebBinding

    private lateinit var buttonIdList: ArrayList<ImageButton>
    private lateinit var userPrefs: SharedPreferences
    private lateinit var authJson: JsonObject
    private lateinit var userObject: JsonObject

    private var accessExpired = 0
    private var accessToken = ""
    private var refreshExpired = 0
    private var refreshToken = ""
    private var success = false

    private lateinit var menuLinks: ArrayList<MenuItem>

    private lateinit var historyMenuLinks: ArrayList<MenuItem>

//    companion object {
//        var webHeaderContent = ""
//    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWebBinding.bind(view)
        userPrefs =
            requireActivity().getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)
        accessExpired = userPrefs.getInt("access_expired", 0)
        accessToken = userPrefs.getString("access_token", "").toString()
        refreshExpired = userPrefs.getInt("refresh_expired", 0)
        refreshToken = userPrefs.getString("refresh_token", "").toString()
        success = userPrefs.getBoolean("success", false)

        buttonIdList = ArrayList()
        historyMenuLinks = ArrayList()
        menuLinks = arrayListOf(
            MenuItem(MAIN_LINK),
            MenuItem(MATERIALS_LINK),
            MenuItem(FEEDBACK_LINK),
            MenuItem(FAVORITE_LINK),
            MenuItem(SETTINGS_LINK),
        )

        with(binding) {
            userLoginBody(accessExpired, accessToken, refreshExpired, refreshToken, success)
            webView.apply {

                settings.apply {
                    javaScriptEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportZoom(true)
                    domStorageEnabled = true
                    builtInZoomControls = true
                    allowFileAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    mediaPlaybackRequiresUserGesture = false
                }

                if (savedInstanceState != null) {
                    webView.restoreState(savedInstanceState)
                } else {
                    setWebViewLoadingMode()
                }

                addJavascriptInterface(JavaScriptInterface(), "AndroidInterface")

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (url != null) {
                            Log.d("loaded link", "onPageStarted - $url")
                        }
                        super.onPageStarted(view, url, favicon)
                        setUserAuth(authJson, view)
                        refreshTitle()
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        view?.loadUrl(request?.url.toString())
                        val url = request?.url.toString()
                        if (view != null) {
                            setWebViewActive(view)
                        }
                        Log.d("loaded link", "shouldOverrideUrlLoading - $url")
                        return true
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String,
                        isReload: Boolean
                    ) {
                        Log.d("loaded link", "doUpdateVisitedHistory - $url and $isReload")
                        super.doUpdateVisitedHistory(view, url, isReload)
                        if (getCurrentLink() == url) {
                            return
                        }
                        changeButtonStateAndAddToHistory(url)
                        refreshTitle()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        super.onPageFinished(view, url)
                        setUserAuth(authJson, view)
                        refreshTitle()
                        if (url != null) {
                            Log.d("loaded link", "onPageFinished - $url")
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {

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
                onBackButtonClicked()
            }

            logOutLinearLayout.setOnClickListener {
                findNavController().navigate(R.id.action_webFragment_to_loginFragment)
            }

            changeButtonStateAndAddToHistory(MAIN_LINK)
            flagBtnMenuWeb.setOnClickListener {
                changeButtonStateAndAddToHistory(MAIN_LINK)
            }

            saveToBtnMenuWeb.setOnClickListener {
                changeButtonStateAndAddToHistory(MATERIALS_LINK)
            }

            chatBtnMenuWeb.setOnClickListener {
                changeButtonStateAndAddToHistory(FEEDBACK_LINK)
            }

            starBtnMenuWeb.setOnClickListener {
                changeButtonStateAndAddToHistory(FAVORITE_LINK)
            }

            settingsBtnMenuWeb.setOnClickListener {
                changeButtonStateAndAddToHistory(SETTINGS_LINK)
            }
        }
        onBackPressed()
    }

    private fun setUserAuth(authJson: JsonObject, view: WebView?) {
        with(view) {
            this?.evaluateJavascript(
                "localStorage.setItem('ember_simple_auth-session', '$authJson');",
                null
            )
            this?.loadUrl("javascript:localStorage.setItem('ember_simple_auth-session', '$authJson');")
        }
    }

    private fun userLoginBody(
        accessExpired: Int,
        accessToken: String,
        refreshExpired: Int,
        refreshToken: String,
        success: Boolean
    ) {
        userObject = JsonObject()
        userObject.apply {
            addProperty("access_expired", accessExpired)
            addProperty("access_token", accessToken)
            addProperty("refresh_expired", refreshExpired)
            addProperty("refresh_token", refreshToken)
            addProperty("success", success)
        }
        authJson = JsonObject()
        authJson.add("authenticated", userObject)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
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

    private fun getCurrentMenuItem(): MenuItem? {
        return if (historyMenuLinks.isNotEmpty()) {
            val currentItem = historyMenuLinks[historyMenuLinks.lastIndex]
            if (currentItem.subItem != null) {
                if (currentItem.subItem!!.subItem != null) {
                    return currentItem.subItem!!.subItem
                } else {
                    return currentItem.subItem
                }
            } else {
                return currentItem
            }
        } else {
            null
        }
    }

    private fun getCurrentLink(): String {
        return if (historyMenuLinks.isNotEmpty()) {
            historyMenuLinks[historyMenuLinks.lastIndex].url
        } else {
            MAIN_LINK
        }
    }

    private fun setWebViewActive(view: WebView) {
        view.apply {
            visibility = View.VISIBLE
            loadUrl(getCurrentLink())
        }
    }

    private fun setLayoutVisibilitySettings(
        webView: WebView,
        noInternetLayout: LinearLayoutCompat
    ) {
        webView.visibility = View.GONE
        noInternetLayout.visibility = View.GONE
    }

    private fun onBackButtonClicked() {
        if (getCurrentMenuItem()?.subItem != null) {
            getCurrentMenuItem()?.subItem = null
            binding.webView.goBack()
        } else {
            if (historyMenuLinks.size != 0) {
                backBtnHistoryChange()
            }
            binding.webView.goBack()
        }
        refreshTitle()
    }

    private fun refreshTitle() {
        setTitleByLink()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackButtonClicked()
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


    private fun backBtnHistoryChange() {
        setInactiveIcons()
        historyMenuLinks.removeAt(historyMenuLinks.lastIndex)
        val link = getCurrentLink()
        changeBtnState(link)
        setLogoutVisibility(link)
    }

    private fun setLogoutVisibility(link: String) {
        with(binding) {
            if (link == SETTINGS_LINK) {
                logOutLinearLayout.visibility = View.VISIBLE
            } else {
                logOutLinearLayout.visibility = View.GONE
            }
        }
    }

    private fun changeBtnState(
        link: String
    ) {
        setInactiveIcons()
        with(binding) {
            when (link) {
                MAIN_LINK -> {
                    flagBtnMenuWeb.setImageResource(R.drawable.flag_yellow)
                }
                MATERIALS_LINK -> {
                    saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
                }
                FEEDBACK_LINK -> {
                    chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud_yellow)
                }
                FAVORITE_LINK -> {
                    starBtnMenuWeb.setImageResource(R.drawable.star_yellow)
                }
                SETTINGS_LINK -> {
                    settingsBtnMenuWeb.setImageResource(R.drawable.settings_yellow)
                }
                MATERIALS_ID_LINK -> {
                    saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
                }
            }
        }
        with(binding) {
            webView.loadUrl(link)
        }
        setLogoutVisibility(link)
    }

    private fun changeButtonStateAndAddToHistory(link: String) {
        changeBtnState(link)
        addMenuItemToHistory(link)
    }

    private fun addMenuItemToHistory(link: String) {
        menuLinks.forEach { menuLink ->
            if (menuLink.url == link) {
                historyMenuLinks.add(menuLink)
                return
            } else {
                if (link == MATERIALS_ID_LINK) {
                    historyMenuLinks.add(MenuItem(MATERIALS_ID_LINK))
                    return
                } else if (link.contains("video")) {
                    historyMenuLinks.add(MenuItem(link))
                    return
                }
            }
        }
    }

    private fun setTitleByLink() {
        with(binding) {
            val title = getCurrentMenuItem()?.getName(requireContext())
            titleTextView.text = title ?: "null"
        }
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

//    override fun onResume() {
//        super.onResume()
//        binding.webView.evaluateJavascript("javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });",
//            object : ValueCallback<String?> {
//                override fun onReceiveValue(p0: String?) {
//                    if (p0 != null) {
//                        Log.d("!!!", p0)
//                    }
//                }
//            })
//        binding.webView.loadUrl("javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });")
//    }

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun onMessageReceived(message: String) {
            Log.d("JavaScriptInterface", "Received message: $message")
        }
    }
}










