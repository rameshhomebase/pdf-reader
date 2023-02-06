package com.example.pdfproject

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pdfproject.ui.theme.PdfProjectTheme
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels<MainViewModel>() {
        MainViewModel.createFactory(assets, resources)
    }

    val randomColors = listOf(Color.Blue, Color.Red, Color.Magenta, Color.Yellow)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PDFBoxResourceLoader.init(applicationContext)

        // Enable Android asset loading
        setContent {
            val configuration = LocalConfiguration.current
            val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
            val screenHeightPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }

            viewModel.loadDataReverse(screenWidthPx.toInt(), screenHeightPx.toInt())
            PdfProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val configuration = LocalConfiguration.current
                    val screenHeight = configuration.screenHeightDp
                    val screenWidth = configuration.screenWidthDp

                    Log.e(
                        /* tag = */ "ramesh new ", /* msg = */
                        "${screenHeight.dp} = ${configuration.screenHeightDp} \n$screenWidth = ${configuration.screenWidthDp}\n${configuration.densityDpi}"
                    )
//                    MainContent(viewModel)
                    val data = viewModel.imagesLD.get(0)
//                    PdfPage(data.first, data.second)
                }
            }
        }
    }

    @Composable
    private fun MainContent(viewModel: MainViewModel) {
        val data = viewModel.imagesLD
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(data) { pos, item ->
//                PdfPage(item.first, item.second)
            }
        }
    }

    @Composable
    private fun MainContent1(viewModel: MainViewModel) {
        val data = viewModel.imagesLD
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            Column {
                repeat(data.size) { index ->
                    Box(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
//                        Image(
//                            modifier = Modifier.wrapContentSize().layout { measurable, constraints ->
//                                val placeable = measurable.measure(constraints)
//
//                                layout(placeable.width, placeable.height) {
//                                    placeable.place(0, 0)
//                                }
//                            },
//                            painter = BitmapPainter(data[index].asImageBitmap()),
//                            contentDescription = "image",
//                            contentScale = ContentScale.Fit
//                        )
//                        PdfPageContentView(bitmap = data[index].first, position = index)
                    }
                }
            }
        }
    }

    @Composable
    private fun PdfPageContentView(bitmap: Bitmap, position: Int) {
        var scale by remember { mutableStateOf(1f) }
//        val horizontalScrollState = ScrollableState(0)
//        val verticalScrollState = ScrollState(0)

        Box(
            modifier = Modifier.fillMaxSize()
//                .horizontalScroll(enabled = true, state = horizontalScrollState)
//                .verticalScroll(enabled = true, state = verticalScrollState)
                .background(randomColors[position])
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = when {
                            scale < 0.5f -> 0.5f
                            scale > 3f -> 3f
                            else -> scale * zoom
                        }
                    }
                }
        ) {
            Image(
                modifier = Modifier.wrapContentSize().layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)

                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                },
                painter = BitmapPainter(bitmap.asImageBitmap()),
                contentDescription = "image",
                contentScale = ContentScale.Fit
            )
            Layout(
                measurePolicy = alignMeasurePolicy(0, 0),
                content = @Composable {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "${bitmap.asImageBitmap().height} ${bitmap.asImageBitmap().width}"
                    )
                },
                modifier = Modifier
            )
        }
    }
}

fun overlappingRowMeasurePolicy(overlapFactor: Float) = MeasurePolicy { measurables, constraints ->
    val placeables = measurables.map { measurable -> measurable.measure(constraints) }
    val height = placeables.maxOf { it.height }
    val width = (
        placeables.subList(1, placeables.size)
            .sumOf { it.width } * overlapFactor + placeables[0].width
        ).toInt()
    layout(width, height) {
        var xPos = 0
        for (placeable in placeables) {
            placeable.placeRelative(xPos, 0, 0f)
            xPos += (placeable.width * overlapFactor).toInt()
        }
    }
}

fun alignMeasurePolicy(x: Int, y: Int) = MeasurePolicy { measurables, constraints ->
    val placeables = measurables.map { measurable -> measurable.measure(constraints) }
    val height = placeables.maxOf { it.height }
    val width = placeables.maxOf { it.width }

    layout(width, height) {
        for (placeable in placeables) {
            placeable.placeRelative(x, y, 0f)
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PdfProjectTheme {
        Greeting("Android")
    }
}

// @Composable
// fun PdfViewer(
//    modifier: Modifier = Modifier,
//    uri: Uri,
//    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
// ) {
//    val rendererScope = rememberCoroutineScope()
//    val mutex = remember { Mutex() }
//    val renderer by produceState<PdfRenderer?>(null, uri) {
//        rendererScope.launch(Dispatchers.IO) {
// //            val input = ParcelFileDescriptor.open(uri.toFile(), ParcelFileDescriptor.MODE_READ_ONLY)
//            val input = AssetFileDescriptor(uri.toFile(), ParcelFileDescriptor.MODE_READ_ONLY)
//            value = PdfRenderer(input)
//        }
//        awaitDispose {
//            val currentRenderer = value
//            rendererScope.launch(Dispatchers.IO) {
//                mutex.withLock {
//                    currentRenderer?.close()
//                }
//            }
//        }
//    }
//    val context = LocalContext.current
//    val imageLoader = LocalContext.current.imageLoader
//    val imageLoadingScope = rememberCoroutineScope()
//    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
//        val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
//        val height = (width * sqrt(2f)).toInt()
//        val pageCount by remember(renderer) { derivedStateOf { renderer?.pageCount ?: 0 } }
//        LazyColumn(
//            verticalArrangement = verticalArrangement
//        ) {
//            items(
//                count = pageCount,
//                key = { index -> "$uri-$index" }
//            ) { index ->
//                val cacheKey = MemoryCache.Key("$uri-$index")
//                var bitmap by remember { mutableStateOf(imageLoader.memoryCache?.get(cacheKey) as? Bitmap) }
//                if (bitmap == null) {
//                    DisposableEffect(uri, index) {
//                        val job = imageLoadingScope.launch(Dispatchers.IO) {
//                            val destinationBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//                            mutex.withLock {
// //                                Timber.d("Loading PDF $uri - page $index/$pageCount")
//                                if (!coroutineContext.isActive) return@launch
//                                try {
//                                    renderer?.let {
//                                        it.openPage(index).use { page ->
//                                            page.render(
//                                                destinationBitmap,
//                                                null,
//                                                null,
//                                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
//                                            )
//                                        }
//                                    }
//                                } catch (e: Exception) {
//                                    // Just catch and return in case the renderer is being closed
//                                    return@launch
//                                }
//                            }
//                            bitmap = destinationBitmap
//                        }
//                        onDispose {
//                            job.cancel()
//                        }
//                    }
//                    Box(modifier = Modifier.background(Color.White).aspectRatio(1f / sqrt(2f)).fillMaxWidth())
//                } else {
//                    val request = ImageRequest.Builder(context)
//                        .size(width, height)
//                        .memoryCacheKey(cacheKey)
//                        .data(bitmap)
//                        .build()
//
//                    Image(
//                        modifier = Modifier.background(Color.White).aspectRatio(1f / sqrt(2f)).fillMaxWidth(),
//                        contentScale = ContentScale.Fit,
//                        painter = rememberImagePainter(request),
//                        contentDescription = "Page ${index + 1} of $pageCount"
//                    )
//                }
//            }
//        }
//    }
// }
