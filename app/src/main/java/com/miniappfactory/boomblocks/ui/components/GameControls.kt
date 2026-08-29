package com.miniappfactory.boomblocks.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.readableOn

// Faz 158 — STOK MATERIAL KONTROLLERININ YERINE gecen oyun kontrolleri.
//
// Kullanicinin teshisi: "biz sanki websitesi hissi veriyoruz". En somut
// kaniti buydu — acma/kapama stok `Switch`, ses siddeti stok `Slider`,
// secimler chevron'lu acilir liste. Bunlar WEB/FORM kontrolleri; bir oyunda
// hicbir kontrol stok gorunmemeli.
//
// Hepsi ayni malzemeden: oyuk ray (inner well) + gradyan dolgu + parlama +
// kalin kenarlik + kabartmali tutamak. Renkler yine sadece skin'in
// accentGradient'inden ve palette'inden turuyor — 6 skin de kendi
// kimliginde kalir.

// ---------------------------------------------------------------------------
// Toggle (Switch yerine)
// ---------------------------------------------------------------------------

@Composable
fun GameToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onColor: Color = Color(0xFF35D96B),
    trackWidth: Dp = 58.dp,
    trackHeight: Dp = 32.dp,
    accessibilityLabel: String? = null
) {
    val knobSize = trackHeight - 6.dp
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "gameToggleProgress"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) onColor else surfaces.sunken,
        label = "gameToggleTrack"
    )
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            // Dokunma hedefi: gorsel ray 32dp ama tiklanabilir kutu 48dp.
            .defaultMinSize(minWidth = trackWidth, minHeight = 48.dp)
            .semantics {
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                if (accessibilityLabel != null) contentDescription = accessibilityLabel
            }
            .clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(trackWidth)
                .height(trackHeight)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(lerp(trackColor, Color.Black, 0.18f), lerp(trackColor, Color.White, 0.12f))
                    )
                )
                .border(
                    width = 2.dp,
                    color = if (checked) lerp(onColor, Color.White, 0.45f) else surfaces.panelBorder,
                    shape = shape
                )
                // Acikken disa vuran yumusak parlama — hedef mockup'ta toggle
                // "yaniyor". Golge yerine radyal gradyan: her API'de calisir.
                .drawBehind {
                    if (progress > 0.01f) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(onColor.copy(alpha = 0.35f * progress), Color.Transparent),
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                radius = size.width * 0.8f
                            ),
                            cornerRadius = CornerRadius(size.height)
                        )
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val travel = trackWidth - knobSize - 6.dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .offset(x = travel * progress)
                    .size(knobSize)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(listOf(Color.White, Color(0xFFDDE3EC)))
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Slider (stok Slider yerine)
// ---------------------------------------------------------------------------

