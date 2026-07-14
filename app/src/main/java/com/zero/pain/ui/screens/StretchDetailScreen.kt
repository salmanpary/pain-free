package com.zero.pain.ui.screens

import android.os.Build.VERSION.SDK_INT
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.zero.pain.core.Constants
import com.zero.pain.ui.components.NavigationButton
import com.zero.pain.ui.components.VideoPlayer
import com.zero.pain.ui.components.VideoSkeletonLoader
import com.zero.pain.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun StretchDetailScreen(
    mainTitle: String,
    imageUrls: List<String>,
    fallbackRes: Int,
    instructionsList: List<String>,
    pageTitles: List<String>,
    aspectRatios: List<Float?>,
    defaultInstructions: String,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val scope = rememberCoroutineScope()
    
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

    val displayImages = remember(imageUrls, fallbackRes) {
        imageUrls.ifEmpty { listOf(fallbackRes) }
    }
    val pagerState = rememberPagerState { displayImages.size }
    
    // Track aspect ratio for each page dynamically; seed from Firestore so size is correct before playback
    val detectedAspectRatios = remember(aspectRatios) {
        mutableStateMapOf<Int, Float>().apply {
            aspectRatios.forEachIndexed { index, ratio ->
                ratio?.let { put(index, it) }
            }
        }
    }

    fun pageRatio(page: Int): Float {
        return (if (aspectRatios.size > page) aspectRatios[page] else null)
            ?: detectedAspectRatios[page]
            ?: (4f / 3f)
    }

    val displayRatio by remember {
        derivedStateOf {
            val page = pagerState.currentPage
            val offset = pagerState.currentPageOffsetFraction
            if (offset == 0f || displayImages.size <= 1) {
                pageRatio(page)
            } else {
                val targetPage = page + if (offset > 0f) 1 else -1
                lerp(
                    pageRatio(page),
                    pageRatio(targetPage.coerceIn(0, displayImages.lastIndex)),
                    abs(offset),
                )
            }
        }
    }

    val currentInstructions by remember {
        derivedStateOf {
            if ((instructionsList.size > pagerState.currentPage) && instructionsList[pagerState.currentPage].isNotBlank()) {
                instructionsList[pagerState.currentPage]
            } else {
                defaultInstructions
            }
        }
    }

    val currentTitle by remember {
        derivedStateOf {
            if ((pageTitles.size > pagerState.currentPage) && pageTitles[pagerState.currentPage].isNotBlank()) {
                pageTitles[pagerState.currentPage]
            } else {
                mainTitle
            }
        }
    }

    // Responsive Tight Spacing Values
    val titleToVideoGap = screenHeight * 0.025f
    val videoToDotsGap = screenHeight * 0.015f
    val dotsToInstructionsGap = screenHeight * 0.025f
    val maxMediaHeight = screenHeight * 0.42f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // Move below notification bar
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Extra breathing room for title

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlobBackButton(onClick = onBack)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                ),
                color = EspressoBrown,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(titleToVideoGap))

        // Media Container (Dynamic height based on video ratio)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent,
            shadowElevation = 20.dp,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(displayRatio)
                    .sizeIn(maxHeight = maxMediaHeight),
                contentAlignment = Alignment.Center
            ) {
                VideoSkeletonLoader()

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    pageSpacing = 0.dp,
                    beyondViewportPageCount = 1
                ) { page ->
                    val data = displayImages[page]

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if ((data is String) && data.endsWith(".mp4", ignoreCase = true)) {
                            val pageRatio = (if (aspectRatios.size > page) aspectRatios[page] else null)
                                ?: detectedAspectRatios[page]
                            VideoPlayer(
                                videoUrl = data,
                                modifier = Modifier.fillMaxSize(),
                                isActive = pagerState.currentPage == page,
                                expectedAspectRatio = pageRatio,
                                onVideoSizeKnown = { detectedRatio ->
                                    detectedAspectRatios[page] = detectedRatio
                                }
                            )
                        } else {
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(data = data)
                                    .crossfade(enable = true)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                imageLoader = imageLoader
                            )

                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painter,
                                    contentDescription = "$currentTitle Page $page",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (painter.state is coil.compose.AsyncImagePainter.State.Loading) {
                                    VideoSkeletonLoader()
                                }
                            }
                        }
                    }
                }

                // Floating Navigation Arrows
                if (displayImages.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (pagerState.currentPage > 0) {
                            NavigationButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }

                        if (pagerState.currentPage < (displayImages.size - 1)) {
                            NavigationButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
        }

        // Indicator Dots
        if (displayImages.size > 1) {
            Spacer(modifier = Modifier.height(videoToDotsGap))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(displayImages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) CoralPink else EspressoBrown.copy(alpha = 0.2f)
                    
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dotWidth"
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .width(width)
                            .height(6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(dotsToInstructionsGap))

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            InstructionsSection(currentInstructions)
        }
        
        Spacer(modifier = Modifier.height(screenHeight * 0.15f))
    }
}

@Composable
fun InstructionsSection(instructions: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(32.dp),
        color = LightGrey,
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    CoralPink.copy(alpha = 0.2f),
                    Color.Transparent,
                    CoralPink.copy(alpha = 0.1f),
                )
            )
        ),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CoralPink.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CoralPink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = Constants.INSTRUCTIONS_HEADER,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = EspressoBrown
                )
            }

            val steps = instructions.split("\n").filter { it.isNotBlank() }
            steps.forEachIndexed { index, step ->
                InstructionItem(text = step)
                if (index < (steps.size - 1)) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun InstructionItem(text: String) {
    val numberMatch = remember(text) { Regex("""^(\d+)\.\s*(.*)$""").find(text) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (numberMatch != null) {
            val (num, content) = numberMatch.destructured
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Brush.linearGradient(Constants.ACCENT_GRADIENT),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = num,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    ),
                    color = CreamyWhite,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = EspressoBrown,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(CoralPink, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = EspressoBrown,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BlobBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(150),
        label = "backScale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "backBlob")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backGradientOffset"
    )

    val glanceOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "backGlance"
    )

    val animatedBorder = Brush.linearGradient(
        colors = Constants.ACCENT_GRADIENT,
        start = Offset(gradientOffset, 0f),
        end = Offset(gradientOffset + 500f, 500f)
    )

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(CoralPink.copy(alpha = 0.5f), GoldenYellow.copy(alpha = 0.3f))
    )

    val glanceBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.4f),
            Color.Transparent,
        ),
        start = Offset(glanceOffset, 0f),
        end = Offset(glanceOffset + 150f, 150f)
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(56.dp)
            .scale(scale),
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(2.dp, animatedBorder),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(backgroundGradient).background(glanceBrush), 
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EspressoBrown, modifier = Modifier.size(28.dp))
        }
    }
}
