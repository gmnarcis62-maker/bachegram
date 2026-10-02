package com.example.validator

import com.example.data.model.VideoItem
import com.example.data.model.VideoStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed class ValidationResult {
    data class Success(val contentType: String, val contentLength: Long) : ValidationResult()
    data class Failed(val reason: String, val status: VideoStatus) : ValidationResult()
}

object VideoLinkValidator {

    private const val CONNECT_TIMEOUT_MS = 8000
    private const val READ_TIMEOUT_MS = 10000

    /**
     * Real HTTP/HTTPS check for online stream validity.
     * Follows redirects, checks response code, and inspects Content-Type.
     */
    suspend fun validateVideoUrl(urlStr: String): ValidationResult = withContext(Dispatchers.IO) {
        if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
            return@withContext ValidationResult.Failed("آدرس نامعتبر است (باید با http یا https شروع شود)", VideoStatus.FAILED)
        }

        var currentUrl = urlStr
        var redirects = 0
        val maxRedirects = 5

        while (redirects < maxRedirects) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    setRequestProperty("Accept", "*/*")
                }

                var responseCode = connection.responseCode

                // If HEAD method returns 405 Method Not Allowed or 403, fallback to lightweight GET with Range
                if (responseCode == HttpURLConnection.HTTP_BAD_METHOD || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    connection.disconnect()
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = CONNECT_TIMEOUT_MS
                        readTimeout = READ_TIMEOUT_MS
                        instanceFollowRedirects = false
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        setRequestProperty("Range", "bytes=0-1024")
                    }
                    responseCode = connection.responseCode
                }

                // Handle Redirects
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (location != null) {
                        currentUrl = if (location.startsWith("http")) location else URL(url, location).toString()
                        redirects++
                        connection.disconnect()
                        continue
                    }
                }

                // Check Response Codes
                if (responseCode !in 200..299) {
                    val errorReason = when (responseCode) {
                        HttpURLConnection.HTTP_NOT_FOUND -> "خطای ۴۰۴: فایل ویدیویی پیدا نشد"
                        HttpURLConnection.HTTP_FORBIDDEN -> "خطای ۴۰۳: دسترسی به ویدیو مسدود است"
                        HttpURLConnection.HTTP_UNAUTHORIZED -> "نیاز به لاگین یا دسترسی ویژه"
                        in 500..599 -> "خطای سمت سرور ویدیویی ($responseCode)"
                        else -> "پاسخ ناموفق از سرور ($responseCode)"
                    }
                    return@withContext ValidationResult.Failed(errorReason, VideoStatus.FAILED)
                }

                // Check Content-Type
                val rawContentType = connection.contentType?.lowercase() ?: ""
                val isHtml = rawContentType.contains("text/html")
                if (isHtml) {
                    return@withContext ValidationResult.Failed(
                        "لینک به صفحه وب هدایت شد نه فایل ویدیویی مستقیم",
                        VideoStatus.FAILED
                    )
                }

                val isVideoMedia = rawContentType.contains("video/") ||
                        rawContentType.contains("application/x-mpegurl") ||
                        rawContentType.contains("application/vnd.apple.mpegurl") ||
                        rawContentType.contains("application/octet-stream") ||
                        currentUrl.endsWith(".mp4", ignoreCase = true) ||
                        currentUrl.endsWith(".m3u8", ignoreCase = true)

                if (!isVideoMedia && rawContentType.isNotBlank()) {
                    return@withContext ValidationResult.Failed(
                        "فرمت فایل مدیا معتبر نیست ($rawContentType)",
                        VideoStatus.FAILED
                    )
                }

                val contentLength = connection.contentLengthLong.coerceAtLeast(0L)
                return@withContext ValidationResult.Success(
                    contentType = if (rawContentType.isNotBlank()) rawContentType else "video/mp4",
                    contentLength = contentLength
                )

            } catch (e: java.net.SocketTimeoutException) {
                return@withContext ValidationResult.Failed("تایم‌اوت در اتصال به سرور ویدیو", VideoStatus.FAILED)
            } catch (e: Exception) {
                return@withContext ValidationResult.Failed("عدم برقراری ارتباط با سرور: ${e.message}", VideoStatus.FAILED)
            } finally {
                connection?.disconnect()
            }
        }

        return@withContext ValidationResult.Failed("تعداد ریدایرکت‌های لینک بیش از حد مجاز است", VideoStatus.FAILED)
    }

    /**
     * Checks if URL or Title already exists in the given video collection.
     */
    fun isDuplicate(newVideoUrl: String, newTitle: String, existingVideos: List<VideoItem>): String? {
        val normNewTitle = newTitle.trim().lowercase()
        val normNewUrl = newVideoUrl.trim()

        if (existingVideos.any { it.videoUrl.trim().equals(normNewUrl, ignoreCase = true) }) {
            return "این آدرس ویدیو قبلاً در سیستم ثبت شده است (آدرس تکراری)"
        }
        if (existingVideos.any { it.title.trim().lowercase() == normNewTitle }) {
            return "ویدیویی با این عنوان قبلاً ثبت شده است (عنوان تکراری)"
        }
        return null
    }
}
