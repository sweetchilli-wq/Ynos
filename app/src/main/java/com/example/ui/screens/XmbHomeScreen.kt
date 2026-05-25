package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.db.Comic
import com.example.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun XmbHomeScreen(
    viewModel: ComicViewModel,
    modifier: Modifier = Modifier
) {
    val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
    val selectedItemIndex by viewModel.selectedItemIndex.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val activeReadingComic by viewModel.activeReadingComic.collectAsStateWithLifecycle()

    val isWritingMemoryStick by viewModel.isWritingMemoryStick.collectAsStateWithLifecycle()
    val writingProgress by viewModel.writingProgress.collectAsStateWithLifecycle()
    val writingStage by viewModel.writingStage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Render active state
    XmbBackground(themeColor = themeColor) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (activeReadingComic == null) {
                // Main Console Interface
                Column(modifier = Modifier.fillMaxSize()) {
                    PspStatusBar(viewModel = viewModel)
                    XmbCategoryHeader(viewModel = viewModel)
                    
                    Box(modifier = Modifier.weight(1f)) {
                        // Category Contents
                        when (activeCategory) {
                            0 -> SettingsCategoryView(viewModel = viewModel)
                            1 -> ComicsCategoryView(viewModel = viewModel)
                            2 -> UploadCategoryView(viewModel = viewModel)
                        }
                    }

                    // Bottom Legend Info
                    XmbBottomLegend(activeCategory = activeCategory)
                }

                // Global Memory Stick duo Copy Overlay (MagicGate Write Dialog)
                if (isWritingMemoryStick) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xE60A0A0C))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .widthIn(max = 380.dp)
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "PSP™ DIGITAL COMICS WRITER",
                                color = Color(0xFF00BFFF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.5.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Spinning loading indicator
                            CircularProgressIndicator(
                                progress = { writingProgress },
                                color = Color(0xFFFFCC00),
                                trackColor = Color(0x33FFFFFF),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "MagicGate™ Sector Progress: ${(writingProgress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Retro tech progress bar
                            LinearProgressIndicator(
                                progress = { writingProgress },
                                color = Color(0xFF00BFFF),
                                trackColor = Color(0x22FFFFFF),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            Text(
                                text = writingStage,
                                color = Color(0xCCFFFFFF),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                minLines = 2,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                // Interactive Comic Reader UI Overlay
                ComicReaderScreen(viewModel = viewModel, comic = activeReadingComic!!)
            }
        }
    }
}

