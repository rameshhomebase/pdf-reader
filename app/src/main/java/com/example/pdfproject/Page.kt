package com.example.pdfproject

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
                    val offset = IntOffset(fieldValue.x, fieldValue.y - 10)
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
    imagePainter: Painter,
    focusManager: FocusManager? = null,
    focusRequester: FocusRequester? = null,
    onFocus: ((String) -> Unit)? = null
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
    fields.forEachIndexed { index, it ->
        InputField(it, focusManager, if (index == 0) focusRequester else null, onFocus)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InputField(
    field: Fields,
    focusManager: FocusManager?,
    focusRequester: FocusRequester?,
    onFocus: ((String) -> Unit)? = null
) {
    var text by remember {
        mutableStateOf(field.text)
    }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val modifier: Modifier =
        if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
    var positionInParent by remember {
        mutableStateOf(Offset(0f, 0f))
    }
    BasicTextField(
        textStyle = TextStyle(fontSize = 10.sp),
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .imePadding()
            .background(Color.White, RectangleShape)
            .border(BorderStroke(1.dp, Color.Blue))
            .onFocusChanged {
                val circleCoordinates = Rect(500f, 0f, 1000f, 500f)

                // todo add
                if (it.isFocused) {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView(circleCoordinates)
                    }
//                    onFocus?.invoke("${positionInParent.x} ${positionInParent.y}")
                    onFocus?.invoke(field.tag)
                }
            }
            .defaultMinSize(60.dp, 10.dp)
            .wrapContentSize()
            .testTag(field.tag)
//            .onGloballyPositioned {
//                positionInParent = it.positionInParent()
//                val positionInRoot = it.positionInRoot()
//                Log.e(
//                    "Ramesh InputField ${field.tag}",
//                    "Global Position positionInParent ${positionInParent.x} ${positionInParent.y}"
//                )
//                Log.e(
//                    "Ramesh InputField ${field.tag}",
//                    "Global Position ${positionInRoot.x} ${positionInRoot.y}"
//                )
//            }
        ,
        value = text,
        singleLine = true,
        onValueChange = {
            text = it
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = {
            focusManager?.moveFocus(FocusDirection.Next)
        })
    )
}
