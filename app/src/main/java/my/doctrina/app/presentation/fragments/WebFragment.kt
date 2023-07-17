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
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonObject
import my.doctrina.app.R
import my.doctrina.app.databinding.FragmentWebBinding
import org.json.JSONObject

class WebFragment : Fragment(R.layout.fragment_web) {
    private lateinit var binding: FragmentWebBinding

    private var link = ""
    private lateinit var buttonIdList: ArrayList<ImageButton>
    private var lastItemIndex = 0

    private lateinit var userPrefs: SharedPreferences

    private lateinit var authJson: JsonObject
    private lateinit var userObject: JsonObject

    private var accessExpired = 0
    private var accessToken = ""
    private var refreshExpired = 0
    private var refreshToken = ""

    companion object {
        var webHeaderContent = ""
    }

    private lateinit var jsHandler: JsHandler

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

        link = "https://mobile.doctrina.app/"
        buttonIdList = ArrayList()

        with(binding) {
            userLoginBody(accessExpired, accessToken, refreshExpired, refreshToken)
            webView.apply {
                settings.apply {
                    javaScriptEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    setSupportZoom(true)
                    domStorageEnabled = true
                }
                jsHandler = JsHandler()
                addJavascriptInterface(JsHandler(), "jsHandler")
                addJavascriptInterface(WebAppInterface(), "Android")


                webChromeClient = object : WebChromeClient() {
                    // Обработчик загрузки JSON после загрузки страницы
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (newProgress == 100) {
                            // Выполняем JavaScript код после загрузки страницы
//                            webView.loadUrl("javascript:loadJsonFromWebView()")
//                            evaluateJavascript(
//                                "loadJsonFromKotlin(JSON.stringify(Android.getJson()));",
//                                null
//                            )
                            evaluateJavascript(
                                "function loadJsonFromKotlin(jsonString) {" +
                                        // Полученный JSON из Kotlin в виде строки jsonString
                                        "var json = JSON.parse(jsonString);" +
                                        // Здесь вы можете использовать JSON по вашему усмотрению,
                                        // например, выводить его на страницу или выполнять дополнительные действия с данными.
                                        "console.log(json);}",
                                null
                            )
                        }
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (url != null) {
                            link = url
                        }
                        super.onPageStarted(view, url, favicon)
                        setUserAuth(authJson, view)
                    }

//                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                        if (url != null) {
//                            view?.loadUrl(url)
//                        }
//                        return true
//                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        view?.loadUrl(request?.url.toString())
                        return true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        swipeRefreshLayout.isRefreshing = false
                        if (url != null) {
                            link = url
                        }
                        super.onPageFinished(view, url)
                        setUserAuth(authJson, view)
                        evaluateJavascript(
                            "(function() {" +
                                    "var headers = Array.from(document.head.querySelectorAll('meta')).map(meta => meta.outerHTML);" +
                                    "window.webkit.messageHandlers.jsHandler.postMessage(JSON.stringify({ headers: header })); })();",
                            null
                        )
                    }
                }
            }

            setLayoutVisibilitySettings(webView, noInternetLayout, header)
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
                    "https://mobile.doctrina.app/"
                )
            }

            saveToBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    saveToBtnMenuWeb, R.drawable.save_to_yellow,
                    "https://mobile.doctrina.app/materials"
                )
            }

            chatBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    chatBtnMenuWeb, R.drawable.talk_cloud_yellow,
                    "https://mobile.doctrina.app/feedback"
                )
            }

            starBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
                changeBtnState(
                    starBtnMenuWeb, R.drawable.star_yellow,
                    "https://mobile.doctrina.app/favorite"
                )
            }

            settingsBtnMenuWeb.setOnClickListener {
                setInactiveIcons()
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
                "function loadJsonFromKotlin($authJson) {" +
                        // Полученный JSON из Kotlin в виде строки jsonString
                        "var json = JSON.parse($authJson);" +
                        // Здесь вы можете использовать JSON по вашему усмотрению,
                        // например, выводить его на страницу или выполнять дополнительные действия с данными.
                        "console.log(json);}",
                null
            )

            this?.loadUrl("javascript:loadJsonFromWebView()")


//            this?.evaluateJavascript(
//                "window.localStorage.setItem('user_auth','$authJson');",
//                null
//            )
//            this?.loadUrl("javascript:localStorage.setItem('user_auth','$authJson');")
        }
    }

    private fun userLoginBody(
        accessExpired: Int,
        accessToken: String,
        refreshExpired: Int,
        refreshToken: String
    ) {
        userObject = JsonObject()
        userObject.apply {
            addProperty("access_token", accessToken)
            addProperty("refresh_token", refreshToken)
            addProperty("authenticator", "authenticator:oauth2")
            addProperty("refresh_expired", refreshExpired)
            addProperty("token_type", "Bearer")
            addProperty("access_expired", accessExpired)
        }
        authJson = JsonObject()
        authJson.add("authenticated", userObject)
//        Log.d("!!!", userObject.toString())
    }

    private fun setWebViewLoadingMode() {
        with(binding) {
            setLayoutVisibilitySettings(webView, noInternetLayout, header)
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
            setHeader(link)
        }
    }

    private fun setLayoutVisibilitySettings(
        webView: WebView,
        noInternetLayout: LinearLayoutCompat,
        header: AppCompatTextView
    ) {
        webView.visibility = View.GONE
        noInternetLayout.visibility = View.GONE
        header.visibility = View.GONE
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
            setHeader(link)
        }
        setLogoutVisibility(imageBtn)
        buttonIdList.add(imageBtn)
    }

    private fun setHeader(link: String) {
        with(binding) {
            if ((link == "https://mobile.doctrina.app/" || link == "https://mobile.doctrina.app/materials") && webHeaderContent != "") {
                header.apply {
                    visibility = View.VISIBLE
                    text = webHeaderContent
                }
            } else {
                header.visibility = View.GONE
            }
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

    inner class WebAppInterface {
        @JavascriptInterface
        fun getJson(): String {
            // Здесь формируем JSON в Kotlin
            return authJson.toString()
        }

//        @JavascriptInterface
//        fun sendJson(jsonString: String) {
//            // Обработка JSON в Kotlin
//            val gson = Gson()
//            val json = gson.fromJson(jsonString, MyJson::class.java)
//            // Теперь вы можете использовать объект json в вашем коде Kotlin
//            // Например, вы можете получить значение access_token следующим образом:
////            val accessToken = json.authenticated.access_token
//        }
    }

//    data class MyJson(val authenticated: AuthData)
//    data class AuthData(
//        val access_token: String,
//        val refresh_token: String,
//        val authenticator: String,
//        val refresh_expired: Long,
//        val token_type: String,
//        val access_expired: Long
//    )

    class JsHandler {
        @JavascriptInterface
        fun postMessage(message: String) {
            // Обработка сообщения от JavaScript
            val headerContent: String
            val json = JSONObject(message)
            if (json.has("headers")) {
                val headers = json.getJSONArray("headers")
                for (i in 0 until headers.length()) {
                    headerContent = headers.getString(0)
                    webHeaderContent = headerContent
                    Log.d("!!!", headerContent)
                    break
                }
            }
        }
    }
}