@Composable
fun PspStatusBar(
    viewModel: ComicViewModel,
    modifier: Modifier = Modifier
) {
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()

    var systemTime by remember { mutableStateOf("") }
    var systemDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val sdfTime = SimpleDateFormat("h:mm a", Locale.US)
            val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            systemTime = sdfTime.format(Date())
            systemDate = sdfDate.format(Date())
            delay(15000)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: PSP signature / model emblem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PSP",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            Text(
                text = "│",
                color = Color(0x44FFFFFF),
                fontSize = 12.sp
            )
            Text(
                text = "Digital Comics Reader v1.50",
                color = Color(0x99FFFFFF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Right side: Battery, SD card status info & time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Memory icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Memory Duo Slot",
                    tint = Color(0xFFFFCC00),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "MS™ DUO",
                    color = Color(0xAAFFFFFF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Haptic/audio icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.Settings else Icons.Default.Close,
                    contentDescription = "Audio Config",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Icon(
                    imageVector = if (hapticEnabled) Icons.Default.Settings else Icons.Default.Close,
                    contentDescription = "Haptics State",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            // Battery visual representation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(10.dp)
                        .border(1.dp, Color.White, RoundedCornerShape(1.dp))
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f)
                            .background(Color(0xFF4EEF74))
                    )
                }
                Text(
                    text = "85%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "$systemDate  $systemTime",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun XmbCategoryHeader(
    viewModel: ComicViewModel,
    modifier: Modifier = Modifier
) {
    val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()

    val categories = listOf(
        Triple(0, Icons.Default.Settings, "SETTINGS"),
        Triple(1, Icons.Default.List, "DIGITAL COMICS"),
        Triple(2, Icons.Default.Add, "MEMORY STICK TRANSFER")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { (id, icon, label) ->
            val isActive = activeCategory == id
            val glowAnim by animateFloatAsState(
                targetValue = if (isActive) 1.25f else 0.85f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "iconSize"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.changeCategory(id)
                    }
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else Color(0x55FFFFFF),
                    modifier = Modifier
                        .size((24f * glowAnim).dp)
                        .shadow(
                            elevation = if (isActive) 8.dp else 0.dp,
                            shape = RoundedCornerShape(4.dp),
                            clip = false,
                            ambientColor = Color(0xFF00BFFF),
                            spotColor = Color(0xFF00BFFF)
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(
                    visible = isActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .background(Color(0x33000000), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryView(
    viewModel: ComicViewModel,
    modifier: Modifier = Modifier
) {
    val selectedItemIndex by viewModel.selectedItemIndex.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val settingsList = listOf(
        "Theme Color" to themeColor.title,
        "MagicGate™ Sound FX" to if (soundEnabled) "ENABLED" else "DISABLED",
        "DualShock™ Haptic Vibration" to if (hapticEnabled) "ENABLED" else "DISABLED",
        "Erase Memory Stick™ Progress" to "RESET COMICS"
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left column settings items
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            settingsList.forEachIndexed { idx, (title, status) ->
                val isSelected = selectedItemIndex == idx
                val highlightBg = if (isSelected) {
                    Brush.horizontalGradient(listOf(Color(0xBB42A5F5), Color(0x00FFFFFF)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0x1A000000), Color(0x00FFFFFF)))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(highlightBg)
                        .clickable {
                            viewModel.changeItemIndex(idx)
                            when (idx) {
                                0 -> { // Cycle Themes
                                    val currentOrdinal = themeColor.ordinal
                                    val nextOrdinal = (currentOrdinal + 1) % PspThemeColor.values().size
                                    viewModel.selectTheme(PspThemeColor.values()[nextOrdinal])
                                }
                                1 -> viewModel.toggleSound()
                                2 -> viewModel.toggleHaptic()
                                3 -> viewModel.resetAllComicsProgress()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.White else Color(0xAAFFFFFF),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = status,
                        color = if (isSelected) Color(0xFFFFCC00) else Color(0x88FFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Right column decorative Sony Hardware info display panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f)
                .background(Color(0x33000000), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = themeColor.accentColor,
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    text = "SONY® PLAYSTATION\nHARDWARE SETTING PANEL",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Adjust the console UI configurations here. Touch components to flip toggles. Background waves and MagicGate authentication frequencies will automatically synchronize with your slate configurations.",
                    color = Color(0xAAFFFFFF),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
fun ComicsCategoryView(
    viewModel: ComicViewModel,
    modifier: Modifier = Modifier
) {
    val comics by viewModel.allComics.collectAsStateWithLifecycle()
    val selectedItemIndex by viewModel.selectedItemIndex.collectAsStateWithLifecycle()
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Sync selected item vertically
    LaunchedEffect(selectedItemIndex) {
        if (comics.isNotEmpty() && selectedItemIndex < comics.size) {
            listState.animateScrollToItem(selectedItemIndex)
        }
    }

    if (comics.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize().padding(24.dp)
        ) {
            Text(
                text = "NO COMICS INSTALLED.\nINSERT MEMORY DUO™ OR SEED DEMO COMICS.",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
        return
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Column list of Comics
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(comics) { idx, comic ->
                val isSelected = selectedItemIndex == idx
                val highlightBg = if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(themeColor.accentColor.copy(alpha = 0.5f), Color.Transparent)
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color(0x11000000), Color.Transparent))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comic_item_$idx")
                        .background(highlightBg)
                        .clickable {
                            viewModel.changeItemIndex(idx)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small thumbnail
                    Box(
                        modifier = Modifier
                            .size(36.dp, 48.dp)
                            .background(Color.DarkGray, RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(2.dp))
                    ) {
                        if (comic.filePath.startsWith("sample_") || comic.coverPath == null) {
                            // Dummy cover drawing
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.White, Color.DarkGray)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("UMD", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(File(comic.coverPath)),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = comic.title,
                            color = if (isSelected) Color.White else Color(0xCCFFFFFF),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Vol: ${comic.volume}",
                                color = Color(0x88FFFFFF),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "•",
                                color = Color(0x44FFFFFF),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${comic.pageCount} Pages",
                                color = Color(0x88FFFFFF),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (comic.currentPage > 0) {
                        Text(
                            text = "P.${comic.currentPage + 1}",
                            color = Color(0xFFFFCC00),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .border(0.5.dp, Color(0xFFFFCC00), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Right Column detailed selected UMD Preview Card
        val currentSelectedComic = comics.getOrNull(selectedItemIndex.coerceAtMost(comics.size - 1))
        
        if (currentSelectedComic != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.0f)
                    .background(Color(0x2B000000), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Comic rotating preview mockup
                    val infiniteTransition = rememberInfiniteTransition(label = "discRotate")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = -5f,
                        targetValue = 5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "discRotateAnim"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .height(160.dp)
                                .width(120.dp)
                                .rotate(rotation)
                                .shadow(8.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF141619),
                            border = BorderStroke(2.dp, Color(0x66FFFFFF))
                        ) {
                            if (currentSelectedComic.filePath.startsWith("sample_") || currentSelectedComic.coverPath == null) {
                                // Procedural render cover mockup
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF2E333C), themeColor.bgStart)
                                            )
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFCC00), RoundedCornerShape(2.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                "Retro UMD",
                                                color = Color.Black,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = currentSelectedComic.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            lineHeight = 13.sp,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = currentSelectedComic.artist,
                                            color = Color(0xAAFFFFFF),
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                    }
                                }
                            } else {
                                Image(
                                    painter = rememberAsyncImagePainter(File(currentSelectedComic.coverPath)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Metadata detail card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33000000), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentSelectedComic.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
                        
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("ARTIST:", color = Color(0x88FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(currentSelectedComic.artist, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("VOLUME:", color = Color(0x88FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(currentSelectedComic.volume, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("PAGES CURRENT:", color = Color(0x88FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("${currentSelectedComic.currentPage + 1} / ${currentSelectedComic.pageCount}", color = Color(0xFFFFCC00), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.openComicReader(currentSelectedComic) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor.accentColor),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(32.dp).testTag("open_comic_btn"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("✕ READ DISC", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = { viewModel.deleteComic(currentSelectedComic) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(0.9f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("△ DELETE UMD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UploadCategoryView(
    viewModel: ComicViewModel,
    modifier: Modifier = Modifier
) {
    val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var comicTitle by remember { mutableStateOf("") }
    var comicArtist by remember { mutableStateOf("") }
    var comicVolume by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importComicPdf(
                context = context,
                uri = uri,
                title = comicTitle,
                artist = comicArtist,
                volume = comicVolume
            )
            // Reset input values after start
            comicTitle = ""
            comicArtist = ""
            comicVolume = ""
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Column input settings
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.2f)
                .background(Color(0x33000000), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MagicGate™ COMIC IMPORTER SETUP",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)

            // Custom labeled input
            Column {
                Text("COMIC TITLE:", color = Color(0x99FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = comicTitle,
                    onValueChange = { comicTitle = it },
                    placeholder = { Text("Enter title...", color = Color.Gray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColor.accentColor,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text("ARTIST / MANGA AUTHOR:", color = Color(0x99FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = comicArtist,
                    onValueChange = { comicArtist = it },
                    placeholder = { Text("Author name...", color = Color.Gray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColor.accentColor,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Text("VOLUME NUMBER / ISSUE SECTION:", color = Color(0x99FFFFFF), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = comicVolume,
                    onValueChange = { comicVolume = it },
                    placeholder = { Text("e.g. Vol. 1, Issue 4", color = Color.Gray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColor.accentColor,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    viewModel.playRetroSound()
                    filePickerLauncher.launch("application/pdf")
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColor.accentColor),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("upload_comic_btn")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Upload", tint = Color.Black)
                    Text("COPY PDF TO MS DUO™", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Right Column Uploader Guidance and Connecting Art
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.0f)
                .background(Color(0x2B000000), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connecting PC symbol
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    text = "USB CONNECTION MODE",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Fill in the metadata on the left to organize your comic properly, then tap the launch button. Choosing any Standard PDF document will flash it onto the console Memory Stick Duo Pro™ directory securely.",
                    color = Color(0xAAFFFFFF),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )

                Box(
                    modifier = Modifier
                        .background(Color(0x11FFFFFF), RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayStationGlyph(symbol = "△")
                        Text(
                            "MagicGate™ Authenticated License",
                            color = Color(0xFF4EEF74),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun XmbBottomLegend(
    activeCategory: Int,
    modifier: Modifier = Modifier
) {
    val leftAction = when (activeCategory) {
        0 -> "✕ Select"
        1 -> "✕ Enter Reader / △ Options"
        2 -> "✕ Browse Document"
        else -> ""
    }

    ControlLegendBar(
        actions = listOf(
            "△" to "Back Screen",
            "○" to "Select",
            "×" to "Close Options",
            "□" to "Console Menu"
        ),
        modifier = modifier
    )
}

@Composable
fun ComicReaderScreen(
    viewModel: ComicViewModel,
    comic: Comic,
    modifier: Modifier = Modifier
) {
    val currentPage by viewModel.currentReadingPage.collectAsStateWithLifecycle()
    val zoomFactor by viewModel.zoomFactor.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()

    var showOverlayMenu by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Automatically fade menu overlay after a moment
        delay(4000)
        showOverlayMenu = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.playRetroSound()
                showOverlayMenu = !showOverlayMenu
            }
    ) {
        // Comic visual pages content (wrapped inside customizable container zoom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (zoomFactor > 1.0f) 50.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            val scrollStateHorizontal = rememberScrollState()
            val scrollStateVertical = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollStateVertical)
                    .horizontalScroll(scrollStateHorizontal)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(0.72f) // Standard comic book shape
                        .sizeIn(
                            maxWidth = (600f * zoomFactor).dp,
                            maxHeight = (850f * zoomFactor).dp
                        )
                ) {
                    ComicPageRenderer(filePath = comic.filePath, pageIndex = currentPage)
                }
            }
        }

        // Left Page Flip Trigger Overlay
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(60.dp)
                .align(Alignment.CenterStart)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.prevPage()
                }
        )

        // Right Page Flip Trigger Overlay
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(60.dp)
                .align(Alignment.CenterEnd)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.nextPage()
                }
        )

        // Top Navigation Overlaid Bar
        AnimatedVisibility(
            visible = showOverlayMenu,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xE60A0A0C), Color.Transparent)))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "UMD™ COMIC PLAYBACK MODE",
                        color = Color(0xFF00BFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = comic.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Close / Return button
                Button(
                    onClick = { viewModel.closeComicReader() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text("○ QUIT READING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Bottom PlayStation Remote Controls, Dial scroll, Zoom selector Overlaid Panel
        AnimatedVisibility(
            visible = showOverlayMenu,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE60A0A0C))))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // PSP Dial Slide & Volume Layout ("scroll volume in organis artist")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artist name
                    Column {
                        Text(
                            text = "ARTIST / volume metadata",
                            color = Color(0x88FFFFFF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comic.artist,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("•", color = Color(0x44FFFFFF), fontSize = 10.sp)
                            Text(
                                text = comic.volume,
                                color = Color(0xFFFFCC00),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Authentic PlayStation Volume progress control dial
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .background(Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .border(0.5.dp, Color(0x44FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "VOLUME PAGE DIAL",
                            color = Color(0xAAFFFFFF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        // Left Arrow
                        IconButton(
                            onClick = { viewModel.prevPage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("<", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }

                        // Retro Dial Slider representing progress volume
                        val maxPages = comic.pageCount
                        Slider(
                            value = currentPage.toFloat(),
                            onValueChange = { viewModel.changePageDial(it.toInt()) },
                            valueRange = 0f..(maxPages - 1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00BFFF),
                                activeTrackColor = Color(0xFF00BFFF),
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.width(110.dp)
                        )

                        // Right Arrow
                        IconButton(
                            onClick = { viewModel.nextPage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(">", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }

                        // Page tracker
                        Text(
                            text = "${currentPage + 1}/${comic.pageCount}",
                            color = Color(0xFF00BFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Bottom control legendary buttons bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.background(Color.White, RoundedCornerShape(2.dp)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                                Text("L", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("PREV PAGE", color = Color(0xAAFFFFFF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.background(Color.White, RoundedCornerShape(2.dp)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                                Text("R", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("NEXT PAGE", color = Color(0xAAFFFFFF), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Zoom settings key
                        Button(
                            onClick = { viewModel.toggleZoom() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFFFFF)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(28.dp).testTag("zoom_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                PlayStationGlyph(symbol = "□", modifier = Modifier.size(14.dp))
                                Text("ZOOM: ${zoomFactor}x", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PlayStationGlyph(symbol = "○")
                            Text("MENU MASK", color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
