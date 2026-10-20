package com.example.rehabinsight.ui.library

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.example.rehabinsight.data.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Decoded banners, keyed by the Base64 text they came from, so scrolling back to a card doesn't decode it again. */
private val decodedBanners = LruCache<String, ImageBitmap>(8)

/**
 * The banner photo the server sent with this article (Base64 in banner_image), or null while it
 * is decoding, and for articles with no photo from the server.
 */
@Composable
fun rememberServerBanner(article: Article): ImageBitmap? {
    val encoded = article.bannerImage
    val targetWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp * density }.toInt()
    val banner by produceState(encoded?.let { decodedBanners.get(it) }, encoded) {
        value = encoded?.let {
            decodedBanners.get(it)
                ?: withContext(Dispatchers.Default) { decodeBanner(it, targetWidthPx) }?.also { bitmap -> decodedBanners.put(it, bitmap) }
        }
    }
    return banner
}

/**
 * Decodes at about the screen's width: the server's photos are full camera size, which would
 * take tens of megabytes each at full resolution.
 */
private fun decodeBanner(encoded: String, targetWidthPx: Int): ImageBitmap? = runCatching {
    val bytes = Base64.decode(encoded, Base64.DEFAULT)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= targetWidthPx.coerceAtLeast(1)) sampleSize *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    // Null when the text isn't an image at all.
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
}.getOrNull()
