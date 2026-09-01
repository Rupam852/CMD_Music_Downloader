package com.cmdmusic.app.downloader

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AudioStreamResolver {

    private const val DES_KEY = "38346591"

    suspend fun resolveDirectStreamUrl(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val query = "$title $artist".trim()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val apiUrl = "https://www.jiosaavn.com/api.php?__call=search.getResults&q=$encodedQuery&_format=json&_marker=0&api_version=4&n=5&p=1&ctx=android"

        try {
            val jsonText = httpGet(apiUrl)
            val json = JSONObject(jsonText)
            val results = json.optJSONArray("results")

            if (results != null && results.length() > 0) {
                val song = results.getJSONObject(0)
                val moreInfo = song.optJSONObject("more_info")
                val encUrl = moreInfo?.optString("encrypted_media_url", "")

                if (!encUrl.isNullOrEmpty()) {
                    val decrypted = decryptDes(encUrl)
                    if (decrypted != null) {
                        // Upgrade to high fidelity 320kbps MP4/AAC stream
                        return@withContext decrypted
                            .replace("_96.mp4", "_320.mp4")
                            .replace("_160.mp4", "_320.mp4")
                    }
                }
            }
        } catch (e: Exception) {
            // Log or fallback
        }
        return@withContext null
    }

    private fun decryptDes(encryptedBase64: String): String? {
        return try {
            val keySpec = SecretKeySpec(DES_KEY.toByteArray(Charsets.UTF_8), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun httpGet(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")

        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
