package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ComicPageRenderer(
    filePath: String,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    if (filePath.startsWith("sample_")) {
        // Render high-fidelity procedural comic strip for demo comics
        ProceduralComicPage(comicKey = filePath, pageIndex = pageIndex, modifier = modifier)
    } else {
        // Render real PDF page
        LocalPdfPageRenderer(pdfPath = filePath, pageIndex = pageIndex, modifier = modifier)
    }
}

@Composable
fun LocalPdfPageRenderer(
    pdfPath: String,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(pdfPath, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pdfPath, pageIndex) { mutableStateOf(true) }
    var errorMsg by remember(pdfPath, pageIndex) { mutableStateOf<String?>(null) }

    LaunchedEffect(pdfPath, pageIndex) {
        isLoading = true
        errorMsg = null
        withContext(Dispatchers.IO) {
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null
            var page: PdfRenderer.Page? = null
            try {
                val file = File(pdfPath)
                if (!file.exists()) {
                    errorMsg = "Comic file not found on Memory Stick™ Duo."
                    return@withContext
                }
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(pfd)
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                    errorMsg = "Page $pageIndex fits out of boundaries (Total Pages: ${renderer.pageCount})."
                    return@withContext
                }
                
                page = renderer.openPage(pageIndex)
                
                // Keep dimensions within safe limits to prevent OOM
                val maxDim = 1200f
                val scale = (maxDim / maxOf(page.width, page.height)).coerceAtMost(1.0f)
                val destW = (page.width * scale).toInt()
                val destH = (page.height * scale).toInt()
                
                val bmp = Bitmap.createBitmap(destW, destH, Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                bitmap = bmp
            } catch (e: Exception) {
                errorMsg = "Reading Error:\n${e.localizedMessage ?: "Unknown I/O event"}"
            } finally {
                try { page?.close() } catch (ignored: Exception) {}
                try { renderer?.close() } catch (ignored: Exception) {}
                try { pfd?.close() } catch (ignored: Exception) {}
            }
        }
        isLoading = false
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D0F))
            .border(1.dp, Color(0x33FFFFFF))
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = Color(0xFF00BFFF), strokeWidth = 2.dp)
                Text(
                    text = "DECOMPRESSING UMD DATA...",
                    color = Color(0xAAFFFFFF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else if (errorMsg != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "MAGICGATE™ READ FAILED",
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = errorMsg ?: "Error",
                    color = Color(0xAAFFFFFF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "UMD Digital Comic Page ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ProceduralComicPage(
    comicKey: String,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    // Generate an incredibly styled retro manga / digital comic page procedurally!
    val themeColor = when (comicKey) {
        "sample_metal_gear" -> Color(0xFF4A6B53) // Army/Sage Green vibe
        "sample_gravity_rush" -> Color(0xFF4C1C30) // Purple/Sakura magical vibe
        else -> Color(0xFF141619) // Dark Slate
    }
    
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1EDE4)) // Vintage manga paper
            .padding(8.dp)
            .border(3.dp, Color(0xFF1A1A1A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Clean comic border
            drawRect(
                color = Color(0xFF1A1A1A),
                topLeft = Offset(2f, 2f),
                size = Size(w - 4f, h - 4f),
                style = Stroke(width = 4f)
            )

            // Let's draw different frames based on pages
            when (pageIndex) {
                0 -> { // Cover Page Design
                    // Draw a gorgeous retro vintage comic cover with halftone effects and stylized layers
                    // Background graphic block
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF242830), themeColor, Color(0xFF141619))
                        ),
                        topLeft = Offset(10f, 10f),
                        size = Size(w - 20f, h * 0.7f)
                    )

                    // Cover layout borders
                    drawRect(
                        color = Color(0xFF1A1A1A),
                        topLeft = Offset(10f, 10f),
                        size = Size(w - 20f, h * 0.7f),
                        style = Stroke(width = 3f)
                    )

                    // Draw stylized speed lines or cross-hatching in the canvas
                    val lineCount = 30
                    for (i in 0..lineCount) {
                        val progress = i.toFloat() / lineCount
                        drawLine(
                            color = Color(0x22FFFFFF),
                            start = Offset(10f, h * 0.35f),
                            end = Offset(w - 10f, h * 0.7f * progress),
                            strokeWidth = 2f
                        )
                    }

                    // A cool neon tech circle (classic PlayStation power visual)
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.22f,
                        center = Offset(w * 0.5f, h * 0.35f),
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = Color(0xFF00BFFF),
                        radius = w * 0.18f,
                        center = Offset(w * 0.5f, h * 0.35f),
                        style = Stroke(width = 3f)
                    )
                }

                1 -> { // Page 2: Three Panels
                    val splitY1 = h * 0.35f
                    val splitY2 = h * 0.70f

                    // Panel 1
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(10f, 10f), size = Size(w - 20f, splitY1 - 20f), style = Stroke(width = 3f))
                    // Panel 2 Left
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(10f, splitY1 + 10f), size = Size(w * 0.5f - 15f, splitY2 - splitY1 - 20f), style = Stroke(width = 3f))
                    // Panel 2 Right
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(w * 0.5f + 5f, splitY1 + 10f), size = Size(w * 0.5f - 15f, splitY2 - splitY1 - 20f), style = Stroke(width = 3f))
                    // Panel 3 (Full Bottom)
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(10f, splitY2 + 10f), size = Size(w - 20f, h - splitY2 - 20f), style = Stroke(width = 3f))

                    // Draw graphical representations inside panels:
                    // Panel 1: Sunburst rays representing dramatic intro
                    for (i in 0..360 step 15) {
                        val angle = Math.toRadians(i.toDouble())
                        val centerX = w * 0.5f
                        val centerY = splitY1 * 0.5f
                        drawLine(
                            color = Color(0xFF1A1A1A),
                            start = Offset(centerX, centerY),
                            end = Offset(
                                centerX + (w * 0.4f * Math.cos(angle)).toFloat(),
                                centerY + (splitY1 * 0.4f * Math.sin(angle)).toFloat()
                            ),
                            strokeWidth = 1f
                        )
                    }
                    drawCircle(Color(0xFFF1EDE4), radius = 25.dp.toPx(), center = Offset(w * 0.5f, splitY1 * 0.5f))

                    // Panel 2 Left: Speed lines
                    for (i in 0..10) {
                        val curY = splitY1 + 20f + (splitY2 - splitY1 - 40f) * (i.toFloat() / 10f)
                        drawLine(
                            color = Color(0x771A1A1A),
                            start = Offset(15f, curY),
                            end = Offset(w * 0.45f, curY),
                            strokeWidth = 1.5f
                        )
                    }

                    // Panel 3: Stylized tech wave layout like PSP waves!
                    val path = Path()
                    path.moveTo(15f, splitY2 + 30f)
                    for (x in 15..(w.toInt() - 15) step 10) {
                        val fx = x.toFloat()
                        val fy = splitY2 + 50f + 25f * kotlin.math.sin(fx * 0.02f)
                        path.lineTo(fx, fy)
                    }
                    drawPath(path = path, color = themeColor, style = Stroke(width = 2.5f))
                }

                2 -> { // Page 3: Dramatic Diagonal Layout (Manga Action Style)
                    // Diagonal split
                    val path1 = Path().apply {
                        moveTo(10f, 10f)
                        lineTo(w * 0.6f, 10f)
                        lineTo(w * 0.3f, h - 10f)
                        lineTo(10f, h - 10f)
                        close()
                    }
                    val path2 = Path().apply {
                        moveTo(w * 0.65f, 10f)
                        lineTo(w - 10f, 10f)
                        lineTo(w - 10f, h - 10f)
                        lineTo(w * 0.35f, h - 10f)
                        close()
                    }
                    drawPath(path1, Color(0xFF1A1A1A), style = Stroke(width = 3f))
                    drawPath(path2, Color(0xFF1A1A1A), style = Stroke(width = 3f))

                    // Draw dramatic halftone screen points on the right panel
                    for (x in (w * 0.45f).toInt()..(w - 20).toInt() step 20) {
                        for (y in 20..(h - 20).toInt() step 20) {
                            drawCircle(Color(0x1F000000), radius = 3f, center = Offset(x.toFloat(), y.toFloat()))
                        }
                    }
                }
                
                3 -> { // Page 4: Concentric Tech Circles & Grids
                    // Draw a classic Comic tech layout (schematics or cyber look)
                    for (i in 0..15) {
                        drawLine(
                            color = Color(0x331A1A1A),
                            start = Offset(20f + i * (w / 15f), 10f),
                            end = Offset(20f + i * (w / 15f), h - 10f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0x331A1A1A),
                            start = Offset(10f, 20f + i * (h / 15f)),
                            end = Offset(w - 10f, 20f + i * (h / 15f)),
                            strokeWidth = 1f
                        )
                    }
                    drawCircle(Color(0xFF1A1A1A), radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 1.5f))
                    drawCircle(Color(0xFF1A1A1A), radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = 3f))
                }

                4 -> { // Page 5: Action Panel With Speed Zoom lines
                    val cx = w * 0.5f
                    val cy = h * 0.45f
                    // Draw dramatic manga rays outward from center
                    for (i in 0..360 step 8) {
                        val rad = Math.toRadians(i.toDouble())
                        val sx = (cx + Math.cos(rad) * 60f).toFloat()
                        val sy = (cy + Math.sin(rad) * 60f).toFloat()
                        val ex = (cx + Math.cos(rad) * w).toFloat()
                        val ey = (cy + Math.sin(rad) * h).toFloat()
                        drawLine(
                            color = Color(0xFF1A1A1A),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 2.5f
                        )
                    }
                }

                else -> { // Page 6+: Standard Ending Panels
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(10f, 10f), size = Size(w - 20f, h * 0.48f), style = Stroke(width = 3f))
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(10f, h * 0.52f), size = Size(w - 20f, h * 0.44f), style = Stroke(width = 3f))
                }
            }
        }

        // Overlaid texts and Speeches based on Page Indexes & Comic Selection!
        when (pageIndex) {
            0 -> { // Cover text overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val subtitle = when (comicKey) {
                        "sample_metal_gear" -> "SOLID DIGITAL RETRO SERIES"
                        "sample_gravity_rush" -> "THE CELESTIAL MAIDEN SAGA"
                        else -> "PORTABLE DIGITAL CLASSIC"
                    }
                    
                    val titleText = when (comicKey) {
                        "sample_metal_gear" -> "METAL GEAR\nSOLID"
                        "sample_gravity_rush" -> "GRAVITY\nRUSH"
                        else -> "PSP™ DIGITAL\nCOMIC GUIDE"
                    }

                    // Vintage Issue seal
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE91E63), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ISSUE #1 - EXCLUSIVE FIRST EDITION",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = titleText,
                        color = Color(0xFF1A1A1A),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 36.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subtitle,
                        color = Color(0xFF555555),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .border(1.dp, Color(0x66000000), RoundedCornerShape(20.dp))
                            .background(Color(0x15000000), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "PRESS",
                                color = Color(0xFF333333),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            PlayStationGlyph(symbol = "○", modifier = Modifier.size(16.dp))
                            Text(
                                text = "PLAY / NEXT",
                                color = Color(0xFF333333),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            1 -> { // Detail Panels Text
                // Conversation 1
                Box(
                    modifier = Modifier
                        .offset(x = 16.dp, y = 20.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(max = 180.dp)
                ) {
                    Text(
                        text = when (comicKey) {
                            "sample_metal_gear" -> "Snake, we have verified the MagicGate™ protocol file size!"
                            "sample_gravity_rush" -> "Gravity has shifted, Kat! Use the Analog Stick to float!"
                            else -> "Nostalgic digital comics are loading completely offline!"
                        },
                        color = Color(0xFF1A1A1A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }

                // Call to action speech bubble
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-16).dp, y = 20.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(max = 160.dp)
                ) {
                    Text(
                        text = when (comicKey) {
                            "sample_metal_gear" -> "Liquid is planning a custom firmware launch. Be careful!"
                            "sample_gravity_rush" -> "I see Hekseville under the digital waves!"
                            else -> "Press [L] or [R] key anytime to flip pages."
                        },
                        color = Color(0xFF1A1A1A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }

                // Dramatic action description
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                        .background(Color(0xFF1A1A1A))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CHAPTER 1: THE RETRO CONSOLE DUO ARRIVES",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            2 -> { // Dramatic Action page
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 24.dp, y = (-20).dp)
                ) {
                    Text(
                        text = "SZAAK!",
                        color = Color(0xFF1A1A1A),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 24.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(max = 150.dp)
                ) {
                    Text(
                        text = "“The UMD disc starts spinning up. Can you hear the laser seek and click? Truly nostalgic!”",
                        color = Color(0xFF1A1A1A),
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 13.sp
                    )
                }
            }

            3 -> { // Tech overlay page
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color(0xEEFFFFFF), RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .widthIn(max = 240.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SYSTEM CODE: MAGICGATE™",
                            color = themeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Digital Comic Reader layout running in stunning emulation. This structure provides direct fluid scrolling and volume indices matching native firmware layouts.",
                            color = Color(0xFF1A1A1A),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            4 -> { // Zoom ray action page
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-30).dp)
                ) {
                    Text(
                        text = "KA-BOOM!",
                        color = Color(0xFFFF5252),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .widthIn(max = 220.dp)
                ) {
                    Text(
                        text = "“The system memory loading speed is astonishing! Accessing internal storage slots...”",
                        color = Color(0xFF1A1A1A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
            }

            else -> { // Final Ending Page
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "TO BE CONTINUED...",
                        color = themeColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x33000000), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "You have read all available panels on this demo volume of ${
                                when (comicKey) {
                                    "sample_metal_gear" -> "Metal Gear"
                                    "sample_gravity_rush" -> "Gravity Rush"
                                    else -> "PSP Guide"
                                }
                            }.\n\nTry uploading your own raw PDF comics in the Memory Stick Duo™ setup utility!",
                            color = Color(0xFF333333),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
