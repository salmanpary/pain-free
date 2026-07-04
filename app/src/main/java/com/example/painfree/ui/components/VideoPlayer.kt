package com.example.painfree.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    var isBuffering by remember { mutableStateOf(value = true) }
    
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
                            isBuffering = playbackState == Player.STATE_BUFFERING
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (isPlaying) isBuffering = false
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
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = isBuffering,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            VideoSkeletonLoader()
        }
    }
}
