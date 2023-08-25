package my.doctrina.app.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
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
import org.json.JSONObject


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
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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

                binding.webView.webChromeClient = object : WebChromeClient() {
//                    private var customView: View? = null
//                    private var customViewCallback: CustomViewCallback? = null

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        super.onShowCustomView(view, callback)
//                        if (customView != null) {
//                            callback?.onCustomViewHidden()
//                            return
//                        }
//                        webView.visibility = View.GONE
//                        customView = view
//                        customViewCallback = callback
//                        binding.videoContainer.addView(customView)


                        showFullScreenVideo(view)
                    }

                    override fun onHideCustomView() {
                        super.onHideCustomView()
//                        if (customView == null) {
//                            return
//                        }
//                        webView.visibility = View.VISIBLE
//                        binding.videoContainer.removeView(customView)
//
//                        customViewCallback?.onCustomViewHidden()
//                        customView = null
//                        customViewCallback = null

                        hideFullScreenVideo()
                    }
                }

//                webChromeClient = object : WebChromeClient() {
//
////                    var fullscreen: View? = null
//
//                    var customView: View? = null
//                    var customViewCallback: CustomViewCallback? = null
//
//                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
//                        super.onShowCustomView(view, callback)
//
//                        if (view is FrameLayout) {
//                            customView = view
//                            customViewCallback = callback
//                            val decorView = requireActivity().window.decorView as FrameLayout
//                            decorView.addView(customView)
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                                // Для API 30 и выше
//                                decorView.windowInsetsController?.hide(WindowInsets.Type.systemBars())
//                            } else {
//                                // Для более ранних версий API
//                                decorView.systemUiVisibility =
//                                    (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
//                            }
//                        }
//                    }
//
//                    override fun onHideCustomView() {
//                        super.onHideCustomView()
//                        if (customView != null) {
//                            val decorView = requireActivity().window.decorView as FrameLayout
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                                // Для API 30 и выше
//                                decorView.windowInsetsController?.show(WindowInsets.Type.systemBars())
//                            } else {
//                                // Для более ранних версий API
//                                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
//                            }
//                            decorView.removeView(customView)
//                            customViewCallback?.onCustomViewHidden()
//                            customView = null
//                        }
//                    }
//                }
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

    private fun showFullScreenVideo(videoView: View?) {
        with(binding.videoContainer) {
            addView(videoView)
            visibility = View.VISIBLE
        }
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    private fun hideFullScreenVideo() {
        with(binding.videoContainer) {
            removeAllViews()
            visibility = View.GONE
        }
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
//        return "https://youtu.be/C8BP8K-ZuL0?si=UYRTaAHLhMwO36zB"
        return if (historyMenuLinks.isNotEmpty()) {
            historyMenuLinks[historyMenuLinks.lastIndex].url
        } else {
            MAIN_LINK
        }
    }

    private fun setWebViewActive(view: WebView) {
        view.apply {
            visibility = View.VISIBLE
//            if (getCurrentLink().contains("video")) {
//                loadUrl("https://youtu.be/C8BP8K-ZuL0?si=UYRTaAHLhMwO36zB")
//            } else {
//                loadUrl(getCurrentLink())
//            }
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
                else -> {
                    saveToBtnMenuWeb.setImageResource(R.drawable.save_to_yellow)
                }
            }
        }
        with(binding) {
//            if (getCurrentLink().contains("video")) {
//                webView.loadUrl("https://youtu.be/C8BP8K-ZuL0?si=UYRTaAHLhMwO36zB")
//            } else {
//                webView.loadUrl(link)
//            }
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
//        if (isFullscreen) {
//            showFullScreenVideo(binding.videoContainer)
//        } else {
//            hideFullScreenVideo()
//        }


//        binding.webView.webChromeClient = object : WebChromeClient() {
//            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
//                super.onShowCustomView(view, callback)
//                showFullScreenVideo(view)
//            }
//
//            override fun onHideCustomView() {
//                super.onHideCustomView()
//                hideFullScreenVideo()
//            }
//
//            //            @SuppressLint("InlinedApi")
//            private fun showFullScreenVideo(videoView: View?) {
//                with(binding.videoContainer) {
//                    addView(
//                        videoView, ConstraintLayout.LayoutParams(
//                            ConstraintLayout.LayoutParams.MATCH_PARENT,
//                            ConstraintLayout.LayoutParams.MATCH_PARENT
//                        )
//                    )
//                    visibility = View.VISIBLE
//                }
//                requireActivity().requestedOrientation =
//                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
//            }
//
//            private fun hideFullScreenVideo() {
//                with(binding.videoContainer) {
//                    removeAllViews()
//                    visibility = View.GONE
//                }
//                requireActivity().requestedOrientation =
//                    ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
//            }
//        }


//        var isFullscreenEnabled = false
//        var customView: View? = null
//        var customViewCallback: WebChromeClient.CustomViewCallback? = null
//
//        if (isFullscreen) {
//            if (!isFullscreenEnabled) {
//                // Вход в режим полноэкранного воспроизведения
//                val params = ConstraintLayout.LayoutParams(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.MATCH_PARENT
//                )
////                customView = WebViewFragment.this.requireView()
//                customView = requireView()
//                customView.layoutParams = params
//
//                customViewCallback =
//                    WebChromeClient.CustomViewCallback { // Выход из режима полноэкранного воспроизведения
//                        customView?.visibility = View.GONE
//                        customView = null
//                        customViewCallback = null
//                        isFullscreenEnabled = false
//                    }
//
//                val decorView = requireActivity().window.decorView as ConstraintLayout
//                decorView.addView(customView)
//                customViewCallback?.onCustomViewHidden()
//                isFullscreenEnabled = true
//            }
//        } else {
//            // Выход из режима полноэкранного воспроизведения
//            customView?.visibility = View.GONE
//            val decorView = requireActivity().window.decorView as ConstraintLayout
//            decorView.removeView(customView)
//            customViewCallback?.onCustomViewHidden()
//            isFullscreenEnabled = false
//        }


//        with(binding) {
//            if (!isFullscreen) {
//                webView.onShowCustomView(null, null)
//            } else {
//                webView.onHideCustomView()
//            }
//        }


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










