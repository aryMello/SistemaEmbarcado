package com.yourpackage.pianoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WhiteKey(
    note: String,
    isActive: Boolean,
    velocity: Int,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) {
        Brush.verticalGradient(
            colors = listOf(
                Color(100, 180, 255, 255),
                Color(0, 100 + (velocity * 0.55).toInt(), 200 + (velocity * 0.22).toInt(), 255)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(255, 255, 255, 255),
                Color(240, 240, 240, 255)
            )
        )
    }

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(300.dp)
            .shadow(
                elevation = if (isActive) 1.dp else 4.dp,
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = Color(200, 200, 200, 255),
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nome da nota
        Text(
            text = note,
            fontSize = 12.sp,
            color = if (isActive) Color.White else Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun BlackKey(
    note: String,
    isActive: Boolean,
    velocity: Int,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) {
        Brush.verticalGradient(
            colors = listOf(
                Color(150, 100, 200, 255),
                Color(60 + (velocity * 0.4).toInt(), 0, 100 + (velocity * 0.5).toInt(), 255)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(40, 40, 40, 255),
                Color(10, 10, 10, 255)
            )
        )
    }

    Box(
        modifier = Modifier
            .width(32.dp)
            .height(180.dp)
            .shadow(
                elevation = if (isActive) 1.dp else 8.dp,
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .background(
                brush = backgroundColor,
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = note,
            fontSize = 10.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}