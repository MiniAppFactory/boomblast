package com.example.ui.games.blockblast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlockBlue
import com.example.ui.theme.BlockGreen
import com.example.ui.theme.BlockOrange
import com.example.ui.theme.BlockPink
import com.example.ui.theme.BlockPurple
import com.example.ui.theme.BlockYellow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.utils.SoundManager
import kotlin.random.Random

data class BlockShape(
    val id: Int,
    val pattern: List<List<Boolean>>,
    val colorIndex: Int
)

val BLOCK_COLORS = listOf(
    BlockOrange,
    BlockBlue,
    BlockGreen,
    BlockPink,
    BlockYellow,
    BlockPurple
)

val SHAPE_PATTERNS = listOf(
    // 1x1 Single
    listOf(listOf(true)),
    // 2x1 Line
    listOf(listOf(true, true)),
    // 1x2 Line
    listOf(listOf(true), listOf(true)),
    // 3x1 Line
    listOf(listOf(true, true, true)),
    // 1x3 Line
    listOf(listOf(true), listOf(true), listOf(true)),
    // 2x2 Square
    listOf(listOf(true, true), listOf(true, true)),
    // L Shape 1
    listOf(listOf(true, false), listOf(true, true)),
    // L Shape 2
    listOf(listOf(true, true), listOf(false, true)),
    // T Shape
    listOf(listOf(true, true, true), listOf(false, true, false)),
    // 3x3 Big Square
    listOf(listOf(true, true, true), listOf(true, true, true), listOf(true, true, true))
)

data class BlockThemeOption(
    val id: String,
    val titleTr: String,
    val titleEn: String,
    val icon: String,
    val descriptionTr: String,
    val descriptionEn: String
)

val BLOCK_THEMES = listOf(
    BlockThemeOption(
        id = "CLASSIC",
        titleTr = "Klasik 3D Kabartma",
        titleEn = "Classic 3D Bevel",
        icon = "🧊",
        descriptionTr = "3D kabartmalı kristal bloklar",
        descriptionEn = "3D embossed crystal blocks"
    ),
    BlockThemeOption(
        id = "FRUIT",
        titleTr = "Meyve Küpleri",
        titleEn = "Fruit Cubes",
        icon = "🍉",
        descriptionTr = "Karpuz, peynir, çilek ve portakal",
        descriptionEn = "Watermelon, cheese, strawberry, orange"
    ),
    BlockThemeOption(
        id = "SWEETS",
        titleTr = "Şekerleme & Tatlı",
        titleEn = "Sweets & Donuts",
        icon = "🍩",
        descriptionTr = "Donut, çikolata, bisküvi ve şeker",
        descriptionEn = "Donut, chocolate, cookie, candy"
    ),
    BlockThemeOption(
        id = "MIXED",
        titleTr = "Karma Küpler",
        titleEn = "Mixed Fun",
        icon = "🎲",
        descriptionTr = "Meyve, tatlı ve kristal karışımı",
        descriptionEn = "Mix of fruits, sweets & crystals"
    )
)

