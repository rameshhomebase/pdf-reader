package com.example.pdfproject

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.math.*

@ExperimentalFoundationApi
@Composable
fun ZoomableImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    imageAlign: Alignment = Alignment.Center,
    shape: Shape = RectangleShape,
    maxScale: Float = 2f,
    minScale: Float = 1f,
    contentScale: ContentScale = ContentScale.Fit,
    isRotation: Boolean = false,
    isZoomable: Boolean = true,
    scrollState: ScrollableState? = null
) {
//    screen size pixel width = 1078.0 height = 392
//    width = 1078 height = 2206
   // x = -556.07294 y = 1103 (top right)
   //  x = -539.9226 y = -1112.6095 (bottom right)
   //  x = 528.23535 y = -1059.6223 (bottom left)
   //  x = 528.86743 y = 1106.4851 (top left)

    // x = width/2
    // y = height/2

//    val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
//        val height = (width * sqrt(2f)).toInt()
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    Log.v("Ramesh screen size ", "pixel width = $screenWidthPx height = ${configuration.screenWidthDp}")

    val coroutineScope = rememberCoroutineScope()

    val scale = remember { mutableStateOf(1f) }
    val rotationState = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(1f) }
    val offsetY = remember { mutableStateOf(1f) }
    val position = remember {
        mutableStateOf(1f)
    }
    Column(
        modifier = Modifier.size(
            width = configuration.screenWidthDp.dp,
            height = configuration.screenHeightDp.dp
        )
            .clipToBounds()
            .background(backgroundColor)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* NADA :) */ },
                onDoubleClick = {
                    if (scale.value >= 2f) {
                        scale.value = 1f
                        offsetX.value = 1f
                        offsetY.value = 1f
                    } else scale.value = 2f
                }
            ).onGloballyPositioned {
                Log.v("Ramesh on graphic ", "width = ${it.size.width} height = ${it.size.height}")
            }
            .pointerInput(Unit) {
                if (isZoomable) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                scale.value *= event.calculateZoom()
                                if (scale.value > 1) {
                                    scrollState?.run {
                                        coroutineScope.launch {
                                            setScrolling(false)
                                        }
                                    }
                                    val offset = event.calculatePan()
//                                    if(Math.abs(offsetX.value += offset.x) <= )
                                    offsetX.value += offset.x
                                    offsetY.value += offset.y
                                    rotationState.value += event.calculateRotation()
                                    scrollState?.run {
                                        coroutineScope.launch {
                                            setScrolling(true)
                                        }
                                    }
                                } else {
                                    scale.value = 1f
                                    offsetX.value = 1f
                                    offsetY.value = 1f
                                }
                                Log.v("Ramesh offset ", "x = ${offsetX.value} y = ${offsetY.value}")
                            } while (event.changes.any { it.pressed })
                        }
                    }
                }
            }
            .graphicsLayer {
                if (isZoomable) {
                    scaleX = maxOf(maxScale, minOf(minScale, scale.value))
                    scaleY = maxOf(maxScale, minOf(minScale, scale.value))
                    if (isRotation) {
                        rotationZ = rotationState.value
                    }
//                    if(offsetX.value.)
                    translationX = offsetX.value
                    translationY = offsetY.value

                }
            }

    ) {
        Box(modifier = Modifier.fillMaxSize().background(color = Color.Red))
//
//        Image(
//            painter = painter,
//            contentDescription = null,
//            contentScale = ContentScale.FillBounds,
//            modifier = modifier
// //                .align(imageAlign)
//        )
    }
}

suspend fun ScrollableState.setScrolling(value: Boolean) {
    scroll(scrollPriority = MutatePriority.PreventUserInput) {
        when (value) {
            true -> Unit
            else -> awaitCancellation()
        }
    }
}

/**
 * Creates an Image composable that can be zoomed in and out.
 * @author Arnau Mora, Mr. Pine, umutsoysl
 * @since 20220118
 * @param painter The data to load into the image.
 * @param contentDescription The description of the image for accessibility.
 * @param modifier Modifiers to apply to the image component.
 * @param minScale The minimum scale that can be applied to the image.
 * @param maxScale The maximum scale that can be applied to the image.
 * @param isRotation Whether or not the image can be rotated.
 * @param isZoomable Whether or not the image can be zoomed.
 * @param onSwipeRight Will be called when the user swipes the image to the right.
 * @param onSwipeLeft Will be called when the user swipes the image to the left.
 * @see <a href="https://stackoverflow.com/a/69782530/5717211">StackOverflow</a>
 */
