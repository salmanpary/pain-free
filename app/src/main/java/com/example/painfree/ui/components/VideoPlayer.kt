package com.example.painfree.ui.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    isActive: Boolean = true
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
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            val stateStr = when(playbackState) {
                                Player.STATE_IDLE -> "IDLE"
                                Player.STATE_BUFFERING -> "BUFFERING"
                                Player.STATE_READY -> "READY"
                                Player.STATE_ENDED -> "ENDED"
                                else -> "UNKNOWN"
                            }
                            Log.d("VideoPlayer", "onPlaybackStateChanged: state=$stateStr, url=$videoUrl")
                        }

                        override fun onRenderedFirstFrame() {
                            Log.d("VideoPlayer", "onRenderedFirstFrame: url=$videoUrl, firstFrameRendered was=$firstFrameRendered")
                            firstFrameRendered = true
                        }
                    },
                )
            }
    }

    // Reset and Play/Pause based on activity
    LaunchedEffect(isActive) {
        Log.d("VideoPlayer", "LaunchedEffect isActive=$isActive, url=$videoUrl")
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
            Log.d("VideoPlayer", "onDispose player: url=$videoUrl")
            exoPlayer.release()
        }
    }

    Log.d("VideoPlayer", "Render: url=$videoUrl, isActive=$isActive, firstFrameRendered=$firstFrameRendered")

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    keepScreenOn = true
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            },
            modifier = Modifier.fillMaxSize()
        )

        // Show loader immediately and hide instantly when first frame renders.
        // No AnimatedVisibility or delays - those caused lingering overlays.
        if (!firstFrameRendered) {
            VideoSkeletonLoader()
        }
    }
}
