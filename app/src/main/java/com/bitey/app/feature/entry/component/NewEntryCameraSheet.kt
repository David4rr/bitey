package com.bitey.app.feature.entry.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bitey.app.feature.camera.CameraViewfinder
import java.io.File

import androidx.compose.ui.graphics.RectangleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryCameraSheet(
    onDismiss: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RectangleShape,
        containerColor = Color.Black,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraViewfinder(
                onPhotoCaptured = onPhotoCaptured,
                onClose = onDismiss,
                applyStatusBarPadding = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
