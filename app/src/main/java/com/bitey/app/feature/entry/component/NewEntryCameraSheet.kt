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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryCameraSheet(
    onDismiss: () -> Unit,
    onPhotoCaptured: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(44.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f))
            )
        },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = Color(0xFF141416),
        modifier = modifier.fillMaxWidth().fillMaxHeight(0.92f)
    ) {
        Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
            CameraViewfinder(
                onPhotoCaptured = onPhotoCaptured,
                onClose = onDismiss,
                applyStatusBarPadding = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
