package com.example.painfree.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.painfree.core.VideoCache

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onVideoSizeKnown: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    var firstFrameRendered by remember(videoUrl) { mutableStateOf(value = false) }
    
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
                volume = 0f // No sound
                prepare()
                playWhenReady = false // Start paused, let LaunchedEffect handle it
                
                addListener(
                    object : Player.Listener {
                        override fun onRenderedFirstFrame() {
                            firstFrameRendered = true
                        }

                        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                            if (videoSize.width > 0 && videoSize.height > 0) {
                                onVideoSizeKnown(videoSize.width.toFloat() / videoSize.height.toFloat())
                            }
                        }
                    },
                )
            }
    }

    // Reset and Play/Pause based on activity
    LaunchedEffect(isActive) {
        if (isActive) {
            exoPlayer.seekTo(0)
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Properly manage Lifecycle
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    try {
                        val method = this.javaClass.methods.find { it.name == "setSurfaceType" }
                        method?.let {
                            it.isAccessible = true
                            it.invoke(this, 2) // 2 = SURFACE_TYPE_TEXTURE_VIEW
                        }
                    } catch (_: Exception) {
                        // Silent fallback to SurfaceView if reflection fails
                    }
                    
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    keepScreenOn = true
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(24.dp)
                }
        )

        if (!firstFrameRendered) {
            VideoSkeletonLoader()
        }
    }
}
