package com.example.pdfproject

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.gson.Gson
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.rendering.ImageType
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.rendering.RenderDestination
import kotlinx.coroutines.launch

class MainViewModel(private val assets: AssetManager, private val resources: Resources) :
    ViewModel() {
    //    var imagesLD by mutableStateOf<List<Pair<Bitmap, List<Fields>>>>(emptyList())
    var imagesLD by mutableStateOf<List<Pair<ImageBitmap, List<Fields>>>>(emptyList())
    var pagesState by mutableStateOf<List<Pair<Bitmap, List<Fields>>>>(emptyList())
    fun loadData(width: Int, height: Int) {
        viewModelScope.launch {
            // Load in an already created PDF
//            val document: PDDocument = PDDocument.load(assets.open("template_simple.pdf"))
            val document: PDDocument = PDDocument.load(assets.open("guide.pdf"))
//            val document: PDDocument = PDDocument.load(assets.open("page_1.pdf"))

            val pagesImage = mutableListOf<Pair<ImageBitmap, List<Fields>>>()
            val totalPages = document.pages.count
            val scaleFactor =
                if (width > tempImageWidth) width.toFloat() / tempImageWidth else tempImageWidth / width
            Log.e("Ramesh Scale Factor", "$scaleFactor screen width = $width ")
            // Create a renderer for the document
            val renderer = PDFRenderer(document)

            for (index in 0 until totalPages) {
                // Render the image to an RGB Bitmap
                pagesImage.add(
                    Pair(
                        renderer.renderImage(
                            index,
                            scaleFactor,
                            ImageType.RGB,
                            RenderDestination.VIEW
                        ).asImageBitmap(),
                        mutableListOf<Fields>().apply {
                            add(
                                Fields(
                                    79.times(scaleFactor).toInt(),
                                    175.times(scaleFactor).toInt(),
                                    TYPE_INPUT,
                                    "Your name"
                                )
                            )
                            add(
                                Fields(
                                    150.times(scaleFactor).toInt(),
                                    378.times(scaleFactor).toInt(),
                                    TYPE_INPUT,
                                    "Employer name"
                                )
                            )
                            add(
                                Fields(
                                    117.times(scaleFactor).toInt(),
                                    409.times(scaleFactor).toInt(),
                                    TYPE_INPUT,
                                    "Date here"
                                )
                            )
//                            add(Fields(100, 200, TYPE_INPUT))
                        }
                    )
                )
            }
            imagesLD = (pagesImage)
        }
    }

    fun loadDataReverse1(width: Int, height: Int) {
        viewModelScope.launch {
            // Load in an already created PDF
//            val document: PDDocument = PDDocument.load(assets.open("template_simple.pdf"))
            val document: PDDocument = PDDocument.load(assets.open("guide.pdf"))
//            val document: PDDocument = PDDocument.load(assets.open("page_1.pdf"))

            val pagesImage = mutableListOf<Pair<ImageBitmap, List<Fields>>>()
            val totalPages = document.pages.count
            val scaleFactor =
                if (width > tempImageWidth) width.toFloat() / tempImageWidth else tempImageWidth / width
            Log.e("Ramesh Scale Factor", "$scaleFactor screen width = $width ")
            // Create a renderer for the document
            val renderer = PDFRenderer(document)

            for (index in 0 until totalPages) {
                // Render the image to an RGB Bitmap
                val image = renderer.renderImage(
                    index,
                    scaleFactor,
                    ImageType.RGB,
                    RenderDestination.VIEW
                )
                val reverseHeightDifference = height - image.height
                pagesImage.add(
                    Pair(
                        image.asImageBitmap(),
                        mutableListOf<Fields>().apply {
                            add(
                                Fields(
                                    186.times(scaleFactor).toInt(),
                                    height - (
                                        332.times(scaleFactor)
                                            .toInt() + reverseHeightDifference
                                        ) - inputHeight,
                                    TYPE_INPUT,
                                    "Manager sign here"
                                )
                            )
                            add(
                                Fields(
                                    156.times(scaleFactor).toInt(),
                                    height - (
                                        396.times(scaleFactor)
                                            .toInt() + reverseHeightDifference
                                        ) - inputHeight,
                                    TYPE_INPUT,
                                    "Employer name"
                                )
                            )
                            add(
                                Fields(
                                    117.times(scaleFactor).toInt(),
                                    height - (
                                        381.times(scaleFactor)
                                            .toInt() + reverseHeightDifference
                                        ) - inputHeight,
                                    TYPE_INPUT,
                                    "Date here"
                                )
                            )
//                            add(Fields(100, 200, TYPE_INPUT))
                        }
                    )
                )
            }
            imagesLD = (pagesImage)
        }
    }

    var pdfHeight = 0
    fun loadDataReverse(width: Int, height: Int) {
        viewModelScope.launch {
            val json = resources.openRawResource(R.raw.fields_new)
                .bufferedReader().use { it.readText() }
            val pageData = Gson().fromJson<PageData>(json, PageData::class.java)
            // Load in an already created PDF
//            val document: PDDocument = PDDocument.load(assets.open("doc_ex_2_rotation_fixed.pdf"))
//            val document: PDDocument = PDDocument.load(assets.open("guide.pdf"))
//            val document: PDDocument = PDDocument.load(assets.open("page_1.pdf"))
//            val document: PDDocument = PDDocument.load(assets.open("template_simple.pdf"))
            val document: PDDocument = PDDocument.load(assets.open("template_three_pages.pdf"))

            val pagesImage = mutableListOf<Pair<ImageBitmap, List<Fields>>>()
            val totalPages = document.pages.count

            Log.e("Ramesh Scale Factor", "$scaleFactor screen width = $width ")
            // Create a renderer for the document

            val renderer = PDFRenderer(document)
            for (index in 0 until totalPages) {
                val box = document.getPage(index).cropBox
                Log.e("Ramesh width height", "${box.width}  ${box.height}")
                val scaleFactor =
                    if (width > box.width) width.toFloat() / box.width else box.width / width
                // Render the image to an RGB Bitmap
                val image = renderer.renderImage(
                    index,
                    scaleFactor,
                    ImageType.RGB,
                    RenderDestination.VIEW
                )
                pdfHeight += image.height
                pagesImage.add(
                    Pair(
                        image.asImageBitmap(),
                        if (index > pageData.pages.lastIndex) {
                            emptyList<Fields>()
                        } else {
                            pageData.pages[index].fields.map {
                                Fields(
                                    it.position[0].times(scaleFactor).toInt(),
                                    image.height -
                                        (
                                            it.position[1].times(scaleFactor)
                                                .toInt()
                                            ),
//                                    + inputHeight
                                    TYPE_INPUT,
                                    it.value.orEmpty()
                                )
                            }
                        }
                    )
                )
            }
            imagesLD = (pagesImage)

            Log.v("Ramesh VM", "Total pdf height $pdfHeight")
        }
    }

    private fun toFields(get: Fields, height: Int, scaleFactor: Float): List<Fields> {
        TODO("Not yet implemented")
    }

    private fun pageOneCoordinates(height: Int, scaleFactor: Float): List<Fields> {
        return mutableListOf<Fields>().apply {
            add(
                Fields(
                    188.times(scaleFactor).toInt(),
                    height - (
                        323.times(scaleFactor)
                            .toInt() -
                            inputHeight
                        ),
                    TYPE_INPUT,
                    "Manager sign here"
                )
            )
            add(
                Fields(
                    152.times(scaleFactor).toInt(),
                    height - (
                        392.times(scaleFactor)
                            .toInt() -
                            inputHeight
                        ),
                    TYPE_INPUT,
                    "Employer name"
                )
            )
            add(
                Fields(
                    100.times(scaleFactor).toInt(),
                    height - (
                        369.times(scaleFactor)
                            .toInt() -
                            inputHeight
                        ),
                    TYPE_INPUT,
                    "Date here"
                )
            )
//                            add(Fields(100, 200, TYPE_INPUT))
        }
    }

    private fun pageTwoCoordinates(height: Int, scaleFactor: Float): List<Fields> {
        return mutableListOf<Fields>().apply {
            add(
                Fields(
                    121.times(scaleFactor).toInt(),
                    height - (
                        112.times(scaleFactor)
                            .toInt() -
                            inputHeight
                        ),
                    TYPE_INPUT,
                    "01/02/2002"
                )
            )
            add(
                Fields(
                    195.times(scaleFactor).toInt(),
                    height - (
                        133.times(scaleFactor)
                            .toInt() -
                            inputHeight
                        ),
                    TYPE_INPUT,
                    "Signing here"
                )
            )
            add(
                Fields(
                    202.times(scaleFactor).toInt(),
                    height - (
                        153.times(scaleFactor)
                            .toInt() -
                            inputHeight
                        ),
                    TYPE_INPUT,
                    "name please"
                )
            )
//                            add(Fields(100, 200, TYPE_INPUT))
        }
    }

    companion object {
        const val scaleFactor: Float = 1.77f
        const val tempImageWidth: Float = 612f // we have to change
        const val inputHeight = 20 // we have to change

        fun createFactory(
            assets: AssetManager,
            resources: Resources
        ): ViewModelProvider.AndroidViewModelFactory {
            return object : ViewModelProvider.AndroidViewModelFactory() {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return MainViewModel(assets, resources) as T
                }
            }
        }
    }

    var offsetXState = mutableStateOf(0f)
    var offsetYState = mutableStateOf(0f)
    var scaleFactor: Float = 1f
    var scaleFactorState = mutableStateOf(2f)
    var nextInput = mutableStateOf(Pair(0, 0))
    fun onScaleChange(scale: Float, offset: Offset) {
        offsetXState.value = offset.x
        offsetXState.value = offset.y
        scaleFactor = scale
    }

    fun onFocus(tag: String) {
//        scaleFactorState.value = 3f
//        viewModelScope.launch {
//            delay(1000)
// //            offsetXState.value = -1000f
// //            offsetYState.value =  -1712f
//            val arr = tag.split(" ")
//        Log.d("Ramesh onFocus ","$tag")
//        offsetXState.value = arr[0].toFloat()
//        offsetYState.value = arr[1].toFloat()
//        }
//        val arr = tag.split(" ")
//        offsetXState.value = arr[0].toFloat() * scaleFactor
//        offsetYState.value = arr[1].toFloat() * scaleFactor
//        offsetXState.value = 780f
//        offsetYState.value = -181f

        fieldsMap[tag]?.let {
            nextInput.value = it
        }
//        nextInput.value = Pair(50, 3720)
    }

    var heightFactor = 1f
    val fieldsMap = mutableMapOf<String, Pair<Int, Int>>()
    var heightRetrieved = 0
    fun heightRetrieved(height: Int) {
        if(height == heightRetrieved) return
        heightRetrieved = height
        heightFactor = if (pdfHeight > height) height.toFloat() / pdfHeight
        else pdfHeight.toFloat() / height
        var previousY = 0
        imagesLD.forEachIndexed { index, pair ->
            pair.second.forEach {
//                previousY = if(index == 0)
                fieldsMap[it.tag] = Pair(it.x, (it.y * heightFactor).toInt() + previousY)
            }
            previousY += (pair.first.height)
        }
    }

    fun calculatePDFInfo() {
    }
}

data class PageData(val pages: List<Page>)
data class Page(val fields: List<Field>)
data class Field(val position: List<Int>, val type: String, val value: String? = null)

data class Fields(
    val x: Int,
    val y: Int,
    val type: String,
    val text: String,
    val tag: String = "$x $y"
)

const val TYPE_INPUT = "input"
const val TYPE_CHECKBOX = "checkbox"
