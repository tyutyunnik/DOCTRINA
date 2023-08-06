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
import my.doctrina.app.data.MenuLinks
import my.doctrina.app.databinding.FragmentWebBinding


class WebFragment : Fragment(R.layout.fragment_web) {
    private lateinit var binding: FragmentWebBinding

    private var currentLink = ""
    private lateinit var buttonIdList: ArrayList<ImageButton>
    private var lastItemIndex = 0

    private lateinit var userPrefs: SharedPreferences

    private lateinit var authJson: JsonObject
    private lateinit var userObject: JsonObject

    private var accessExpired = 0
    private var accessToken = ""
    private var refreshExpired = 0
    private var refreshToken = ""
    private var success = false

    private lateinit var menuLinks: ArrayList<MenuLinks>

    private lateinit var currentMenuItem: MenuLinks

    private lateinit var historyMenuLinks: ArrayList<MenuLinks>

//    private var videoTitle = ""

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
            MenuLinks("main", "https://mobile.doctrina.app/", false),
            MenuLinks("materials", "https://mobile.doctrina.app/materials", false),
            MenuLinks("feedback", "https://mobile.doctrina.app/feedback", false),
            MenuLinks("favorite", "https://mobile.doctrina.app/favorite", false),
            MenuLinks("settings", "https://mobile.doctrina.app/settings", false)
        )
        currentLink = menuLinks[0].url

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

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (url != null) {
                            currentLink = url
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
                            setWebViewActive(view, url)
                        }
                        return true
                    }

                    override fun doUpdateVisitedHistory(
                        view: WebView?,
                        url: String,
                        isReload: Boolean
                    ) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        menuLinks.forEach { menuLink ->
                            if (menuLink.url == url) {
                                return
                            }
                        }
                        currentMenuItem.subItem = MenuLinks("video", url, true)
                        refreshTitle()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        if (url != null) {
                            currentLink = url
                        }
                        super.onPageFinished(view, url)
                        setUserAuth(authJson, view)
                        refreshTitle()
                    }
                }

                webChromeClient = object : WebChromeClient() {

                }

                addJavascriptInterface(JavaScriptInterface(requireContext()), "Android")
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

            changeBtnState(
                flagBtnMenuWeb, R.drawable.flag_yellow,
                "https://mobile.doctrina.app/"
            )
            flagBtnMenuWeb.setOnClickListener {
                changeBtnState(
                    flagBtnMenuWeb, R.drawable.flag_yellow,
                    "https://mobile.doctrina.app/"
                )
            }

            saveToBtnMenuWeb.setOnClickListener {
                changeBtnState(
                    saveToBtnMenuWeb, R.drawable.save_to_yellow,
                    "https://mobile.doctrina.app/materials"
                )
            }

            chatBtnMenuWeb.setOnClickListener {
                changeBtnState(
                    chatBtnMenuWeb, R.drawable.talk_cloud_yellow,
                    "https://mobile.doctrina.app/feedback"
                )
            }

            starBtnMenuWeb.setOnClickListener {
                changeBtnState(
                    starBtnMenuWeb, R.drawable.star_yellow,
                    "https://mobile.doctrina.app/favorite"
                )
            }

            settingsBtnMenuWeb.setOnClickListener {
                changeBtnState(
                    settingsBtnMenuWeb, R.drawable.settings_yellow,
                    "https://mobile.doctrina.app/settings"
                )
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
                setWebViewActive(webView, currentLink)
            } else {
                noInternetLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun setWebViewActive(view: WebView, link: String) {
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

    private fun onBackButtonClicked() {
        if (currentMenuItem.subItem != null) {
            currentMenuItem.subItem = null
            binding.webView.goBack()
        } else {
            with(binding) {
//                if (webView.canGoBack()) {
                if (historyMenuLinks.size != 0) {
                    lastItemIndex = historyMenuLinks.size - 1
                    when (historyMenuLinks[lastItemIndex - 1].url) {
                        menuLinks[0].url -> {
                            backBtnHistoryChange(
                                flagBtnMenuWeb,
                                R.drawable.flag_yellow
                            )
                        }
                        menuLinks[1].url -> {
                            backBtnHistoryChange(
                                saveToBtnMenuWeb,
                                R.drawable.save_to_yellow
                            )
                        }
                        menuLinks[2].url -> {
                            backBtnHistoryChange(
                                chatBtnMenuWeb,
                                R.drawable.talk_cloud_yellow
                            )
                        }
                        menuLinks[3].url -> {
                            backBtnHistoryChange(
                                starBtnMenuWeb,
                                R.drawable.star_yellow
                            )
                        }
                        menuLinks[4].url -> {
                            backBtnHistoryChange(
                                settingsBtnMenuWeb,
                                R.drawable.settings_yellow
                            )
                        }
                    }
                }
            }
            binding.webView.goBack()
        }
        refreshTitle()
    }

    private fun refreshTitle() {
        if (currentMenuItem.subItem == null) {
            setTitleByLink(currentLink)
        } else {
            if (currentMenuItem.subItem!!.subItem == null) {
                setTitleByLink(currentMenuItem.subItem!!.url)
            } else {
                setTitleByLink(currentMenuItem.subItem!!.subItem!!.url)
            }
        }
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


    private fun backBtnHistoryChange(imageBtn: ImageButton, imageResource: Int) {
        setInactiveIcons()
        historyMenuLinks.removeAt(lastItemIndex)
        imageBtn.setImageResource(imageResource)
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
        setInactiveIcons()
        imageBtn.setImageResource(resourceId)
        with(binding) {
            webView.loadUrl(link)
        }
        setLogoutVisibility(imageBtn)
        for (menuLink in menuLinks) {
            if (menuLink.url == link) {
                currentMenuItem = menuLink
                historyMenuLinks.add(menuLink)
            }
        }
    }

    private fun setTitleByLink(link: String) {
        with(binding) {
            val title = when (link) {
                "https://mobile.doctrina.app/materials" ->
                    getString(R.string.header_materials)
                "https://mobile.doctrina.app/feedback" ->
                    getString(R.string.header_feedback)
                "https://mobile.doctrina.app/settings" ->
                    getString(R.string.header_settings)
                "https://mobile.doctrina.app/favorite" -> ""
                "https://mobile.doctrina.app/" -> ""
//                "https://mobile.doctrina.app/materials?ID=1" -> ""
                else -> {
//                    if (link.contains("ID")) {
//                        getString(R.string.header_materials)
//                        changeBtnState(
//                            saveToBtnMenuWeb, R.drawable.save_to_yellow,
//                            "https://mobile.doctrina.app/materials"
//                        )

//                    }
//                    else {
                    setVideoTitleFromWeb(link)
//                    }
//                    "else"
                }
            }
            Log.d("loaded_link", "loaded link is -> $link")
            titleTextView.text = title
        }
    }


    private fun setVideoTitleFromWeb(link: String): String {
        // TODO:
        with(binding) {
            if (link.contains("ID")) {
                titleTextView.text = getString(R.string.header_materials)
//                changeBtnState(
//                    saveToBtnMenuWeb, R.drawable.save_to_yellow,
//                    "https://mobile.doctrina.app/materials"
//                )
            } else {
                webView.apply {
                    evaluateJavascript(
                        "javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });"
                    ) { value ->
                        if (value != null) {
                            Log.d("???", value)
                        }
                    }

                    loadUrl("javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });")
                }
            }
//            val videoCode = """
//                <video width="320" height="240" controls>
//                    <source src="$link" type="video/mp4">
//                    Your browser does not support the video tag.
//                </video>
//        """.trimIndent()
//
//            loadData(videoCode, "text/html", "UTF-8")

//            evaluateJavascript(
//                "javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });",
//                object : ValueCallback<String?> {
//                    override fun onReceiveValue(p0: String?) {
//                        if (p0 != null) {
//                            Log.d("???", p0)
//                        }
//                    }
//                })
//
//            loadUrl("javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });")
//        }
            return ""
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

    override fun onResume() {
        super.onResume()
        binding.webView.evaluateJavascript("javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });",
            object : ValueCallback<String?> {
                override fun onReceiveValue(p0: String?) {
                    if (p0 != null) {
                        Log.d("!!!", p0)
                    }
                }
            })
        binding.webView.loadUrl("javascript:window.webkit.messageHandlers.jsHandler.postMessage({ element: 'header', value: value });")
    }

    class JavaScriptInterface(private val context: Context) {
        @JavascriptInterface
        fun postMessage(element: String, value: String) {
            Log.d("!!!", value)
            // Обработка полученных данных из JavaScript
            // Например, вы можете использовать Intent для передачи данных другой активности или фрагменту
        }
    }

//    inner class JavaScriptInterface {
//        @JavascriptInterface
//        fun handleMessage(message: String) {
//            val jsonObject = JSONObject(message)
////            val header = jsonObject.getString("element")
//            val value = jsonObject.getString("value")
//
//            if (currentMenuItem.subItem != null) {
//                webHeaderContent = value
//            }
//
//            Log.d("?????", jsonObject.toString())
//        }
//    }
}










