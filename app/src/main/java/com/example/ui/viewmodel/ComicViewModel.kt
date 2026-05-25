package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.media.ToneGenerator
import android.media.AudioManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleComics
import com.example.data.db.AppDatabase
import com.example.data.db.Comic
import com.example.data.repository.ComicRepository
import com.example.ui.screens.PspThemeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class ComicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ComicRepository(database.comicDao())

    // Theme selector
    private val _themeColor = MutableStateFlow(PspThemeColor.SLATE_GRAY)
    val themeColor: StateFlow<PspThemeColor> = _themeColor.asStateFlow()

    // Sound and haptics flag
    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    // XMB Navigation States
    private val _activeCategory = MutableStateFlow(1) // 0 = Settings, 1 = Comics, 2 = Upload
    val activeCategory: StateFlow<Int> = _activeCategory.asStateFlow()

    private val _selectedItemIndex = MutableStateFlow(0)
    val selectedItemIndex: StateFlow<Int> = _selectedItemIndex.asStateFlow()

    // Memory Stick loading state
    private val _isWritingMemoryStick = MutableStateFlow(false)
    val isWritingMemoryStick: StateFlow<Boolean> = _isWritingMemoryStick.asStateFlow()

    private val _writingProgress = MutableStateFlow(0f)
    val writingProgress: StateFlow<Float> = _writingProgress.asStateFlow()

    private val _writingStage = MutableStateFlow("")
    val writingStage: StateFlow<String> = _writingStage.asStateFlow()

    // Reading Mode States
    private val _activeReadingComic = MutableStateFlow<Comic?>(null)
    val activeReadingComic: StateFlow<Comic?> = _activeReadingComic.asStateFlow()

    private val _currentReadingPage = MutableStateFlow(0)
    val currentReadingPage: StateFlow<Int> = _currentReadingPage.asStateFlow()

    private val _zoomFactor = MutableStateFlow(1.0f) // 1x, 1.5x, 2x, 2.5x zooming
    val zoomFactor: StateFlow<Float> = _zoomFactor.asStateFlow()

    // Tone generator for nostalgic retro UI sound bleeps
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 60)
        } catch (ignored: Exception) {}

        // Check and pre-populate sample comics if database empty
        viewModelScope.launch {
            repository.allComics.first().let { comics ->
                if (comics.isEmpty()) {
                    SampleComics.list.forEach { sample ->
                        repository.insertComic(sample)
                    }
                }
            }
        }
    }

    // Reactive comic stream from DB
    val allComics: StateFlow<List<Comic>> = repository.allComics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun playRetroSound(toneType: Int = ToneGenerator.TONE_PROP_BEEP) {
        if (!_soundEnabled.value) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toneGenerator?.startTone(toneType, 100)
            } catch (ignored: Exception) {}
        }
    }

    fun triggerHapticFeedback() {
        if (!_hapticEnabled.value) return
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(25) // Subtle 25ms vibrator feedback
        } catch (ignored: Exception) {}
    }

    // Navigate XMB horizontally
    fun changeCategory(categoryIndex: Int) {
        if (_activeCategory.value != categoryIndex) {
            _activeCategory.value = categoryIndex
            _selectedItemIndex.value = 0
            playRetroSound(ToneGenerator.TONE_PROP_ACK)
            triggerHapticFeedback()
        }
    }

    // Navigate vertically in items list
    fun changeItemIndex(index: Int) {
        _selectedItemIndex.value = index
        playRetroSound(ToneGenerator.TONE_PROP_PROMPT)
        triggerHapticFeedback()
    }

    // Read details
    fun openComicReader(comic: Comic) {
        _activeReadingComic.value = comic
        _currentReadingPage.value = comic.currentPage
        _zoomFactor.value = 1.0f
        
        playRetroSound(ToneGenerator.TONE_PROP_BEEP2)
        triggerHapticFeedback()
    }

    fun closeComicReader() {
        val currentComic = _activeReadingComic.value
        val page = _currentReadingPage.value
        if (currentComic != null) {
            viewModelScope.launch {
                repository.updateProgress(currentComic.id, page)
            }
        }
        _activeReadingComic.value = null
        playRetroSound(ToneGenerator.TONE_PROP_NACK)
        triggerHapticFeedback()
    }

    fun nextPage() {
        val maxPages = _activeReadingComic.value?.pageCount ?: 1
        if (_currentReadingPage.value < maxPages - 1) {
            _currentReadingPage.value += 1
            playRetroSound(ToneGenerator.TONE_PROP_PROMPT)
            triggerHapticFeedback()
        }
    }

    fun prevPage() {
        if (_currentReadingPage.value > 0) {
            _currentReadingPage.value -= 1
            playRetroSound(ToneGenerator.TONE_PROP_PROMPT)
            triggerHapticFeedback()
        }
    }

    fun toggleZoom() {
        _zoomFactor.value = when (_zoomFactor.value) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            2.0f -> 2.5f
            else -> 1.0f
        }
        playRetroSound(ToneGenerator.TONE_PROP_BEEP2)
        triggerHapticFeedback()
    }
    
    fun setZoom(factor: Float) {
        _zoomFactor.value = factor.coerceIn(1.0f, 3.0f)
    }

    fun changePageDial(page: Int) {
        val maxPages = _activeReadingComic.value?.pageCount ?: 1
        val coerced = page.coerceIn(0, maxPages - 1)
        if (coerced != _currentReadingPage.value) {
            _currentReadingPage.value = coerced
            playRetroSound(ToneGenerator.TONE_PROP_ACK)
            triggerHapticFeedback()
        }
    }

    fun selectTheme(pspThemeColor: PspThemeColor) {
        _themeColor.value = pspThemeColor
        playRetroSound(ToneGenerator.TONE_PROP_ACK)
        triggerHapticFeedback()
    }

    fun toggleHaptic() {
        _hapticEnabled.value = !_hapticEnabled.value
        triggerHapticFeedback()
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
        if (_soundEnabled.value) {
            playRetroSound(ToneGenerator.TONE_PROP_ACK)
        }
    }

    fun deleteComic(comic: Comic) {
        viewModelScope.launch {
            repository.deleteComicById(comic.id)
            // Delete accompanying files if they are local
            if (!comic.filePath.startsWith("sample_")) {
                try {
                    val f = File(comic.filePath)
                    if (f.exists()) f.delete()
                    if (comic.coverPath != null) {
                        val cf = File(comic.coverPath)
                        if (cf.exists()) cf.delete()
                    }
                } catch (ignored: Exception) {}
            }
            if (_selectedItemIndex.value > 0) {
                _selectedItemIndex.value -= 1
            }
            playRetroSound(ToneGenerator.TONE_PROP_NACK)
            triggerHapticFeedback()
        }
    }

    fun resetAllComicsProgress() {
        viewModelScope.launch {
            allComics.value.forEach { comic ->
                repository.updateProgress(comic.id, 0)
            }
            playRetroSound(ToneGenerator.TONE_PROP_BEEP2)
            triggerHapticFeedback()
        }
    }

    // Import real PDF document through standard android stream
    fun importComicPdf(context: Context, uri: Uri, title: String, artist: String, volume: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isWritingMemoryStick.value = true
            _writingProgress.value = 0.05f
            _writingStage.value = "AUTHENTICATING MAGICGATE™ MEMORY STUCK..."
            delay(700)

            _writingProgress.value = 0.20f
            _writingStage.value = "READING RAW UMD PDF DIRECT SECTOR..."
            delay(600)

            try {
                // Copy selected stream to filesDir
                val outputName = "comic_${System.currentTimeMillis()}.pdf"
                val outFile = File(context.filesDir, outputName)

                _writingProgress.value = 0.40f
                _writingStage.value = "WRITING SECTORS TO MEMORY STICK PRO DUO™..."
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                _writingProgress.value = 0.65f
                _writingStage.value = "EXTRACTING COMIC COVER THUMBNAIL..."
                delay(500)

                var finalPageCount = 0
                var coverPath: String? = null

                var pfd: ParcelFileDescriptor? = null
                var renderer: PdfRenderer? = null
                try {
                    pfd = ParcelFileDescriptor.open(outFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    renderer = PdfRenderer(pfd)
                    finalPageCount = renderer.pageCount

                    if (finalPageCount > 0) {
                        val page = renderer.openPage(0)
                        
                        // Small cover thumbnail size
                        val designWidth = (page.width / 2).coerceAtLeast(300)
                        val designHeight = (page.height / 2).coerceAtLeast(400)
                        val config = Bitmap.Config.ARGB_8888
                        val coverBmp = Bitmap.createBitmap(designWidth, designHeight, config)
                        
                        page.render(coverBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        val coverFile = File(context.filesDir, "cover_${System.currentTimeMillis()}.png")
                        coverFile.outputStream().use { outStream ->
                            coverBmp.compress(Bitmap.CompressFormat.PNG, 85, outStream)
                        }
                        coverPath = coverFile.absolutePath
                    }
                } catch (pe: Exception) {
                    pe.printStackTrace()
                } finally {
                    try { renderer?.close() } catch (ignored: Exception) {}
                    try { pfd?.close() } catch (ignored: Exception) {}
                }

                _writingProgress.value = 0.85f
                _writingStage.value = "REGISTERING SYSTEM DATA ID LICENSE..."
                delay(600)

                val newComic = Comic(
                    title = title.ifBlank { "Untitled Comic Book" },
                    filePath = outFile.absolutePath,
                    coverPath = coverPath,
                    artist = artist.ifBlank { "Unspecified Artist" },
                    volume = volume.ifBlank { "Vol. 1" },
                    pageCount = if (finalPageCount > 0) finalPageCount else 1,
                    currentPage = 0
                )
                repository.insertComic(newComic)

                _writingProgress.value = 1.0f
                _writingStage.value = "TRANSFER RESYNC SUCCESS!"
                delay(800)
            } catch (e: Exception) {
                _writingStage.value = "ERROR: WRITING FAILED!\n${e.localizedMessage ?: "Unknown hardware trigger"}"
                delay(1500)
            } finally {
                _isWritingMemoryStick.value = false
                _writingProgress.value = 0f
                _writingStage.value = ""
            }
        }
    }
}
