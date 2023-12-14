package my.doctrina.app.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import my.doctrina.app.R
import my.doctrina.app.data.*
import my.doctrina.app.data.repository.SharedPreferencesRepository
import my.doctrina.app.databinding.FragmentWebBinding
import my.doctrina.app.presentation.viewmodels.WebViewModel
import org.json.JSONObject

@AndroidEntryPoint
class WebFragment : Fragment(R.layout.fragment_web) {
    private lateinit var binding: FragmentWebBinding
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    private val viewModel: WebViewModel by viewModels()

    private lateinit var buttonIdList: ArrayList<ImageButton>

    private lateinit var authJson: JsonObject
    private lateinit var userObject: JsonObject

    private var accessExpired = 0
    private var accessToken = ""
    private var refreshExpired = 0
    private var refreshToken = ""
    private var success = false

    private lateinit var menuLinks: ArrayList<MenuItem>

    private lateinit var historyMenuLinks: ArrayList<MenuItem>

    private var backBtnVisible = false

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWebBinding.bind(view)
        sharedPreferencesRepository = SharedPreferencesRepository(requireContext())

        accessExpired = sharedPreferencesRepository.getAccessExpirationUserData("access_expired", 0)
        accessToken = sharedPreferencesRepository.getAccessUserToken("access_token", "")
        refreshExpired =
            sharedPreferencesRepository.getRefreshExpirationUserData("refresh_expired", 0)
        refreshToken =
            sharedPreferencesRepository.getRefreshUserToken("refresh_token", "")
        success =
            sharedPreferencesRepository.getStatusUserData("success", false)

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
                    builtInZoomControls = true
                    displayZoomControls = false
                    domStorageEnabled = true
                    builtInZoomControls = true
                    allowFileAccess = true
                    allowContentAccess = true
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
                    var fullscreen: View? = null

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        super.onShowCustomView(view, callback)

                        webView.visibility = View.GONE

                        if (fullscreen != null) {
                            (requireActivity().window.decorView as FrameLayout).removeView(
                                fullscreen
                            )
                        }

                        fullscreen = view
                        (requireActivity().window.decorView as FrameLayout).addView(
                            fullscreen,
                            FrameLayout.LayoutParams(-1, -1)
                        )
                        fullscreen!!.visibility = View.VISIBLE

                        hideSystemUI()
                    }

                    override fun onHideCustomView() {
                        super.onHideCustomView()
                        fullscreen?.visibility = View.GONE
                        webView.visibility = View.VISIBLE
                        showSystemUI()
                    }
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

            buttonIdList.add(homeBtnMenuWeb)

            backBtnWeb.setOnClickListener {
                onBackButtonClicked()
            }

            logOutLinearLayout.setOnClickListener {
                findNavController().navigate(R.id.action_webFragment_to_loginFragment)
            }

            changeButtonStateAndAddToHistory(MAIN_LINK)
            homeBtnMenuWeb.setOnClickListener {
                showBackButton()
                changeButtonStateAndAddToHistory(MAIN_LINK)
            }

            phonesBtnMenuWeb.setOnClickListener {
                showBackButton()
                changeButtonStateAndAddToHistory(MATERIALS_LINK)
            }

            chatBtnMenuWeb.setOnClickListener {
                showBackButton()
                changeButtonStateAndAddToHistory(FEEDBACK_LINK)
            }

            favouriteBtnMenuWeb.setOnClickListener {
                showBackButton()
                changeButtonStateAndAddToHistory(FAVORITE_LINK)
            }

            settingsBtnMenuWeb.setOnClickListener {
                showBackButton()
                changeButtonStateAndAddToHistory(SETTINGS_LINK)
            }
        }

        viewModel.test()

        onBackPressed()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, binding.webView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        WindowInsetsControllerCompat(requireActivity().window, binding.webView).show(
            WindowInsetsCompat.Type.systemBars()
        )
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
            homeBtnMenuWeb.setImageResource(R.drawable.home)
            phonesBtnMenuWeb.setImageResource(R.drawable.phones)
            chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud)
            favouriteBtnMenuWeb.setImageResource(R.drawable.heart)
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
                    homeBtnMenuWeb.setImageResource(R.drawable.home_yellow)
                }
                MATERIALS_LINK -> {
                    phonesBtnMenuWeb.setImageResource(R.drawable.phones_yellow)
                }
                FEEDBACK_LINK -> {
                    chatBtnMenuWeb.setImageResource(R.drawable.talk_cloud_yellow)
                }
                FAVORITE_LINK -> {
                    favouriteBtnMenuWeb.setImageResource(R.drawable.heart_yellow)
                }
                SETTINGS_LINK -> {
                    settingsBtnMenuWeb.setImageResource(R.drawable.settings_yellow)
                }
                else -> {
                    phonesBtnMenuWeb.setImageResource(R.drawable.phones_yellow)
                }
            }
        }
        with(binding) {
            webView.loadUrl(link)
        }
        setLogoutVisibility(link)
    }

    private fun changeButtonStateAndAddToHistory(link: String) {
//        showBackButton()
        changeBtnState(link)
        addMenuItemToHistory(link)
    }

    private fun showBackButton() {
        with(binding) {
            backBtnVisible = true
            backBtnWeb.visibility = View.VISIBLE
        }
    }

    private fun addMenuItemToHistory(link: String) {
        menuLinks.forEach { menuLink ->
            if (menuLink.url == link) {
                historyMenuLinks.add(menuLink)
                return
            } else {
//                if (link == MATERIALS_ID_LINK) {
                if (link.contains("ID")) {
                    historyMenuLinks.add(MenuItem(link))
                    return
                } else if (link.contains("video")) {
                    historyMenuLinks.add(MenuItem(link))
                    return
                }
            }
        }
    }

    private fun setTitleByLink() {
        val title: String? = if (getCurrentLink().contains("video")) {
            ""
        } else if (getCurrentLink().contains("ID")) {
            getString(R.string.header_materials)
        } else {
            getCurrentMenuItem()?.getName(requireContext())
        }
        setTitle(title ?: "null")
    }

    private fun setTitle(title: String) {
        binding.titleTextView.text = title
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

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun onMessageReceived(message: String) {
            Log.d("JavaScriptInterface", "Received message: $message")

            val eventData = JSONObject(message)
            when (val element = eventData.getString("element")) {
                "isFullscreen" -> fullscreenHandle(eventData.getBoolean("value"))
                "backButton" -> backButtonHandle(eventData.getBoolean("value"))
                "header" -> headerHandle(eventData.getString("value"))
                "openSignIn" -> noActiveUserErrorOnFrontHandle(eventData.getBoolean("value"))
                else -> Log.d("JavaScriptInterface", "Unknown element: $element")
            }
        }
    }

    fun noActiveUserErrorOnFrontHandle(isNoActiveUserFound: Boolean) {
        if (isNoActiveUserFound) {
            findNavController().navigate(R.id.action_webFragment_to_loginFragment)
        }
    }

    fun fullscreenHandle(isFullscreen: Boolean) {
        Log.d("JavaScriptInterface", "Received message: $isFullscreen")
    }

    fun backButtonHandle(showBackButton: Boolean) {
        Log.d("JavaScriptInterface", "Received message: $showBackButton")
    }

    fun headerHandle(content: String) {
        if (getCurrentLink().contains("video")) {
            val ellipsis = "..."
            val shortenedText = if (content.length > 20) {
                content.substring(0, 20 - ellipsis.length) + ellipsis
            } else {
                content
            }
            setTitle(shortenedText)
        } else {
            setTitleByLink()
        }
        Log.d("JavaScriptInterface", "Received message: $content")
    }
}










