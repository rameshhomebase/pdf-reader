package com.example.pdfproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter

@Composable
fun ImageBased(bitmap: ImageBitmap) {
    Box(modifier = Modifier.wrapContentSize(align = Alignment.Center)) {
        Image(
            painter = BitmapPainter(bitmap),
            contentDescription = "",
            modifier = Modifier.wrapContentSize()
        )
    }
}