@Composable
fun ZoomableImageNew(
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    minScale: Float = 1f,
    maxScale: Float = 3f,
    isRotation: Boolean = false,
    isZoomable: Boolean = true,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeLeft: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }

    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var imageCenter by remember { mutableStateOf(Offset.Zero) }
    var transformOffset by remember { mutableStateOf(Offset.Zero) }

    fun onTransformGesture(
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        transformRotation: Float
    ) {
        offset += pan
        scale *= zoom

        // Constrain scale
        scale = maxOf(minScale, minOf(maxScale, scale))

        if (isRotation) {
            rotation += transformRotation
        } else {
            rotation = 0f
        }

        val x0 = centroid.x - imageCenter.x
        val y0 = centroid.y - imageCenter.y

        val hyp0 = sqrt(x0 * x0 + y0 * y0)
        val hyp1 = zoom * hyp0 * (if (x0 > 0) 1f else -1f)

        val alpha0 = atan(y0 / x0)

        val alpha1 = alpha0 + (transformRotation * ((2 * PI) / 360))

        val x1 = cos(alpha1) * hyp1
        val y1 = sin(alpha1) * hyp1

        transformOffset =
            centroid - (imageCenter - offset) - Offset(x1.toFloat(), y1.toFloat())
        offset = transformOffset
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .background(MaterialTheme.colorScheme.background)
            .let { mod ->
                return@let if (!isZoomable) {
                    mod
                } else {
                    mod
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale != 1f) {
                                        scope.launch {
                                            state.animateZoomBy(1 / scale)
                                        }
                                        offset = Offset.Zero
                                        rotation = 0f
                                    } else {
                                        scope.launch {
                                            state.animateZoomBy(2f)
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            val panZoomLock = true
                            forEachGesture {
                                awaitPointerEventScope {
                                    var transformRotation = 0f
                                    var zoom = 1f
                                    var pan = Offset.Zero
                                    var pastTouchSlop = false
                                    val touchSlop = viewConfiguration.touchSlop
                                    var lockedToPanZoom = false
                                    var drag: PointerInputChange?
                                    var overSlop = Offset.Zero

                                    val down = awaitFirstDown(requireUnconsumed = false)

                                    var transformEventCounter = 0
                                    do {
                                        val event = awaitPointerEvent()
                                        val canceled =
                                            event.changes.fastAny { it.positionChangeConsumed() }
                                        var relevant = true
                                        if (event.changes.size > 1) {
                                            if (!canceled) {
                                                val zoomChange = event.calculateZoom()
                                                val rotationChange = event.calculateRotation()
                                                val panChange = event.calculatePan()

                                                if (!pastTouchSlop) {
                                                    zoom *= zoomChange
                                                    transformRotation += rotationChange
                                                    pan += panChange

                                                    val centroidSize =
                                                        event.calculateCentroidSize(useCurrent = false)
                                                    val zoomMotion = abs(1 - zoom) * centroidSize
                                                    val rotationMotion =
                                                        abs(transformRotation * PI.toFloat() * centroidSize / 180f)
                                                    val panMotion = pan.getDistance()

                                                    if (zoomMotion > touchSlop ||
                                                        rotationMotion > touchSlop ||
                                                        panMotion > touchSlop
                                                    ) {
                                                        pastTouchSlop = true
                                                        lockedToPanZoom =
                                                            panZoomLock && rotationMotion < touchSlop
                                                    }
                                                }

                                                if (pastTouchSlop) {
                                                    val eventCentroid =
                                                        event.calculateCentroid(useCurrent = false)
                                                    val effectiveRotation =
                                                        if (lockedToPanZoom) 0f else rotationChange
                                                    if (effectiveRotation != 0f ||
                                                        zoomChange != 1f ||
                                                        panChange != Offset.Zero
                                                    ) {
                                                        onTransformGesture(
                                                            eventCentroid,
                                                            panChange,
                                                            zoomChange,
                                                            effectiveRotation
                                                        )
                                                    }
                                                    event.changes.fastForEach {
                                                        if (it.positionChanged()) {
                                                            it.consumeAllChanges()
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (transformEventCounter > 3) relevant = false
                                        transformEventCounter++
                                    } while (!canceled && event.changes.fastAny { it.pressed } && relevant)

                                    do {
                                        awaitPointerEvent()
                                        drag =
                                            awaitTouchSlopOrCancellation(down.id) { change, over ->
                                                change.consumePositionChange()
                                                overSlop = over
                                            }
                                    } while (drag != null && !drag.positionChangeConsumed())
                                    if (drag != null) {
                                        dragOffset = Offset.Zero
                                        if (scale !in 0.92f..1.08f) {
                                            offset += overSlop
                                        } else {
                                            dragOffset += overSlop
                                        }
                                        if (
                                            drag(drag.id) {
                                                if (scale !in 0.92f..1.08f) {
                                                    offset += it.positionChange()
                                                } else {
                                                    dragOffset += it.positionChange()
                                                }
                                                it.consumePositionChange()
                                            }
                                        ) {
                                            if (scale in 0.92f..1.08f) {
                                                val offsetX = dragOffset.x
                                                if (offsetX > 300) {
                                                    onSwipeRight?.invoke()
                                                } else if (offsetX < -300) {
                                                    onSwipeLeft?.invoke()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }
            }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
//                .align(Alignment.Center)
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .graphicsLayer(
                    scaleX = scale - 0.02f,
                    scaleY = scale - 0.02f,
                    rotationZ = rotation
                )
                .onGloballyPositioned { coordinates ->
                    val localOffset =
                        Offset(
                            coordinates.size.width.toFloat() / 2,
                            coordinates.size.height.toFloat() / 2
                        )
                    val windowOffset = coordinates.localToWindow(localOffset)
//                    imageCenter = coordinates.parentLayoutCoordinates?.windowToLocal(windowOffset)
//                        ?: Offset.Zero
                }
        )
    }
}
