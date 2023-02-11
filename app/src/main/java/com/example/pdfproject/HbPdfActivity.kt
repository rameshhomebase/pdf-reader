package com.example.pdfproject

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdfproject.ui.theme.PdfProjectTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.withSign

class HbPdfActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels<MainViewModel>() {
        MainViewModel.createFactory(assets, resources)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, "start") {
                composable("start") {
                    CustomFormScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CustomFormScreen(viewModel: MainViewModel) {
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }

    LaunchedEffect(key1 = Unit, block = {
        viewModel.loadDataReverse(screenWidthPx.toInt(), screenHeightPx.toInt())
    })
    val state = viewModel.imagesLD
    PdfProjectTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                topBar = {
//                    Column(
//                        Modifier
//                            .height(70.dp).zIndex(1f)
//                            .fillMaxHeight()
//                            .background(color = Color.Blue)
//                    ) {
//                    }
                }
            ) {
                var scale by remember { mutableStateOf(1f) }
                val focusManager = LocalFocusManager.current
                val focusRequester = FocusRequester()
//                Column(
// //                    Modifier.size(1080.dp, 2200.dp)
//                ) {
                Text(
                    text = "This is header text",
                    modifier = Modifier.zIndex(2f).height(48.dp).fillMaxWidth().background(
                        Color.Green
                    ).clickable {
//                        focusManager.clearFocus(true)
//                        focusManager.moveFocus(FocusDirection.Next)
                        focusRequester.requestFocus()

                    }
                )
                val scrollEnabled = remember { mutableStateOf(true) }

                CustomFormsComponent(
                    modifier = Modifier.padding(top = 48.dp),
                    list = state,
                    scrollEnabled = scrollEnabled,
                    focusRequester = focusRequester,
                    offSetStateX = viewModel.offsetXState,
                    offSetStateY = viewModel.offsetYState,
                    onScale = viewModel::onScaleChange,
                    scaleState = viewModel.scaleFactorState,
                    onFocus = viewModel::onFocus
                )
//                CustomFormsComponentNative(list = state)
//                ViewPager(
//                    list = state,
//                    scrollEnabled = scrollEnabled,
//                    focusRequester = focusRequester
//                )
//                    Footer {
//                        focusManager.moveFocus(FocusDirection.Next)
//                    }
//                }
            }
        }
    }
}

@Composable
fun Footer(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        Modifier.zIndex(2f).height(48.dp)
            .fillMaxWidth().background(
                Color.Blue
            )
    ) {
        Text(text = "Start")
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun CustomFormsComponent(
    modifier: Modifier,
    list: List<Pair<ImageBitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 4f,
    scrollEnabled: MutableState<Boolean>,
    isRotation: Boolean = false,
    focusRequester: FocusRequester,
    onScale: (Float, Offset) -> Unit,
    offSetStateX: MutableState<Float>,
    offSetStateY: MutableState<Float>,
    scaleState: MutableState<Float>,
    onFocus: (String) -> Unit
) {
    var targetScale by remember { scaleState }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var rotationState by remember { mutableStateOf(1f) }
//    var offsetX by remember { offSetStateX }
//    var offsetY by remember { offSetStateY }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val focusManager = LocalFocusManager.current
    var scrollState by remember {
        mutableStateOf(ScrollState(0))
    }
    Box(
        modifier = modifier
            // apply pan offset state as a layout transformation before other modifiers
//        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .then(
            Modifier
                .wrapContentSize()
// //            .verticalScroll(scrollState)
//            .fillMaxSize(1f)
//            .background(color = Color.Blue)
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
                        } else {
                            targetScale = 3f
                        }
                        onScale(targetScale, Offset(offsetX, offsetY))
//                    scrollState = ScrollState(scrollState.value)
                    }
                )
                .graphicsLayer {
                    this.scaleX = scale.value
                    this.scaleY = scale.value
//                if (isRotation) {
//                    rotationZ = rotationState
//                }
                    this.translationX = offsetX
                    this.translationY = offsetY

                    Log.e("Ramesh lazycolumn size", "${this.size.width} ${this.size.height}")
                }
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown()
//                            var offsetX = offSetStateX.value
//                            var offsetY = offSetStateY.value
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
                                    // screen height = 2212
                                    // scale factor 4 -> offsety = +/- 3236  (n-3)*h + h = h/2 + h
                                    // scale factor 3 -> offsety = +/- 2212  (n-3)*h + h = h        = (n-2)h/n -
                                    // scale factor 2 -> offsety = +/- 1100  (n-3)*h + h = h/2      = (n-1)h/n - 0

//                                if (abs(offsetY + offset.y) <= screenHeightPx / 2) {
//                                if (offsetY + offset.y < screenHeightPx && offsetY + offset.y > -screenHeightPx) {
                                    if (abs(offsetY + offset.y) <= (
                                        (scale.value - 1) * abs(
                                                screenHeightPx
                                            )
                                        ) / 2
                                    ) {
                                        offsetY += offset.y
                                    }
