package com.example.pdfproject

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.applyCanvas
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.withSign

@Composable
fun PdfPage(bitmap: Bitmap, fields: List<Fields>) {
    val scrollEnabled = remember { mutableStateOf(true) }
    val view = LocalView.current
    var capturingViewBounds by remember { mutableStateOf<Rect?>(null) }
    var image: Bitmap? by remember {
        mutableStateOf(null)
    }
    val configuration = LocalConfiguration.current
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
//    val bitmapWidth = with(LocalDensity.current) { bitmap.toPx() }
    Log.e("Ramesh dp to px", "$screenHeight $screenWidth")
    Log.e("Ramesh bitmap", "${bitmap.height} ${bitmap.width}")
    if (image != null) {
        Image(
            painter = BitmapPainter(image!!.asImageBitmap()),
            contentDescription = null
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Yellow),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZoomablePagerImage1(
                painter = BitmapPainter(bitmap.asImageBitmap()),
                scrollEnabled = scrollEnabled,
                modifier = Modifier.wrapContentSize()
                    .onGloballyPositioned {
                        capturingViewBounds = it.boundsInRoot()
                    },
                fields = fields,
                width = bitmap.width,
                height = bitmap.height
            )
        }
    }
//
//    Button(
//        modifier = Modifier.wrapContentSize(),
//        onClick = {
//            val bounds = capturingViewBounds ?: return@Button
//            image = Bitmap.createBitmap(
//                bounds.width.roundToInt(),
//                bounds.height.roundToInt(),
//                Bitmap.Config.ARGB_8888
//            ).applyCanvas {
//                translate(-bounds.left, -bounds.top)
//                view.draw(this)
//            }
//        }
//    ) {
//        Text("Capture ${bitmap.height} ${bitmap.width}")
//    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    scrollEnabled: MutableState<Boolean>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    contentScale: ContentScale = ContentScale.Fit,
    isRotation: Boolean = false
) {
    var targetScale by remember { mutableStateOf(1f) }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var rotationState by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(1f) }
    var offsetY by remember { mutableStateOf(1f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }

    Box(
        modifier = Modifier
            .clip(RectangleShape)
            .background(Color.Transparent)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { },
                onDoubleClick = {
                    if (targetScale >= 2f) {
                        targetScale = 1f
                        offsetX = 1f
                        offsetY = 1f
                        scrollEnabled.value = true
                    } else targetScale = 3f
                }
            )
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
                                offsetY += offset.y
                                if (zoom > 1) {
                                    scrollEnabled.value = false
                                    rotationState += event.calculateRotation()
                                }
                                val imageWidth = screenWidthPx * scale.value
                                val borderReached = imageWidth - screenWidthPx - 2 * abs(offsetX)
                                scrollEnabled.value = borderReached <= 0
                                if (borderReached < 0) {
                                    offsetX = ((imageWidth - screenWidthPx) / 2f).withSign(offsetX)
                                    if (offset.x != 0f) offsetY -= offset.y
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }

    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    this.scaleX = scale.value
                    this.scaleY = scale.value
                    if (isRotation) {
                        rotationZ = rotationState
                    }
                    this.translationX = offsetX
                    this.translationY = offsetY
                }
        )

        Layout(
            measurePolicy = alignMeasurePolicy(100, 100),
            content = @Composable {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .graphicsLayer {
                            this.scaleX = scale.value
                            this.scaleY = scale.value
                            if (isRotation) {
                                rotationZ = rotationState
                            }
                            this.translationX = offsetX
                            this.translationY = offsetY
                        },
                    text = "testing"

                )
            },
            modifier = Modifier
        )
    }
}
