package com.example.painfree.ui.screens

import androidx.activity.compose.BackHandler
import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.painfree.core.Constants
import com.example.painfree.ui.components.NavigationButton
import com.example.painfree.ui.components.VideoPlayer
import com.example.painfree.ui.components.VideoSkeletonLoader
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
    
    // Track aspect ratio for each page dynamically
    val detectedAspectRatios = remember { mutableStateMapOf<Int, Float>() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

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
                color = Color.White,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Dynamic Spacing Logic
        val currentRatio = detectedAspectRatios[pagerState.currentPage] 
            ?: (if (aspectRatios.size > pagerState.currentPage) aspectRatios[pagerState.currentPage] else null) 
            ?: (4f / 3f)
            
        // Reduce gaps for tall videos to save screen real estate
        val dynamicGap = if (currentRatio < 1.1f) 20.dp else 32.dp

        // Media Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .sizeIn(maxHeight = 380.dp)
                .aspectRatio(currentRatio)
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp,
                beyondViewportPageCount = 1
            ) { page ->
                val data = displayImages[page]
                val ratio = detectedAspectRatios[page] 
                    ?: (if (aspectRatios.size > page) aspectRatios[page] else null) 
                    ?: (4f / 3f)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.2f),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                ) {
                    if ((data is String) && data.endsWith(".mp4", ignoreCase = true)) {
                        VideoPlayer(
                            videoUrl = data,
                            modifier = Modifier.fillMaxSize(),
                            isActive = pagerState.currentPage == page,
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

            // Floating Navigation Arrows (Inside the dynamic box)
            if (displayImages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
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

        // Indicator Dots (Dynamic Spacing)
        if (displayImages.size > 1) {
            Spacer(modifier = Modifier.height(dynamicGap))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(displayImages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.2f)
                    
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

        Spacer(modifier = Modifier.height(dynamicGap))

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            InstructionsSection(currentInstructions)
        }
        
        Spacer(modifier = Modifier.height(140.dp))
    }
}

@Composable
fun InstructionsSection(instructions: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.2f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.1f),
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
                        .background(Color(0xFF60A5FA).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
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
                    color = Color.White
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
                    color = Color.White,
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
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF60A5FA), CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.85f),
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
    val blobOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "backB1",
    )

    val blobGradient = Brush.radialGradient(
        0.0f to Color(0xFFC084FC).copy(alpha = 0.6f),
        1.0f to Color.Transparent,
        center = Offset(blobOffset1, blobOffset1),
        radius = 100f
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(56.dp)
            .scale(scale),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.15f),
        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFFC084FC)))),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(blobGradient), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}
