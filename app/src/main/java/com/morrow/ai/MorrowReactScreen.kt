package com.morrow.ai

import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader

private const val MORROW_WEB_ORIGIN = "https://appassets.androidplatform.net"
private const val MORROW_REACT_CHAT_ENDPOINT = "https://morrow-backend.prasunmukherjeeee28.workers.dev/chat"

@Composable
internal fun MorrowReactScreen(
    modifier: Modifier = Modifier,
    onOpenClassic: () -> Unit,
) {
    val webViewHolder = remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webViewHolder.value?.let { webView ->
                webView.stopLoading()
                webView.removeJavascriptInterface("MorrowBridge")
                webView.loadUrl("about:blank")
                webView.destroy()
            }
            webViewHolder.value = null
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()

            WebView(context).apply {
                webViewHolder.value = this
                setBackgroundColor(AndroidColor.rgb(8, 10, 22))
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                val currentWebView = this
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun openClassic() {
                            Handler(Looper.getMainLooper()).post { onOpenClassic() }
                        }

                        @JavascriptInterface
                        fun sendMessage(requestId: String, message: String) {
                            val safeId = requestId.take(128)
                            Thread({
                                val payload = try {
                                    JSONObject()
                                        .put("requestId", safeId)
                                        .put("reply", requestMorrowReactReply(message))
                                } catch (error: Exception) {
                                    JSONObject()
                                        .put("requestId", safeId)
                                        .put("error", error.message ?: "Morrow could not complete the request.")
                                }
                                currentWebView.post {
                                    currentWebView.evaluateJavascript(
                                        "window.dispatchEvent(new CustomEvent('morrow-native-reply',{detail:$payload}));",
                                        null,
                                    )
                                }
                            }, "Morrow-Chat-Bridge").start()
                        }
                    },
                    "MorrowBridge",
                )

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): WebResourceResponse? {
                        val url = request?.url ?: return null
                        return assetLoader.shouldInterceptRequest(url)
                            ?: super.shouldInterceptRequest(view, request)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val url = request?.url ?: return true
                        return url.scheme != "https" || url.host != "appassets.androidplatform.net"
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: android.webkit.RenderProcessGoneDetail?,
                    ): Boolean {
                        view?.destroy()
                        return true
                    }
                }

                loadUrl("$MORROW_WEB_ORIGIN/assets/morrow-web/index.html")
            }
        },
    )
}


private fun requestMorrowReactReply(message: String): String {
    val connection = (URL(MORROW_REACT_CHAT_ENDPOINT).openConnection() as HttpURLConnection)
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 20_000
        connection.readTimeout = 60_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Accept", "application/json")

        val body = JSONObject().put("message", message)
        connection.outputStream.use { output ->
            output.write(body.toString().toByteArray(Charsets.UTF_8))
        }

        val statusCode = connection.responseCode
        val responseBody = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: ""
        val json = try {
            JSONObject(responseBody)
        } catch (_: Exception) {
            JSONObject()
        }

        if (statusCode !in 200..299) {
            val detail = json.optString("error").ifBlank { "HTTP $statusCode" }
            throw IOException(detail)
        }

        return json.optString("reply").takeIf { it.isNotBlank() }
            ?: throw IOException("The server returned an empty reply.")
    } finally {
        connection.disconnect()
    }
}
