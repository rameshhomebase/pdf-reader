package com.example.pdfproject

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Page(
    modifier: Modifier = Modifier,
    fields: List<Fields>,
    pageContent: @Composable () -> Unit,
    color: Color
) {
    Box(
        modifier = modifier
//        Modifier.fillMaxHeight().background(color)
//        .height(placeables[0].height)
    ) {
        Layout(
            content = pageContent
        ) { measurables, constraints ->
            val placeables = measurables.map { measurable -> measurable.measure(constraints) }

            Log.e(
                "Ramesh",
                "Placeable ${placeables[0].height} ${placeables[0].width} \nConstraints ${constraints.maxWidth}, ${constraints.maxHeight}"
            )
            val factor = placeables[0].height
            layout(
                width = constraints.maxWidth,
                height = placeables[0].height
            ) {
                placeables[0].place(0, 0, 0f)
                for (index in 1..placeables.lastIndex) {
                    val fieldValue = fields[index - 1]
                    val offset = IntOffset(fieldValue.x, fieldValue.y - 70)
                    placeables[index].placeRelative(offset, 0f)
//                    placeables[index].placeRelative(fieldValue.x, fieldValue.y, 0f)
//                    placeables[index].place(fieldValue.x, fieldValue.y - 120, 0f)
                }
            }
        }
    }
}


@Composable
fun PageContent(
    modifier: Modifier = Modifier,
    fields: List<Fields>,
    imagePainter: Painter
) {
    Image(
        painter = imagePainter,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
//            .onGloballyPositioned {
//                val position = it.positionInRoot()
//                Log.e("ramesh image coordinates", "x = ${position.x} y = ${position.y}")
//                Log.e("ramesh image size", "x = ${it.size.width} y = ${it.size.height}")
//            }
    )
    fields.forEach {
        InputField(it)
    }
}

@Composable
fun InputField(field: Fields) {
    var text by remember {
        mutableStateOf(field.text)
    }

    BasicTextField(
        textStyle = TextStyle(fontSize = 10.sp),
        modifier = Modifier
            .defaultMinSize(10.dp, 60.dp)
            .wrapContentSize(),
        value = text,
        onValueChange = {
            text = it
        }
    )
}