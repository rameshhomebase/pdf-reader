package com.example.pdfproject

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.withSign

@Composable
fun PdfPagePt() {
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerImage1(
    modifier: Modifier = Modifier,
    painter: Painter,
    scrollEnabled: MutableState<Boolean>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    contentScale: ContentScale = ContentScale.Fit,
    isRotation: Boolean = false,
    fields: List<Fields>,
    width: Int,
    height: Int
) {
    var targetScale by remember { mutableStateOf(1f) }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var rotationState by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(1f) }
    var offsetY by remember { mutableStateOf(1f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    Box(
        modifier = Modifier.wrapContentSize()
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
            ).graphicsLayer {
                this.scaleX = scale.value
                this.scaleY = scale.value
                if (isRotation) {
                    rotationZ = rotationState
                }
                this.translationX = offsetX
                this.translationY = offsetY
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
        Layout(
            content = {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = modifier
                        .align(Alignment.Center)
                        .onGloballyPositioned {
                            val position = it.positionInRoot()
                            Log.e("ramesh image coordinates", "x = ${position.x} y = ${position.y}")
                            Log.e("ramesh image size", "x = ${it.size.width} y = ${it.size.height}")
                        }
//                .graphicsLayer {
//                    this.scaleX = scale.value
//                    this.scaleY = scale.value
//                    if (isRotation) {
//                        rotationZ = rotationState
//                    }
//                    this.translationX = offsetX
//                    this.translationY = offsetY
//                }
                )
                for (i in 0..fields.lastIndex) {
                    var text by remember {
                        mutableStateOf(fields[i].text)
                    }
                    Layout(
                        measurePolicy = alignMeasurePolicy(0, 0),
                        content = @Composable {
                            BasicTextField(
                                textStyle = TextStyle(fontSize = 10.sp),
                                modifier = Modifier
                                    .wrapContentSize(),
//                        .graphicsLayer {
//                            this.scaleX = scale.value
//                            this.scaleY = scale.value
//                            if (isRotation) {
//                                rotationZ = rotationState
//                            }
//                            this.translationX = offsetX
//                            this.translationY = offsetY
//                        }
                                value = text,
                                onValueChange = {
                                    text = it
                                }

                            )
                        },
                        modifier = Modifier
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { measurable -> measurable.measure(constraints) }

            Log.e(
                "Ramesh",
                "Placeable ${placeables[0].height} ${placeables[0].width} \nConstraints ${constraints.maxWidth}, ${constraints.maxHeight}"
            )
            val ratio = createRatio(width, height, placeables[0].width, placeables[0].height)
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables[0].placeRelative(0, 0, 0f)
                for (index in 1..placeables.lastIndex) {
                    val fieldValue = fields[index - 1]

//                    placeables[index].placeRelative(fieldValue.x.times(.38).dp.toPx().toInt(), fieldValue.y.times(.38).dp.toPx().toInt(), 0f)
//                    placeables[index].placeRelative(fieldValue.x.times(.3629).dp.toPx().toInt(), fieldValue.y.times(.3629).dp.toPx().toInt(), 0f)
//                    placeables[index].placeRelative(fieldValue.x.times(.3).dp.toPx().toInt(), fieldValue.y.times(.3).dp.toPx().toInt(), 0f)
//                    placeables[index].placeRelative(fieldValue.x.times(.64).dp.toPx().toInt(), fieldValue.y.times(.64).dp.toPx().toInt(), 0f)
                    placeables[index].placeRelative(fieldValue.x, fieldValue.y, 0f)
                }
//                for (placeable in placeables) {
//                    placeable.placeRelative(0, 0, 0f)
//                }
            }
        }
    }
}

fun createRatio(
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int
): Array<Int> {
    val widthRatio =
        if (imageWidth > screenWidth) screenWidth / imageWidth else imageWidth / screenWidth
    val heightRatio =
        if (imageHeight > screenHeight) screenHeight / imageHeight else imageHeight / screenHeight
    return arrayOf(widthRatio, heightRatio)
}
