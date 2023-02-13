package com.example.pdfproject

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.abs
import kotlin.math.withSign

val points = mutableListOf<Pair<Float, Float>>().apply {
//    add(Pair(683.86475f, 1236.6946f))
//    add(Pair(605.8618f, 882.7106f))
//    add(Pair(540.5531f, 746.0698f))
//    add(Pair(393.22607f, 628.0619f))
//    add(Pair(712.5328f, 601.3822f))
//    add(Pair(1076.1943f, -328.61804f))
//    add(Pair(1070.1958f, -365.9618f))
//    add(Pair(-207.6413f, -774.67566f))
//    add(Pair(-397.63873f, -844.67566f))
//    add(Pair(-850.95636f, -852.6835f))
//    add(Pair(424.3316f, -1042.9886f))
    add(Pair(462.97485f, -863.4302f))
}

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun CustomFormTest(
    modifier: Modifier,
    list: List<Pair<ImageBitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 4f,
    scrollEnabled: MutableState<Boolean>,
    focusRequester: FocusRequester,
    onScale: (Float, Offset) -> Unit,
    offSetStateX: MutableState<Float>,
    offSetStateY: MutableState<Float>,
    scaleState: MutableState<Float>,
    onFocus: (String) -> Unit
) {
    var targetScale by remember { scaleState }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var offsetY by remember {
        mutableStateOf(0f)
    }
//    var offsetY by remember {
//        mutableStateOf(0f)
//    }
//    var offsetY = animateFloatAsState(targetValue = maxOf(2000f, maxOf(-2000f, offsetY)))
    var offsetX by remember {
        mutableStateOf(0f)
    }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val focusManager = LocalFocusManager.current
    var coOrd by remember {
        mutableStateOf("")
    }
    var x by remember {
        mutableStateOf(0f)
    }
    var y by remember {
        mutableStateOf(0f)
    }
    var index by remember {
        mutableStateOf(-1)
    }
    LaunchedEffect(key1 = coOrd) {
//        this.onDispose {
//        offsetY = -1580f
//        offsetX = -181f
//        }
        if(index == -1) {
            index += 1
        } else {
            if (index < points.size) {
                val pair = points[index]
                x = pair.first
                y = pair.second
            }
            Log.e("Ramesh", "translation $x $y")
            offsetX = x
            offsetY = y
        }
    }



//    Column {
    val coroutineScope = rememberCoroutineScope()
    val onFocus1: (String) -> Unit = { co ->

//        index += 1
//        if (index < points.size) {
//            val pair = points[index]
//            x = pair.first
//            y = pair.second
//        }
        coOrd = co
//        x = 0f
//        if (y > 1700) {
//            y -= 200
//        } else if (y < -1700) {
//            y += 300
//        }

//        coroutineScope.launch {
//            offsetY = -780f
//            offsetX = -181f
//        }
    }
    Box(
        modifier = // modifier
        Modifier.padding(top = 100.dp).background(Color.Green)
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
                                scrollEnabled.value = true
                            } else {
                                targetScale = 3f

                                offsetX = 1f
                                offsetY = 1f
                            }
//                                coroutineScope.launch {
// //                                    delay(1000)
//                                    if(offsetY + 500 > 1180)
//                                    offsetY-=500f
//                                    offsetX = 281f
//                                }
//                            }
                            onScale(targetScale, Offset(offsetX, offsetY))
                        }
                    )
                    .graphicsLayer {
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                        this.translationX = offsetX
                        this.translationY = offsetY

                        Log.e(
                            "Ramesh lazycolumn size",
                            "${this.size.width} ${this.size.height}"
                        )

                        Log.e("Ramesh","translation $offsetX $offsetY")
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
                                    scrollEnabled.value = true
                                } else {
                                    offsetX += offset.x

                                    Log.e(
                                        "Ramesh offset issue ",
                                        "screenHeightPx = $screenHeightPx offsetY = $offsetY offset.y = ${offset.y} === ${offset.y + offsetY}"
                                    )
                                    Log.e(
                                        "Ramesh offset issue ",
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
                                    scrollEnabled.value = borderReached <= 0
                                    if (borderReached < 0) {
                                        offsetX =
                                            ((imageWidth - screenWidthPx) / 2f).withSign(offsetX)
                                        if (offset.x != 0f) offsetY -= offset.y
                                    }
                                }
                                onScale(targetScale, Offset(offsetX, offsetY))

                                Log.e("Ramesh lazycolumn xy ", "$offsetX $offsetY")
                            } while (event.changes.any { it.pressed })
                        }
                    }
            )
    ) {
        Column(
            Modifier.verticalScroll(state = rememberScrollState())
        ) {
            list.forEachIndexed { index, it ->
                Page(
                    pageContent = {
                        PageContent(
                            modifier = Modifier,
                            imagePainter = BitmapPainter(it.first),
                            fields = it.second,
                            focusManager = focusManager,
                            focusRequester = if (index == 0) focusRequester else null,
                            onFocus = onFocus1
                        )
                    },
                    fields = it.second,
                    color = if (index % 2 == 0) Color.Yellow else Color.Cyan
                )
            }
        }
        TextButton(
            modifier = Modifier.fillMaxWidth().background(Color.Blue),
            onClick = {
                offsetY = 780f
                offsetX = -181f
            }
        ) {
            Text(text = "Change offset")
        }
    }
//    }
}
