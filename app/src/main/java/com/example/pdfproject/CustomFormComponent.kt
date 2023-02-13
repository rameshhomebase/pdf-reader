package com.example.pdfproject

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomFormsComponent2_1(
    list: List<Pair<ImageBitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    scrollEnabled: MutableState<Boolean>,
    isRotation: Boolean = false
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        // todo add max zoom and min zoom
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    Column(
        Modifier
            // apply other transformations like rotation and zoom
            // on the pizza slice emoji
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                clip = true,
//                rotationZ = rotation,
                translationX = offset.x,
                translationY = offset.y
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onDoubleClick = {
                    if (scale >= 2f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else scale = 3f
                }
            )
            // add transformable to listen to multitouch transformation events
            // after offset
            .transformable(state = state)
            .background(Color.Blue)
    ) {
        list.forEachIndexed { index, it ->
            Page(
                Modifier.graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                },
                pageContent = {
                    PageContent(
                        modifier = Modifier,
                        imagePainter = BitmapPainter(it.first),
                        fields = it.second
                    )
                },
                fields = it.second,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomFormsComponent2(
    list: List<Pair<ImageBitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    scrollEnabled: MutableState<Boolean>,
    isRotation: Boolean = false
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        // todo add max zoom and min zoom
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    if (list.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize(1f)) {
            ZoomableImage(painter = BitmapPainter(list[0].first))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomFormsComponentNative(
    list: List<Pair<ImageBitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    scrollEnabled: MutableState<Boolean> = mutableStateOf(true),
    isRotation: Boolean = false
) {
    // set up all transformation states
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()
    // let's create a modifier state to specify how to update our UI state defined above
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        // note: scale goes by factor, not an absolute difference, so we need to multiply it
        // for this example, we don't allow downscaling, so cap it to 1f
        scale = max(scale * zoomChange, 1f)
        rotation += rotationChange
        offset += offsetChange

        Log.e("CustomFormsComponentNative", "Offset = ${offset.x} ${offset.y}")
    }
    Box(
        Modifier
            // apply pan offset state as a layout transformation before other modifiers
//            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            // add transformable to listen to multitouch transformation events after offset
            .transformable(state = state)
            // optional for example: add double click to zoom
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch { state.animateZoomBy(4f) }
                    }
                )
            }
            .wrapContentSize()
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
                Log.e("CustomFormsComponentNative", "graphiclayer = ${translationX} ${translationY}")


            }
            .border(1.dp, Color.Green),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn {
            itemsIndexed(list) { index, it ->
                Page(
                    Modifier,
                    pageContent = {
                        PageContent(
                            modifier = Modifier,
                            imagePainter = BitmapPainter(it.first),
                            fields = it.second
                        )
                    },
                    fields = it.second,
                    color = Color.White
                )
            }
        }
    }
}
