package com.example.pdfproject

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.withSign

private val tag = "PdfViewer"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PdfViewer(
    modifier: Modifier,
    viewModel: MainViewModel,
    minScale: Float = 1f,
    maxScale: Float = 4f,
    focusRequester: FocusRequester
) {
    val coroutineScope = rememberCoroutineScope()
    var scrollEnabled by remember { mutableStateOf(true) }
    var targetScale by remember { viewModel.scaleFactorState }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var offsetY by remember {
        mutableStateOf(0f)
    }
    var offsetX by remember {
        mutableStateOf(0f)
    }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val focusManager = LocalFocusManager.current
    var coOrd by remember {
        viewModel.nextInput
    }
    var scrollState = rememberScrollState()
    var list = viewModel.imagesLD
    LaunchedEffect(key1 = (coOrd).hashCode()) {
        coroutineScope.launch {
            scrollState.animateScrollTo(coOrd.second)
            // todo change x offset too
        }
    }

    Box(
        modifier = Modifier
            .padding(top = 100.dp)
            .background(Color.Green)
            .then(
                Modifier
                    .fillMaxSize(1f)
                    .combinedClickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                        },
                        onDoubleClick = {
                            if (targetScale >= 2f) {
                                targetScale = 1f
                                offsetX = 1f
                                offsetY = 1f
                                scrollEnabled = true
                            } else {
                                targetScale = 3f
                                offsetX = 1f
                                offsetY = 1f
                            }
                        }
                    )
                    .graphicsLayer {
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                        this.translationX = offsetX
                        this.translationY = offsetY

                        Log.e(
                            tag,
                            " box size ${this.size.width} ${this.size.height}"
                        )

                        Log.e(tag, "translation $offsetX $offsetY")
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                targetScale *= zoom
                                val offset = event.calculatePan() * 2f
                                if (targetScale <= 1) {
                                    offsetX = 1f
                                    offsetY = 1f
                                    targetScale = 1f
                                    scrollEnabled = true
                                } else {
                                    offsetX += offset.x

                                    Log.e(
                                        tag,
                                        "screenHeightPx = $screenHeightPx offsetY = $offsetY offset.y = ${offset.y} === ${offset.y + offsetY}"
                                    )
                                    Log.e(
                                        tag,
                                        "scale = ${((scale.value - 1) * abs(screenHeightPx)) / 2}"
                                    )
                                    if (abs(offsetY + offset.y) <= (
                                        (scale.value - 1) * abs(
                                                screenHeightPx
                                            )
                                        ) / 2
                                    ) {
                                        offsetY += offset.y
                                    }
//
                                    val imageWidth = screenWidthPx * scale.value

                                    val borderReached =
                                        imageWidth - screenWidthPx - 2 * abs(offsetX)
                                    scrollEnabled = borderReached <= 0
                                    if (borderReached < 0) {
                                        offsetX =
                                            ((imageWidth - screenWidthPx) / 2f).withSign(offsetX)
                                        if (offset.x != 0f) offsetY -= offset.y
                                    }
                                }

                                Log.e(tag, "lazycolumn xy $offsetX $offsetY")
                            } while (event.changes.any { it.pressed })
                        }
                    }
            )
    ) {
        Column(
            Modifier.verticalScroll(state = scrollState)
                .onGloballyPositioned {
                    if (scrollState.maxValue > 0) {
                        Log.v(tag, "column size w = ${it.size.width} w = ${it.size.height} ")
                        viewModel.heightRetrieved(it.size.height)
                        Log.e(tag, "max scroll ${scrollState.maxValue}")
                    }
                }
        ) {
            list.forEachIndexed { index, it ->

                Page(
                    pageContent = {
                        PageContent(
                            imagePainter = BitmapPainter(it.first),
                            fields = it.second,
                            focusManager = focusManager,
                            focusRequester = if (index == 0) focusRequester else null,
                            onFocus = viewModel::onFocus
                        )
                    },
                    fields = it.second,
                    color = if (index % 2 == 0) Color.Yellow else Color.Cyan
                )
            }
        }
    }
    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Blue),
        onClick = {
            offsetY = 780f
            offsetX = -181f
        }
    ) {
        Text(text = "Change offset")
    }
}