// Kalin oyuk ray + gradyan dolgu + parlayan tutamak + altinda %0/%50/%100
// kademe etiketleri. Stok Slider'dan sadece daha "oyun" degil, daha
// BILGILENDIRICI: kullanici nerede oldugunu okuyabiliyor.
@Composable
fun GameSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = surfaces.accentPrimary,
    showTicks: Boolean = true,
    tickLabels: List<String> = listOf("0%", "50%", "100%"),
    accessibilityLabel: String? = null
) {
    val clamped = value.coerceIn(0f, 1f)
    val railHeight = 12.dp
    val knobSize = 26.dp
    val disabledAlpha = if (enabled) 1f else 0.45f

    Column(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                // Dokunma alani 48dp; gorsel ray 12dp.
                .height(48.dp)
                .semantics {
                    if (accessibilityLabel != null) contentDescription = accessibilityLabel
                    stateDescription = "${(clamped * 100).toInt()}%"
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    val usable = size.width.toFloat()
                    fun emit(x: Float) {
                        if (usable <= 0f) return
                        onValueChange((x / usable).coerceIn(0f, 1f))
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        emit(down.position.x)
                        down.consume()
                        drag(down.id) { change ->
                            emit(change.position.x)
                            change.consume()
                        }
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val trackWidth = maxWidth
            // Oyuk ray
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(railHeight)
                    .clip(RoundedCornerShape(50))
                    .background(surfaces.sunken.copy(alpha = disabledAlpha))
                    .border(1.dp, surfaces.panelBorder.copy(alpha = disabledAlpha), RoundedCornerShape(50))
            )
            // Dolgu + parlama
            Box(
                modifier = Modifier
                    .width(trackWidth * clamped)
                    .height(railHeight)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                lerp(accent, Color.White, 0.35f).copy(alpha = disabledAlpha),
                                accent.copy(alpha = disabledAlpha)
                            )
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(accent.copy(alpha = 0.30f * disabledAlpha), Color.Transparent)
                            ),
                            cornerRadius = CornerRadius(size.height)
                        )
                    }
            )
            // Tutamak
            Box(
                modifier = Modifier
                    .offset(x = (trackWidth - knobSize) * clamped)
                    .size(knobSize)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                lerp(accent, Color.White, 0.65f).copy(alpha = disabledAlpha),
                                accent.copy(alpha = disabledAlpha)
                            )
                        )
                    )
                    .border(3.dp, Color.White.copy(alpha = 0.9f * disabledAlpha), RoundedCornerShape(50))
            )
        }
        if (showTicks && tickLabels.size >= 2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tickLabels.forEachIndexed { index, label ->
                    // Ortadaki etiket aktif degeri isaret ediyorsa vurgulanir.
                    val isCurrent = when (index) {
                        0 -> clamped < 0.05f
                        tickLabels.lastIndex -> clamped > 0.95f
                        else -> kotlin.math.abs(clamped - 0.5f) < 0.05f
                    }
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                        color = if (isCurrent && enabled) {
                            if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.4f) else accent
                        } else {
                            surfaces.hairline.copy(alpha = 0.9f * disabledAlpha)
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Segmented (iki/uc secenekli anahtar)
// ---------------------------------------------------------------------------

// Aktif taraf accent gradyanli pill + parlama + ikon; pasif taraf duz.
//
// TASMA: segment genisligi weight(1f), yukseklik SABIT DEGIL (defaultMinSize
// alt sinir). "Modalità Scura" / "Notifications de Rappel" gibi uzun ceviriler
// ikinci satira taser, kontrol uzar — kirpilmaz.
@Composable
fun <T> GameSegmented(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    optionIcon: (T) -> ImageVector? = { null },
    optionTestTag: (T) -> String = { "" },
    accent: Color = surfaces.accentPrimary
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(surfaces.sunken.copy(alpha = 0.55f))
            .border(1.dp, surfaces.panelBorder, shape)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val innerShape = RoundedCornerShape(11.dp)
            val contentColor = if (isSelected) readableOn(accent) else surfaces.accentText
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(innerShape)
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    Brush.verticalGradient(
                                        listOf(lerp(accent, Color.White, 0.22f), lerp(accent, Color.Black, 0.10f))
                                    )
                                )
                                .border(1.5.dp, lerp(accent, Color.White, 0.55f), innerShape)
                        } else {
                            Modifier
                        }
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(option) }
                    )
                    // Faz 160: 44dp -> 48dp. Segment TIKLANABILIR alandi ama
                    // dokunma hedefi asgari 48dp kuralinin altindaydi; "Mod"
                    // satiri yan yana duzene gecince kontrol daha da darlasiyor,
                    // yani hedef ALANI kucultmemek kritik hale geldi. Bu bir
                    // ALT sinir: uzun ceviri iki satira tasarsa segment yine
                    // uzar, kisalmaz.
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .then(
                        if (optionTestTag(option).isNotEmpty()) {
                            Modifier.testTagOrEmpty(optionTestTag(option))
                        } else {
                            Modifier
                        }
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = optionIcon(option)
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = optionLabel(option),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun Modifier.testTagOrEmpty(tag: String): Modifier =
    this.then(Modifier.testTag(tag))

// ---------------------------------------------------------------------------
// Acilir liste tetikleyicisi
// ---------------------------------------------------------------------------

// Duz form alani degil: oyuk zemin + accent kenarlik + sagda kabartmali
// chevron kutucugu. Menunun kendisi cagiran tarafta kaliyor (DropdownMenu),
// burasi sadece "malzeme"yi tasiyor.
//
// Faz 160 — `compact` BAYRAGI. Ayarlar ekraninda Tema/Gorunum satirlari
// "etiket solda, kontrol sagda" duzenine gecti; kontrole artik satirin
// tamami degil YARISINDAN AZI kaliyor. Alan darken metnin disindaki her sey
// (dolgu, chevron kutusu, leading bosluk) metinden calan olu yuktur.
// `compact` bu olu yuku 12dp kisar ve bu 12dp, "Fiaba della Principessa"
// (IT tema) / "Predeterminado" (ES gorunum) gibi ceviriler icin
// sigar/sigmaz farkidir.
// Yukseklik DEGISMEZ (52dp >= 48dp dokunma hedefi).
//
// DIKKAT: asagidaki olculer SettingsScreen.kt icindeki
// `DROPDOWN_CHROME_*` sabitleriyle elle esletiliyor (yan yana duzenin
// sigar/sigmaz karari o sabitlerle veriliyor). Burada bir olcu degisirse
// orasi da guncellenmeli.
@Composable
fun GameDropdownField(
    label: String,
    surfaces: GameSurfaces,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = surfaces.accentPrimary,
    compact: Boolean = false,
    leading: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val horizontalPadding = if (compact) 10.dp else 12.dp
    val leadingGap = if (compact) 8.dp else 10.dp
    val chevronBox = if (compact) 22.dp else 26.dp
    val chevronGlyph = if (compact) 17.dp else 20.dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(surfaces.sunken.copy(alpha = 0.55f), surfaces.panel)
                )
            )
            .border(2.dp, accent.copy(alpha = 0.75f), shape)
            .clickable(onClick = onClick)
            // Dokunma hedefi: compact modda da 52dp — kisalan sey GENISLIK,
            // asla yukseklik degil.
            .defaultMinSize(minHeight = 52.dp)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(leadingGap))
        }
        // TASMA: metin weight(1f) — chevron ve leading agirliksiz oldugu icin
        // ONCE olculur, kalani metne gider. Metin sigmazsa iki satira taser,
        // yine sigmazsa ellipsis ile kisalir; kirpilmaz ve satiri tasirmaz.
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (surfaces.isLightSurface) Color(0xFF12161F) else Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(chevronBox)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.35f) else lerp(accent, Color.White, 0.4f),
                modifier = Modifier.size(chevronGlyph)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Kademeli ilerleme cubugu