@Composable
fun BlockBlastGame(
    highScore: Int,
    currentTheme: String = "CLASSIC",
    isTr: Boolean = true,
    soundEnabled: Boolean = true,
    onSelectTheme: (String) -> Unit = {},
    onBack: () -> Unit,
    onGameOver: (score: Int, coins: Int) -> Unit
) {
    val gridSize = 8
    val board = remember { mutableStateListOf<Int>().apply { repeat(gridSize * gridSize) { add(0) } } }
    var score by remember { mutableIntStateOf(0) }
    var comboCount by remember { mutableIntStateOf(0) }
    var selectedShapeIndex by remember { mutableIntStateOf(-1) }
    var isGameOver by remember { mutableStateOf(false) }
    var coinsEarned by remember { mutableIntStateOf(0) }
    var lastClearedText by remember { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }

    val trayShapes = remember { mutableStateListOf<BlockShape?>() }

    val currentLevel = (score / 300) + 1
    val levelProgress = ((score % 300).toFloat() / 300f).coerceIn(0f, 1f)

    fun generateNewTray() {
        trayShapes.clear()
        val availablePatterns = when {
            currentLevel <= 1 -> SHAPE_PATTERNS.take(6) // 1x1, 2x1, 1x2, 3x1, 1x3, 2x2
            currentLevel <= 2 -> SHAPE_PATTERNS.take(8) // + L shapes
            else -> SHAPE_PATTERNS // All shapes including T-shape and 3x3 square
        }
        repeat(3) { index ->
            val randomPattern = availablePatterns[Random.nextInt(availablePatterns.size)]
            val randomColor = Random.nextInt(1, BLOCK_COLORS.size + 1)
            trayShapes.add(BlockShape(id = index, pattern = randomPattern, colorIndex = randomColor))
        }
    }

    fun canPlaceShape(shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        for (r in shape.pattern.indices) {
            for (c in shape.pattern[r].indices) {
                if (shape.pattern[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    if (targetR !in 0 until gridSize || targetC !in 0 until gridSize) return false
                    if (board[targetR * gridSize + targetC] != 0) return false
                }
            }
        }
        return true
    }

    fun canPlaceAnywhere(shape: BlockShape): Boolean {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (canPlaceShape(shape, r, c)) return true
            }
        }
        return false
    }

    fun checkGameOver() {
        val remainingShapes = trayShapes.filterNotNull()
        if (remainingShapes.isNotEmpty()) {
            val valid = remainingShapes.any { canPlaceAnywhere(it) }
            if (!valid) {
                isGameOver = true
                coinsEarned = score / 10
                onGameOver(score, coinsEarned)
            }
        }
    }

    fun resetGame() {
        board.clear()
        repeat(gridSize * gridSize) { board.add(0) }
        score = 0
        comboCount = 0
        selectedShapeIndex = -1
        isGameOver = false
        coinsEarned = 0
        lastClearedText = ""
        generateNewTray()
    }

    LaunchedEffect(Unit) {
        if (trayShapes.isEmpty()) {
            generateNewTray()
        }
    }

    fun placeShape(shape: BlockShape, startRow: Int, startCol: Int) {
        if (!canPlaceShape(shape, startRow, startCol)) return

        SoundManager.playBeep(soundEnabled)

        var placedBlocks = 0
        for (r in shape.pattern.indices) {
            for (c in shape.pattern[r].indices) {
                if (shape.pattern[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    board[targetR * gridSize + targetC] = shape.colorIndex
                    placedBlocks++
                }
            }
        }

        score += placedBlocks * 10

        // Check full rows & columns
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until gridSize) {
            var fullRow = true
            for (c in 0 until gridSize) {
                if (board[r * gridSize + c] == 0) {
                    fullRow = false
                    break
                }
            }
            if (fullRow) rowsToClear.add(r)
        }

        for (c in 0 until gridSize) {
            var fullCol = true
            for (r in 0 until gridSize) {
                if (board[r * gridSize + c] == 0) {
                    fullCol = false
                    break
                }
            }
            if (fullCol) colsToClear.add(c)
        }

        val totalLinesCleared = rowsToClear.size + colsToClear.size
        if (totalLinesCleared > 0) {
            SoundManager.playSuccess(soundEnabled)
            comboCount++
            val lineBonus = totalLinesCleared * 100 * comboCount
            score += lineBonus
            lastClearedText = if (totalLinesCleared > 1) "MULTI-BLAST! +$lineBonus ($comboCount x COMBO)" else "BLAST! +$lineBonus"

            // Clear cells
            rowsToClear.forEach { r ->
                for (c in 0 until gridSize) {
                    board[r * gridSize + c] = 0
                }
            }
            colsToClear.forEach { c ->
                for (r in 0 until gridSize) {
                    board[r * gridSize + c] = 0
                }
            }
        } else {
            comboCount = 0
            lastClearedText = ""
        }

        // Remove used shape from tray
        trayShapes[selectedShapeIndex] = null
        selectedShapeIndex = -1

        // If all 3 shapes used, spawn 3 new
        if (trayShapes.all { it == null }) {
            generateNewTray()
        }

        checkGameOver()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("block_blast_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTr) "BLOK PATLAT" else "BLOCK BLAST",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme Choice Button
                    Surface(
                        color = NeonPurple.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showThemeDialog = true }
                            .testTag("block_blast_theme_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeTheme = BLOCK_THEMES.find { it.id == currentTheme } ?: BLOCK_THEMES.first()
                            Text(text = activeTheme.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTr) "TEMA" else "THEME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { resetGame() },
                        modifier = Modifier.testTag("block_blast_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Game",
                            tint = NeonGold
                        )
                    }
                }
            }

            // Score & Level Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (isTr) "SKOR" else "SCORE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$score", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.1f).padding(horizontal = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTr) "SEVİYE $currentLevel" else "LEVEL $currentLevel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { levelProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = NeonCyan,
                            trackColor = Color(0xFF334155)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (isTr) "EN YÜKSEK" else "BEST", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${maxOf(score, highScore)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeonGold)
                    }
                }
            }

            // Combo Text Banner
            if (lastClearedText.isNotEmpty()) {
                Text(
                    text = lastClearedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonMagenta,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 8x8 Main Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(2.dp, Brush.linearGradient(listOf(NeonCyan, NeonPurple)), RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (r in 0 until gridSize) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (c in 0 until gridSize) {
                                val cellVal = board[r * gridSize + c]
                                val selectedShape = if (selectedShapeIndex in trayShapes.indices) trayShapes[selectedShapeIndex] else null
                                val isHoverValid = selectedShape != null && canPlaceShape(selectedShape, r, c)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.5.dp)
                                        .background(
                                            if (cellVal == 0 && !isHoverValid) Color(0xFF0F172A) else Color.Transparent,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = if (cellVal > 0) Color.Transparent else Color(0xFF334155),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            if (selectedShape != null) {
                                                placeShape(selectedShape, r, c)
                                            }
                                        }
                                        .testTag("block_cell_${r}_${c}")
                                ) {
                                    if (cellVal > 0 || isHoverValid) {
                                        EmbossedBlockCell(
                                            colorIndex = cellVal,
                                            theme = currentTheme,
                                            isHover = cellVal == 0 && isHoverValid,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (selectedShapeIndex != -1) {
                    if (isTr) "Izgarada bir yere dokunarak yerleştir!" else "Tap grid to place shape!"
                } else {
                    if (isTr) "Aşağıdan bir blok seçin:" else "Pick a block below:"
                },
                fontSize = 12.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Tray with 3 Shapes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 3) {
                    val shape = trayShapes.getOrNull(i)
                    val isSelected = selectedShapeIndex == i

                    val scaleFactor by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = tween(150, easing = FastOutSlowInEasing),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .padding(4.dp)
                            .scale(scaleFactor)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF334155) else Color(0xFF1E293B))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonCyan else Color(0xFF475569),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = shape != null) {
                                selectedShapeIndex = if (selectedShapeIndex == i) -1 else i
                            }
                            .testTag("tray_shape_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (shape != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                for (r in shape.pattern.indices) {
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        for (c in shape.pattern[r].indices) {
                                            val active = shape.pattern[r][c]
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(0.5.dp)
                                            ) {
                                                if (active) {
                                                    EmbossedBlockCell(
                                                        colorIndex = shape.colorIndex,
                                                        theme = currentTheme,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(if (isTr) "BOŞ" else "EMPTY", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showThemeDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .border(2.dp, NeonPurple, RoundedCornerShape(20.dp))
                        .clickable(enabled = false) {} // Prevent dismiss on card click
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme", tint = NeonPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTr) "BLOK TEMASI SEÇİN" else "SELECT BLOCK THEME",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BLOCK_THEMES.forEach { theme ->
                                val isSelected = currentTheme == theme.id
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) NeonPurple.copy(alpha = 0.25f) else Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) NeonPurple else Color(0xFF334155),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            SoundManager.playBeep(soundEnabled)
                                            onSelectTheme(theme.id)
                                            showThemeDialog = false
                                        }
                                        .testTag("select_block_theme_${theme.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = theme.icon, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isTr) theme.titleTr else theme.titleEn,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isTr) theme.descriptionTr else theme.descriptionEn,
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        if (isSelected) {
                                            Surface(
                                                color = NeonGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (isTr) "SEÇİLİ" else "ACTIVE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showThemeDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isTr) "KAPAT" else "CLOSE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Game Over Modal
        AnimatedVisibility(
            visible = isGameOver,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp)
                        .border(2.dp, NeonMagenta, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isTr) "OYUN BİTTİ!" else "GAME OVER!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonMagenta
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(if (isTr) "Toplam Skor" else "Total Score", fontSize = 14.sp, color = Color.Gray)
                        Text("$score", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("${if (isTr) "Kazanılan Coin" else "Coins Earned"}: +$coinsEarned 🪙", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NeonGold)

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("block_blast_restart_confirm")
                        ) {
                            Text(if (isTr) "TEKRAR OYNA" else "PLAY AGAIN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmbossedBlockCell(
    colorIndex: Int,
    theme: String,
    modifier: Modifier = Modifier,
    isHover: Boolean = false
) {
    val baseColor = if (isHover) {
        NeonCyan.copy(alpha = 0.35f)
    } else {
        BLOCK_COLORS.getOrElse((colorIndex - 1).coerceAtLeast(0)) { NeonCyan }
    }

    val emoji = if (isHover) "" else when (theme.uppercase()) {
        "FRUIT" -> when (colorIndex % 6) {
            1 -> "🍉"
            2 -> "🍊"
            3 -> "🥝"
            4 -> "🍓"
            5 -> "🧀"
            0 -> "🍇"
            else -> ""
        }
        "SWEETS" -> when (colorIndex % 6) {
            1 -> "🍩"
            2 -> "🍫"
            3 -> "🍪"
            4 -> "🧁"
            5 -> "🍬"
            0 -> "🧇"
            else -> ""
        }
        "MIXED" -> when (colorIndex % 6) {
            1 -> "🍉"
            2 -> "🍩"
            3 -> "🧀"
            4 -> "🍓"
            5 -> "🍪"
            0 -> "🍇"
            else -> ""
        }
        else -> ""
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val b = w * 0.16f

            drawRect(color = baseColor)

            if (!isHover) {
                // Top Bevel (Bright Highlight)
                val topPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w - b, b)
                    lineTo(b, b)
                    close()
                }
                drawPath(topPath, color = Color.White.copy(alpha = 0.45f))

                // Left Bevel (Mid Highlight)
                val leftPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(b, b)
                    lineTo(b, h - b)
                    lineTo(0f, h)
                    close()
                }
                drawPath(leftPath, color = Color.White.copy(alpha = 0.25f))

                // Right Bevel (Mid Shadow)
                val rightPath = Path().apply {
                    moveTo(w, 0f)
                    lineTo(w, h)
                    lineTo(w - b, h - b)
                    lineTo(w - b, b)
                    close()
                }
                drawPath(rightPath, color = Color.Black.copy(alpha = 0.25f))

                // Bottom Bevel (Dark Shadow)
                val bottomPath = Path().apply {
                    moveTo(0f, h)
                    lineTo(b, h - b)
                    lineTo(w - b, h - b)
                    lineTo(w, h)
                    close()
                }
                drawPath(bottomPath, color = Color.Black.copy(alpha = 0.45f))

                // Inner Face Rect
                drawRect(
                    color = baseColor,
                    topLeft = Offset(b, b),
                    size = Size((w - 2 * b).coerceAtLeast(0f), (h - 2 * b).coerceAtLeast(0f))
                )

                // Inner Face Top Shine
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    topLeft = Offset(b, b),
                    size = Size((w - 2 * b).coerceAtLeast(0f), ((h - 2 * b) * 0.45f).coerceAtLeast(0f))
                )
            }
        }

        if (emoji.isNotEmpty() && !isHover) {
            Text(
                text = emoji,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