//                                if (zoom > 1) {
//                                    scrollEnabled.value = false
//                                    rotationState += event.calculateRotation()
//                                }
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
                }
        )
    ) {

        Column(
            Modifier.verticalScroll(state = rememberScrollState(offsetY.toInt()))
//            flingBehavior = FlingBehavior
        ) {
//            itemsIndexed(list) { index, it ->
            list.forEachIndexed { index, it ->
                Page(
                    pageContent = {
                        PageContent(
                            modifier = Modifier,
                            imagePainter = BitmapPainter(it.first),
                            fields = it.second,
                            focusManager = focusManager,
                            focusRequester = if (index == 0) focusRequester else null,
                            onFocus = onFocus
                        )
                    },
                    fields = it.second,
                    color = if (index % 2 == 0) Color.Yellow else Color.Cyan
                )
            }
        }
    }
}

@Composable
fun CustomFormsComponent1(
    list: List<Pair<Bitmap, List<Fields>>>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    scrollEnabled: MutableState<Boolean>,
    isRotation: Boolean = false
) {
    var targetScale by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }
    var rotationState by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(1f) }
    var offsetY by remember { mutableStateOf(1f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    var scrollState = rememberScrollState()
    var modifier by remember {
        mutableStateOf<Modifier?>(null)
    }
    LazyColumn(
        Modifier
            .then(modifier ?: Modifier)
            .onGloballyPositioned {
                if (modifier == null) {
                    modifier = Modifier.wrapContentSize()
                }
            }
            .pointerInput(Unit) {
                detectZoom { zoom ->
                    scale *= zoom
                }
            }
            .graphicsLayer {
                scaleX = maxOf(1f, minOf(3f, scale))
                scaleY = maxOf(1f, minOf(3f, scale))
            }
//            .combinedClickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null,
//                onClick = { },
//                onDoubleClick = {
//                    if (targetScale >= 2f) {
//                        targetScale = 1f
//                        offsetX = 1f
//                        offsetY = 1f
//                        scrollEnabled.value = true
//                    } else targetScale = 2f
//                }
//            )

    ) {
//        list.forEachIndexed { index, it ->
        itemsIndexed(list) { index, it ->
            Page(
                modifier = Modifier,
                pageContent = {
                    PageContent(
                        modifier = Modifier,
                        imagePainter = BitmapPainter(it.first.asImageBitmap()),
                        fields = it.second
                    )
                },
                fields = it.second,
                color = if (index % 2 == 0) Color.Yellow else Color.Cyan
            )
        }
    }
}

suspend fun PointerInputScope.detectZoom(
    onGesture: (zoom: Float) -> Unit
) {
    awaitEachGesture {
//        awaitPointerEventScope {
        var zoom = 1f
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()

                if (!pastTouchSlop) {
                    zoom *= zoomChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize

                    if (zoomMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    if (zoomChange != 1f) {
                        onGesture(zoomChange)
                        event.changes.forEach() {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
    }
//    }
}

//    Box(
//        modifier = Modifier
//            .clip(RectangleShape)
//            .background(Color.Transparent)
//    ) {
//        Layout(
//            content = {

//            }
//        ) { measurables, constraints ->
//            val placeables = measurables.map { measurable -> measurable.measure(constraints) }
//
//            Log.e(
//                "Ramesh",
//                "Placeable ${placeables[0].height} ${placeables[0].width} \nConstraints ${constraints.maxWidth}, ${constraints.maxHeight}"
//            )
//            layout(constraints.maxWidth, constraints.maxHeight) {
//                placeables[0].placeRelative(0, 0, 0f)
//                for (index in 1..placeables.lastIndex) {
//                    val fieldValue = fields[index - 1]
//                    placeables[index].placeRelative(fieldValue.x, fieldValue.y, 0f)
//                }
//            }
//        }
//    }
// }

// class VM : ViewModel() {
//    private fun pdfToBitmap(pdfFile: File): ArrayList<Bitmap>? {
//        val bitmaps: ArrayList<Bitmap> = ArrayList()
//        try {
//            val renderer =
//                PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
//            var bitmap: Bitmap
//            val pageCount = renderer.pageCount
//            for (i in 0 until pageCount) {
//                val page = renderer.openPage(i)
//                val width: Int = getResources().getDisplayMetrics().densityDpi / 72 * page.width
//                val height: Int = getResources().getDisplayMetrics().densityDpi / 72 * page.height
//                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                page.render(bitmap, null, null, Page.RENDER_MODE_FOR_DISPLAY)
//                bitmaps.add(bitmap)
//
//                // close the page
//                page.close()
//            }
//
//            // close the renderer
//            renderer.close()
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
//        return bitmaps
//    }
// }