// ---------------------------------------------------------------------------

enum class GameTierState { LOCKED, READY, CLAIMED }

// Hedef mockup'taki gorev cubugu: kalin oyuk ray, ilerleyen kisim PARLAYAN
// accent dolgu ve ucunda parlak tutamak, kademe dugumleri rayin uzerinde.
// Ilerlemesi olmayan gorevde dolgu yok -> kart kendiliginden "sonuk" kalir.
// Durum farki boylece renkten DEGIL, dolgudan da okunuyor (renk tek ayrim
// kanali olamaz kurali).
@Composable
fun GameTieredProgressBar(
    fraction: Float,
    tierMarkers: List<Pair<Float, GameTierState>>,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    accent: Color = surfaces.accentPrimary,
    completeColor: Color = Color(0xFF35D96B),
    goldColor: Color = GoldPillAccent
) {
    val clamped = fraction.coerceIn(0f, 1f)
    val fillColor = if (clamped >= 1f) completeColor else accent
    val railHeight = 14.dp
    val nodeSize = 18.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidth = maxWidth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(railHeight)
                .clip(RoundedCornerShape(50))
                .background(surfaces.sunken)
                .border(1.dp, surfaces.panelBorder, RoundedCornerShape(50))
        )
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .width(trackWidth * clamped)
                    .height(railHeight)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(lerp(fillColor, Color.White, 0.30f), fillColor)
                        )
                    )
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                            ),
                            cornerRadius = CornerRadius(size.height),
                            size = Size(size.width, size.height * 0.55f)
                        )
                    }
            )
        }
        // Kademe dugumleri
        tierMarkers.forEach { (position, state) ->
            val nodeFill = when (state) {
                GameTierState.CLAIMED -> goldColor
                GameTierState.READY -> completeColor
                GameTierState.LOCKED -> lerp(surfaces.sunken, surfaces.panelBorder, 0.7f)
            }
            Box(
                modifier = Modifier
                    .offset(x = (trackWidth - nodeSize) * position.coerceIn(0f, 1f))
                    .size(nodeSize)
                    .clip(RoundedCornerShape(50))
                    .background(nodeFill)
                    .border(
                        width = 2.dp,
                        color = when (state) {
                            GameTierState.LOCKED -> surfaces.panelBorder
                            else -> Color.White.copy(alpha = 0.85f)
                        },
                        shape = RoundedCornerShape(50)
                    )
            )
        }
        // Dolgu ucundaki parlak tutamak — "buraya kadar geldin" isareti.
        if (clamped > 0.02f && clamped < 1f) {
            Box(
                modifier = Modifier
                    .offset(x = (trackWidth - nodeSize) * clamped)
                    .size(nodeSize)
                    .clip(RoundedCornerShape(50))
                    .background(lerp(fillColor, Color.White, 0.55f))
                    .border(2.5.dp, Color.White, RoundedCornerShape(50))
            )
        }
    }
}

// Oyuk yuzey cizen kucuk yardimci (dropdown menu govdesi gibi yerlerde).
internal fun Modifier.gameWell(surfaces: GameSurfaces, cornerRadius: Dp = 12.dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = surfaces.sunken.copy(alpha = 0.45f),
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
        drawRoundRect(
            color = surfaces.panelBorder,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = 1f * density)
        )
    }
