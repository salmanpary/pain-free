package com.example.painfree.ui.components

import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.painfree.core.VideoCache

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    expectedAspectRatio: Float? = null,
    onVideoSizeKnown: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    var firstFrameRendered by remember(videoUrl) { mutableStateOf(value = false) }
    var videoAspectRatio by remember(videoUrl) {
        mutableFloatStateOf(expectedAspectRatio ?: 0f)
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (firstFrameRendered) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "videoFadeIn",
    )

    val exoPlayer = remember(videoUrl) {
        val cacheDataSourceFactory = VideoCache.getCacheDataSourceFactory(context)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                prepare()
                playWhenReady = true

                addListener(
                    object : Player.Listener {
                        override fun onRenderedFirstFrame() {
                            firstFrameRendered = true
                        }

                        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                            if (videoSize.width > 0 && videoSize.height > 0) {
                                val ratio = videoSize.width.toFloat() / videoSize.height.toFloat()
                                videoAspectRatio = ratio
                                onVideoSizeKnown(ratio)
                            }
                        }
                    },
                )
            }
    }

    LaunchedEffect(isActive, firstFrameRendered) {
        when {
            isActive -> exoPlayer.play()
            firstFrameRendered -> exoPlayer.pause()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val frameLayout = AspectRatioFrameLayout(ctx).apply {
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val textureView = TextureView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    isOpaque = false
                }

                frameLayout.addView(textureView)
                exoPlayer.setVideoTextureView(textureView)
                frameLayout
            },
            update = { frameLayout ->
                val ratio = videoAspectRatio.takeIf { it > 0f } ?: expectedAspectRatio
                if (ratio != null && ratio > 0f) {
                    frameLayout.setAspectRatio(ratio)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = videoAlpha }
                .clip(RoundedCornerShape(24.dp))
        )

        if (!firstFrameRendered) {
            VideoSkeletonLoader()
        }
    }
}
