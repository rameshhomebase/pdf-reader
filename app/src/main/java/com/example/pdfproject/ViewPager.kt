package com.example.pdfproject

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import kotlin.math.abs
import kotlin.math.withSign

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun ViewPager(
    list: List<Pair<ImageBitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 2f,
    scrollEnabled: MutableState<Boolean>,
    isRotation: Boolean = false
) {

    var targetScale by remember { mutableStateOf(1f) }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var rotationState by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(1f) }
    var offsetY by remember { mutableStateOf(1f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val focusManager = LocalFocusManager.current
    VerticalPager(
         modifier= Modifier
            .wrapContentSize()
//            .verticalScroll(scrollState)
            .fillMaxSize(1f)
            .background(color = Color.Blue)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    focusManager.clearFocus()
                },
                onDoubleClick = {
                    if (targetScale >= 2f) {
                        targetScale = 1f
                        offsetX = 1f
                        offsetY = 1f
                        scrollEnabled.value = true
                    } else targetScale = 2f
                }
            )
            .graphicsLayer {
                this.scaleX = scale.value
                this.scaleY = scale.value
                if (isRotation) {
                    rotationZ = rotationState
                }
                this.translationX = offsetX
                this.translationY = offsetY

                Log.e("Ramesh lazycolumn size", "${this.size.width} ${this.size.height}")
            }
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            val zoom = event.calculateZoom()
                            targetScale *= zoom
                            val offset = event.calculatePan()
                            if (targetScale <= 1) {
                                offsetX = 1f
                                offsetY = 1f
                                targetScale = 1f
                                scrollEnabled.value = true
                            } else {
                                offsetX += offset.x
                                // todo change
                                if (offsetY + offset.y < screenHeightPx && offsetY + offset.y > -screenHeightPx) {
                                    offsetY += offset.y
                                }
                                if (zoom > 1) {
                                    scrollEnabled.value = false
                                    rotationState += event.calculateRotation()
                                }
                                val imageWidth = screenWidthPx * scale.value

                                val borderReached =
                                    imageWidth - screenWidthPx - 2 * abs(offsetX)
                                scrollEnabled.value = borderReached <= 0
                                if (borderReached < 0) {
                                    offsetX =
                                        ((imageWidth - screenWidthPx) / 2f).withSign(offsetX)
                                    if (offset.x != 0f) offsetY -= offset.y
                                }
                            }
                            Log.e("Ramesh lazycolumn xy ", "$offsetX $offsetY")
                        } while (event.changes.any { it.pressed })
                    }
                }
            },
        count = list.size,
        contentPadding = PaddingValues(0.dp),
        flingBehavior = ScrollableDefaults.flingBehavior()
    ) { page ->
        val it = list[page]
        Page(
            pageContent = {
                PageContent(
                    modifier = Modifier,
                    imagePainter = BitmapPainter(it.first),
                    fields = it.second
                )
            },
            fields = it.second,
            color = if (page % 2 == 0) Color.Yellow else Color.Cyan
        )
    }
}